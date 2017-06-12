package com.kyu.boot.jpa.others;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
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
public class PersistenceContextTest {

    @PersistenceContext
    private EntityManager em;

    @Test
    @Transactional
    public void 영속성컨텍스트_인스턴스비교() {
        PMember pMember = new PMember();
        pMember.setId(1);

        em.persist(pMember);
        em.flush();
        em.clear();

        // 트랜잭션이 같이면 같은 영속성 컨텍스트를 사용한다.
        PMember entityPMember1 = em.find(PMember.class, 1); // DB에서 PMember 조회 후 영속성 컨텍스트에 저장
        PMember entityPMember2 = em.find(PMember.class, 1); // 영속성 컨텍스트에 저장되어 있는 PMember 엔티티 리턴
        assertThat(entityPMember1, is(sameInstance(entityPMember2)));
    }
}

@Data
@Entity
class PMember {

    @Id
    private int id;
}


