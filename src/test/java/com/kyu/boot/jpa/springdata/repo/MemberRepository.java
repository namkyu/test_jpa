package com.kyu.boot.jpa.springdata.repo;

import com.kyu.boot.jpa.springdata.dto.SpringMemberDTO;
import com.kyu.boot.jpa.springdata.entity.SpringMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.scheduling.annotation.Async;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @Project : test_project
 * @Date : 2017-06-23
 * @Author : nklee
 * @Description :
 */
public interface MemberRepository extends JpaRepository<SpringMember, Integer> {

    SpringMember findBySeqAndName(int seq, String name);

    List<SpringMember> findTop3ByNameLike(String name);

    List<SpringMember> findAllByOrderBySeqAsc();

    List<SpringMember> findAllByOrderBySeqDesc();

    List<SpringMember> findTop2ByNameLikeOrderBySeqDesc(String name);

    SpringMember findTop1ByNameOrderBySeqDesc(String name);

    SpringMember readBySeq(int seq);

    SpringMember readByName(String name);

    SpringMember queryBySeq(int seq);

    long countByNameLike(String name);

    long countByName(String name);

    @Query(value = "SELECT seq, name FROM SPRING_DATA_MEMBER", nativeQuery = true)
    List<SpringMember> nativeQuery();

    @Query(value = "SELECT seq, name FROM SPRING_DATA_MEMBER WHERE name = :name", nativeQuery = true)
    SpringMember nativeQueryByName(@Param(value = "name") String name);

    @Query(value = "SELECT new com.kyu.boot.jpa.springdata.dto.SpringMemberDTO(s.name, count(s.seq)) FROM SpringMember s group by s.name")
    List<SpringMemberDTO> convertDtoType();

    @Async
    CompletableFuture<List<SpringMember>> readAllBy();
}

