package com.kyu.boot.jpa.springdata;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @Project : test_project
 * @Date : 2017-06-22
 * @Author : nklee
 * @Description :
 */
@Data
@Table(name = "SPRING_DATA_MEMBER")
@Entity
public class SpringMember {

    @Id
    private int seq;

    private String name;

    public SpringMember(int seq, String name) {
        this.seq = seq;
        this.name = name;
    }

    public SpringMember() {
    }
}
