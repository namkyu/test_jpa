package com.kyu.boot.jpa.option;

import lombok.Data;
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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * @Project : test_project
 * @Date : 2017-06-16
 * @Author : nklee
 * @Description :
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class InsertableTest {

    @PersistenceContext
    private EntityManager em;

    @Test
    @Transactional
    public void test_insertable() {
        Member member = new Member();
        member.setId(1);
        member.setFirstName("namkyu");
        member.setLastName("Lee");

        em.persist(member);
        em.flush();
        em.clear();

        member = em.find(Member.class, 1);
        assertThat("namkyu", is(member.getFirstName()));

        // 위에서 "Lee"를 저장했지만 insertable = false 이기 때문에 저장이 되어 있지 않음
        // insert 문 할 때 last_name 컬럼은 제외된다.
        assertNull(member.getLastName());
    }

    @Test
    @Transactional
    public void test_insertable_생성자에값셋팅() {
        Member member = new Member(1, "Lee", "namkyu");

        em.persist(member);
        em.flush();
        em.clear();

        member = em.find(Member.class, 1);
        assertThat("namkyu", is(member.getFirstName()));

        // 생성자를 통해서 lastName을 셋팅했지만 insert 문 생성할 떄 last_name 제외 된다.
        assertNull(member.getLastName());
    }

    @Test
    @Transactional
    public void test_insertable_cascade로저장시도() {
        KeyBoard keyBoard = new KeyBoard();
        keyBoard.setId(1);
        keyBoard.setName("realforce");

        KeyCap keyCap = new KeyCap();
        keyCap.setId(1);
        keyCap.setName("A");
        keyCap.setKeyBoard(keyBoard);

        em.persist(keyCap);
        em.flush();
        em.clear();

        keyCap = em.find(KeyCap.class, 1);

        // cascade로도 저장 불가하여 last_name 값이 null 이다.
        assertNull(keyCap.getKeyBoard().getName());
    }

    @Test
    @Transactional
    public void test_insertable_올바른사용법() {
        // insertable = false means that JPA won't include the column in the insert statement when saving the entity.
        // But it will when updating the entity, and it will load it from the database.

        // 정리하면 insertable = false 일 때 엔티티를 이용해 저장은 불가능하고 DB에 저장되어 있는 데이터를 가져오는 것은 가능
        // 아래의 테스트 케이스는 import.sql 파일에 insert 문을 작성하여 DB에 데이터를 저장한 뒤 진행했음
        Account account = em.find(Account.class, 2);
        assertThat("nklee2", is(account.getName()));

        // update는 가능
        account.setName("nklee22");
        em.flush();
        em.clear();

        account = em.find(Account.class, 2);
        assertThat("nklee22", is(account.getName()));
    }
}


@Data
@Entity
@Table(name = "INSERTABLE_MEMBER")
class Member {

    @Id
    @Column(name = "MEMBER_ID")
    private int id;

    @Column(name = "LAST_NAME", insertable = false)
    private String lastName;

    @Column(name = "FIRST_NAME")
    private String firstName;

    public Member(int id, String lastName, String firstName) {
        this.id = id;
        this.lastName = lastName;
        this.firstName = firstName;
    }

    public Member() {

    }
}

@Data
@Entity
@lombok.ToString(exclude = "keyCaps")
class KeyBoard {

    @Id
    @Column(name = "KEYBOARD_ID")
    private int id;

    @Column(name = "NAME", insertable = false)
    private String name;

    @OneToMany
    private List<KeyCap> keyCaps = new ArrayList<>();
}

@Data
@Entity
class KeyCap {

    @Id
    @Column(name = "KEY_CAP_ID")
    private int id;

    @Column(name = "NAME")
    private String name;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "KEYBOARD_ID")
    private KeyBoard keyBoard;

    public void setKeyBoard(KeyBoard keyBoard) {
        this.keyBoard = keyBoard;
        keyBoard.getKeyCaps().add(this);
    }
}

@Data
@Entity
@Table(name = "INSERTABLE_ACCOUNT")
class Account {

    @Id
    private int id;

    @Column(name = "NAME", insertable = false)
    private String name;

}