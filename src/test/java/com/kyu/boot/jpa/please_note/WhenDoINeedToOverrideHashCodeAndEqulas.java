package com.kyu.boot.jpa.please_note;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @Project : test_project
 * @Date : 2017-06-29
 * @Author : nklee
 * @Description : 엔티티에서 hashcode, equals 를 오버라이드 해야 하는 상황이 어떤 경우인지를 알아보기 위한 테스트
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class WhenDoINeedToOverrideHashCodeAndEqulas {

    @PersistenceContext
    private EntityManager em;


    @Test
    public void Set기본테스트() {
        // Set은 중복을 허용하지 않는다.
        Set<Integer> sets = new HashSet<>();
        sets.add(1);
        sets.add(2);
        sets.add(3);
        sets.add(1);

        assertThat(3, is(sets.size()));
    }

    @Test
    @Transactional
    public void Set엔티티클래스테스트() {
        em.persist(new IamEntity(1, "nklee1"));
        em.persist(new IamEntity(2, "nklee2"));
        em.flush();
        em.clear();

        // entityManager에서 hashcode() 메서드를 호출하지 않는다.
        IamEntity entity1 = em.find(IamEntity.class, 1);
        IamEntity entity2 = em.find(IamEntity.class, 1);
        IamEntity entity3 = em.find(IamEntity.class, 2);

        // entity1, entity2의 hashcode는 같다. (엔티티매니저가 같은 오브젝트를 return해줌)
        Set<IamEntity> sets = new HashSet<>();
        sets.add(entity1);
        sets.add(entity2);
        sets.add(entity3);

        assertThat(2, is(sets.size()));
    }

    @Test
    public void SetPure클래스테스트() {
        IamEntity pureClass1 = new IamEntity(1, "nklee1");
        IamEntity pureClass2 = new IamEntity(1, "nklee1");
        IamEntity pureClass3 = new IamEntity(2, "nklee2");

        System.out.println("pureClass1 : " + pureClass1.hashCode());
        System.out.println("pureClass2 : " + pureClass2.hashCode());
        System.out.println("pureClass3 : " + pureClass3.hashCode());

//        [hashcode() 메서드 오버라이드 하지 않았을 때의 hash값]
//        pureClass1 : 215911532
//        pureClass2 : 390537668
//        pureClass3 : 898810308
//        [hashcode() 메서드 오버라이드 했을 때의 hash값]
//        pureClass1 : -1043626239
//        pureClass2 : -1043626239
//        pureClass3 : -1043626207

        Set<IamEntity> sets = new HashSet<>();
        sets.add(pureClass1);
        sets.add(pureClass2);
        sets.add(pureClass3);

        assertThat(2, is(sets.size()));
    }
}

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
class IamEntity {

    @Id
    private int id;

    private String name;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IamEntity entity = (IamEntity) o;

        if (id != entity.id) return false;
        return name != null ? name.equals(entity.name) : entity.name == null;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
