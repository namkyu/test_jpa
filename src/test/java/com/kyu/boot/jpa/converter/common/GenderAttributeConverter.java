package com.kyu.boot.jpa.converter.common;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;


@Converter
public class GenderAttributeConverter implements AttributeConverter<String, Integer> {


    @Override
    public Integer convertToDatabaseColumn(String s) {
        if ("남자".equals(s)) {
            return 1;
        } else if ("여자".equals(s)) {
            return 2;
        }

        return 0;
    }

    @Override
    public String convertToEntityAttribute(Integer code) {
        if (1 == code) {
            return "남자";
        } else if (2 == code) {
            return "여자";
        }

        return "뭐지?";
    }
}
