package com.kyu.boot.jpa.lock;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.*;
import javax.transaction.Transactional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @Project : test_project
 * @Date : 2017-06-20
 * @Author : nklee
 * @Description :
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class VersionTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private MemberService memberService;

    @Test
    @Transactional
    public void testVersionAnnotation() {
        Member member = new Member();
        member.setId(1);
        member.setName("nklee");

        em.persist(member);
        em.flush();
        em.clear();

        // version 0
        member = em.find(Member.class, 1);
        assertThat(0, is(member.getVersion()));

        // version 1
        member.setName("nklee2");
        em.flush();
        em.clear();

        member = em.find(Member.class, 1);
        assertThat(1, is(member.getVersion()));

        // version 2
        member.setName("nklee3");
        em.flush();
        em.clear();

        member = em.find(Member.class, 1);
        assertThat(2, is(member.getVersion()));
    }
}

/**
 * create table version_member (
 * id integer not null,
 * name varchar(255),
 * version integer not null,
 * primary key (id)
 * )
 */
@Data
@Table(name = "VERSION_MEMBER")
@Entity
class Member {

    @Id
    private int id;

    @Column(name = "NAME")
    private String name;

    @Version
    @Column(name = "VERSION")
    private int version;
}
