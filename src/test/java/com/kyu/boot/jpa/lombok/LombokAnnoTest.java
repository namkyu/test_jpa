package com.kyu.boot.jpa.lombok;

import lombok.*;
import org.junit.Test;

/**
 * @Project : test_project
 * @Date : 2017-06-28
 * @Author : nklee
 * @Description :
 */
public class LombokAnnoTest {

    @Test
    public void test() {

        Member member = Member.builder().id(1)
                .name("nklee")
                .age(33)
                .salary(100000000000L)
                .married(true)
                .build();

        System.out.println(member);
    }

}


@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
class Member {

    private int id;
    private String name;
    private Integer age;
    private long salary;
    private boolean married;
}
