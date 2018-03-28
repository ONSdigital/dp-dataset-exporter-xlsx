package dp.api;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

public final class AuthUtils {

    @Deprecated
    private static final String AUTH_HEADER_KEY_OLD = "Internal-Token";

    private static final String AUTH_HEADER_KEY = "Authorization";

    public static <T> HttpEntity<T> createHeaders(final String serviceToken, final String oldToken, final T object) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(AUTH_HEADER_KEY_OLD, oldToken);
        httpHeaders.add(AUTH_HEADER_KEY, serviceToken);
        return new HttpEntity<>(object, httpHeaders);
    }

    public static <T> HttpEntity<T> createAuthHeaders(final String token) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(AUTH_HEADER_KEY, token);
        return new HttpEntity<>(httpHeaders);
    }
}
