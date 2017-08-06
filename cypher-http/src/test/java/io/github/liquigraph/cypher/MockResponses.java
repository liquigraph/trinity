package io.github.liquigraph.cypher;

import okhttp3.Headers;
import okhttp3.mockwebserver.MockResponse;

class MockResponses {

    public static MockResponse jsonOkResponse(String body, Header... headers) {
        return jsonResponse(body, headers).setResponseCode(200);
    }

    public static MockResponse jsonResponse(String body, Header... headers) {
        return new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setHeaders(customHeaders(headers))
                .setBody(body);
    }

    private static Headers customHeaders(Header[] headers) {
        return Headers.of(Header.asMap(headers));
    }
}
