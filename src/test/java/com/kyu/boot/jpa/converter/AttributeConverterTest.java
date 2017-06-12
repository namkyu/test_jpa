package com.kyu.boot.jpa.converter;

import com.kyu.boot.jpa.converter.common.LocalDateTimeAttributeConverter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.core.IsSame;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.*;
import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;

import static org.junit.Assert.assertThat;

/**
 * @Project : test_project
 * @Date : 2017-06-12
 * @Author : nklee
 * @Description :
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class AttributeConverterTest {

    @PersistenceContext
    private EntityManager em;

    @Test
    @Transactional
    public void 날짜_컨버터() {
        Member member = new Member();
        member.setId(1);
        member.setChanged(new Date());
        member.setRegistered(LocalDateTime.now());

        em.persist(member);
        em.flush();
        em.clear();

        member = em.find(Member.class, 1);

        assertThat(member.getRegistered().getClass(), IsSame.sameInstance(LocalDateTime.class));
        assertThat(member.getChanged().getClass(), IsSame.sameInstance(Timestamp.class));
    }
}


@Data
@Table(name = "MEMBER_CONVERTER")
@Entity
class Member {

    @Id
    private int id;

    @Convert(converter = LocalDateTimeAttributeConverter.class)
    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime registered;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Date changed;
}
