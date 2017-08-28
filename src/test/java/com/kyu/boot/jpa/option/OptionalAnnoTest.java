package com.kyu.boot.jpa.option;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.*;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

/**
 * @Project : test_project
 * @Date : 2017-07-21
 * @Author : nklee
 * @Description :
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class OptionalAnnoTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private OptionalTeamRepository optionalTeamRepository;

    @Before
    public void init() {
        OptionalMember member1 = new OptionalMember(1);
        OptionalMember member2 = new OptionalMember(2);
        OptionalMember member3 = new OptionalMember(3);

        OptionalMemberComputer memberComputer1 = new OptionalMemberComputer(1);
        OptionalMemberComputer memberComputer2 = new OptionalMemberComputer(2);
        OptionalMemberComputer memberComputer3 = new OptionalMemberComputer(3);

        OptionalTeam team = new OptionalTeam(1);
        team.addMember(member1);
        team.addMember(member2);
        team.addMember(member3);
        team.addMemberComputer(memberComputer1);
        team.addMemberComputer(memberComputer2);
        team.addMemberComputer(memberComputer3);

        em.persist(team);
        em.flush();
        em.clear();
    }

    @Test
    public void leftouter조인쿼리가생성됨() {
        List<OptionalTeam> result = optionalTeamRepository.findAll();
        result.forEach(System.out::println);
    }

    @Test
    public void inner조인쿼리가생성됨() {

    }
}

@Data
@Entity
@NoArgsConstructor
@lombok.ToString(exclude = "team")
@Table(name = "OPTIONAL_MEMBER")
class OptionalMember {

    @Id
    private int id;

    // optional 반드시 값이 필요하다면 true, false로 설정했을 때 해당 객체에 null이 들어갈 수 있음
    @ManyToOne(optional = false)
    private OptionalTeam team;

    public OptionalMember(int id) {
        this.id = id;
    }
}

@Data
@NoArgsConstructor
@lombok.ToString(exclude = "team")
@Entity
@Table(name = "OPTIONAL_MEMBER_COMPUTER")
class OptionalMemberComputer {

    @Id
    private int id;

    @ManyToOne
    private OptionalTeam team;

    public OptionalMemberComputer(int id) {
        this.id = id;
    }
}


@Data
@Entity
@Table(name = "OPTIONAL_TEAM")
class OptionalTeam {

    @Id
    private int id;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST, mappedBy = "team")
    @Fetch(FetchMode.JOIN)
    private List<OptionalMember> members = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST, mappedBy = "team")
    private List<OptionalMemberComputer> memberComputers = new ArrayList<>();

    public OptionalTeam(int id) {
        this.id = id;
    }

    public OptionalTeam() {
    }

    public void addMember(OptionalMember member) {
        members.add(member);
        member.setTeam(this);
    }

    public void addMemberComputer(OptionalMemberComputer optionalMemberComputer) {
        memberComputers.add(optionalMemberComputer);
        optionalMemberComputer.setTeam(this);
    }

}


interface OptionalTeamRepository extends JpaRepository<OptionalTeam, Integer> {

}