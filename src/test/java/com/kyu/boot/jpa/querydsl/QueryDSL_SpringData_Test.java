package com.kyu.boot.jpa.querydsl;

import com.kyu.boot.com.kyu.boot.entity.querydsl.*;
import com.querydsl.jpa.JPQLQuery;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.support.QueryDslRepositorySupport;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @Project : test_project
 * @Date : 2017-06-26
 * @Author : nklee
 * @Description :
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class QueryDSL_SpringData_Test extends QueryDslRepositorySupport {

    @PersistenceContext
    private EntityManager em;

    public QueryDSL_SpringData_Test() {
        super(DslMember.class);
    }

    @Before
    public void before() {
        DslPhone phone = new DslPhone();
        phone.setId(1);
        phone.setNumber("010-1111-1111");
        DslPhone phone1 = new DslPhone();
        phone1.setId(2);
        phone1.setNumber("010-2222-2222");

        DslHomeAddress homeAddress = new DslHomeAddress();
        homeAddress.setId(1);
        homeAddress.setAddress("경기도 구리시");

        DslMember member = new DslMember();
        member.setId(1);
        member.setName("nklee");
        member.setHomeAddress(homeAddress);
        member.addPhone(phone);
        member.addPhone(phone1);

        em.persist(member);
        em.flush();
        em.clear();
    }

    @Test
    @Transactional
    public void Where조건() {
        QDslMember member = QDslMember.dslMember;

        JPQLQuery jpqlQuery = from(member);
        jpqlQuery.where(member.name.eq("nklee"));
        List<DslMember> result = jpqlQuery.fetch();
        assertThat(1, is(result.size()));
    }

    @Test
    @Transactional
    public void Where_다른테이블조건() {
        QDslMember member = QDslMember.dslMember;

        JPQLQuery jpqlQuery = from(member);
        jpqlQuery.where(member.homeAddress.id.eq(1));
        List<DslMember> result = jpqlQuery.fetch();
        assertThat(1, is(result.size()));
    }

    /**
     * select
     * dslmember0_.member_id as member_i1_4_,
     * dslmember0_.name as name2_4_
     * from
     * dsl_member dslmember0_
     * inner join
     * dsl_phone phonelist1_
     * on dslmember0_.member_id=phonelist1_.member_id
     */
    @Test
    @Transactional
    public void join() {
        QDslMember member = QDslMember.dslMember;
        QDslPhone phone = QDslPhone.dslPhone;

        JPQLQuery jpqlQuery = from(member);
        jpqlQuery.join(member.phoneList, phone);

        List<DslMember> result = jpqlQuery.fetch();
        assertThat(2, is(result.size()));
    }

    /**
     * select
     * dslmember0_.member_id as member_i1_4_,
     * dslmember0_.name as name2_4_
     * from
     * dsl_member dslmember0_
     * inner join
     * dsl_home_address dslhomeadd1_
     * on dslmember0_.member_id=dslhomeadd1_.member_id
     */
    @Test
    @Transactional
    public void innerJoin() {
        QDslMember member = QDslMember.dslMember;
        QDslHomeAddress homeAddress = QDslHomeAddress.dslHomeAddress;

        JPQLQuery jpqlQuery = from(member);
        jpqlQuery.innerJoin(member.homeAddress, homeAddress);

        List<DslHomeAddress> result = jpqlQuery.fetch();
        assertThat(1, is(result.size()));
    }

    /**
     * select
     * dslmember0_.member_id as member_i1_4_,
     * dslmember0_.name as name2_4_
     * from
     * dsl_member dslmember0_
     * inner join
     * dsl_home_address dslhomeadd1_
     * on dslmember0_.member_id=dslhomeadd1_.member_id
     * left outer join
     * dsl_phone phonelist2_
     * on dslmember0_.member_id=phonelist2_.member_id
     * order by
     * dslmember0_.name desc
     */
    @Test
    @Transactional
    public void leftJoin() {
        QDslMember member = QDslMember.dslMember;
        QDslHomeAddress homeAddress = QDslHomeAddress.dslHomeAddress;
        QDslPhone phone = QDslPhone.dslPhone;

        JPQLQuery jpqlQuery = from(member);
        jpqlQuery.innerJoin(member.homeAddress, homeAddress);
        jpqlQuery.leftJoin(member.phoneList, phone);

        jpqlQuery.orderBy(member.name.desc());

        List<DslMember> result = jpqlQuery.fetch();
        assertThat(2, is(result.size()));
    }
}
