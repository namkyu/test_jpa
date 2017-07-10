package com.kyu.boot.jpa.batch;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.*;
import javax.transaction.Transactional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @Project : test_project
 * @Date : 2017-07-10
 * @Author : nklee
 * @Description :
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class BulkTest {

    @PersistenceContext
    private EntityManager em;

    @Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
    private int batchSize;

    @Test
    public void batch사이즈() {
        assertThat(20, is(batchSize));
    }

    @Test
    @Transactional
    public void bulkUpdate() {

        for (int i = 0; i < 10000; i++) {
            BulkMember member = new BulkMember();
            member.setId(i);
            member.setName("nklee");
            em.persist(member);

            if (i % batchSize == 0) {
                // Flush a batch of inserts and release memory.
                em.flush();
                em.clear();
            }
        }

        em.flush();
        em.clear();
    }
}


@Data
@Entity
@Table(name = "BULK_MEMBER")
class BulkMember {

    @Id
    private int id;

    @Lob
    private String name;

}