package com.kyu.boot.jpa.jpql;

import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.*;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class JPQLTest {

    public static final int CNT = 10;

    @PersistenceContext
    private EntityManager em;

    @Before
    public void before() {
        Department department = new Department();
        department.setId(1);
        department.setName("development");

        for (int i = 0; i < CNT; i++) {
            Person person = new Person();
            person.setId(i);
            person.setName("nklee" + i);
            person.setDepartment(department);
            em.persist(person);
        }

        em.flush();
        em.clear();
    }

    @Test
    @Transactional
    public void testJPQL() {
        // JPQL은 객체지향 쿼리 (Person은 class 이름이다. Table 이름이 아님)
        em.createQuery("SELECT p FROM Person p")
                .getResultList()
                .forEach(System.out::println);

        System.out.println("===================================================");
        em.createQuery("SELECT p FROM Person p")
                .setMaxResults(5)
                .getResultList()
                .forEach(System.out::println);

        Query query = em.createQuery("select p from Person p");
        List<Person> list = query.getResultList();
        assertThat(list.size(), is(CNT));
    }

    @Test
    @Transactional
    public void testCompositTypeQuery() {
        TypedQuery typedQuery = em.createQuery("select p from Person p", Person.class);
        List<Person> personList = typedQuery.getResultList();
        personList.forEach(System.out::println);

        TypedQuery typedQuery1 = em.createQuery("select p.department from Person p", Department.class);
        List<Department> departmentList = typedQuery1.getResultList();
        departmentList.forEach(System.out::println);
    }

    @Test
    @Transactional
    public void testFilter() {
        TypedQuery typedQuery = em.createQuery("select p from Person p where p.department.name = :name", Person.class);
        typedQuery.setParameter("name", "development");

        List<Person> list = typedQuery.getResultList();
        assertThat(list.size(), is(10));
    }

    @Test
    @Transactional
    public void 쓰기지연_처리후에_JPQL호출하면_flush되는지확인() {
        Person person = new Person();
        person.setId(20);
        person.setName("nklee");
        em.persist(person);

        // 영속성 컨텍스트에 where id = 20 인 Person 엔티티 존재 여부
        Person returnPersonEntity = em.find(Person.class, 20);
        assertThat("nklee", is(returnPersonEntity.getName()));

        System.out.println("-----------------------------------------");
        System.out.println("execute JPQL");
        System.out.println("-----------------------------------------");
        TypedQuery typedQuery = em.createQuery("select p from Person p where p.id = 20", Person.class);
        List<Person> list = typedQuery.getResultList(); // JPQL을 실행하게 되면서 영속성 컨텍스트를 flush 한다. 이때 DB로 insert문이 전송
        assertThat(1, is(list.size()));
        System.out.println("-----------------------------------------");
    }

    @Test
    @Transactional
    public void JPQL로_조회한_엔티티는_영속상태이다() {
        TypedQuery typedQuery = em.createQuery("select p from Person p where p.id = 1", Person.class);
        List<Person> persons = typedQuery.getResultList();
        Person person = persons.get(0);

        boolean isLoaded = em.getEntityManagerFactory().getPersistenceUnitUtil().isLoaded(person);
        assertThat(true, is(isLoaded));
    }
}


/**
 * create table jpql_person (
 * person_id integer generated by default as identity,
 * name varchar(255),
 * dept_id integer,
 * primary key (person_id)
 * )
 * <p>
 * alter table jpql_person
 * add constraint FK7hjuswhyf2f04k4ene1hlj17l
 * foreign key (dept_id)
 * references jpql_dept
 */
@Data
@Table(name = "JPQL_PERSON")
@Entity
class Person {

    @Id
    @Column(name = "PERSON_ID")
    private int id;

    private String name;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "DEPT_ID")
    private Department department;

    public void setDepartment(Department department) {
        this.department = department;
        department.addPerson(this);
    }
}

@Data
@ToString(exclude = "personList")
@Table(name = "JPQL_DEPT")
@Entity
class Department {

    @Id
    @Column(name = "DEPT_ID")
    private int id;

    private String name;

    @OneToMany(mappedBy = "department")
    private List<Person> personList = new ArrayList<>();

    public void addPerson(Person person) {
        personList.add(person);
    }
}


