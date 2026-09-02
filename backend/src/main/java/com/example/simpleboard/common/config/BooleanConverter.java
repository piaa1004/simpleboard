package com.example.simpleboard.common.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** boolean 을 'Y' / 'N' 문자로 저장한다. */
@Converter
public class BooleanConverter implements AttributeConverter<Boolean, String> {

    @Override
    public String convertToDatabaseColumn(Boolean attribute) {
        return (attribute != null && attribute) ? "Y" : "N";
    }

    @Override
    public Boolean convertToEntityAttribute(String dbData) {
        return "Y".equalsIgnoreCase(dbData);
    }
}
