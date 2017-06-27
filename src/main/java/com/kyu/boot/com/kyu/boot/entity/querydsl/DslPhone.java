package com.kyu.boot.com.kyu.boot.entity.querydsl;

import lombok.Data;
import lombok.ToString;

import javax.persistence.*;

/**
 * @Project : test_project
 * @Date : 2017-06-26
 * @Author : nklee
 * @Description :
 */
@Data
@ToString(exclude = "member")
@Entity
@Table(name = "DSL_PHONE")
public class DslPhone {

    @Id
    private int id;

    private String number;

    @ManyToOne
    @JoinColumn(name = "MEMBER_ID")
    private DslMember member;
}
