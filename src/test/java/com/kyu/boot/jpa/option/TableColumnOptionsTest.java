package com.kyu.boot.jpa.option;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.*;
import javax.transaction.Transactional;

/**
 * @Project : test_project
 * @Date : 2017-06-20
 * @Author : nklee
 * @Description :
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class TableColumnOptionsTest {

    @PersistenceContext
    private EntityManager em;

    @Test
    @Transactional
    public void test() {

        Namkyu namkyu = new Namkyu();
        namkyu.setId(1);
        namkyu.setName("nklee");
        namkyu.setAddress("구리시");
        namkyu.setAge(36);

        em.persist(namkyu);
        em.flush();
        em.clear();
    }

}


/**
 * create table namkyu (
 * id integer not null,
 * address varchar(128),
 * age integer,
 * name varchar(12) not null,
 * primary key (id)
 * )
 */
@Data
@Table(name = "NAMKYU")
@Entity
class Namkyu {

    @Id
    private int id;

    @Column(name = "NAME", length = 12, nullable = false)
    private String name;

    @Column(name = "AGE")
    private int age;

    @Column(name = "ADDRESS", length = 128)
    private String address;
}
