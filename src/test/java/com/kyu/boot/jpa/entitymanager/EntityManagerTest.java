package com.kyu.boot.jpa.entitymanager;

import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Service;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Propagation;

import javax.persistence.*;
import javax.transaction.Transactional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * @Project : test_project
 * @Date : 2017-06-09
 * @Author : nklee
 * @Description :
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class EntityManagerTest {

    @PersistenceContext(type = PersistenceContextType.TRANSACTION)
    private EntityManager em;

    @Autowired
    private AccountService accountService;

    /**
     * 테스트 데이터 생성
     */
    @Before
    public void testData() {
        // test 데이터
        Account account = new Account();
        account.setId(1);
        account.setName("nklee");
        em.persist(account);
        em.flush();
        em.clear();
    }

    @Test
    @Transactional
    public void 영속성_컨텍스트에_엔티티_저장되어져있는지_확인() {
        // 엔티티 조회
        Account account = em.find(Account.class, 1);
        assertNotNull(account);

        // 영속성 컨텍스트에 id=1 인 엔티티가 존재하는지 확인
        PersistenceUnitUtil persistenceUnitUtil = em.getEntityManagerFactory().getPersistenceUnitUtil();
        boolean hasEntity = persistenceUnitUtil.isLoaded(account);
        assertThat(true, is(hasEntity));
    }

    @Test
    @Transactional
    public void 영속성_컨텍스트에_저장되어_있는_엔티티_재사용_확인() {
        // 엔티티 조회
        Account account = em.find(Account.class, 1);
        assertNotNull(account);

        // 동일 엔티티 조회
        Account account2 = em.find(Account.class, 1);
        assertNotNull(account2);

        // 동일한 인스턴스인지 확인
        assertThat(account, is(sameInstance(account2)));
        System.out.println("account hashcode : " + account.hashCode());
        System.out.println("account2 hashcode : " + account2.hashCode());
    }

    @Test
    @Transactional
    public void 언제_영속성_컨텍스트_flush가_이뤄지나() {
        // 다른 트랜잭션에서 Account 테스트 데이터 저장.
        // 이와 같이 다른 트랜잭션에서 테스트 데이터를 저장하는 이유는 실제 DB에 commit 되어진 데이터를 생성하기 위함이다.
        accountService.testData();

        // 다른 트랜잭션에서 Account where = 2 의 name 변경 처리
        accountService.updateAccount(2);

        // 변경되어진 이름 확인
        Account account2 = em.find(Account.class, 2);
        assertThat("changedName", is(account2.getName()));
    }

    @Test
    @Transactional
    public void testProxy() {

        Account proxyAccount = em.getReference(Account.class, 1);
        System.out.println("Account proxy name : " + proxyAccount.getClass().getName());

        PersistenceUnitUtil persistenceUnitUtil = em.getEntityManagerFactory().getPersistenceUnitUtil();
        boolean loadedEntity = persistenceUnitUtil.isLoaded(proxyAccount);
        assertThat(false, is(loadedEntity));

        proxyAccount.getName(); // load entity
        loadedEntity = persistenceUnitUtil.isLoaded(proxyAccount);
        assertThat(true, is(loadedEntity));

        // em.getReference 를 통해 Account 프록시 객체를 return 하고자 하지만
        // 영속성 컨텍스트에 Account 엔티티가 존재하기 때문에 프록시 객체가 아닌 엔티티가 리턴된다.
        proxyAccount = em.getReference(Account.class, 1);
        loadedEntity = persistenceUnitUtil.isLoaded(proxyAccount);
        assertThat(true, is(loadedEntity));
    }
}


@Service(value = "entityManagerAccountService")
class AccountService {

    @Getter
    @PersistenceContext
    private EntityManager em;

    @org.springframework.transaction.annotation.Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateAccount(int id) {
        Account account = em.find(Account.class, id);
        account.setName("changedName");
    }

    @org.springframework.transaction.annotation.Transactional(propagation = Propagation.REQUIRES_NEW)
    public void testData() {
        Account account = new Account();
        account.setId(2);
        account.setName("nklee");
        em.persist(account);
    }
}


@Data
@Entity
class Account {

    @Id
    private int id;

    private String name;
}


