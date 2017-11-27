package com.kyu.boot.jpa.transaction;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Service;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.io.FileNotFoundException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


/**
 * @Project : test_project
 * @Date : 2017-07-17
 * @Author : nklee
 * @Description :
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class TransactionTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private TMemberService memberService;

    @Before
    public void before() {
        memberService.insertData();
    }

    @Test
    @Transactional
    public void 정상케이스() {
        memberService.occursNothing("Leenamkyu", 1);
        Member member = em.find(Member.class, 1);
        assertThat("Leenamkyu", is(member.getName()));
    }

    @Test
    @Transactional
    public void 롤백테스트_unchecked_exception() {
        try {
            memberService.occursUnCheckedException("Leenamkyu", 1);
        } catch (RuntimeException ex) {
        }

        // unchecked Exception 예외 발생 시 롤백 처리되어짐
        Member member = em.find(Member.class, 1);
        assertThat("nklee1", is(member.getName()));
    }


    @Test
    @Transactional
    public void 롤백테스트_checked_exception() {
        try {
            memberService.occursCheckedException("Leenamkyu", 1);
        } catch (Exception ex) {
        }

        // checked Exception 예외 발생 시 롤백 되지 않는다. (유의사항)
        Member member = em.find(Member.class, 1);
        assertThat("Leenamkyu", is(member.getName()));
    }

    @Test
    @Transactional
    public void 롤백테스트_checked_exception_rollback처리() {
        try {
            memberService.occursCheckedExceptionRollback("Leenamkyu", 1);
        } catch (Exception ex) {
        }

        // checked Exception 예외 발생 시 롤백 되지 않는다. (유의사항)
        Member member = em.find(Member.class, 1);
        assertThat("nklee1", is(member.getName()));
    }
}


@Service
class TMemberService {

    @PersistenceContext
    private EntityManager em;

    // 정상 케이스
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void occursNothing(String name, int id) {
        Member member = em.find(Member.class, id);
        member.setName(name);
    }

    // checked exception
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void occursCheckedException(String name, int id) throws FileNotFoundException {
        Member member = em.find(Member.class, id);
        member.setName(name);
        throw new FileNotFoundException();
    }

    // checked exception
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void occursCheckedExceptionRollback(String name, int id) throws FileNotFoundException {
        Member member = em.find(Member.class, id);
        member.setName(name);
        throw new FileNotFoundException();
    }


    // unchecked exception (spring boot default rollback)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void occursUnCheckedException(String name, int id) {
        Member member = em.find(Member.class, id);
        member.setName(name);
        throw new NullPointerException();
    }


    // for test data
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void insertData() {
        for (int i = 1; i < 10; i++) {
            Member member = new Member();
            member.setId(i);
            member.setName("nklee" + i);
            em.persist(member);
        }

        em.flush();
        em.clear();
    }
}

@Data
@Table(name = "TRANSACTION_MEMBER")
@Entity
class Member {

    @Id
    private int id;

    @Column(name = "NAME")
    private String name;
}