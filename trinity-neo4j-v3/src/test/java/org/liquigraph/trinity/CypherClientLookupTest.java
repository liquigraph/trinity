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
package org.liquigraph.trinity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.liquigraph.trinity.bolt.BoltClient;
import org.liquigraph.trinity.http.HttpClient;
import org.liquigraph.trinity.neo4jv3.EmbeddedClient;
import org.neo4j.harness.junit.Neo4jRule;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

public class CypherClientLookupTest {

    @Rule public Neo4jRule neo4j = new Neo4jRule();
    @Rule public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void looks_up_available_clients() throws Exception {
        CypherClientLookup subject = new CypherClientLookup();

        assertThat(subject.getInstance(
            CypherTransport.EMBEDDED_3,
            $("cypher.embeddedv3.path", folder.newFolder().getPath())
        ).get()).isInstanceOf(EmbeddedClient.class);
        assertThat(subject.getInstance(
            CypherTransport.HTTP,
            $("cypher.http.baseurl", neo4j.httpURI().toString())
        ).get()).isInstanceOf(HttpClient.class);
        assertThat(subject.getInstance(
            CypherTransport.BOLT,
            $("cypher.bolt.baseurl", neo4j.boltURI().toString())
        ).get()).isInstanceOf(BoltClient.class);
    }

    private Properties $(String key, String value) {
        Properties properties = new Properties();
        properties.setProperty(key, value);
        return properties;
    }
}
