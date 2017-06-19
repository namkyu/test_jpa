package com.kyu.boot.jpa.option;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.*;
import javax.transaction.Transactional;

import static junit.framework.TestCase.fail;

/**
 * @Project : test_project
 * @Date : 2017-06-19
 * @Author : nklee
 * @Description :
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class UniqueOptionTest {

    @PersistenceContext
    private EntityManager em;

    @Test(expected = PersistenceException.class)
    @Transactional
    public void testUnique() {
        UniqueMember member = new UniqueMember();
        member.setId(1);
        member.setName("nklee");
        member.setEmail("nklee@a.com");

        em.persist(member);
        em.flush();
        em.clear();

        UniqueMember member1 = new UniqueMember();
        member1.setId(2);
        member1.setName("nklee2");
        member1.setEmail("nklee@a.com");

        em.persist(member1);
        em.flush();

        // email 컬럼에 unique 제약 조건이 설정되어 있음
        fail("Unique index or primary key violation 오류 발생!!");
    }
}

/**
 * ------------------------------------------
 * 테이블 구조
 * ------------------------------------------
 * create table unique_member (
 * id integer not null,
 * email varchar(255),
 * name varchar(255),
 * primary key (id)
 * )
 * <p>
 * alter table unique_member
 * add constraint UK_mo1u0kayw5grtwswkd95229e7 unique (email)
 */
@Data
@Table(name = "UNIQUE_MEMBER")
@Entity
class UniqueMember {

    @Id
    private int id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "EMAIL", unique = true)
    private String email;

}
