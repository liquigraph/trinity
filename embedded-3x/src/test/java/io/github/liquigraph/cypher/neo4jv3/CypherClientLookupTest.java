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
package io.github.liquigraph.cypher.neo4jv3;

import io.github.liquigraph.cypher.CypherClient;
import io.github.liquigraph.cypher.CypherClientLookup;
import io.github.liquigraph.cypher.CypherTransport;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

public class CypherClientLookupTest {

    @Rule public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void loads_client() throws Exception {
        CypherClientLookup lookup = new CypherClientLookup();

        CypherClient client = lookup.getInstance(CypherTransport.EMBEDDED_3, configuration());

        assertThat(client).isInstanceOf(EmbeddedClient.class);
    }

    private Properties configuration() throws Exception {
        Properties props = new Properties();
        try (ByteArrayInputStream stream = new ByteArrayInputStream(rawConfig().getBytes("UTF-8"))) {
            props.load(stream);
        }
        return props;
    }

    private String rawConfig() throws IOException {
        return String.format("cypher.embeddedv3.path=%s", folder.newFolder().getPath());
    }
}
