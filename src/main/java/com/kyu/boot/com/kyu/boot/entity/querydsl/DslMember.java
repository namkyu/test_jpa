package com.kyu.boot.com.kyu.boot.entity.querydsl;

import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @Project : test_project
 * @Date : 2017-06-26
 * @Author : nklee
 * @Description :
 */
@Data
@Entity
@Table(name = "DSL_MEMBER")
public class DslMember {

    @Id
    @Column(name = "MEMBER_ID")
    private int id;

    private String name;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "member", cascade = CascadeType.PERSIST)
    private DslHomeAddress homeAddress;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "member", cascade = CascadeType.PERSIST)
    private List<DslPhone> phoneList = new ArrayList<>();

    public void setHomeAddress(DslHomeAddress homeAddress) {
        this.homeAddress = homeAddress;
        homeAddress.setMember(this);
    }

    public void addPhone(DslPhone phone) {
        phoneList.add(phone);
        phone.setMember(this);
    }
}
