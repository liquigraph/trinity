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
import io.github.liquigraph.cypher.CypherClient;
import io.github.liquigraph.cypher.DefaultEither;
import io.github.liquigraph.cypher.Either;
import io.github.liquigraph.cypher.ResultData;
import io.github.liquigraph.cypher.ResultError;
import io.github.liquigraph.cypher.Row;
import org.neo4j.cypher.CypherException;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class EmbeddedClient implements CypherClient<OngoingLocalTransaction> {

    private final GraphDatabaseService graphDatabase;
    private final ExecutionEngine cypherExecutor;

    public EmbeddedClient(GraphDatabaseService graphDatabase) {
        this.graphDatabase = graphDatabase;
        cypherExecutor = new ExecutionEngine(graphDatabase);
    }

    @Override
    public Either<List<ResultError>, List<ResultData>> runSingleTransaction(String query, String... queries) {
        try (Transaction transaction = graphDatabase.beginTx()) {
            List<ResultError> errors = new ArrayList<>(1 + queries.length);
            List<ResultData> data = new ArrayList<>(1 + queries.length);
            addExecutionResult(execute(query), errors, data);
            for (String nextQuery : queries) {
                addExecutionResult(execute(nextQuery), errors, data);
            }
            if (!errors.isEmpty()) {
                transaction.failure();
                return DefaultEither.left(errors);
            }
            transaction.success();
            return DefaultEither.right(data);
        }
    }

    @Override
    public Either<List<ResultError>, OngoingLocalTransaction> openTransaction(String... queries) {
        return executeQueriesInTransaction(graphDatabase.beginTx(), queries);
    }

    @Override
    public Either<List<ResultError>, OngoingLocalTransaction> execute(OngoingLocalTransaction transaction, String... queries) {
        return executeQueriesInTransaction(transaction.getLocalTransaction(), queries);
    }

    @Override
    public Either<List<ResultError>, ClosedTransaction> commit(OngoingLocalTransaction transaction, String... queries) {
        try (Transaction localTransaction = transaction.getLocalTransaction()) {
            List<ResultError> errors = new ArrayList<>(queries.length);
            List<ResultData> data = new ArrayList<>(queries.length);
            for (String nextQuery : queries) {
                addExecutionResult(execute(nextQuery), errors, data);
            }
            if (!errors.isEmpty()) {
                localTransaction.failure();
                return DefaultEither.left(errors);
            }
            localTransaction.success();
            return DefaultEither.right(new ClosedTransaction(data, false));
        }
    }

    @Override
    public Either<List<ResultError>, ClosedTransaction> rollback(OngoingLocalTransaction transaction) {
        try (Transaction localTransaction = transaction.getLocalTransaction()) {
            localTransaction.failure();
            return DefaultEither.right(ClosedTransaction.ROLLED_BACK);
        }
    }

    private Either<List<ResultError>, OngoingLocalTransaction> executeQueriesInTransaction(Transaction localTransaction,
                                                                                           String[] queries) {

        List<ResultError> errors = new ArrayList<>(queries.length);
        List<ResultData> data = new ArrayList<>(queries.length);
        for (String nextQuery : queries) {
            addExecutionResult(execute(nextQuery), errors, data);
        }

        if (!errors.isEmpty()) {
            localTransaction.failure();
            localTransaction.close();
            return DefaultEither.left(errors);
        }
        return DefaultEither.right(new OngoingLocalTransaction(localTransaction, data));
    }

    private Either<ResultError, ResultData> execute(String query) {
        try {
            ExecutionResult executionResult = cypherExecutor.execute(query);
            try (ResourceIterator<Map<String, Object>> resultIterator = executionResult.iterator()) {
                List<Row> rows = new ArrayList<>();
                while (resultIterator.hasNext()) {
                    rows.add(new Row(asJavaMap(resultIterator.next())));
                }
                return DefaultEither.right(new ResultData(executionResult.columns(), rows));
            }
        }
        catch (CypherException exception) {
            return DefaultEither.left(CypherExceptionConverter.INSTANCE.convert(exception));
        }
    }

    private void addExecutionResult(Either<ResultError, ResultData> execution, List<ResultError> errors, List<ResultData> result) {
        if (execution.isLeft()) {
            errors.add(execution.getLeft());
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
