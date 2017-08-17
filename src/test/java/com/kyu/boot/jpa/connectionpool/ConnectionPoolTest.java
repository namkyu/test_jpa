package com.kyu.boot.jpa.connectionpool;


import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.*;
import java.util.Map;

import static org.junit.Assert.fail;

/**
 * @Project : test_project
 * @Date : 2017-08-16
 * @Author : nklee
 * @Description :
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ConnectionPoolTest {

    @PersistenceUnit
    private EntityManagerFactory emf;

    private PoolConfiguration poolConfiguration;
    private int maxActive;

    @Before
    public void before() {
        // 커넥션 pool configuration
        Map<String, Object> map = emf.getProperties();
        DataSource poolInfo = (DataSource) map.get("javax.persistence.nonJtaDataSource");
        this.poolConfiguration = poolInfo.getPoolProperties();
        this.maxActive = poolConfiguration.getMaxActive();
        String toStr = ReflectionToStringBuilder.toString(poolConfiguration, ToStringStyle.MULTI_LINE_STYLE);
        System.out.println("----------------------");
        System.out.println(toStr);
        System.out.println("----------------------");
    }

    @Test(expected = PersistenceException.class)
    public void 풀에서_커넥션을_취득하기위한_시간을_초과하여_오류() throws InterruptedException {

        // pool 에서 커넥션 취득하기 위한 대기 시간
        poolConfiguration.setMaxWait(3000);

        // 커넥션 pool configuration
        Map<String, Object> map = emf.getProperties();
        DataSource poolInfo = (DataSource) map.get("javax.persistence.nonJtaDataSource");
        PoolConfiguration poolConfiguration = poolInfo.getPoolProperties();

        // 커넥션 성공 후 쿼리
        for (int i = 1; i <= maxActive; i++) {
            EntityManager em = emf.createEntityManager();
            em.createQuery("select a from PoolEntity as a").getResultList();
            System.out.println("-------------------------------------------------");
            System.out.println("number of connection pool used : " + i);
            System.out.println("-------------------------------------------------");
        }

        // 101번째 쿼리 요청 시 대기 상태로 빠진다.
        EntityManager em = emf.createEntityManager();
        em.createQuery("select a from PoolEntity as a").getResultList();

        // 풀에서 커넥션을 취득하기 위한 시간을 초과하여 아래와 같은 예외 발생 후 테스트 종료
        fail("[main] Timeout: Pool empty. Unable to fetch a connection in 3 seconds, none available[size:100; busy:100; idle:0; lastwait:3000]");
    }
}


@Data
@Entity
@Table(name = "POOL_TEST_ENTITY")
class PoolEntity {

    @Id
    private int id;
    private String name;

}


