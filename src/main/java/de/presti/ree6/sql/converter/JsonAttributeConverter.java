package de.presti.ree6.sql.converter;

import com.google.gson.JsonElement;
import de.presti.ree6.utils.data.TypUtil;
import jakarta.persistence.AttributeConverter;

import java.sql.Blob;

public class JsonAttributeConverter implements AttributeConverter<JsonElement, Blob> {

    @Override
    public Blob convertToDatabaseColumn(JsonElement attribute) {
        return TypUtil.convertJSONToBlob(attribute);
    }

    @Override
    public JsonElement convertToEntityAttribute(Blob dbData) {
        return TypUtil.convertBlobToJSON(dbData);
    }
}
