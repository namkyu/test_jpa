package com.kyu.boot.jpa.springdata.repo;

import com.kyu.boot.jpa.springdata.entity.Partners;
import com.kyu.boot.jpa.springdata.entity.PartnersStatus;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @Project : test_project
 * @Date : 2017-06-23
 * @Author : nklee
 * @Description :
 */
public interface PartnersRepository extends JpaRepository<Partners, Integer> {

    Partners findTop1ByStatusOrderBySeqDesc(PartnersStatus partnersStatus);
}

