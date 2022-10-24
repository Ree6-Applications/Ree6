package de.presti.ree6.sql.converter;

import com.google.gson.*;
import jakarta.persistence.AttributeConverter;

import javax.sql.rowset.serial.SerialBlob;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Blob;
import java.sql.SQLException;

/**
 * A AttributeConverter to allow us the usage of JsonElements in entities.
 */
public class JsonAttributeConverter implements AttributeConverter<JsonElement, Blob> {

    /**
     * Instance of GSON.
     */
    public static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * @inheritDoc
     */
    @Override
    public Blob convertToDatabaseColumn(JsonElement attribute) {
        try {
            return new SerialBlob(gson.toJson(attribute).getBytes());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public JsonElement convertToEntityAttribute(Blob dbData) {
        if (dbData == null)
            return JsonNull.INSTANCE;

        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(dbData.getBinaryStream()))) {

            for (String read; (read = reader.readLine()) != null; ) {
                content.append(read);
            }
        } catch (Exception ignore) {
        }

        if (content.length() == 0)
            return JsonNull.INSTANCE;

        return JsonParser.parseString(content.toString());
    }
}
