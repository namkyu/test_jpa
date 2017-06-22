package com.kyu.boot.jpa.lock;

import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Service;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.*;
import javax.transaction.Transactional;

import static junit.framework.TestCase.fail;

/**
 * @Project : test_project
 * @Date : 2017-06-21
 * @Author : nklee
 * @Description :
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class OptimisticLockTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private MemberService memberService;

    /**
     * merge() 이용시 DB에 select를 한번 날린 후 데이터가 존재하면 update 존재하지 않으면 insert 한다.
     * merge()는 추가 시킨 후 entity instance의 복사본을 만들어 원본이 아닌 복사본이 관리를 받도록 한다.
     * 즉, 원본이 되는 entity instance에 아무리 업데이트를 해도 다시 merge()를 하지 않는 이상 데이터베이스에 반영이 되지 않는다.
     */
    @Test
    @Transactional
    public void test엔티티매니저Merge() {
        OptimisticMember member = new OptimisticMember();
        member.setId(1); // DB에 ID 1인 데이터 존재
        member.setName("nklee1111");

        em.merge(member);
        em.flush();
        em.clear();
    }


    @Test(expected = OptimisticLockException.class)
    @Transactional
    public void 낙관적락예외발생() {
        // 영속 상태의 OptimisticMember
        OptimisticMember member = em.find(OptimisticMember.class, 1); // version 0

        // version 1로 변경되어 DB 반영됨
        member.setName("nklee2");
        memberService.updateMember(member);

        try {
            // update 시 version이 0이므로 낙관적 락 오류 발생 (DB는 1로 되어 있음)
            member.setName("nklee3");
            memberService.updateMember(member);
        } catch (Exception e) {
            e.printStackTrace(); // javax.persistence.OptimisticLockException: Row was updated or deleted by another transaction (or unsaved-value mapping was incorrect)
            throw e;
        }

        fail("Optimistic lock exception 발생!!");
    }
}

@Service
class MemberService {

    @Getter
    @PersistenceContext
    private EntityManager em;

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void updateMember(OptimisticMember member) {
        em.merge(member);
        em.flush();
    }
}

@Data
@Table(name = "OPTIMISTIC_MEMBER")
@Entity
class OptimisticMember {

    @Id
    private int id;

    @Column(name = "NAME")
    private String name;

    @Version
    @Column(name = "VERSION")
    private int version;
}

