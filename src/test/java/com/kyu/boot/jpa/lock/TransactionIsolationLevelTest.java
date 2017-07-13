package com.kyu.boot.jpa.lock;

import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
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
@EnableAsync
public class TransactionIsolationLevelTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private AccountService accountService;


    @Test(expected = PersistenceException.class)
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void 동시insert_유니크오류버전() {
        TIL_Account account = new TIL_Account();
        account.setId(2);
        account.setName("nklee");

        // insert
        accountService.requiresNewInsert(account);

        // insert
        em.persist(account); // error
        em.flush();
        em.clear();

        fail("Unique index or primary key violation: \"PRIMARY KEY ON PUBLIC.TIL_ACCOUNT(ID)\"");
    }


    @Test(expected = PessimisticLockingFailureException.class)
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void 동시insert_lock버전() {
        TIL_Account account = new TIL_Account();
        account.setId(2);
        account.setName("nklee");

        // insert
        em.persist(account);
        em.flush();
        em.clear();

        // 위의 insert문은 커밋되지 않은 상태이다. 이와 동시에 다른 트랜잭션에서 같은 PK로 insert를 하게 되면 lock이 걸린다.
        accountService.requiresNewInsert(account);

        fail("Timeout trying to lock table");
        fail("insert into til_account (name, id) values (?, ?) [50200-195]");
        fail("Concurrent update in table \"TIL_ACCOUNT\": another transaction has updated or deleted the same row [90131-195]");
    }


    // -------------------------------------------------------------------------
    // READ_UNCOMMITTED 테스트
    // -------------------------------------------------------------------------
    @Test
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public void READ_UNCOMMITTED_테스트() throws SQLException {
        // 테스트 데이터 검증
        TIL_Account account = em.find(TIL_Account.class, 1);
        assertThat("nklee", is(account.getName()));

        account.setName("namkyu");
        em.flush(); // DB 반영하였지만 트랜잭션이 종료되지 않았기 때문에 커밋되지 않은 상태
        em.clear();

        // 정상 업데이트 검증
        account = em.find(TIL_Account.class, 1);
        assertThat("namkyu", is(account.getName()));

        // READ UNCOMMITTED 로 새로운 트랜잭션을 실행하고 데이터를 조회하면 커밋되지 않은 데이터가 출력된다.
        account = accountService.requiresNewWithUnCommitted(1);
        assertThat("nklee", is(account.getName()));
    }


    // -------------------------------------------------------------------------
    // READ_COMMITTED 테스트
    // -------------------------------------------------------------------------
    @Test
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void READ_COMMITTED_테스트() throws SQLException {
        // 테스트 데이터 검증
        TIL_Account account = em.find(TIL_Account.class, 1);
        assertThat("nklee", is(account.getName()));

        account.setName("namkyu");
        em.flush(); // DB 반영하였지만 트랜잭션이 종료되지 않았기 때문에 커밋되지 않은 상태
        em.clear();

        // 정상 업데이트 검증
        account = em.find(TIL_Account.class, 1);
        assertThat("namkyu", is(account.getName()));

        // MSSQL 에서는 select 문장이 실행되는 동안 해당 데이터에 Shared lock 이 걸리지만 H2 에서는 lock이 걸리지 않는다.
        // 각각의 DB에 따라 isolation이 다르게 동작하므로 격리수준별 테스트가 꼭 필요해 보인다.
        account = accountService.requiresNew(1);
        assertThat("namkyu", not(account.getName()));
        assertThat("nklee", is(account.getName()));
    }


    // -------------------------------------------------------------------------
    // REPEATABLE_READ 테스트
    // -------------------------------------------------------------------------
    @Test
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void REPEATABLE_READ_테스트() throws SQLException, InterruptedException {

        // 대량의 테스트 데이터
        accountService.insertData();

        // Async 방식으로 select 쿼리
        CompletableFuture<List<TIL_Account>> completableFuture = accountService.requiresNewWithRepeatableRead();

        while (completableFuture.isDone() == false) {
            System.out.println("waiting for the CompletableFuture to finish...");
            TimeUnit.MILLISECONDS.sleep(500);

            // select 쿼리 하는 중에 특정 row 업데이트 (H2 데이터베이스는 10번에 해당하는 row에 Shared lock이 걸리지 않는다.)
            accountService.changeEntity(10, "Lee namkyu");
        }

        TIL_Account account = em.find(TIL_Account.class, 10);
        assertThat("Lee namkyu", is(account.getName()));
    }


}


@Service
class AccountService {

    @Getter
    @PersistenceContext
    private EntityManager em;

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.DEFAULT)
    public TIL_Account requiresNew(int id) {
        TIL_Account account = em.find(TIL_Account.class, id);
        return account;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.DEFAULT)
    public void requiresNewInsert(TIL_Account account) {
        em.persist(account);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_UNCOMMITTED)
    public TIL_Account requiresNewWithUnCommitted(int id) {
        TIL_Account account = em.find(TIL_Account.class, id);
        return account;
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
    public CompletableFuture<List<TIL_Account>> requiresNewWithRepeatableRead() throws InterruptedException {
        TypedQuery typedQuery = em.createQuery("select a from TIL_Account a where a.id between  1 and 100000", TIL_Account.class);
        List<TIL_Account> list = typedQuery.getResultList();
        System.out.println("list size : " + list.size());
        return CompletableFuture.completedFuture(list);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
    public void changeEntity(int id, String name) {
        TIL_Account account = em.find(TIL_Account.class, id);
        account.setName(name);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void insertData() {
        // 테스트 데이터 (select 타임을 길게 가지기 위해서 대랑 insert)
        for (int i = 10; i < 100000; i++) {
            TIL_Account account = new TIL_Account();
            account.setId(i);
            account.setName("nklee" + i);
            em.persist(account);
        }
    }
}

@Data
@Table(name = "TIL_ACCOUNT")
@Entity
class TIL_Account {

    @Id
    private int id;

    private String name;
}