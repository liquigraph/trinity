/*
 * Copyright 2017 the original author or authors.
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
package org.liquigraph.trinity.http;

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
