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
}


/**
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

    private int empNo;
    private String empName;
}


/**
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

@Data
class OrderProductPK implements Serializable {
    private int orderId;
    private int productId;
}
