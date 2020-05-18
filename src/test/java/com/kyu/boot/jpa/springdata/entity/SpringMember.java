package com.kyu.boot.jpa.springdata.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @Project : test_project
 * @Date : 2017-06-23
 * @Author : nklee
 * @Description :
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "SPRING_DATA_MEMBER")
@Entity
public class SpringMember {

    @Id
    private Integer seq;
    private String name;
}
