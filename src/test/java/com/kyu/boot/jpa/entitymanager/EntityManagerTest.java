package com.kyu.boot.jpa.entitymanager;

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
 * @Date : 2017-06-09
 * @Author : nklee
 * @Description :
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class EntityManagerTest {

    @PersistenceContext
    private EntityManager em;

    @Test
    @Transactional
    public void testProxy() {
        // test 데이터
        Account account = new Account();
        account.setId(1);
        account.setName("nklee");
        em.persist(account);
        em.flush();
        em.clear();

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


@Data
@Entity
class Account {

    @Id
    private int id;

    private String name;
}


