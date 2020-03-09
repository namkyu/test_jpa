package com.kyu.boot.jpa.springdata;

import com.kyu.boot.jpa.springdata.entity.SpringMember;
import com.kyu.boot.jpa.springdata.repo.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.core.StringContains;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceUnit;
import javax.transaction.Transactional;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

/**
 * @Project : test_project
 * @Date : 2017-06-23
 * @Author : nklee
 * @Description :
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class JpaRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @PersistenceUnit
    private EntityManagerFactory emf;


    @Test
    @Transactional
    public void getOne메서드테스트() {
        // proxy 객체가 리턴된다. (lazy loading)
        SpringMember member = memberRepository.getOne(1);
        String className = member.getClass().getName();
        assertThat(className, StringContains.containsString("jvst"));

        // 엔티티 초기화 되어 있지 않은 상태
        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(member);
        assertThat(false, is(loaded));

        // 엔티티 초기화
        System.out.println("------------ init entity ------------");
        member.getName();
        System.out.println("-------------------------------------");

        // 엔티티 존재
        loaded = emf.getPersistenceUnitUtil().isLoaded(member);
        assertThat(true, is(loaded));
    }

    @Test
    @Transactional
    public void findOne메서드테스트() {
        System.out.println("------------ init entity ------------");
        SpringMember member = memberRepository.findOne(1);
        System.out.println("-------------------------------------");

        // proxy 객체 아님
        String className = member.getClass().getName();
        assertThat(className, is(not(StringContains.containsString("jvst"))));

        // 엔티티 초기화 되어 있지 않은 상태
        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(member);
        assertThat(true, is(loaded));
    }


    @Test(expected = EntityNotFoundException.class)
    @Transactional
    public void getOne데이터없을때() {
        SpringMember member = memberRepository.getOne(2222);

        // 데이터가 DB에 존재하지 않지만 proxy 객체가 리턴되어 null이 아닌 상태
        assertNotNull(member);

        // 에러 발생
        member.getName();

        fail("javax.persistence.EntityNotFoundException: Unable to find com.kyu.boot.jpa.springdata.entity.SpringMember with id 2222 오류 발생");
    }

    @Test
    @Transactional
    public void findOne데이터없을때() {
        SpringMember member = memberRepository.findOne(2222);

        // 데이터가 DB에 존재하지 않아 null 리턴
        assertNull(member);
    }

}
