package de.presti.ree6.sql.converter;

import jakarta.persistence.AttributeConverter;

import java.util.Base64;

public class ByteAttributeConverter implements AttributeConverter<byte[], String> {

    @Override
    public byte[] convertToEntityAttribute(String attribute) {
        return Base64.getDecoder().decode(attribute);
    }

    @Override
    public String convertToDatabaseColumn(byte[] dbData) {
        return Base64.getEncoder().encodeToString(dbData);
    }
}
