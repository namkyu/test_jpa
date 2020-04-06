package com.kyu.boot.jpa.pk;


import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.*;
import javax.transaction.Transactional;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @Project : test-jpa
 * @Date : 2020-04-02
 * @Author : nklee
 * @Description :
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class CompositePKTest2 {

    @PersistenceContext
    private EntityManager em;

    @Before
    public void before() {
        // given
        Scraping scraping = new Scraping();
        scraping.setTitle("스크랩1");

        Schedule schedule = new Schedule();
        schedule.setTitle("스케줄1");

        ScheduleJobId id = new ScheduleJobId();
        id.setScheduleId(schedule.getId());
        id.setScrapingId(scraping.getId());

        ScheduleJob scheduleJob = new ScheduleJob();
        scheduleJob.setScheduleJobId(id);
        scheduleJob.setOrdering(1);
        scheduleJob.setScraping(scraping);
        scheduleJob.setSchedule(schedule);
        schedule.addScheduleJob(scheduleJob);

        em.persist(scraping);
        em.persist(schedule);
        em.flush();
        em.clear();
    }

    @Test
    @Transactional
    public void 식별관계() {
        Schedule scheduleEntity = em.find(Schedule.class, "1");
        assertThat("스케줄1", is(scheduleEntity.getTitle()));

        Scraping scrapingEntity = em.find(Scraping.class, "1");
        assertThat("스크랩1", is(scrapingEntity.getTitle()));

        ScheduleJobId id = new ScheduleJobId();
        id.setScheduleId(scheduleEntity.getId());
        id.setScrapingId(scrapingEntity.getId());

        ScheduleJob scheduleJobEntity = em.find(ScheduleJob.class, id);
        assertThat("스케줄1", is(scheduleJobEntity.getSchedule().getTitle()));
        assertThat("스크랩1", is(scheduleJobEntity.getScraping().getTitle()));

        // 삭제해 보기
        em.remove(scheduleJobEntity); // 이거 먼저 삭제해야 schedule row 삭제 가능
        em.remove(scheduleEntity);
        em.flush();
        em.clear();
    }

    @Test
    @Transactional
    public void test_엔티티_수정() {
        Schedule scheduleEntity = em.find(Schedule.class, "1");
        scheduleEntity.setTitle("타이틀 변경");
        scheduleEntity.getScheduleJobs().forEach(scheduleJob -> {
            scheduleJob.setOrdering(2);
        });

        em.persist(scheduleEntity);
        em.flush();
        em.clear();
    }
}

@Data
@Table(name = "schedule_job")
@Entity
class ScheduleJob {

    @EmbeddedId
    private ScheduleJobId scheduleJobId;

    @ManyToOne
    @MapsId("scrapingId")
    @JoinColumn(name = "scraping_id")
    private Scraping scraping;

    @ManyToOne
    @MapsId("scheduleId")
    @JoinColumn(name = "schedule_id")
    private Schedule schedule;

    @Column(name = "ordering")
    private int ordering;

}

@Data
@Embeddable
class ScheduleJobId implements Serializable {

    private String scrapingId;

    private String scheduleId;
}

@Data
@Table(name = "scraping")
@Entity
class Scraping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @Column(name = "title")
    private String title;
}

@Data
@Table(name = "schedule")
@Entity
class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @Column(name = "title")
    private String title;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "schedule")
    private Collection<ScheduleJob> scheduleJobs = new ArrayList<>();

    public void addScheduleJob(ScheduleJob scheduleJob) {
        scheduleJobs.add(scheduleJob);
        scheduleJob.setSchedule(this);
    }
}
