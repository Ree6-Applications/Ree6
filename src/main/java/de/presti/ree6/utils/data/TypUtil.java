package de.presti.ree6.utils.data;

import com.google.gson.*;

import javax.sql.rowset.serial.SerialBlob;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Blob;

public class TypUtil {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Convert a Blob to a {@link JsonElement}
     *
     * @param blob the Blob to convert.
     * @return the {@link JsonElement} or {@link JsonNull} if the Blob is null.
     */
    public static JsonElement convertBlobToJSON(Blob blob) {
        if (blob == null)
            return JsonNull.INSTANCE;

        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(blob.getBinaryStream()))) {

            for (String read; (read = reader.readLine()) != null; ) {
                content.append(read);
            }
        } catch (Exception ignore) {
        }

        if (content.length() == 0)
            return JsonNull.INSTANCE;

        return JsonParser.parseString(content.toString());
    }

    /**
     * Convert a {@link JsonElement} to a Blob.
     *
     * @param jsonElement the {@link JsonElement} to convert.
     * @return the Blob or null if the {@link JsonElement} is null.
     */
    public static Blob convertJSONToBlob(JsonElement jsonElement) {
        try {
            return new SerialBlob(gson.toJson(jsonElement).getBytes());
        } catch (Exception ignore) {
        }

        return null;
    }

}
