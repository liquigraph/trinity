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
package io.github.liquigraph.cypher.neo4jv2;

import io.github.liquigraph.cypher.ClosedTransaction;
import io.github.liquigraph.cypher.Either;
import io.github.liquigraph.cypher.Neo4jVersionDetector;
import io.github.liquigraph.cypher.ResultData;
import io.github.liquigraph.cypher.ResultError;
import io.github.liquigraph.cypher.Row;
import io.github.liquigraph.cypher.SemanticVersion;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.test.TestGraphDatabaseFactory;
import org.neo4j.tooling.GlobalGraphOperations;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.github.liquigraph.cypher.Assertions.assertThat;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public class EmbeddedClientTest {

    private static final SemanticVersion NEO4J_200 = SemanticVersion.parse("2.0.0");
    private static final SemanticVersion NEO4J_201 = SemanticVersion.parse("2.0.1");
    private static final SemanticVersion NEO4J_202 = SemanticVersion.parse("2.0.2");
    private static final SemanticVersion NEO4J_220 = SemanticVersion.parse("2.2.0");
    private static final SemanticVersion NEO4J_230 = SemanticVersion.parse("2.3.0");
    private static final SemanticVersion NEO4J_300 = SemanticVersion.parse("3.0.0");
    private static final SemanticVersion CURRENT_VERSION =
        Neo4jVersionDetector.detect(EmbeddedClientTest.class.getResourceAsStream("/filtered/version.txt"));

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private EmbeddedClient subject;
    private GraphDatabaseService graphDatabase;

    @Before
    public void prepare() throws IOException {
        if (CURRENT_VERSION.compareTo(NEO4J_200) < 0 || CURRENT_VERSION.compareTo(NEO4J_300) >= 0) {
            Assertions.fail("%s should a Neo4j 2.x.y stable version");
        }
        graphDatabase = graphDatabase(
            this.folder.newFolder("cypher-embedded-2x"),
            CURRENT_VERSION);
        subject = new EmbeddedClient(graphDatabase);
    }

    @Test
    public void runs_statements_in_single_transaction() {
        Either<List<ResultError>, List<ResultData>> result = subject.runSingleTransaction(
            "MATCH (n) RETURN COUNT(n)",
            "RETURN 42 AS `Réponse à La Grande Question sur la vie, l'univers et le reste`");

        assertThat(result).isRight();
        List<ResultData> data = result.getRight();
        assertThat(data)
            .containsExactly(
                new ResultData(
                    singletonList("COUNT(n)"),
                    singletonList(
                        singleColumnRow("COUNT(n)", 0L))),
                new ResultData(
                    singletonList("Réponse à La Grande Question sur la vie, l'univers et le reste"),
                    singletonList(
                        singleColumnRow("Réponse à La Grande Question sur la vie, l'univers et le reste", 42L))));
    }

    @Test
    public void returns_errors_from_invalid_statements() {
        Either<List<ResultError>, List<ResultData>> result = subject.runSingleTransaction(
            "MATCH (n) RETURN COUNT(n)",
            "JEU, SET et MATCH -- oops not a valid query");

        assertThat(result).isLeft();
        List<ResultError> data = result.getLeft();
        assertThat(data)
            .containsExactly(
                new ResultError(errorCode(), errorDescription()));
    }

    @Test
    public void opens_transaction() {
        Either<List<ResultError>, OngoingLocalTransaction> result = subject.openTransaction(
            "RETURN [1,2,3] AS x"
        );

        assertThat(result).isRight();
        OngoingLocalTransaction localTransaction = result.getRight();
        assertThat(localTransaction.getResultData())
            .containsExactly(new ResultData(
                singletonList("x"),
                singletonList(singleColumnRow("x", asList(1L, 2L, 3L))
            )));
    }

    @Test
    public void executes_in_open_transaction() {
        Either<List<ResultError>, OngoingLocalTransaction> openTransaction = subject.openTransaction();
        Either<List<ResultError>, OngoingLocalTransaction> result = subject.execute(openTransaction.getRight(), "RETURN [4,5,6] AS x");

        assertThat(result).isRight();
        OngoingLocalTransaction localTransaction = result.getRight();
        assertThat(localTransaction.getResultData())
            .containsExactly(new ResultData(
                singletonList("x"),
                singletonList(singleColumnRow("x", asList(4L, 5L, 6L))
            )));
    }

    @Test
    public void commits_an_open_transaction() {
        Either<List<ResultError>, OngoingLocalTransaction> openTransaction = subject.openTransaction();
        Either<List<ResultError>, ClosedTransaction> result = subject.commit(
            openTransaction.getRight(),
            "CREATE (n:Foo {type:'Fighter'}) RETURN n.type");

        assertThat(result).isRight();
        ClosedTransaction completedTransaction = result.getRight();
        assertThat(completedTransaction.isRolledBack()).overridingErrorMessage("Transaction must not be rolled back").isFalse();
        assertThat(completedTransaction.getResultData())
            .containsExactly(new ResultData(
                singletonList("n.type"),
                singletonList(singleColumnRow("n.type", "Fighter")
            )));

        try (Transaction ignored = graphDatabase.beginTx()) {
            assertThat(GlobalGraphOperations.at(graphDatabase).getAllNodes())
                .overridingErrorMessage("The node insertion must be committed")
                .hasSize(1);
        }
    }

    @Test
    public void rolls_back_an_open_transaction() {
        Either<List<ResultError>, OngoingLocalTransaction> openTransaction = subject.openTransaction("CREATE (n:Bar {type:'Ry White'}) RETURN n.type");
        Either<List<ResultError>, ClosedTransaction> result = subject.rollback(openTransaction.getRight());

        assertThat(result).isRight();
        ClosedTransaction completedTransaction = result.getRight();
        assertThat(completedTransaction.isRolledBack()).overridingErrorMessage("Transaction must be rolled back").isTrue();
        assertThat(completedTransaction.getResultData()).isEmpty();

        try (Transaction ignored = graphDatabase.beginTx()) {
            assertThat(GlobalGraphOperations.at(graphDatabase).getAllNodes())
                .overridingErrorMessage("The node insertion must be rolled back")
                .isEmpty();
        }
    }

    private String errorCode() {
        if (CURRENT_VERSION.compareTo(NEO4J_202) < 0) {
            return "Neo.PreV202Error.CypherException.Unknown";
        }
        return "Neo.ClientError.Statement.InvalidSyntax";
    }

    private String errorDescription() {
        if (CURRENT_VERSION.compareTo(NEO4J_200) == 0) {
            return
                "Invalid input 'J': expected SingleStatement (line 1, column 1)\n" +
                    "\"JEU, SET et MATCH -- oops not a valid query\"\n" +
                    " ^";
        }
        if (CURRENT_VERSION.compareTo(NEO4J_201) == 0) {
            return
                "Invalid input 'J': expected <init> (line 1, column 1)\n" +
                    "\"JEU, SET et MATCH -- oops not a valid query\"\n" +
                    " ^";
        }
        if (CURRENT_VERSION.compareTo(NEO4J_220) < 0) {
            return "The statement contains invalid or unsupported syntax.\n" +
                "Invalid input 'J': expected <init> (line 1, column 1)\n" +
                "\"JEU, SET et MATCH -- oops not a valid query\"\n" +
                " ^";
        }
        return "The statement contains invalid or unsupported syntax.\n" +
            "Invalid input 'J': expected <init> (line 1, column 1 (offset: 0))\n" +
            "\"JEU, SET et MATCH -- oops not a valid query\"\n" +
            " ^";
    }

    private Row singleColumnRow(String column, Object value) {
        Map<String, Object> map = new HashMap<>((int) Math.ceil(1 / 0.75));
        map.put(column, value);
        return new Row(map);
    }

    private GraphDatabaseService graphDatabase(File folder, SemanticVersion neo4jVersion) {
        TestGraphDatabaseFactory factory = new TestGraphDatabaseFactory();
        return newImpermanentGraphDatabase(folder, factory, neo4jVersion);
    }

    private GraphDatabaseService newImpermanentGraphDatabase(File folder, TestGraphDatabaseFactory factory, SemanticVersion neo4jVersion) {
        try {
            return (GraphDatabaseService) invokeFactoryMethod(folder, factory, neo4jVersion);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private static Object invokeFactoryMethod(File folder, TestGraphDatabaseFactory factory, SemanticVersion neo4jVersion) throws Exception {
        Class<?> factoryClass = factory.getClass();
        if (neo4jVersion.compareTo(NEO4J_230) < 0) {
            return factoryClass.getDeclaredMethod("newImpermanentDatabase", String.class)
                .invoke(factory, folder.getPath());
        }
        return factoryClass.getDeclaredMethod("newImpermanentDatabase", File.class)
            .invoke(factory, folder);
    }

}
