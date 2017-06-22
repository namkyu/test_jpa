package com.kyu.boot.jpa.lock;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.*;
import javax.transaction.Transactional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @Project : test_project
 * @Date : 2017-06-20
 * @Author : nklee
 * @Description :
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class VersionTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private MemberService memberService;

    /**
     * 격리성 : 동시에 실행되는 트랜잭션이 서로에게 영향을 미치지 않도록 격리한다. 예를 들어 동시에 같은 데이터를 수정하지 못하도록 해야 한다.
     * 격리성과 관련된 성능 이슈로 인해 격리 수준을 선택할 수 있다.
     * <p>
     * READ UNCOMMITTED (커밋되지 않는 읽기)
     * READ COMMITTED (커밋된 읽기)
     * REPEATABLE READ (반복 가능한 읽기)
     * SERIALIZABLE (직렬화 기능)
     * <p>
     * 순서대로 READ UNCOMMITTED가 격리 수준이 가장 낮다.
     * <p>
     * 애플리케이션 대부분은 동시성 처리가 중요하므로 데이터베이스들은 보통 READ COMMITTED 격리 수준을 기본으로 사용한다.
     * <p>
     * [낙관적락]
     * 낙관적 락은 트랜잭션 대부분은 충돌이 발생하지 않는다고 낙관적으로 가정하는방법이다.
     * 이것은 데이터베이스가 제공하는 락 기능을 사용하는것이 아니라 JPA가 제공하는 버전 관리 기능을 사용한다.
     * 애플리케이션이 제공하는락이다. 트랜잭션을 커밋하기 전까지는 트랜잭션의 충돌을 알수없다는 특징이있다.
     * <p>
     * [비관적락]
     * 비관적락은 트랜잭션의 충돌이 발생한다고 가정하고 우선락을 걸고 보는 방법이다.
     * 이것은 데이터베이스가 제공하는 락기능을 사용한다.
     * 대표적으로 select for update 구문이 있다.
     */
    @Test
    @Transactional
    public void testVersionAnnotation() {
        Member member = new Member();
        member.setId(1);
        member.setName("nklee");

        em.persist(member);
        em.flush();
        em.clear();

        // version 0
        member = em.find(Member.class, 1);
        assertThat(0, is(member.getVersion()));

        // version 1
        member.setName("nklee2");
        em.flush();
        em.clear();

        member = em.find(Member.class, 1);
        assertThat(1, is(member.getVersion()));

        // version 2
        member.setName("nklee3");
        em.flush();
        em.clear();

        member = em.find(Member.class, 1);
        assertThat(2, is(member.getVersion()));
    }
}

/**
 * create table version_member (
 * id integer not null,
 * name varchar(255),
 * version integer not null,
 * primary key (id)
 * )
 */
@Data
@Table(name = "VERSION_MEMBER")
@Entity
class Member {

    @Id
    private int id;

    @Column(name = "NAME")
    private String name;

    @Version
    @Column(name = "VERSION")
    private int version;
}
