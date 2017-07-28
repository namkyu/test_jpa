package com.kyu.boot.jpa.problem;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.BatchSize;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.*;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * @Project : test_project
 * @Date : 2017-07-12
 * @Author : nklee
 * @Description :
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class NPlusOneProblem {

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private CatRepository catRepository;

    @Autowired
    private OrderRepository orderRepository;

    @PersistenceContext
    private EntityManager em;

    @Before
    public void before() {

        // 테스트 데이터
        int catId = 1;
        int ownId = 1;
        for (int i = 1; i < 20; i++) {
            Owner owner = new Owner();
            owner.setId(ownId);
            owner.setName("nklee" + ownId);
            em.persist(owner);
            ownId++;

            // cats 테스트 데이터
            for (int j = 1; j < 10; j++) {
                Cat cat = new Cat();
                cat.setId(catId);
                cat.setName("야옹이" + catId);
                cat.setOwner(owner);
                em.persist(cat);
                catId++;
            }
        }

        em.flush();
        em.clear();
        System.out.println("===================== Done!!");
    }


    @Test
    @Transactional
    public void test데이터검증() {
        Owner owner = ownerRepository.findOne(1);
        assertThat(1, is(owner.getId()));
        assertThat(9, is(owner.getCats().size()));
        assertThat(19, is(ownerRepository.findAll().size()));
    }

    /**
     * 문제의 핵심은
     * 1번의 쿼리로 100개의 리스트 정보를 가져왔지만 (쿼리 1번)
     * 100개의 로우에서 필요한 데이터를 1번씩 쿼리해서 가져온다. (쿼리 100번)
     */
    @Test
    @Transactional
    public void N플러스1문제_ManyToOne() {
        // @ManyToOne이 EAGER이면 N+1 문제 발생
        // Cat.findAll() 하면 N개의 Cat이 조회되지만 (쿼리 1번)
        // Owner 정보도 필요하므로 N번 Owner를 조회한다. (쿼리 N번)
        List<Cat> list = catRepository.findAll();
        System.out.println("---------------------------------------");
        System.out.println("list size : " + list.size());
        System.out.println("---------------------------------------");

//        select cat0_.id as id1_3_, cat0_.name as name2_3_, cat0_.owner_id as owner_id3_3_ from cat cat0_
//        위 쿼리 결과로 나온 N개의 레코드가 반환된다고 했을 때 추가로 수행되는 쿼리
//        select owner0_.id as id1_38_0_, owner0_.name as name2_38_0_ from owner owner0_ where owner0_.id =?
//        select owner0_.id as id1_38_0_, owner0_.name as name2_38_0_ from owner owner0_ where owner0_.id =?
//        select owner0_.id as id1_38_0_, owner0_.name nasame2_38_0_ from owner owner0_ where owner0_.id =?
//        ...(owner row 갯수만큼)
    }

    @Test
    @Transactional
    public void N플러스1문제_OneToMany() {
        List<Owner> ownerList = ownerRepository.findAll();
        System.out.println("---------------------------------------");
        System.out.println("list size : " + ownerList.size());
        System.out.println("---------------------------------------");

        ownerList.forEach(owner -> {
            owner.getCats().forEach(cat -> {
                System.out.println(cat.getName());
            });
        });
    }

    private void makeTestData() {
        Product product = new Product();
        product.setId(1);
        product.setName("바구니");
        em.persist(product);

        int orderProductId = 0;
        for (int i = 0; i < 10; i++) {
            Shipping shipping = new Shipping();
            shipping.setId(i);

            Order order = new Order();
            order.setId(i);
            order.setShipping(shipping);

            for (int j = 0; j < 5; j++) {
                OrderProduct orderProduct = new OrderProduct();
                orderProduct.setId(orderProductId);
                orderProduct.setOrder(order);
                orderProductId++;
            }

            em.persist(order);
        }

        em.flush();
        em.clear();
    }

    /**
     * OneToOne N+1 문제
     * order 엔티티를 10개 가져왔을 때 shipping 엔티티의 데이터를 가져오기 위해 10번 select 한다.
     * 이 처리에 대해서 LAZY, EAGER 둘 중 어떤 전략이 좋을지 확인해 보자.
     */
    @Test
    @Transactional
    public void OneToOne쿼리() {
        makeTestData();
        orderRepository.findAll();
    }

    /**
     * M+1 문제가 해소됐다. 한번의 쿼리로 모든 데이터를 가져왔음
     * 페치 조인으로 조회해서 지연 로딩 발생 안함
     * <p>
     * -----------------------------------------
     * fetch 적용 쿼리
     * -----------------------------------------
     * select
     * order0_.id as id1_36_0_,
     * shipping1_.id as id1_39_1_,
     * shipping1_.order_id as order_id2_39_1_
     * from
     * nplus_order order0_
     * left outer join
     * nplus_shipping shipping1_
     * on order0_.id=shipping1_.order_id
     * <p>
     * <p>
     * -----------------------------------------
     * fetch 미적용 쿼리 1+N 문제 발생
     * -----------------------------------------
     * select
     * order0_.id as id1_36_
     * from
     * nplus_order order0_
     * left outer join
     * nplus_shipping shipping1_
     * on order0_.id=shipping1_.order_id
     */
    @Test
    @Transactional
    public void OneToOne쿼리_fetch적용() {
        makeTestData();

        TypedQuery typedQuery = em.createQuery("select o from Order o left join fetch o.shipping", Order.class);
        List<Order> orderList = typedQuery.getResultList();
        assertThat(10, is(orderList.size()));

        Order order = em.find(Order.class, 8);
        assertNotNull(order);
    }

    /**
     * Order 테이블에 데이터 10개
     * OrderProducts 테이블에 데이터 5개
     * 위의 두 개의 테이블을 서로 조인하면 50개가 나온다.
     */
    @Test
    @Transactional
    public void fetch적용시_데이터가_중복노출되는현상() {
        makeTestData();

        TypedQuery typedQuery = em.createQuery("select o from Order o join fetch o.orderProducts", Order.class);
        System.out.println("--------------------------------------------");
        List<Order> orderList = typedQuery.getResultList();
        System.out.println("--------------------------------------------");

        assertThat(10, is(not(orderList.size())));
        assertThat(50, is(orderList.size()));
    }

    @Test
    @Transactional
    public void fetch적용시_데이터가_중복노출되는현상_distinct로_해결하기() {
        makeTestData();

        TypedQuery typedQuery = em.createQuery("select distinct o from Order o join fetch o.orderProducts", Order.class);
        System.out.println("--------------------------------------------");
        List<Order> orderList = typedQuery.getResultList();
        System.out.println("--------------------------------------------");

        assertThat(10, is(orderList.size()));
    }
}

/**
 * create table cats (
 * id integer not null,
 * owner_id integer,
 * primary key (id)
 * )
 * <p>
 * create table owner (
 * id integer not null,
 * name varchar(255),
 * primary key (id)
 * )
 * <p>
 * alter table cats
 * add constraint FKlet4cncad4b281dourgr2687d
 * foreign key (owner_id)
 * references owner
 */
@Getter
@Setter
@NoArgsConstructor
@lombok.ToString
@Entity
@BatchSize(size = 10)
class Owner {
    @Id
    private int id;
    private String name;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST, mappedBy = "owner")
    @BatchSize(size = 10)
    private List<Cat> cats = new ArrayList<>();
}

@Getter
@Setter
@NoArgsConstructor
@lombok.ToString(exclude = "owner")
@Entity
class Cat {
    @Id
    private int id;
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    private Owner owner;

}


interface OwnerRepository extends JpaRepository<Owner, Integer> {
}

interface CatRepository extends JpaRepository<Cat, Integer> {
}

interface OrderRepository extends JpaRepository<Order, Integer> {
}


@Getter
@Setter
@NoArgsConstructor
@lombok.ToString
@Entity
@Table(name = "NPLUS_ORDER")
class Order {
    @Id
    private int id;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "order", cascade = CascadeType.PERSIST, optional = false)
    private Shipping shipping;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "order", cascade = CascadeType.PERSIST)
    private List<OrderProduct> orderProducts = new ArrayList<>();

    public void setShipping(Shipping shipping) {
        this.shipping = shipping;
        shipping.setOrder(this);
    }

    public void addOrderProducts(OrderProduct orderProduct) {
        orderProducts.add(orderProduct);
    }
}


@Getter
@Setter
@NoArgsConstructor
@lombok.ToString(exclude = "order")
@Entity
@Table(name = "NPLUS_SHIPPING")
class Shipping {

    @Id
    private int id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    private Order order;
}


@Getter
@Setter
@NoArgsConstructor
@lombok.ToString(exclude = "order")
@Entity
@Table(name = "NPLUS_ORDER_PRODUCT")
class OrderProduct {

    @Id
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Order order;

    public void setOrder(Order order) {
        this.order = order;
        order.addOrderProducts(this);
    }
}


@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "NPLUS_PRODUCT")
class Product {

    @Id
    private int id;

    private String name;
}