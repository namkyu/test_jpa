package com.kyu.boot.jpa.fetchtype;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.core.StringContains;
import org.hibernate.collection.internal.PersistentBag;
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
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class LazyLoadingTest {

    @PersistenceContext
    private EntityManager em;

    private PersistenceUnitUtil unitUtil;

    @Before
    public void before() {
        unitUtil = em.getEntityManagerFactory().getPersistenceUnitUtil();
    }

    @Test
    @Transactional
    public void lazyOneToOne() {
        LMember member = new LMember();
        member.setId(1);

        LHomeAddress lHomeAddress = new LHomeAddress();
        lHomeAddress.setId(1);
        lHomeAddress.setLMember(member);

        em.persist(lHomeAddress);
        em.flush();
        em.clear();

        lHomeAddress = em.find(LHomeAddress.class, 1);
        LMember entityMember = lHomeAddress.getLMember();

        // JPA 프록시 객체에는 다음과 같이 class 이름에 jvst 가 포함되어 있다.
        // com.kyu.boot.jpa.fetchtype.LMember_$$_jvst45c_b
        String className = entityMember.getClass().getName();
        assertThat(className, StringContains.containsString("jvst"));

        boolean loaded = unitUtil.isLoaded(entityMember);
        assertThat(false, is(loaded));

        entityMember.getId(); // init entity
        loaded = unitUtil.isLoaded(entityMember);
        assertThat(true, is(loaded));
    }

    @Test
    @Transactional
    public void lazyManyToOne() {
        LMember member = new LMember();
        member.setId(1);

        LPhone lPhone = new LPhone(1, "010-1111-1111");
        lPhone.setLMember(member);

        LPhone lPhone1 = new LPhone(2, "010-2222-2222");
        lPhone1.setLMember(member);

        em.persist(lPhone);
        em.persist(lPhone1);

        em.flush();
        em.clear();

        LPhone entityPhone = em.find(LPhone.class, 1);
        LMember entityMember = entityPhone.getLMember();

        boolean loaded = unitUtil.isLoaded(entityMember);
        assertThat(false, is(loaded));

        String className = entityMember.getClass().getName();
        assertThat(className, StringContains.containsString("jvst"));

        entityMember.getId();
        loaded = unitUtil.isLoaded(entityMember);
        assertThat(true, is(loaded));
    }

    @Test
    @Transactional
    public void lazyOneToMany() {
        LMember member = new LMember();
        member.setId(1);

        LPhone lPhone = new LPhone(1, "010-1111-1111");
        member.addPhone(lPhone);

        LPhone lPhone1 = new LPhone(2, "010-2222-2222");
        member.addPhone(lPhone1);

        // 영속 상태 전 phoneList 객체는 ArrayList 타입이다.
        assertThat(member.getPhoneList().getClass(), sameInstance(ArrayList.class));

        // 영속 상태로 저장
        em.persist(member);
        em.flush();
        em.clear();

        LMember entityMember = em.find(LMember.class, 1);
        assertThat(1, is(entityMember.getId()));

        System.out.println("----------------------------------------------------");
        List<LPhone> phoneList = entityMember.getPhoneList();

        // 엔티티 초기화 안 되어 있음
        boolean loaded = unitUtil.isLoaded(phoneList);
        assertThat(false, is(loaded));

        // 원본 컬렉션인 phoneList는 PersistentBag 래퍼 컬렉션으로 감싸져 있다.
        assertThat(phoneList.getClass(), sameInstance(PersistentBag.class));

        // 엔티티 초기화
        System.out.println("init entity");
        phoneList.get(0).getId();

        // 엔티티 초기화 되어 있음
        loaded = unitUtil.isLoaded(phoneList);
        assertThat(true, is(loaded));
        System.out.println("----------------------------------------------------");
    }
}


@Data
@ToString(exclude = "phoneList")
@Entity
class LMember {

    @Id
    @Column(name = "MEMBER_ID")
    private int id;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "lMember", cascade = CascadeType.PERSIST)
    private List<LPhone> phoneList = new ArrayList<>();

    public void addPhone(LPhone phone) {
        this.phoneList.add(phone);
        phone.setLMember(this);
    }
}


@Data
@Entity
class LHomeAddress {

    @Id
    @Column(name = "HOME_ADDRESS_ID")
    private int id;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "MEMBER_ID")
    private LMember lMember;
}

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
class LPhone {

    @Id
    @Column(name = "PHONE_ID")
    private int id;

    @Column(name = "PHONE_NAME")
    private String number;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private LMember lMember;

    public LPhone(int id, String number) {
        this.number = number;
        this.id = id;
    }
}