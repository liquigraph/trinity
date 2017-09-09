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

import org.liquigraph.trinity.CypherClient;
import org.liquigraph.trinity.CypherClientLookup;
import org.liquigraph.trinity.CypherTransport;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.Charset;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

public class CypherClientLookupTest {

    @Test
    public void loads_client() throws Exception {
        CypherClientLookup lookup = new CypherClientLookup();

        CypherClient client = lookup.getInstance(CypherTransport.HTTP, configuration());

        assertThat(client).isInstanceOf(HttpClient.class);
    }

    private Properties configuration() throws Exception {
        Properties props = new Properties();
        props.setProperty("cypher.http.baseurl", String.format("http://localhost:%d", availablePort()));
        return props;
    }

    private int availablePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }
}
