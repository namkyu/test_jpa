package com.kyu.boot.jpa.others;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.*;
import javax.transaction.Transactional;

/**
 * @Project : test_project
 * @Date : 2017-06-20
 * @Author : nklee
 * @Description :
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class TransientAnnoTest {

    @PersistenceContext
    private EntityManager em;

    @Test
    @Transactional
    public void test() {
        TransientMember member = new TransientMember();
        member.setId(1);
        member.setName("nklee");
        member.setPhoneNumber("010-1111-1111");

        em.persist(member);
        em.flush();
        em.clear();
    }
}

/**
 * create table transient_member (
 * id integer not null,
 * name varchar(255),
 * primary key (id)
 * )
 */
@Data
@Entity
@Table(name = "TRANSIENT_MEMBER")
class TransientMember {

    @Id
    private int id;

    private String name;

    // 필드를 매핑하지 않을 때 사용 (DB에 저장하지도 않고 조회 하지도 않음)
    @Transient
    private String phoneNumber;
}
