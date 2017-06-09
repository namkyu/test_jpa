package com.kyu.boot.jpa.entitymanager;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.Entity;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Id;
import javax.persistence.PersistenceUnit;

/**
 * @Project : test_project
 * @Date : 2017-06-09
 * @Author : nklee
 * @Description :
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class EntityManagerFactoryTest {

    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;

    @Test
    public void testEntityManagerFactory() {
        entityManagerFactory.getProperties().forEach((k, v) -> System.out.println("key : " + k + ", value : " + v));
    }
}


@Data
@Entity
class Member {

    @Id
    private int id;
}
