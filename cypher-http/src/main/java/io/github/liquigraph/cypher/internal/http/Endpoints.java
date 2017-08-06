package io.github.liquigraph.cypher.internal.http;

import okhttp3.HttpUrl;

public class Endpoints {

    public static final HttpUrl singleTransactionUri(String baseUrl) {
        return HttpUrl.parse(baseUrl).newBuilder()
                .addPathSegment("db")
                .addPathSegment("data")
                .addPathSegment("transaction")
                .addPathSegment("commit")
                .build();
    }

    public static final HttpUrl openTransactionUri(String baseUrl) {
        return HttpUrl.parse(baseUrl).newBuilder()
                .addPathSegment("db")
                .addPathSegment("data")
                .addPathSegment("transaction")
                .build();
    }
}
