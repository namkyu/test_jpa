package com.kyu.boot.jpa.pk;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.*;
import javax.transaction.Transactional;
import java.io.Serializable;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @Project : test_project
 * @Date : 2017-06-14
 * @Author : nklee
 * @Description :
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class CompositePKTest {

    @PersistenceContext
    private EntityManager em;

    /**
     * @Embeddable : 객체지향 방식에 가깝다.
     */
    @Test
    @Transactional
    public void testEmbeddable() {
        EmpId empId = new EmpId();
        empId.setEmpNo(1);
        empId.setEmpName("nklee");

        Emp emp = new Emp();
        emp.setEmpId(empId);
        emp.setPhone("010-1111-1111");

        em.persist(emp);
        em.flush();
        em.clear();

        emp = em.find(Emp.class, empId);
        assertThat(1, is(emp.getEmpId().getEmpNo()));
        assertThat("nklee", is(emp.getEmpId().getEmpName()));
    }

    /**
     * @IdClass : DB 방식에 가깝다.
     */
    @Test
    @Transactional
    public void testIdClass() {
        OrderProduct orderProduct = new OrderProduct();
        orderProduct.setOrderId(1);
        orderProduct.setProductId(2);
        orderProduct.setAmount(1000);

        em.persist(orderProduct);
        em.flush();
        em.clear();

        OrderProductPK pk = new OrderProductPK();
        pk.setOrderId(1);
        pk.setProductId(2);

        orderProduct = em.find(OrderProduct.class, pk);
        assertThat(1, is(orderProduct.getOrderId()));
        assertThat(2, is(orderProduct.getProductId()));
        assertThat(1000, is(orderProduct.getAmount()));
    }

    @Test
    @Transactional
    public void 식별관계() {

        HomeAddressId id = new HomeAddressId();
        id.setMemberId(1);
        id.setId(1);

        HomeAddress homeAddress = new HomeAddress();
        homeAddress.setHomeAddressId(id);

        Member member = new Member();
        member.setMemberId(1);
        member.setName("nklee");
        member.setHomeAddress(homeAddress);

        em.persist(member);
        em.flush();
        em.clear();

        // 초기 검증
        HomeAddress entityHomeAddress = em.find(HomeAddress.class, id);
        assertThat(1, is(entityHomeAddress.getHomeAddressId().getId()));
        assertThat(1, is(entityHomeAddress.getHomeAddressId().getMemberId()));

        // SpringMember 엔티티의 name 변경
        entityHomeAddress.getMember().setName("nklee2");
        em.flush();
        em.clear();

        // SpringMember 엔티티 name 변경 여부 검증
        Member entityMember = em.find(Member.class, 1);
        assertThat(1, is(entityMember.getMemberId()));
        assertThat("nklee2", is(entityMember.getName()));
    }
}


/**
 * -----------------------------------------------------------
 * 테이블 구조
 * -----------------------------------------------------------
 * create table emp (
 * emp_name varchar(255) not null,
 * emp_no integer not null,
 * name varchar(255),
 * primary key (emp_name, emp_no)
 * )
 */
@Data
@Entity
class Emp {

    @EmbeddedId
    private EmpId empId;

    private String phone;
}

@Data
@Embeddable
class EmpId implements Serializable {

    @Column(name = "EMP_NO")
    private int empNo;

    @Column(name = "EMP_NAME")
    private String empName;
}


/**
 * -----------------------------------------------------------
 * 테이블 구조
 * -----------------------------------------------------------
 * create table order_product (
 * order_id integer not null,
 * product_id integer not null,
 * amount integer not null,
 * primary key (order_id, product_id)
 * )
 */
@Data
@Entity
@IdClass(OrderProductPK.class)
class OrderProduct {
    @Id
    private int orderId;

    @Id
    private int productId;

    private int amount;
}

/**
 * 1. The primary key class must be public and must have a public no-arg constructor.
 * 2. The primary key class must be serializable.
 * 3. The primary key class must define equals and hashCode methods
 * 4. 엔티티의 필드명과 같아야 한다. OrderProduct 엔티티의 식별자 orderId 필드 이름이 OrderProductPK의 orderId 필드 이름과 같아야 함.
 */
@Data
class OrderProductPK implements Serializable {
    private int orderId;
    private int productId;
}


/**
 * -----------------------------------------------------------
 * 테이블 구조
 * -----------------------------------------------------------
 * create table composite_pk_member (
 * member_id integer not null,
 * primary key (member_id)
 * )
 * <p>
 * create table composite_pk_home_address (
 * home_address_id integer not null,
 * member_id integer not null,
 * primary key (home_address_id, member_id)
 * )
 * <p>
 * alter table composite_pk_home_address
 * add constraint FK6rodm0s8976kjct91uw5gye8q
 * foreign key (member_id)
 * references composite_pk_member
 */
@Data
@Table(name = "COMPOSITE_PK_MEMBER")
@Entity
class Member {

    @Id
    @Column(name = "MEMBER_ID")
    private int memberId;

    @Column(name = "MEMBER_NAME")
    private String name;

    @OneToOne(cascade = CascadeType.PERSIST, mappedBy = "member")
    private HomeAddress homeAddress;

    public void setHomeAddress(HomeAddress homeAddress) {
        this.homeAddress = homeAddress;
        homeAddress.setMember(this);
    }
}


@Data
@Table(name = "COMPOSITE_PK_HOME_ADDRESS")
@Entity
class HomeAddress {

    @EmbeddedId
    private HomeAddressId homeAddressId;

    @OneToOne
    @JoinColumn(name = "MEMBER_ID", insertable = false, updatable = false) // MEMBER_ID 수정 및 저장 불가
    private Member member;

}

@Data
@Embeddable
class HomeAddressId implements Serializable {

    @Column(name = "HOME_ADDRESS_ID")
    private int id;

    @Column(name = "MEMBER_ID")
    private int memberId;
}