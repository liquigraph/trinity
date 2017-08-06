package io.github.liquigraph.cypher;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;

import java.util.Arrays;

class Neo4jDispatcher extends Dispatcher {
    private final MockResponse expectedResponse;
    private final boolean authorization;

    public Neo4jDispatcher(MockResponse expectedResponse) {
        this(expectedResponse, false);
    }

    public Neo4jDispatcher(MockResponse expectedResponse, boolean authorization) {
        this.expectedResponse = expectedResponse;
        this.authorization = authorization;
    }

    @Override
    public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
        if (authorization && request.getHeader("Authorization") == null) {
            return new MockResponse()
                    .setResponseCode(401)
                    .setHeader("Content", "application/json")
                    .setBody("{\"code\": \"Neo.ClientError.Security.Unauthorized\"," +
                            "\"message\": \"No authentication header supplied.\"}");
        }

        if (!request.getHeader("Accept").startsWith("application/json")) {
            return new MockResponse().setResponseCode(406);
        }
        if (!request.getRequestUrl().pathSegments().containsAll(Arrays.asList("db", "data", "transaction"))) {
            return new MockResponse().setResponseCode(404);
        }
        return expectedResponse;
    }
}
