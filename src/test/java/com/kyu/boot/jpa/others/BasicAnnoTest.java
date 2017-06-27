package com.kyu.boot.jpa.others;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.*;
import javax.transaction.Transactional;

/**
 * @Project : test_project
 * @Date : 2017-06-26
 * @Author : nklee
 * @Description :
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class BasicAnnoTest {

    @PersistenceContext
    private EntityManager em;

    @Test
    @Transactional
    public void test() {
        BasicAnnoMember member = new BasicAnnoMember();
        member.setId(1);
        member.setName("nklee");
        member.setPhoneNumber("010-1111-1111");

        em.persist(member);
        em.flush();
        em.clear();

        System.out.println("--------------------------------");
        em.find(BasicAnnoMember.class, 1);
        System.out.println("--------------------------------");
    }

}

@Data
@Entity
@Table(name = "BASIC_ANNO_MEMBER")
class BasicAnnoMember {

    @Id
    @Column(name = "MEMBER_ID")
    private int id;

    @Basic(fetch = FetchType.EAGER)
    private String name;

    // The LAZY strategy is a hint to the persistence provider runtime
    @Basic(fetch = FetchType.LAZY)
    private String phoneNumber;

}