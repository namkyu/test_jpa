package com.kyu.boot.jpa.springdata;

import com.kyu.boot.jpa.springdata.dto.SpringMemberDTO;
import com.kyu.boot.jpa.springdata.entity.Partners;
import com.kyu.boot.jpa.springdata.entity.PartnersStatus;
import com.kyu.boot.jpa.springdata.entity.SpringMember;
import com.kyu.boot.jpa.springdata.repo.MemberRepository;
import com.kyu.boot.jpa.springdata.repo.PartnersRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.transaction.Transactional;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers.contains;


@Slf4j
@Transactional
@RunWith(SpringRunner.class)
@SpringBootTest
@EnableAsync
public class SpringDataJPATest {

    public static final int CNT = 200;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private SpringDataEmpRepository springDataEmpRepository;

    @Autowired
    private PartnersRepository partnersRepository;

    /**
     * 각각의 @Test메서드 실행 전 호출된다.
     * Test 메서드는 랜덤하게 실행된다. (순서 보장 없음)
     */
    @Before
    public void before() {
        for (int i = 0; i < CNT; i++) {
            SpringMember member = new SpringMember(i, "Lee" + i);
            memberRepository.save(member);
        }

        memberRepository.flush();
    }

    @Test
    public void 중복키_저장_처리() {
        SpringMember member = new SpringMember(5, "Lee555");
        System.out.println("-------------------------");
        memberRepository.saveAndFlush(member);
        System.out.println("-------------------------");
    }

    @Test
    public void test_repository_save() {
        SpringMember member = new SpringMember(5, "updated5");
        memberRepository.save(member);
        System.out.println("================");
        SpringMember memberEntity = memberRepository.findOne(5);
        System.out.println(memberEntity);
    }

    @Test
    public void test_repository_saveAndFlush() {
        SpringMember member = new SpringMember(5, "Lee555");
        memberRepository.saveAndFlush(member); // 여기서 쿼리가 전송된다.
        System.out.println("================");
    }

    @Test
    public void 전체삭제() {
        memberRepository.deleteAll();
        memberRepository.flush();

        SpringMember entityMember = memberRepository.findOne(5);
        assertNull(entityMember);
    }

    /**
     * 테스트 데이터 정상 입력 확인
     */
    @Test
    public void testTotalCnt() {
        List<SpringMember> list = memberRepository.findAll();
        assertThat(list.size(), is(CNT));
        assertThat(memberRepository.count(), is(Long.valueOf(CNT)));
    }

    /**
     * 영속성 컨텍스트에 저장되어 있는 SpringMember 엔티티 비교
     */
    @Test
    public void testSameInstance() {
        SpringMember member1 = memberRepository.readByName("Lee0");
        SpringMember member2 = memberRepository.readByName("Lee0");
        assertThat(member1, is(sameInstance(member2)));
    }

    @Test
    public void testParamIsNull() {
        List<SpringMember> result = memberRepository.findAll();
        assertThat(200, is(result.size()));

        SpringMember springMemberBuild1 = SpringMember.builder().name(null).build();
        List<SpringMember> result1 = memberRepository.findAll(Example.of(springMemberBuild1));
        assertThat(200, is(result1.size()));

        SpringMember springMemberBuild2 = SpringMember.builder().name("Lee0").build();
        List<SpringMember> result2 = memberRepository.findAll(Example.of(springMemberBuild2));
        assertThat(1, is(result2.size()));

        ExampleMatcher matcher = ExampleMatcher.matchingAll()
            .withMatcher("name", contains().ignoreCase());

        SpringMember springMemberBuild3 = SpringMember.builder().name("Lee0").build();
        List<SpringMember> result3 = memberRepository.findAll(Example.of(springMemberBuild3, matcher));
        assertThat(1, is(result3.size()));
    }


    /**
     * 메서드 이름으로 쿼리 생성하는 방법에 대해서
     */
    @Test
    public void testQueryCreation() {
        assertThat(3, is(memberRepository.findTop3ByNameLike("Lee%").size()));
        assertThat(Long.valueOf(CNT), is(memberRepository.countByNameLike("Lee%")));
    }

    /**
     * Note, that we need to disable the surrounding transaction to be able to asynchronously read the written
     * data from from another thread within the same test method
     * <p>
     * Transactional.TxType.NOT_SUPPORTED 추가 이유에 대한 설명
     * 1. testAsyncQueryCreate() 메서드에 @Transactional이 설정되어 있다고 가정하고 테스트 실행
     * 2. 트랜잭션 시작
     * 3. 데이터 200건 저장 (커밋되어 있지 않음, 즉 DB에 반영되어 있지 않은 상태)
     * 4. 새로운 쓰레드로 memberRepository의 readAllBy 실행하면서 새로운 트랜잭션 생성
     * 5. 새로운 쓰레드는 DB에서 데이터를 가져오려고 하지만 존재하지 않음 (200건의 데이터는 현재 commit 되어 있지 않았기 때문에)
     * <p>
     * [해결 방법]
     * Test 메서드 실행 시 트랜잭션을 무시하도록 한다.
     */
    @Test
    @Transactional(Transactional.TxType.NOT_SUPPORTED)
    public void testAsyncQueryCreation() throws InterruptedException, ExecutionException {
        CompletableFuture<List<SpringMember>> completableFuture = memberRepository.readAllBy();

        while (completableFuture.isDone() == false) {
            System.out.println("waiting for the CompletableFuture to finish...");
            TimeUnit.MILLISECONDS.sleep(500);
        }

        List<SpringMember> members = completableFuture.get();
        assertThat(members.size(), is(CNT));
    }

    @Test
    public void testNativeQuery() {
        List<SpringMember> list = memberRepository.nativeQuery();
        assertThat(list.size(), is(CNT));

        SpringMember member = memberRepository.nativeQueryByName("Lee0");
        assertThat(member.getName(), is("Lee0"));
    }

    @Test
    public void testNativeQueryUsingJPQL() {
        List<SpringMemberDTO> springMembers = memberRepository.convertDtoType();
        System.out.println(springMembers);
    }

    @Test
    public void testPaging() {
        Page<SpringMember> page = memberRepository.findAll(new PageRequest(3, 15));
        System.out.println("=======================================================");
        System.out.println("page : " + page);
        System.out.println("totalElements : " + page.getTotalElements());
        System.out.println("totalPages : " + page.getTotalPages());
        System.out.println("nextPage : " + page.nextPageable());
        System.out.println("previousPage : " + page.previousPageable());
        System.out.println("=======================================================");
    }

    @Test
    public void compositKey() {
        SpringDataEmp emp = new SpringDataEmp(new SpringDataEmpId(1, "nklee"), "010-1111-1111");
        SpringDataEmp emp2 = new SpringDataEmp(new SpringDataEmpId(2, "nklee2"), "010-2222-2222");
        springDataEmpRepository.save(emp);
        springDataEmpRepository.save(emp2);
        springDataEmpRepository.flush();

        List<SpringDataEmp> list = springDataEmpRepository.findByIdEmpNo(1);
        assertThat(1, is(list.size()));

        List<SpringDataEmp> list2 = springDataEmpRepository.findByIdEmpName("nklee");
        assertThat(1, is(list2.size()));

        List<SpringDataEmp> list3 = springDataEmpRepository.findByIdEmpNoAndIdEmpName(1, "nklee");
        assertThat(1, is(list3.size()));

        List<SpringDataEmp> list4 = springDataEmpRepository.findByIdEmpNameIn(new String[]{"nklee", "nklee2"});
        assertThat(2, is(list4.size()));
    }

    @Test
    public void test_orderByAsc() {
        List<SpringMember> list = memberRepository.findAllByOrderBySeqAsc();
        list.forEach(System.out::println);
    }

    @Test
    public void test_orderByDesc() {
        List<SpringMember> list = memberRepository.findAllByOrderBySeqDesc();
        list.forEach(System.out::println);
    }

    @Test
    public void test_search_nam_and_orderBy() {
        List<SpringMember> list = memberRepository.findTop2ByNameLikeOrderBySeqDesc("Lee%");
        System.out.println("===============================");
        list.forEach(System.out::println);
        System.out.println("===============================");
    }

    @Test
    public void test_search_top1_orderBy() {
        SpringMember springMember = memberRepository.findTop1ByNameOrderBySeqDesc("Lee2");
        System.out.println("===============================");
        System.out.println(springMember);
        System.out.println("===============================");
    }

    @Test
    public void test_partners_entity() {
        Partners partners1 = new Partners(1, "장난감1", PartnersStatus.STANDBY);
        Partners partners2 = new Partners(2, "장난감2", PartnersStatus.DONE);
        Partners partners3 = new Partners(3, "장난감3", PartnersStatus.STANDBY);
        partnersRepository.save(Arrays.asList(partners1, partners2, partners3));

        Partners entity = partnersRepository.findTop1ByStatusOrderBySeqDesc(PartnersStatus.STANDBY);
        System.out.println("==================================");
        System.out.println(entity);
        System.out.println("==================================");
    }


}


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
class SpringDataEmp {

    @EmbeddedId
    private SpringDataEmpId id;

    private String phone;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
class SpringDataEmpId implements Serializable {

    @Column(name = "EMP_NO")
    private int empNo;

    @Column(name = "EMP_NAME")
    private String empName;
}

interface SpringDataEmpRepository extends JpaRepository<SpringDataEmp, SpringDataEmpId> {

    List<SpringDataEmp> findByIdEmpNo(int empNo);

    List<SpringDataEmp> findByIdEmpName(String empName);

    List<SpringDataEmp> findByIdEmpNoAndIdEmpName(int empNo, String empName);

    List<SpringDataEmp> findByIdEmpNameIn(String[] empNames);

}
