package com.kyu.boot.jpa.entitymanager;

import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Service;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import javax.persistence.*;

import static org.hamcrest.CoreMatchers.*;
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
public class EntityManagerFactoryTest {

    @PersistenceUnit
    private EntityManagerFactory emf;

    @PersistenceContext
    private EntityManager em;

    @Resource(name = "EntityManagerFactoryMemberService")
    private MemberService memberService;

    @Test
    public void testEntityManagerFactory() {
        emf.getProperties().forEach((k, v) -> System.out.println("key : " + k + ", value : " + v));
    }

    /**
     * 데이타베이스를 하나만 사용하는 어플리케이션은 일반적으로 EntityManagerFactory를 하나만 생성
     */
    @Test
    public void 엔티티매니저팩토리_인스턴스_체크() {
        assertThat(emf, is(sameInstance(memberService.getEmf())));
        assertThat(emf, is(sameInstance(memberService.getEmf2())));
        assertThat(memberService.getEmf(), is(sameInstance(memberService.getEmf2())));
    }

    /**
     * Spring에서는 EntityManager를 Proxy로 감싼다.
     * EntityManager 호출 시 마다 Proxy를 통해 EntityManager를 생성 하여 Thread-Safety를 보장 한다.
     * https://doanduyhai.wordpress.com/2011/11/21/spring-persistencecontext-explained/
     */
    @Test
    public void 스프링_엔티티매니저관리() {
        assertThat(em, is(not(sameInstance(memberService.getEm())))); // proxy 객체가 다름
    }

    @Test
    public void makeEntityManager() {
        EntityManager em1 = emf.createEntityManager();
        EntityManager em2 = emf.createEntityManager();
        EntityManager em3 = emf.createEntityManager();

        assertThat(em1, is(not(sameInstance(em2))));
        assertThat(em1, is(not(sameInstance(em3))));
    }
}

@Service("EntityManagerFactoryMemberService")
class MemberService {

    @Getter
    @PersistenceUnit
    private EntityManagerFactory emf;

    @Getter
    @PersistenceUnit
    private EntityManagerFactory emf2;

    @Getter
    @PersistenceContext
    private EntityManager em;
}


@Data
@Entity
class Member {

    @Id
    private int id;
}


