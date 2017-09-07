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
package io.github.liquigraph.cypher.http;

import io.github.liquigraph.cypher.CypherClientCreator;
import io.github.liquigraph.cypher.CypherTransport;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class HttpClientCreator implements CypherClientCreator {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientCreator.class);

    @Override
    public boolean supports(CypherTransport transport) {
        return CypherTransport.HTTP == transport;
    }

    @Override
    public HttpClient create(Properties properties) {
        LOGGER.trace("About to instantiate the HTTP client");
        String baseUrl = read(properties, "cypher.http.baseurl", "the base URL of Neo4j REST API (e.g. http://localhost:7474)");
        String username = read(properties, "cypher.http.username");
        String password = read(properties, "cypher.http.password", "the user password", username == null);
        return new HttpClient(baseUrl, createHttpClient(username, password));
    }

    private String read(Properties properties, String name) {
        return read(properties, name, null, true);
    }

    private String read(Properties properties, String name, String description) {
        return read(properties, name, description, false);
    }

    private String read(Properties properties, String name, String description, boolean nullable) {
        String property = properties.getProperty(name);
        if (property == null && !nullable) {
            throw new IllegalArgumentException(String.format("Cypher HTTP Client setting %s should be set as %s", name, description));
        }
        return property;
    }

    private static OkHttpClient createHttpClient(String username, String password) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (username != null && password != null) {
            LOGGER.trace("Targetting Neo4j without authentication credentials");
            builder = builder.authenticator(new BasicAuthenticator(username, password));
        }
        return builder.build();
    }
}
