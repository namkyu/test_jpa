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
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * @Project :
 * @Date : 2017-06-12
 * @Author : nklee
 * @Description :
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class LombokTest {

    @PersistenceContext
    private EntityManager em;

    @Test(expected = StackOverflowError.class)
    @Transactional
    public void 롬복_무한루프_오류() {

        // dummy data
        LMember member = new LMember();
        member.setId(1);
        member.setName("nklee");

        LPhone phone = new LPhone();
        phone.setId(1);
        phone.setNumber("010-1111-1111");
        member.addPhone(phone);

        LHomeAddress lHomeAddress = new LHomeAddress();
        lHomeAddress.setId(1);
        member.setlHomeAddress(lHomeAddress);

        // save
        em.persist(member);
        em.flush();
        em.clear();

        // verify
        member = em.find(LMember.class, 1);
        assertThat(1, is(member.getId()));

        phone = em.find(LPhone.class, 1);
        assertThat(1, is(phone.getId()));

        // toString() 메서드 호출로 인해 StackOverFlowError 발생
        System.out.println(phone.getLMember());

        fail("java.lang.StackOverflowError 오류 발생!!");
    }

    @Test
    @Transactional
    public void 롬복_무한루프_해소() {

        // dummy data
        LMember member = new LMember();
        member.setId(1);
        member.setName("nklee");

        LHomeAddress lHomeAddress = new LHomeAddress();
        lHomeAddress.setId(1);
        member.setlHomeAddress(lHomeAddress);

        // save
        em.persist(member);
        em.flush();
        em.clear();

        // verify
        member = em.find(LMember.class, 1);
        assertThat(1, is(member.getId()));

        // StackOverFlowError 발생하지 않음
        // @ToString(exclude = "lMember") 설정 추가 후 해결
        System.out.println(member.getLHomeAddress());
    }
}

@Data
@Entity
class LMember {

    @Id
    @Column(name = "MEMBER_ID")
    private int id;

    private String name;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "lMember", cascade = CascadeType.ALL)
    private List<LPhone> phoneList = new ArrayList<>();

    @OneToOne(mappedBy = "lMember", cascade = CascadeType.ALL)
    private LHomeAddress lHomeAddress;

    public void addPhone(LPhone phone) {
        this.phoneList.add(phone);
        phone.setLMember(this);
    }

    public void setlHomeAddress(LHomeAddress homeAddress) {
        this.lHomeAddress = homeAddress;
        homeAddress.setLMember(this);
    }
}

@Data
@Entity
class LPhone {

    @Id
    @Column(name = "PHONE_ID")
    private int id;

    private String number;

    @ManyToOne
    @JoinColumn(name = "MEMBER_ID")
    private LMember lMember;
}

@Data
@ToString(exclude = "lMember")
@Entity
class LHomeAddress {

    @Id
    @Column(name = "HOME_ADDRESS_ID")
    private int id;

    @OneToOne
    @JoinColumn(name = "MEMBER_ID")
    private LMember lMember;
}



