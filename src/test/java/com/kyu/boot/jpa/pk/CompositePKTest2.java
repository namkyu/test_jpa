package com.kyu.boot.jpa.pk;


import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.*;
import javax.transaction.Transactional;
import java.io.Serializable;
import java.util.ArrayList;
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


    @Test
    @Transactional
    public void 식별관계() {

        // given
        Schedule schedule = new Schedule();
        schedule.setTitle("스케줄1");
        em.persist(schedule);

        Scraping scraping = new Scraping();
        scraping.setTitle("스크랩1");
        em.persist(scraping);

        ScheduleJobId id = new ScheduleJobId();
        id.setSchedule(schedule);
        id.setScraping(scraping);

        ScheduleJob scheduleJob = new ScheduleJob();
        scheduleJob.setScheduleJobId(id);
        scheduleJob.setOrdering(1);

        // when
        em.persist(scheduleJob);
        em.flush();
        em.clear();

        // then
        Schedule scheduleEntity = em.find(Schedule.class, schedule.getId());
        assertThat("스케줄1", is(scheduleEntity.getTitle()));

        Scraping scrapingEntity = em.find(Scraping.class, scraping.getId());
        assertThat("스크랩1", is(scrapingEntity.getTitle()));

        ScheduleJob scheduleJobEntity = em.find(ScheduleJob.class, id);
        ScheduleJobId scheduleJobId = scheduleJobEntity.getScheduleJobId();
        assertThat("스케줄1", is(scheduleJobId.getSchedule().getTitle()));
        assertThat("스크랩1", is(scheduleJobId.getScraping().getTitle()));

        // 삭제해 보기
        em.remove(scheduleJobEntity); // 이거 먼저 삭제해야 schedule row 삭제 가능
        em.remove(scheduleEntity);
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

    @Column(name = "ordering")
    private int ordering;
}

@Data
@Embeddable
class ScheduleJobId implements Serializable {

    @ManyToOne
    @JoinColumn(name = "scraping_id")
    private Scraping scraping;

    @ManyToOne
    @JoinColumn(name = "schedule_id")
    private Schedule schedule;
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
}
