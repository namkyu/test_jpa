package com.kyu.boot.jpa.fetchtype;

import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.*;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @Project : test_project
 * @Date : 2017-06-13
 * @Author : nklee
 * @Description :
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class EegerLoadingTest {

    @PersistenceContext
    private EntityManager em;

    @Test
    @Transactional
    public void 즉시로딩() {
        PersistenceUnitUtil unitUtil = em.getEntityManagerFactory().getPersistenceUnitUtil();

        EagerMember member = new EagerMember();
        member.setId(1);

        EagerPhone phone = new EagerPhone();
        phone.setId(1);
        member.addPhone(phone);

        EagerPhone phone1 = new EagerPhone();
        phone1.setId(2);
        member.addPhone(phone1);

        em.persist(member);
        em.flush();
        em.clear();

        member = em.find(EagerMember.class, 1);

        // phones 초기화 되어 있음
        boolean loaded = unitUtil.isLoaded(member.getPhones());
        assertThat(true, is(loaded));
    }
}

@Data
@ToString(exclude = "phones")
@Entity
@Table(name = "EAGER_MEMBER")
class EagerMember {

    @Id
    @Column(name = "MEMBER_ID")
    private int id;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST, mappedBy = "member")
    private Collection<EagerPhone> phones = new ArrayList<>();

    public void addPhone(EagerPhone phone) {
        this.phones.add(phone);
        phone.setMember(this);
    }
}


@Data
@Entity
@Table(name = "EAGER_PHONE")
class EagerPhone {

    @Id
    @Column(name = "PHONE_ID")
    private int id;

    @JoinColumn(name = "MEMBER_ID", nullable = false)
    @ManyToOne(fetch = FetchType.EAGER)
    private EagerMember member;
}