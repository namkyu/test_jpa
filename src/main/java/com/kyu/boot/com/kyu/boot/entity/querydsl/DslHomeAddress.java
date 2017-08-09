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
@Table(name = "DSL_HOME_ADDRESS")
public class DslHomeAddress {

    @Id
    private int id;

    private String address;

    @OneToOne(optional = false)
    @JoinColumn(name = "MEMBER_ID")
    private DslMember member;
}
