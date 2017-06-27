package com.kyu.boot.jpa.cascade;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.*;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * @Project : test_project
 * @Date : 2017-06-27
 * @Author : nklee
 * @Description :
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class DeleteAllEntityTest {

    @PersistenceContext
    private EntityManager em;

    @Before
    public void before() {
        Product product = new Product();
        product.setId(1);
        product.setName("테스트 상품");
        em.persist(product);

        Shipping shipping = new Shipping();
        shipping.setId(1);

        Refund refund = new Refund();
        refund.setId(1);

        Order order = new Order();
        order.setId(1);
        order.setRefund(refund);
        order.setShipping(shipping);

        OrderProduct orderProduct = new OrderProduct();
        orderProduct.setId(1);
        orderProduct.setProduct(product);
        orderProduct.setOrder(order);

        em.persist(order);
        em.flush();
        em.clear();
    }

    @Test
    @Transactional
    public void 데이터검증() {
        Order order = em.find(Order.class, 1);
        assertThat(1, is(order.getId()));
        assertThat(1, is(order.getShipping().getId()));
        assertThat(1, is(order.getRefund().getId()));
        assertThat(1, is(order.getOrderProduct().size()));
    }

    /**
     * 1. modeling_order_product 삭제
     * 2. modeling_refund 삭제
     * 3. modeling_shipping 삭제
     * 4. modeling_order 삭제
     */
    @Test
    @Transactional
    public void 주문관련_모두삭제() {
        Order order = em.find(Order.class, 1);
        System.out.println("-----------------------------");
        em.remove(order);
        em.flush();
        System.out.println("-----------------------------");

        assertNull(em.find(Order.class, 1));
        assertNull(em.find(OrderProduct.class, 1));
        assertNull(em.find(Shipping.class, 1));
        assertNull(em.find(Refund.class, 1));

        assertThat(1, is(em.find(Product.class, 1).getId()));
    }
}


@Data
@Entity
@Table(name = "MODELING_PRODUCT")
class Product {

    @Id
    private int id;

    private String name;
}

@Data
@Entity
@Table(name = "MODELING_ORDER")
class Order {

    @Id
    private int id;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderProduct> orderProduct = new ArrayList<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    private Shipping shipping;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    private Refund refund;

    public void setRefund(Refund refund) {
        this.refund = refund;
        refund.setOrder(this);
    }

    public void setShipping(Shipping shipping) {
        this.shipping = shipping;
        shipping.setOrder(this);
    }

}

@Data
@Entity
@Table(name = "MODELING_ORDER_PRODUCT")
class OrderProduct {

    @Id
    private int id;

    @ManyToOne(cascade = CascadeType.PERSIST)
    private Product product;

    @ManyToOne(cascade = CascadeType.ALL)
    private Order order;

    public void setProduct(Product product) {
        this.product = product;
    }

    public void setOrder(Order order) {
        this.order = order;
        order.getOrderProduct().add(this);
    }

}

@Data
@Entity
@Table(name = "MODELING_SHIPPING")
class Shipping {

    @Id
    private int id;

    @OneToOne
    private Order order;
}

@Data
@Entity
@Table(name = "MODELING_REFUND")
class Refund {
    @Id
    private int id;

    @OneToOne
    private Order order;

}