package com.kyu.boot.jpa.springdata.entity;

import lombok.Getter;

/**
 * @Project : test-jpa
 * @Date : 2020-12-22
 * @Author : nklee
 * @Description :
 */
public enum PartnersStatus {

    STANDBY(1)
    , DONE(2);

    @Getter
    private int status;

    PartnersStatus(int status) {
        this.status = status;
    }
}
