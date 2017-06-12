package com.kyu.boot.jpa.converter;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.*;
import javax.transaction.Transactional;

import static org.hamcrest.CoreMatchers.is;
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
public class EnumTest {

    @PersistenceContext
    private EntityManager em;

    @Test
    @Transactional
    public void enum_테스트() {
        EMember member = new EMember();
        member.setId(1);
        member.setGender(Gender.MALE);
        member.setGender1(Gender.FEMALE);

        em.persist(member);
        em.flush();
        em.clear();

        member = em.find(EMember.class, 1);
        assertThat(Gender.MALE, is(member.getGender()));
        assertThat(Gender.FEMALE.name(), is(member.getGender1().name()));

        System.out.println(member);
    }
}


/**
 * create table emember (
 * id integer not null,
 * gender integer,
 * gender1 varchar(255),
 * primary key (id)
 * )
 */
@Data
@Entity
class EMember {

    @Id
    private int id;

    @Enumerated(EnumType.ORDINAL)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    private Gender gender1;

}


enum Gender {
    MALE, FEMALE;
}




