package pw.aru.api.nekos4j;

import com.github.natanbc.reliqua.request.RequestException;

@SuppressWarnings("WeakerAccess")
public class MissingScopeException extends RequestException {
    public MissingScopeException(String message, StackTraceElement[] elements) {
        super(message, elements);
    }
}
