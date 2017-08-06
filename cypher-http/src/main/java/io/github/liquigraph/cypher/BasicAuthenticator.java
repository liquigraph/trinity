package io.github.liquigraph.cypher;

import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

import java.io.IOException;

public class BasicAuthenticator implements Authenticator {

    private final String credentials;

    public BasicAuthenticator(String username, String password) {
        credentials = Credentials.basic(username, password);
    }

    @Override
    public Request authenticate(Route route, Response response) throws IOException {
        String currentCredentials = response.request().header("Authorization");
        if (credentials.equals(currentCredentials)) {
            return null;
        }
        return response.request().newBuilder()
                .header("Authorization", credentials)
                .build();
    }
}
