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
package org.liquigraph.trinity.bolt;

import org.junit.Rule;
import org.junit.Test;
import org.liquigraph.trinity.CypherTransport;
import org.liquigraph.trinity.Data;
import org.liquigraph.trinity.Either;
import org.liquigraph.trinity.Fault;
import org.liquigraph.trinity.Row;
import org.neo4j.harness.junit.Neo4jRule;

import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.liquigraph.trinity.Assertions.assertThat;
import static org.liquigraph.trinity.bolt.BoltProperties.AUTHENTICATION_TYPE;
import static org.liquigraph.trinity.bolt.BoltProperties.BASE_URL;

public class BoltClientCreatorTest {

   @Rule
   public Neo4jRule neo4j = new Neo4jRule();

   private final BoltClientCreator boltClientCreator = new BoltClientCreator();


   @Test
   public void supports_bolt_protocol() {
      assertThat(boltClientCreator.supports(CypherTransport.BOLT)).isTrue();
      assertThat(boltClientCreator.supports(CypherTransport.EMBEDDED_2)).isFalse();
      assertThat(boltClientCreator.supports(CypherTransport.EMBEDDED_3)).isFalse();
      assertThat(boltClientCreator.supports(CypherTransport.HTTP)).isFalse();
   }

   @Test
   public void creates_bolt_clients_with_custom_configuration_no_authentication() {
      Properties props = new Properties();
      props.setProperty(AUTHENTICATION_TYPE, "none");
      props.setProperty(BASE_URL, neo4j.boltURI().toString());
      BoltClient boltClient = boltClientCreator.create(props);

      Either<List<Fault>, List<Data>> result = boltClient.runSingleTransaction("RETURN 0*2 AS result");

      assertThat(result).isRight();
      assertThat(result.getRight()).containsExactly(
            new Data("result", new Row("result", 0L))
      );
   }
}
