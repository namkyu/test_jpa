package com.kyu.boot.jpa.lock;

import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;

import static org.junit.Assert.fail;


/**
 * @Project : test_project
 * @Date : 2017-06-21
 * @Author : nklee
 * @Description :
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class PessimisticLockTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private PMemberService memberService;

    @Test
    @Transactional
    public void 비관적락쿼리확인() {
        em.setProperty("javax.persistence.query.timeout", 10000);
        em.find(PessimisticMember.class, 1, LockModeType.PESSIMISTIC_WRITE); // where pessimisti0_.id=? for update (H2)
        em.flush();
        em.clear();

        em.find(PessimisticMember.class, 1, LockModeType.PESSIMISTIC_READ); // where pessimisti0_.id=? for update (H2)
        em.flush();
        em.clear();
    }

    @Test(expected = PessimisticLockingFailureException.class)
    @Transactional
    public void 비관적락_예외_CASE_1() {
        em.find(PessimisticMember.class, 1, LockModeType.PESSIMISTIC_WRITE); // where pessimisti0_.id=? for update (H2)
        em.flush();

        // 해당 row에 lock이 설정되어 있어 오류 발생
        memberService.updateMember(1);

        fail("Timeout trying to lock table ; SQL statement:");
    }

    @Test(expected = PessimisticLockingFailureException.class)
    @Transactional
    public void 비관적락_예외_CASE_2() {
        PessimisticMember member = em.find(PessimisticMember.class, 1);
        member.setName("!2312313");
        em.flush();

        // 위에서 name을 변경하고 commit 하지 않아 오류 발생
        memberService.updateMember(1);
    }
}

@Service
class PMemberService {

    @Getter
    @PersistenceContext
    private EntityManager em;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateMember(int id) {
        em.setProperty("javax.persistence.query.timeout", 10000);
        PessimisticMember member = em.find(PessimisticMember.class, id);
        member.setName("Lee namkyu");
    }
}

@Data
@Table(name = "PESSIMISTIC_MEMBER")
@Entity
class PessimisticMember {

    @Id
    private int id;

    @Column(name = "NAME")
    private String name;
}


