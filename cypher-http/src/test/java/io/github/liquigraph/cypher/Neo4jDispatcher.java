/*
 * Copyright 2014-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
