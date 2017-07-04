package com.kyu.boot.jpa.please_note;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.*;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * @Project : test_project
 * @Date : 2017-06-28
 * @Author : nklee
 * @Description :
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class DefaultConstructure {

    @PersistenceContext
    private EntityManager em;

    private PersistenceUnitUtil unitUtil;

    @Before
    public void before() {
        unitUtil = em.getEntityManagerFactory().getPersistenceUnitUtil();
    }

    @Test(expected = PersistenceException.class)
    @Transactional
    public void 기본생성자없음() {
        Account account = new Account(1);
        em.persist(account);
        em.flush();
        em.clear();

        // 엔티티 초기화 되는 시점에 default constructor 없다고 오류 발생
        em.find(Account.class, 1);

        fail("No default constructor for entity:  : com.kyu.boot.jpa.please_note.Account");
    }

    @Test(expected = org.hibernate.InstantiationException.class)
    @Transactional
    public void 연관관계설정_LAZY_기본생성자없음() {
        Member member = new Member(1, "nklee");

        Team team = new Team();
        team.setId(1);
        team.addMember(member);

        em.persist(team);
        em.flush();
        em.clear();

        team = em.find(Team.class, 1);
        assertThat(1, is(team.getId()));

        // LAZY
        List<Member> memberList = team.getMemberList();

        // 엔티티 초기화 되어 있지 않음
        boolean loaded = unitUtil.isLoaded(memberList);
        assertThat(false, is(loaded));

        // init entity
        System.out.println("init entity");
        memberList.get(0).getId(); // 오류 발생

        fail("No default constructor for entity:  : com.kyu.boot.jpa.please_note.Member");
    }
}

@Getter
@Setter
@Entity
@Table(name = "PN_TEAM")
class Team {

    @Id
    private int id;

    @OneToMany(mappedBy = "team", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private List<Member> memberList = new ArrayList<>();

    public void addMember(Member member) {
        memberList.add(member);
        member.setTeam(this);
    }
}

@Getter
@Setter
@Entity
@AllArgsConstructor
@Table(name = "PN_MEMBER")
class Member {

    @Id
    private int id;

    private String name;

    public Member(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    private Team team;
}

@Getter
@Setter
@Entity
class Account {

    @Id
    private int id;

    public Account(int id) {
        this.id = id;
    }
}
