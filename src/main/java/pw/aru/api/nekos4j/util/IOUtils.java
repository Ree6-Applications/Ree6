package pw.aru.api.nekos4j.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@SuppressWarnings({"unused", "WeakerAccess"})
public class IOUtils {
    public static final int DEFAULT_BUFFER_SIZE = 4096;
    public static final InputStreamFunction<byte[]> READ_FULLY = readFully(DEFAULT_BUFFER_SIZE);

    public static InputStreamFunction<byte[]> readFully(int bufferSize) {
        return is -> readFully(is, bufferSize);
    }

    public static InputStreamFunction<Void> writeTo(OutputStream os, int bufferSize) {
        return is -> {
            copy(is, os, bufferSize);
            return null;
        };
    }

    public static InputStreamFunction<Void> writeTo(OutputStream os) {
        return writeTo(os, DEFAULT_BUFFER_SIZE);
    }

    private static void copy(InputStream from, OutputStream to, int bufferSize) throws IOException {
        byte[] buffer = new byte[bufferSize];
        int r;
        while ((r = from.read(buffer)) != -1) {
            to.write(buffer, 0, r);
        }
    }

    private static byte[] readFully(InputStream is, int bufferSize) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(bufferSize);
        copy(is, baos, bufferSize);
        return baos.toByteArray();
    }
}
