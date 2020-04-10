package com.kyu.boot.jpa.springdata.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Project : test-jpa
 * @Date : 2020-04-10
 * @Author : nklee
 * @Description :
 */
@Data
@AllArgsConstructor
public class SpringMemberDTO {
    private String name;
    private long count;
}
