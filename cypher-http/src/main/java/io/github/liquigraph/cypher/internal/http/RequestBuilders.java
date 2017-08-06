package io.github.liquigraph.cypher.internal.http;

import okhttp3.Request;

public class RequestBuilders {

    public static Request.Builder json() {
        return new Request.Builder().addHeader("Accept", "application/json");
    }
}
