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

import static org.assertj.core.api.Assertions.assertThat;
import static org.liquigraph.trinity.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.liquigraph.trinity.ClosedTransaction;
import org.liquigraph.trinity.Data;
import org.liquigraph.trinity.Either;
import org.liquigraph.trinity.Fault;
import org.liquigraph.trinity.Row;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.neo4j.driver.v1.AccessMode;
import org.neo4j.driver.v1.Config;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.harness.junit.Neo4jRule;

public class BoltClientTest {

   @Rule
   public Neo4jRule neo4j = new Neo4jRule();
   private Driver driver;
   private BoltClient subject;

   @BeforeClass
   public static void prepareAll() {
      SLF4JBridgeHandler.removeHandlersForRootLogger();
      SLF4JBridgeHandler.install();
   }

   @Before
   public void prepare() {
      driver = GraphDatabase.driver(neo4j.boltURI(), Config.build().withoutEncryption().toConfig());
      subject = new BoltClient(driver);
   }

   @After
   public void cleanUp() {
      subject.close();
   }

   @Test
   public void executes_query_in_one_transaction() {
      String query = "CREATE (c:Crew {name:'Liquigraph crew'}) RETURN c.name";

      Either<List<Fault>, List<Data>> result = subject.runSingleTransaction(query);

      assertThat(result).isRight();
      assertThat(result.getRight())
            .containsExactly(new Data("c.name", new Row("c.name", "Liquigraph crew")));
      try (Session session = driver.session(AccessMode.READ)) {
         StatementResult queryResult = session.run("MATCH (n:Crew) RETURN n.name AS crewName");
         assertThat(queryResult.single().get("crewName").asString()).isEqualTo("Liquigraph crew");
      }
   }

   @Test
   public void handles_failing_query() {
      String query = "Not a valid query";

      Either<List<Fault>, List<Data>> result = subject.runSingleTransaction(query);

      assertThat(result).isLeft();
      assertThat(result.getLeft()).containsExactly(new Fault(
            "Neo.ClientError.Statement.SyntaxError",
            "Invalid input 'N': expected <init> (line 1, column 1 (offset: 0))\n" +
                  "\"Not a valid query\"\n" +
                  " ^"));
   }

   @Test
   public void opens_transaction() {
      String query = "MATCH (n) RETURN COUNT(n) AS count";

      Either<List<Fault>, BoltTransaction> result = subject.openTransaction(query);

      assertThat(result).isRight();
      assertThat(result.getRight().getData())
            .containsExactly(new Data("count", new Row("count", 0L)));
   }

   @Test
   public void executes_in_transaction() {
      try (Session session = driver.session();
           Transaction transaction = session.beginTransaction()) {

         BoltTransaction openTransaction = new BoltTransaction(session, transaction, Collections.emptyList());

         Either<List<Fault>, BoltTransaction> result = subject.execute(openTransaction, "MATCH (n) RETURN COUNT(n) AS count");

         assertThat(result).isRight();
         BoltTransaction boltTransaction = result.getRight();
         assertThat(boltTransaction.getTransaction()).isEqualTo(transaction);
         assertThat(boltTransaction.getData())
               .containsExactly(new Data("count", new Row("count", 0L)));
      }
   }

   @Test
   public void commits_transaction() {
      Session session = driver.session();
      Transaction transaction = session.beginTransaction();

      BoltTransaction openTransaction = new BoltTransaction(session, transaction, Collections.emptyList());

      Either<List<Fault>, ClosedTransaction> result = subject.commit(openTransaction, "MATCH (n) RETURN COUNT(n) AS count");

      assertThat(result).isRight();
      ClosedTransaction closedTransaction = result.getRight();
      assertThat(closedTransaction.isRolledBack())
            .overridingErrorMessage("Committed transaction shouldn't be rolled back")
            .isFalse();
      assertThat(closedTransaction.getData())
            .containsExactly(new Data("count", new Row("count", 0L)));
   }

   @Test
   public void rolls_back_transaction() {
      Session session = driver.session();
      Transaction transaction = session.beginTransaction();

      BoltTransaction openTransaction = new BoltTransaction(session, transaction, Collections.emptyList());

      Either<List<Fault>, ClosedTransaction> result = subject.rollback(openTransaction);

      assertThat(result).isRight();
      ClosedTransaction closedTransaction = result.getRight();
      assertThat(closedTransaction.isRolledBack())
            .overridingErrorMessage("Rolled back transaction should be... rolled back")
            .isTrue();
      assertThat(closedTransaction.getData()).isEmpty();
   }
}