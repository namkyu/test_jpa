package com.kyu.boot.jpa.cascade;

import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.*;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * @Project : test_project
 * @Date : 2017-06-07
 * @Author : nklee
 * @Description :
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class CascadeTypeAllTest {

    @PersistenceContext
    private EntityManager em;

    @After
    public void after() {
        // 테스트 케이스가 종료되면 무조건 영속석 컨텍스트를 초기화한다.
        em.clear();
    }

    /**
     * 1.테스트 데이터를 생성한다.
     * 2.persist 를 통해 영속성 컨텍스트에 엔티티를 저장한다.
     * 3.flush 를 통해 insert 문이 DB에 반영되게 한다.
     * 4.clear 영속성 컨텍스트를 초기화 한다.
     */
    private void memberTestData() {
        // member
        Member member = new Member();
        member.setId(1);
        member.setName("남규");

        // home address
        HomeAddress homeAddress = new HomeAddress();
        homeAddress.setId(1);
        homeAddress.setAddress("경기도");
        homeAddress.setMember(member);

        System.out.println("Start Test Data ---------------------------------------");
        em.persist(homeAddress); // save
        em.flush(); // DB 반영
        em.clear(); // 영속석 컨텍스트 초기화
        System.out.println("End Test Data ---------------------------------------");
    }

    private void accountTestData() {
        // Account
        Account account = new Account();
        account.setId(1);
        account.setName("nklee");

        // Phone
        AccountPhone accountPhone = new AccountPhone();
        accountPhone.setId(1);
        accountPhone.setNumber("010-1111-1111");
        account.addPhone(accountPhone);

        AccountPhone accountPhone1 = new AccountPhone();
        accountPhone1.setId(2);
        accountPhone1.setNumber("010-2222-2222");
        account.addPhone(accountPhone1);

        // Address
        AccountAddress accountAddress = new AccountAddress();
        accountAddress.setId(1);
        accountAddress.setAddress("경기도");
        account.addAddress(accountAddress);

        AccountAddress accountAddress1 = new AccountAddress();
        accountAddress1.setId(2);
        accountAddress1.setAddress("서울");
        account.addAddress(accountAddress1);

        System.out.println("Start Test Data ---------------------------------------");
        em.persist(account); // save
        em.flush(); // DB 반영
        em.clear(); // 영속석 컨텍스트 초기화
        System.out.println("End Test Data ---------------------------------------");
    }

    /**
     * HOME_ADDRESS 엔티티만 em.persist 했을 때 member도 함께 저장되었는지 확인
     */
    @Test
    @Transactional
    public void Member_엔티티저장상태() {
        memberTestData();

        HomeAddress homeAddress = em.find(HomeAddress.class, 1);
        assertThat("경기도", is(homeAddress.getAddress()));
        assertThat("남규", is(homeAddress.getMember().getName()));

        Member member = em.find(Member.class, 1);
        assertThat("남규", is(member.getName()));
    }

    /**
     * HOME_ADDRESS 엔티티만 em.remove 했을 때 member 엔티티도 함께 제거되었는지 확인
     */
    @Test
    @Transactional
    public void Member_엔티티삭제() {
        memberTestData();

        HomeAddress homeAddress = em.find(HomeAddress.class, 1);
        em.remove(homeAddress);
        em.flush();

        homeAddress = em.find(HomeAddress.class, 1);
        assertNull(homeAddress);

        Member member = em.find(Member.class, 1);
        assertNull(member);
    }

    @Test
    @Transactional
    public void Account_엔티티저장상태() {
        accountTestData();

        Account account = em.find(Account.class, 1);
        assertThat("nklee", is(account.getName()));
        assertThat(2, is(account.getPhones().size()));
        assertThat(2, is(account.getAddressList().size()));
    }

    @Test
    @Transactional
    public void Account_엔티티삭제() {
        accountTestData();

        Account account = em.find(Account.class, 1);
        em.remove(account);
        em.flush();

        account = em.find(Account.class, 1);
        assertNull(account);

        AccountPhone accountPhone = em.find(AccountPhone.class, 1);
        assertNull(accountPhone);
    }


    @Test(expected = AssertionError.class)
    @Transactional
    public void AccountPhone_엔티티삭제_실패() {
        accountTestData();

        AccountPhone accountPhone = em.find(AccountPhone.class, 1);
        em.remove(accountPhone);
        em.flush();
        em.clear();

        accountPhone = em.find(AccountPhone.class, 1);
        assertNull(accountPhone);

        fail("AccountPhone 엔티티 제거 안 된다. ");
    }

    @Test
    @Transactional
    public void AccountPhone_엔티티삭제_성공() {
        accountTestData();

        // 삭제 대상
        AccountAddress accountAddress = em.find(AccountAddress.class, 1);

        // 부모 엔티티에서 삭제 대상의 엔티티를 제거
        Account account = em.find(Account.class, 1);
        account.removeAddress(accountAddress); // addressList 필드에 orphanRemoval = true 가 선언되어 있어야 한다.

        // 삭제 SQL DB로 전송
        em.flush();
        em.clear();

        accountAddress = em.find(AccountAddress.class, 1);
        assertNull(accountAddress);
    }


}


/**
 * ------------------------------------------------------------
 * 테이블 구조
 * ------------------------------------------------------------
 * <p>
 * create table test_cascade_account (
 * account_id integer not null,
 * account_name varchar(255),
 * primary key (account_id)
 * )
 * <p>
 * create table test_cascade_account_address (
 * account_address_id integer not null,
 * address varchar(255),
 * account_id integer,
 * primary key (account_address_id)
 * )
 * <p>
 * create table test_cascade_account_phone (
 * account_phone_id integer not null,
 * account_phone_number varchar(255),
 * account_id integer,
 * primary key (account_phone_id)
 * )
 * <p>
 * alter table test_cascade_account_address
 * add constraint FKsarc0f3e1004ibkuud7xm9u32
 * foreign key (account_id)
 * references test_cascade_account
 * <p>
 * alter table test_cascade_account_phone
 * add constraint FK3o8qx5csp41scgcgiu0a3n17l
 * foreign key (account_id)
 * references test_cascade_account
 */
@Data
@Entity
@Table(name = "TEST_CASCADE_MEMBER")
class Member {

    @Id
    @Column(name = "MEMBER_ID")
    private int id;

    @Column(name = "NAME")
    private String name;
}

@Data
@ToString(exclude = "member")
@Entity
@Table(name = "TEST_CASCADE_HOME_ADDRESS")
class HomeAddress {

    @Id
    @Column(name = "HOME_ADDRESS_ID")
    private int id;

    @Column(name = "ADDRESS")
    private String address;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "MEMBER_ID") // 조인 컬럼을 명시하지 않으면 member_member_id로 정의된다.
    private Member member;
}


/**
 * ------------------------------------------------------------
 * 테이블 구조
 * ------------------------------------------------------------
 * <p>
 * create table test_cascade_account (
 * account_id integer not null,
 * account_name varchar(255),
 * primary key (account_id)
 * )
 * <p>
 * create table test_cascade_account_phone (
 * account_phone_id integer not null,
 * account_phone_number varchar(255),
 * account_id integer,
 * primary key (account_phone_id)
 * )
 * <p>
 * alter table test_cascade_account_phone
 * add constraint FK3o8qx5csp41scgcgiu0a3n17l
 * foreign key (account_id)
 * references test_cascade_account
 */

@Data
@ToString(exclude = {"phones", "addressList"})
@Entity
@Table(name = "TEST_CASCADE_ACCOUNT")
class Account {

    @Id
    @Column(name = "ACCOUNT_ID")
    private int id;

    @Column(name = "ACCOUNT_NAME")
    private String name;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "account", cascade = CascadeType.ALL)
    private List<AccountPhone> phones = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AccountAddress> addressList = new ArrayList<>();

    public void addPhone(AccountPhone accountPhone) {
        this.phones.add(accountPhone);
        accountPhone.setAccount(this); // 해당 코드가 없으면 TEST_CASCADE_ACCOUNT_PHONE 테이블의 ACCOUNT_ID 값이 null로 된다.
    }

    public void addAddress(AccountAddress accountAddress) {
        this.addressList.add(accountAddress);
        accountAddress.setAccount(this);
    }

    public void removeAddress(AccountAddress accountAddress) {
        this.addressList.remove(accountAddress);
    }
}


@Data
@Entity
@Table(name = "TEST_CASCADE_ACCOUNT_PHONE")
class AccountPhone {

    @Id
    @Column(name = "ACCOUNT_PHONE_ID")
    private int id;

    @Column(name = "ACCOUNT_PHONE_NUMBER")
    private String number;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ACCOUNT_ID")
    private Account account;
}

@Data
@Entity
@Table(name = "TEST_CASCADE_ACCOUNT_ADDRESS")
class AccountAddress {

    @Id
    @Column(name = "ACCOUNT_ADDRESS_ID")
    private int id;

    @Column(name = "ADDRESS")
    private String address;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ACCOUNT_ID")
    private Account account;
}




