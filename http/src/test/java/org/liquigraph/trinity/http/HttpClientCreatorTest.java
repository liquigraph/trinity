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

import org.liquigraph.trinity.Data;
import org.liquigraph.trinity.Either;
import org.liquigraph.trinity.Fault;
import org.liquigraph.trinity.Row;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.util.List;
import java.util.Properties;

import static org.liquigraph.trinity.Assertions.assertThat;

public class HttpClientCreatorTest {

    @Rule public TemporaryFolder folder = new TemporaryFolder();

    private final HttpClientCreator subject = new HttpClientCreator();

    @Before
    public void prepare() {
        Assume.assumeTrue(
            "Neo4j remote instance is provisioned with Docker",
            "true".equals(System.getenv("WITH_DOCKER"))
        );
    }

    @Test
    public void creates_embedded_clients_with_custom_configuration() throws Exception {
        Properties props = new Properties();
        props.setProperty("cypher.http.baseurl", "http://localhost:7474");
        props.setProperty("cypher.http.username", "neo4j");
        props.setProperty("cypher.http.password", "j4oen");
        HttpClient httpClient = subject.create(props);

        Either<List<Fault>, List<Data>> result =
            httpClient.runSingleTransaction("MATCH (s:Nope)--(n) RETURN COUNT(n) AS result");

        assertThat(result).isRight();
        assertThat(result.getRight()).containsExactly(
            new Data("result", new Row("result", 0.0))
        );
    }
}
