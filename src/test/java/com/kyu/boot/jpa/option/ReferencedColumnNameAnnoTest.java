package com.kyu.boot.jpa.option;

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
 * @Date : 2017-06-20
 * @Author : nklee
 * @Description :
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ReferencedColumnNameAnnoTest {

    @PersistenceContext
    private EntityManager em;

    @Test
    @Transactional
    public void test() {
        FCAddress address = new FCAddress();
        address.setId(1);

        FCMember member = new FCMember();
        member.setId(1);
        member.setName("nklee");

        member.setFcAddress(address);
        address.setMember(member);

        em.persist(address);
        em.flush();
        em.clear();

        address = em.find(FCAddress.class, 1);
        assertThat("nklee", is(address.getMember().getName()));

        System.out.println(address);
    }

}

/**
 * create table fcmember (
 * fcmember_id integer not null,
 * name varchar(255),
 * primary key (fcmember_id)
 * )
 * <p>
 * create table fcaddress (
 * id integer not null,
 * member_id integer,
 * primary key (id)
 * )
 * <p>
 * alter table fcaddress
 * add constraint FKltro86ts4t7caqbsumjn99l4h
 * foreign key (member_id)
 * references fcmember
 */
@Data
@lombok.ToString(exclude = "fcAddress")
@Entity
class FCMember {

    @Id
    @Column(name = "FCMEMBER_ID")
    private int id;

    private String name;

    @OneToOne(mappedBy = "member")
    private FCAddress fcAddress;
}

@Data
@Entity
class FCAddress {

    @Id
    private int id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "MEMBER_ID", referencedColumnName = "FCMEMBER_ID")
    private FCMember member;

}


