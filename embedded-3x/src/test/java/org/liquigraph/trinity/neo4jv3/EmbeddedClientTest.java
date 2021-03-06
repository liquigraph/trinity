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
package org.liquigraph.trinity.neo4jv3;

import org.liquigraph.trinity.ClosedTransaction;
import org.liquigraph.trinity.Data;
import org.liquigraph.trinity.Either;
import org.liquigraph.trinity.Fault;
import org.liquigraph.trinity.Neo4jVersionDetector;
import org.liquigraph.trinity.Row;
import org.liquigraph.trinity.SemanticVersion;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

import java.util.List;

import static org.liquigraph.trinity.Assertions.assertThat;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public class EmbeddedClientTest {

    private static final SemanticVersion CURRENT_NEO4J_VERSION = Neo4jVersionDetector.detect(EmbeddedClientTest.class.getResourceAsStream("/filtered/version.txt"));
    @Rule public final EmbeddedGraphDatabaseRule graphDatabaseRule = new EmbeddedGraphDatabaseRule(CURRENT_NEO4J_VERSION);

    private EmbeddedClient subject;

    @Before
    public void prepare() {
        subject = new EmbeddedClient(graphDatabaseRule.getGraphDatabaseService());
    }

    @Test
    public void executes_statements_in_single_transaction() {
        Either<List<Fault>, List<Data>> result =
            subject.runSingleTransaction("MATCH (n) RETURN COUNT(n)", "CREATE (n:Bolt {name: 'Usain'}) RETURN n.name");

        assertThat(result).isRight();
        List<Data> data = result.getRight();
        assertThat(data)
            .containsExactly(
                new Data("COUNT(n)", new Row("COUNT(n)", 0L)),
                new Data("n.name",   new Row("n.name", "Usain")));
        List<Long> counts = graphDatabaseRule.doInTransaction((tx, db) -> {
            Result executionResult = db.execute("MATCH (n) RETURN count(n) AS count");
            return executionResult
                .columnAs("count")
                .map(Object::toString)
                .map(Long::valueOf)
                .stream().collect(toList());
        });
        assertThat(counts).containsExactly(1L);
    }

    @Test
    public void returns_errors_from_invalid_statements() {
        Either<List<Fault>, List<Data>> result = subject.runSingleTransaction(
            "MATCH (n) RETURN COUNT(n)",
            "JEU, SET et MATCH -- oops not a valid query");

        assertThat(result).isLeft();
        List<Fault> data = result.getLeft();
        assertThat(data)
            .containsExactly(
                new Fault(
                    "Neo.ClientError.Statement.SyntaxError",
                    "Invalid input 'J': expected <init> (line 1, column 1 (offset: 0))\n" +
                        "\"JEU, SET et MATCH -- oops not a valid query\"\n" +
                        " ^"));
    }

    @Test
    public void opens_transaction() {
        Either<List<Fault>, OngoingLocalTransaction> result = subject.openTransaction(
            "RETURN [1,2,3] AS x"
        );

        assertThat(result).isRight();
        OngoingLocalTransaction localTransaction = result.getRight();
        assertThat(localTransaction.getTransaction()).isNotNull();
        assertThat(localTransaction.getData())
            .containsExactly(new Data("x", new Row("x", asList(1L, 2L, 3L))));
    }

    @Test
    public void executes_in_open_transaction() {
        Either<List<Fault>, OngoingLocalTransaction> openTransaction = subject.openTransaction();
        Either<List<Fault>, OngoingLocalTransaction> result = subject.execute(openTransaction.getRight(), "RETURN [4,5,6] AS x");

        assertThat(result).isRight();
        OngoingLocalTransaction localTransaction = result.getRight();
        assertThat(localTransaction.getData())
            .containsExactly(new Data("x", new Row("x", asList(4L, 5L, 6L))));
    }

    @Test
    public void commits_an_open_transaction() {
        Either<List<Fault>, OngoingLocalTransaction> openTransaction = subject.openTransaction();
        Either<List<Fault>, ClosedTransaction> result = subject.commit(
            openTransaction.getRight(),
            "CREATE (n:Foo {type:'Fighter'}) RETURN n.type");

        assertThat(result).isRight();
        ClosedTransaction completedTransaction = result.getRight();
        assertThat(completedTransaction.isRolledBack()).overridingErrorMessage("Transaction must not be rolled back").isFalse();
        assertThat(completedTransaction.getData())
            .containsExactly(new Data("n.type", new Row("n.type", "Fighter")));

        GraphDatabaseService graphDatabase = graphDatabaseRule.getGraphDatabaseService();
        try (Transaction ignored = graphDatabase.beginTx()) {
            assertThat(graphDatabase.getAllNodes())
                .overridingErrorMessage("The node insertion must be committed")
                .hasSize(1);
        }
    }

    @Test
    public void rolls_back_an_open_transaction() {
        Either<List<Fault>, OngoingLocalTransaction> openTransaction = subject.openTransaction("CREATE (n:Bar {type:'Ry White'}) RETURN n.type");
        Either<List<Fault>, ClosedTransaction> result = subject.rollback(openTransaction.getRight());

        assertThat(result).isRight();
        ClosedTransaction completedTransaction = result.getRight();
        assertThat(completedTransaction.isRolledBack()).overridingErrorMessage("Transaction must be rolled back").isTrue();
        assertThat(completedTransaction.getData()).isEmpty();

        GraphDatabaseService graphDatabase = graphDatabaseRule.getGraphDatabaseService();
        try (Transaction ignored = graphDatabase.beginTx()) {
            assertThat(graphDatabase.getAllNodes())
                .overridingErrorMessage("The node insertion must be rolled back")
                .isEmpty();
        }
    }

}
