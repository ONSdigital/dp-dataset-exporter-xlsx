package dp.api;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

public final class AuthUtils {

    @Deprecated
    private static final String AUTH_HEADER_KEY_OLD = "Internal-Token";

    private static final String AUTH_HEADER_KEY = "Authorization";

    public static <T> HttpEntity<T> createHeaders(final String token, final T object) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(AUTH_HEADER_KEY_OLD, token);
        httpHeaders.add(AUTH_HEADER_KEY, token);
        return new HttpEntity<>(object, httpHeaders);
    }
}
