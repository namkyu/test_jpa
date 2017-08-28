package com.kyu.boot.jpa.converter;

import com.kyu.boot.jpa.converter.common.GenderAttributeConverter;
import com.kyu.boot.jpa.converter.common.LocalDateTimeAttributeConverter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.*;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @Project : test_project
 * @Date : 2017-06-12
 * @Author : nklee
 * @Description :
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class AttributeConverterTest {

    @PersistenceContext
    private EntityManager em;

    @Test
    @Transactional
    public void Attribute_컨버터() {
        Member member = new Member();
        member.setId(1);
        member.setGender("남자");
        member.setChanged(new Date());
        member.setRegistered(LocalDateTime.now());

        em.persist(member);
        em.flush();
        em.clear();

        // native query를 이용하여 gender = 1 조회
        Query query = em.createNativeQuery("select * from MEMBER_CONVERTER where gender = :gender", Member.class);
        query.setParameter("gender", 1);
        List<Member> list = query.getResultList();

        // 검증
        String resultGender = list.get(0).getGender();
        assertThat("남자", is(resultGender));
    }
}


@Data
@Table(name = "MEMBER_CONVERTER")
@Entity
class Member {

    @Id
    private int id;

    @Convert(converter = GenderAttributeConverter.class)
    private String gender;

    @Convert(converter = LocalDateTimeAttributeConverter.class)
    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime registered;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Date changed;
}
