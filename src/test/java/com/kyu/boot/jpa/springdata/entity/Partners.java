package com.kyu.boot.jpa.springdata.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "PARTNERS")
@Entity
public class Partners {

    @Id
    private Integer seq;
    private String title;
    private PartnersStatus status;
}
