package com.kyu.boot.jpa.springdata;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;


@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringDataTransactionTest {

    @Autowired
    private SDTService sdtService;

    @Before
    public void dummyData() {
        sdtService.insert();
    }

    @Test
    public void SpringDate_트랜잭션롤백_성공케이스() {
        // 테스트 데이터 검증
        SpringDataMember member = sdtService.getMember(1);
        assertThat(1, is(member.getId()));

        try {
            // 업데이트 오류 발생 (@Transactional 설정 있음)
            sdtService.occursNPEWithTransactionalAnno(1);
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        }

        // 롤백 여부 검증 (정상 롤백된다)
        member = sdtService.getMember(1);
        assertThat("nklee", is(member.getName()));
    }

    @Test(expected = AssertionError.class)
    public void SpringDate_트랜잭션롤백_실패케이스() {
        try {
            // 업데이트 오류 발생 (@Transactional 설정 없음)
            sdtService.occursNPEWithoutTransactionalAnno(1);
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        }

        // 롤백 여부 검증 (롤백되지 않음)
        SpringDataMember member = sdtService.getMember(1);
        assertThat("nklee", is(member.getName()));


        fail("occursNPEWithoutTransactionalAnno 메서드에 @Transactional 설정이 없으면 예외가 발생하여도 Rollback이 되지 않는다. ");
    }


}


@Service
class SDTService {

    @Autowired
    private SDTRepository sdtRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void insert() {
        SpringDataMember member = new SpringDataMember();
        member.setId(1);
        member.setName("nklee");
        sdtRepository.save(member);
    }

    public SpringDataMember getMember(int id) {
        return sdtRepository.findOne(id);
    }

    @Transactional
    public void occursNPEWithTransactionalAnno(int id) {
        SpringDataMember member = sdtRepository.findOne(id);
        member.setName("changedName");
        sdtRepository.save(member);

        // 강제 예외 발생
        throw new NullPointerException();
    }

    public void occursNPEWithoutTransactionalAnno(int id) {
        SpringDataMember member = sdtRepository.findOne(id);
        member.setName("changedName");
        sdtRepository.save(member);

        // 강제 예외 발생
        throw new NullPointerException();
    }

}

interface SDTRepository extends JpaRepository<SpringDataMember, Integer> {

}

@Entity
@Getter
@Setter
@Table(name = "SDT_MEMBER")
class SpringDataMember {

    @Id
    private Integer id;

    private String name;
}


