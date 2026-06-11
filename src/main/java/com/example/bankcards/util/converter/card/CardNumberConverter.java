package com.example.bankcards.util.converter.card;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Base64;

@Converter
public class CardNumberConverter implements AttributeConverter<String, String> {

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) return null;
        return Base64.getEncoder().encodeToString(attribute.getBytes());
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        return new String(Base64.getDecoder().decode(dbData));
    }
}
