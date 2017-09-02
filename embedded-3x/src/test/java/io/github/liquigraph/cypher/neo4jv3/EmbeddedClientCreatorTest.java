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

import io.github.liquigraph.cypher.Data;
import io.github.liquigraph.cypher.Either;
import io.github.liquigraph.cypher.Fault;
import io.github.liquigraph.cypher.Row;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.util.List;
import java.util.Properties;

import static io.github.liquigraph.cypher.Assertions.assertThat;

public class EmbeddedClientCreatorTest {

    @Rule public TemporaryFolder folder = new TemporaryFolder();

    private final EmbeddedClientCreator subject = new EmbeddedClientCreator();

    @Test
    public void creates_embedded_clients_with_custom_configuration() throws Exception {
        Properties props = new Properties();
        props.setProperty("cypher.embeddedv3.path", folder.newFolder().getPath());
        props.setProperty("dbms.auto_index.nodes.enabled", "true");
        props.setProperty("dbms.auto_index.nodes.keys", "name");
        EmbeddedClient embeddedClient = subject.create(props);

        Either<List<Fault>, List<Data>> result =
            embeddedClient.runSingleTransaction(
                "MATCH (n) RETURN COUNT(n) AS result");

        assertThat(result).isRight();
        assertThat(result.getRight()).containsExactly(
            new Data("result", new Row("result", 0L))
        );
    }
}
