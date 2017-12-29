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
package org.liquigraph.trinity.neo4jv2;

import org.liquigraph.trinity.ClosedTransaction;
import org.liquigraph.trinity.CypherClient;
import org.liquigraph.trinity.Data;
import org.liquigraph.trinity.DefaultEither;
import org.liquigraph.trinity.Either;
import org.liquigraph.trinity.Fault;
import org.liquigraph.trinity.Row;
import org.neo4j.cypher.CypherException;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class EmbeddedClient implements CypherClient<OngoingLocalTransaction> {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmbeddedClient.class);

    private final GraphDatabaseService graphDatabase;
    private final ExecutionEngine cypherExecutor;

    public EmbeddedClient(GraphDatabaseService graphDatabase) {
        this.graphDatabase = graphDatabase;
        this.cypherExecutor = new ExecutionEngine(graphDatabase);
    }

    @Override
    public Either<List<Fault>, List<Data>> runSingleTransaction(String query, String... queries) {
        int queryCount = 1 + queries.length;
        LOGGER.debug("About to run {} queries in a single transaction", queryCount);
        try (Transaction transaction = graphDatabase.beginTx()) {
            List<Fault> errors = new ArrayList<>(queryCount);
            List<Data> data = new ArrayList<>(queryCount);
            addExecutionResult(execute(query), errors, data);
            for (String nextQuery : queries) {
                addExecutionResult(execute(nextQuery), errors, data);
            }
            if (!errors.isEmpty()) {
                LOGGER.warn("Rolling back and closing the transaction after encountering {} errors on {} queries", errors.size(), queries.length);
                transaction.failure();
                return DefaultEither.left(errors);
            }
            transaction.success();
            return DefaultEither.right(data);
        }
    }

    @Override
    public Either<List<Fault>, OngoingLocalTransaction> openTransaction(String... queries) {
        LOGGER.debug("About to open a transaction and run {} queries", queries.length);
        return executeQueriesInTransaction(graphDatabase.beginTx(), queries);
    }

    @Override
    public Either<List<Fault>, OngoingLocalTransaction> execute(OngoingLocalTransaction transaction, String... queries) {
        LOGGER.debug("About to run {} queries in currently open transaction", queries.length);
        return executeQueriesInTransaction(transaction.getLocalTransaction(), queries);
    }

    @Override
    public Either<List<Fault>, ClosedTransaction> commit(OngoingLocalTransaction transaction, String... queries) {
        LOGGER.debug("About to run {} queries and commit open transaction", queries.length);
        try (Transaction localTransaction = transaction.getLocalTransaction()) {
            List<Fault> errors = new ArrayList<>(queries.length);
            List<Data> data = new ArrayList<>(queries.length);
            for (String nextQuery : queries) {
                addExecutionResult(execute(nextQuery), errors, data);
            }
            if (!errors.isEmpty()) {
                LOGGER.warn("Rolling back and closing the transaction after encountering {} errors on {} queries", errors.size(), queries.length);
                localTransaction.failure();
                return DefaultEither.left(errors);
            }
            localTransaction.success();
            return DefaultEither.right(new ClosedTransaction(data, false));
        }
    }

    @Override
    public Either<List<Fault>, ClosedTransaction> rollback(OngoingLocalTransaction transaction) {
        LOGGER.debug("About to roll back open transaction");
        try (Transaction localTransaction = transaction.getLocalTransaction()) {
            localTransaction.failure();
            return DefaultEither.right(ClosedTransaction.ROLLED_BACK);
        }
    }

    private Either<List<Fault>, OngoingLocalTransaction> executeQueriesInTransaction(Transaction localTransaction,
                                                                                     String[] queries) {

        List<Fault> errors = new ArrayList<>(queries.length);
        List<Data> data = new ArrayList<>(queries.length);
        for (String nextQuery : queries) {
            addExecutionResult(execute(nextQuery), errors, data);
        }

        if (!errors.isEmpty()) {
            LOGGER.warn("Rolling back and closing the transaction after encountering {} errors on {} queries", errors.size(), queries.length);
            localTransaction.failure();
            localTransaction.close();
            return DefaultEither.left(errors);
        }
        return DefaultEither.right(new OngoingLocalTransaction(localTransaction, data));
    }

    private Either<Fault, Data> execute(String query) {
        try {
            ExecutionResult executionResult = cypherExecutor.execute(query);
            try (ResourceIterator<Map<String, Object>> resultIterator = executionResult.iterator()) {
                List<Row> rows = new ArrayList<>();
                while (resultIterator.hasNext()) {
                    rows.add(new Row(asJavaMap(resultIterator.next())));
                }
                return DefaultEither.right(new Data(executionResult.columns(), rows));
            }
        }
        catch (CypherException exception) {
            LOGGER.error("An unexpected error happened while executing the Cypher query", exception);
            return DefaultEither.left(CypherExceptionConverter.INSTANCE.convert(exception));
        }
    }

    private void addExecutionResult(Either<Fault, Data> execution, List<Fault> errors, List<Data> result) {
        if (execution.isLeft()) {
            Fault fault = execution.getLeft();
            LOGGER.error("Encountered error after execution: {}", fault);
            errors.add(fault);
        }
        else {
            result.add(execution.getRight());
        }
    }

    /**
     * This re-creates a Map from scratch
     *
     * <code>org.neo4j.cypher.javacompat.ExecutionEngine</code> returns a
     * <code>scala.collection.convert.Wrappers$MapWrapper</code> which is
     * sadly not equal to a j.u.HashMap.
     */
    private Map<String, Object> asJavaMap(Map<String, Object> row) {
        Map<String, Object> result = new HashMap<>((int) Math.ceil(row.size() / 0.75));
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
}
