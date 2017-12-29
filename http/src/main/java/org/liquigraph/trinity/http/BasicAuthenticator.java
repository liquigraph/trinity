/*
 * Copyright 2018 the original author or authors.
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
