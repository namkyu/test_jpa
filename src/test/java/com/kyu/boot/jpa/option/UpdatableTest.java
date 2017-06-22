package com.kyu.boot.jpa.option;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.*;
import javax.transaction.Transactional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @Project : test_project
 * @Date : 2017-06-19
 * @Author : nklee
 * @Description :
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class UpdatableTest {

    @PersistenceContext
    private EntityManager em;

    @Test
    @Transactional
    public void test() {
        Hotel hotel = new Hotel();
        hotel.setId(10);
        hotel.setAge(40);
        hotel.setName("힐탑");

        em.persist(hotel);
        em.flush();
        em.clear();

        // 저장 여부 확인
        hotel = em.find(Hotel.class, 10);
        assertThat("힐탑", is(hotel.getName()));

        // name 컬럼 변경하지만 실제 DB에는 반영되지 않음 (updatable = false로 인해)
        hotel.setName("롯데호텔");
        hotel.setAge(20);
        em.flush();
        em.clear();

        hotel = em.find(Hotel.class, 10);
        assertThat("힐탑", is(hotel.getName()));
    }
}


@Data
@Entity
@Table(name = "UPDATABLE_HOTEL")
class Hotel {

    @Id
    @Column(name = "ID")
    private int id;

    @Column(name = "NAME", updatable = false)
    private String name;

    @Column(name = "AGE")
    private int age;
}
