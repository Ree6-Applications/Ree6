package pw.aru.api.nekos4j.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Maps an input stream to a different type.
 *
 * @param <T> Type returned after mapping.
 * @see IOUtils#READ_FULLY
 * @see IOUtils#readFully(int)
 * @see IOUtils#writeTo(OutputStream)
 * @see IOUtils#writeTo(OutputStream, int)
 */
@FunctionalInterface
public interface InputStreamFunction<T> {
    T accept(InputStream is) throws IOException;
}
