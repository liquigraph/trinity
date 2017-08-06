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
package io.github.liquigraph.cypher;

import okhttp3.Credentials;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.logging.LogManager;

import static io.github.liquigraph.cypher.Assertions.assertThat;

public class BasicAuthenticatorTest {

    private MockWebServer server;
    private Request request;

    @BeforeClass
    public static void prepareAll() {
        LogManager.getLogManager().reset();
    }

    @Before
    public void prepare() throws IOException {
        server = new MockWebServer();
        server.setDispatcher(new AuthenticatedDispatcher("user", "s3cr3t"));
        server.start();
        HttpUrl url = new HttpUrl.Builder().scheme("http").host("localhost").port(server.getPort()).build();
        request = new Request.Builder().url(url).build();
    }

    @Test
    public void authenticates_clients() throws IOException {
        BasicAuthenticator subject = new BasicAuthenticator("user", "s3cr3t");
        OkHttpClient httpClient = new OkHttpClient.Builder().authenticator(subject).build();

        Response response = httpClient.newCall(request).execute();

        assertThat(response.code()).isEqualTo(200);
        assertThat(response.body().string()).isEqualTo("Whatever floats your boat");
    }

    @Test
    public void retries_only_once_when_auth_setting_is_wrong() throws IOException {
        BasicAuthenticator subject = new BasicAuthenticator("user", "oopsiewrongpassword");
        OkHttpClient httpClient = new OkHttpClient.Builder().authenticator(subject).build();

        Response response = httpClient.newCall(request).execute();

        assertThat(response.code()).isEqualTo(401);
        assertThat(response.body().string()).isEqualTo("Sorry not sorry");
        assertThat(server.getRequestCount()).isEqualTo(2);
    }

    private static class AuthenticatedDispatcher extends Dispatcher {
        private final String user;
        private final String password;

        public AuthenticatedDispatcher(String user, String password) {
            this.user = user;
            this.password = password;
        }

        @Override
        public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
            String authorization = request.getHeader("Authorization");
            if (authorization == null || !authorization.equals(Credentials.basic(user, password))) {
                return new MockResponse().setResponseCode(401).setBody("Sorry not sorry");
            }
            return new MockResponse().setResponseCode(200).setBody("Whatever floats your boat");
        }
    }
}
