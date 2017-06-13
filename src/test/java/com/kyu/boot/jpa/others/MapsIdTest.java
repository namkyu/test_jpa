package com.kyu.boot.jpa.others;

import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.*;
import javax.transaction.Transactional;

/**
 * @Project : test_project
 * @Date : 2017-06-13
 * @Author : nklee
 * @Description :
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class MapsIdTest {

    @PersistenceContext
    private EntityManager em;

    /**
     * 주테이블의 PK -> 자식테이블의 식별자(PK, 외래키) 형태로 내려가는 경우
     * Hibernate에서 @MapsId로 OneToOne 매핑시 자식측에 @JoinColumn이 필요하다.
     * JPA2 방식에서는 @MapsId, @JoinColumn 을 사용하고, JPA1 방식에서는 @PrimaryKeyJoinColumn을 사용한다.
     */
    @Test
    @Transactional
    public void sharedPK() {
        MRefund refund = new MRefund();
        refund.setRefundAmount(1000);

        MOrder order = new MOrder();
        order.setOrderId(12345);
        order.setRefund(refund);

        em.persist(order);
        em.flush();
        em.clear();
    }
}

/**
 * create table morder (
 * order_id integer not null,
 * primary key (order_id)
 * )
 * <p>
 * create table mrefund (
 * order_id integer not null,
 * refund_amount integer,
 * primary key (order_id)
 * )
 * <p>
 * alter table mrefund
 * add constraint FKgh3rjrqp9hvm8u2jsdb82t7e0
 * foreign key (order_id)
 * references morder
 */

@Data
@ToString(exclude = "refund")
@Entity
class MOrder {

    // PK
    @Id
    @Column(name = "ORDER_ID")
    private int orderId;

    @OneToOne(mappedBy = "order", cascade = CascadeType.PERSIST)
    private MRefund refund;

    public void setRefund(MRefund refund) {
        this.refund = refund;
        refund.setOrder(this);
    }

}

@Data
@Entity
class MRefund {

    // PK
    @Id
    @Column(name = "ORDER_ID")
    private int orderId;

    // @MapsId는 외래 키와 매핑한 연관관계를 기본 키에도 매핑하겠다는 뜻 -> @Id를 사용해서 식별자로 지정한 MRefund.orderId와 매핑
    @OneToOne
    @MapsId
    @JoinColumn(name = "ORDER_ID")
    private MOrder order;

    @Column(name = "REFUND_AMOUNT")
    private int refundAmount;

}
