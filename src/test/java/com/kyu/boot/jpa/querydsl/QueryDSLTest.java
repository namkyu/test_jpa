package com.kyu.boot.jpa.querydsl;

import com.kyu.boot.com.kyu.boot.entity.querydsl.*;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
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
import static org.junit.Assert.assertNull;
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
public class QueryDSLTest extends QueryDslRepositorySupport {

    @PersistenceContext
    private EntityManager em;

    public QueryDSLTest() {
        super(DslMember.class);
    }

    @Before
    public void before() {
        DslPhone phone = new DslPhone(1, "010-1111-1111", "LG");
        DslPhone phone1 = new DslPhone(2, "010-2222-2222", "SAMSUNG");
        DslPhone phone2 = new DslPhone(3, "010-2222-2222", "SAMSUNG");
        DslPhone phone3 = new DslPhone(4, "010-2222-2222", "SAMSUNG");

        DslHomeAddress homeAddress = new DslHomeAddress();
        homeAddress.setId(1);
        homeAddress.setAddress("경기도 구리시");

        DslMember member = new DslMember();
        member.setId(1);
        member.setName("nklee");
        member.setHomeAddress(homeAddress);

        member.addPhone(phone);
        member.addPhone(phone1);
        member.addPhone(phone2);
        member.addPhone(phone3);

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

    @Test
    @Transactional
    public void Where_리스트형_다른테이블조건() {
        QDslMember member = QDslMember.dslMember;

        JPQLQuery jpqlQuery = from(member);
        jpqlQuery.where(member.phoneList.any().id.eq(1));
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

    @Test
    @Transactional
    public void 업데이트() {
        DslPhone phoneEntity = em.find(DslPhone.class, 2);
        assertThat("010-2222-2222", is(phoneEntity.getNumber()));

        // 엔티티 매니저 flush 하지 않아도 아래 구문 실행 시 DB 업데이트 한다. (영속성 컨텍스트에 있는 엔티티는 수정되지 않는다.)
        System.out.println("---------------------------------------------------------");
        QDslPhone phone = QDslPhone.dslPhone;
        update(phone).where(phone.id.eq(2))
                .set(phone.number, "010-3333-3333")
                .execute();
        System.out.println("---------------------------------------------------------");

        // 영속성 컨텍스트에 이미 phone 엔티티가 초기화되어 있어 변경되지 않은 핸드폰 번호가 출력된다.
        phoneEntity = em.find(DslPhone.class, 2);
        assertThat("010-2222-2222", is(phoneEntity.getNumber()));

        em.clear(); // 영속성 컨텍스트 초기화
        phoneEntity = em.find(DslPhone.class, 2); // phone 엔티티 초기화 진행 (변경된 폰번호가 출력된다.)
        assertThat("010-3333-3333", is(phoneEntity.getNumber()));
    }

    @Test
    @Transactional
    public void 삭제() {
        QDslPhone phone = QDslPhone.dslPhone;
        delete(phone).where(phone.id.eq(2)).execute();

        DslPhone phoneEntity = em.find(DslPhone.class, 2);
        assertNull(phoneEntity);
    }

    @Test
    @Transactional
    public void GroupBy() {
        QDslPhone phone = QDslPhone.dslPhone;

        JPQLQuery query = from(phone);
        query.groupBy(phone.number, phone.manufacture);
        query.select(phone.number, phone.manufacture);

        List<Tuple> list = query.fetch();
        assertThat(2, is(list.size()));
        for (Tuple tuple : list) {
            System.out.println("number : " + tuple.get(phone.number) + ", manufacture : " + tuple.get(phone.manufacture));
        }
    }

    @Test
    @Transactional
    public void GroupByToDto() {
        QDslPhone phone = QDslPhone.dslPhone;

        JPQLQuery query = from(phone);
        query.groupBy(phone.number, phone.manufacture);
        query.select(Projections.bean(PhoneDto.class, phone.number, phone.manufacture));

        List<PhoneDto> list = query.fetch();
        assertThat(2, is(list.size()));

        for (PhoneDto phoneDto : list) {
            System.out.println(phoneDto);
        }
    }

}

