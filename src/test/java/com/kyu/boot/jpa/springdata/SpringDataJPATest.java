package com.kyu.boot.jpa.springdata;

import com.kyu.boot.jpa.springdata.repository.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.junit4.SpringRunner;

import javax.transaction.Transactional;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;


@Slf4j
@Transactional
@RunWith(SpringRunner.class)
@SpringBootTest
@EnableAsync
public class SpringDataJPATest {

    public static final int CNT = 200;

    @Autowired
    private MemberRepository memberRepository;

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
}


