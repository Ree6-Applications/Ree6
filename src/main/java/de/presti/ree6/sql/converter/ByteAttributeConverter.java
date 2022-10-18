package de.presti.ree6.sql.converter;

import jakarta.persistence.AttributeConverter;

import java.util.Base64;

/**
 * A AttributeConverter to allow us the usage of byte arrays in entities.
 */
public class ByteAttributeConverter implements AttributeConverter<byte[], String> {

    /**
     * @inheritDoc
     */
    @Override
    public byte[] convertToEntityAttribute(String attribute) {
        return Base64.getDecoder().decode(attribute);
    }

    /**
     * @inheritDoc
     */
    @Override
    public String convertToDatabaseColumn(byte[] dbData) {
        return Base64.getEncoder().encodeToString(dbData);
    }
}
