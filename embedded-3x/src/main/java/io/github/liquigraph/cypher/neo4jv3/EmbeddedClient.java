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

import io.github.liquigraph.cypher.ClosedTransaction;
import io.github.liquigraph.cypher.CypherClient;
import io.github.liquigraph.cypher.ResultData;
import io.github.liquigraph.cypher.ResultError;
import io.github.liquigraph.cypher.Row;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.QueryExecutionException;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.github.liquigraph.cypher.neo4jv3.internal.Streams.createStream;
import static java.util.Arrays.stream;

public final class EmbeddedClient implements CypherClient<OngoingLocalTransaction> {

    private final GraphDatabaseService graphDatabase;

    public EmbeddedClient(GraphDatabaseService graphDatabase) {
        this.graphDatabase = graphDatabase;
    }

    @Override
    public FunctionalEither<List<ResultError>, List<ResultData>> runSingleTransaction(String query, String... queries) {
        try (Transaction transaction = graphDatabase.beginTx()) {
            return map(
                executeAll(query, queries),
                transaction,
                Transaction::failure,
                Transaction::success,
                Function.identity()
            );
        }
    }

    @Override
    public FunctionalEither<List<ResultError>, OngoingLocalTransaction> openTransaction(String... queries) {
        return executeInTransaction(graphDatabase.beginTx(), queries);
    }

    @Override
    public FunctionalEither<List<ResultError>, OngoingLocalTransaction> execute(OngoingLocalTransaction ongoingTransaction, String... queries) {
        return executeInTransaction(ongoingTransaction.getTransaction(), queries);
    }

    @Override
    public FunctionalEither<List<ResultError>, ClosedTransaction> commit(OngoingLocalTransaction transaction, String... queries) {
        return map(
            executeAll(stream(queries)),
            transaction.getTransaction(),
            this::rollbackAndClose,
            (tx) -> {},
            (data) -> new ClosedTransaction(data, false)
        );
    }

    @Override
    public FunctionalEither<List<ResultError>, ClosedTransaction> rollback(OngoingLocalTransaction transaction) {
        return map(
            Collections.emptyList(),
            transaction.getTransaction(),
            this::rollbackAndClose,
            this::rollbackAndClose,
            (data) -> new ClosedTransaction(data, true)
        );
    }

    private FunctionalEither<List<ResultError>, OngoingLocalTransaction> executeInTransaction(Transaction transaction, String[] queries) {
        return map(
            executeAll(stream(queries)),
            transaction,
            this::rollbackAndClose,
            (tx) -> {},
            (data) -> new OngoingLocalTransaction(transaction, data)
        );
    }

    private <T> FunctionalEither<List<ResultError>, T> map(List<FunctionalEither<ResultError, Result>> results,
                                                        Transaction transaction,
                                                        Consumer<Transaction> onError,
                                                        Consumer<Transaction> onSuccess,
                                                        Function<List<ResultData>, T> resultMapper) {

        List<ResultError> errors = collectErrors(results);
        if (!errors.isEmpty()) {
            onError.accept(transaction);
            return FunctionalEither.left(errors);
        }

        onSuccess.accept(transaction);
        return FunctionalEither.right(this.collectResults(results, resultMapper));
    }

    private List<ResultError> collectErrors(List<FunctionalEither<ResultError, Result>> rawResults) {
        return rawResults.stream()
            .filter(FunctionalEither::isLeft)
            .map(FunctionalEither::getLeft)
            .collect(Collectors.toList());
    }

    private <T> T collectResults(List<FunctionalEither<ResultError, Result>> rawResults, Function<List<ResultData>, T> resultExtractor) {
        return resultExtractor.apply(
            rawResults.stream()
                .map(FunctionalEither::getRight)
                .map(this::asResultData)
                .collect(Collectors.toList()));
    }

    private List<FunctionalEither<ResultError, Result>> executeAll(String query, String[] queries) {
        Stream<String> allQueries = Stream.concat(Stream.of(query), Arrays.stream(queries));
        return executeAll(allQueries);
    }

    private List<FunctionalEither<ResultError, Result>> executeAll(Stream<String> stream) {
        return stream.map(this::execute).collect(Collectors.toList());
    }

    private FunctionalEither<ResultError, Result> execute(String query) {
        try {
            return FunctionalEither.right(graphDatabase.execute(query));
        }
        catch (QueryExecutionException exception) {
            return FunctionalEither.left(new ResultError(exception.getStatusCode(), exception.getMessage()));
        }
    }

    private ResultData asResultData(Result result) {
        return new ResultData(result.columns(), rowsOf(result));
    }

    private List<Row> rowsOf(Result result) {
        return createStream(result, Spliterator.ORDERED)
            .map(Row::new)
            .collect(Collectors.toList());
    }

    private void rollbackAndClose(Transaction tx) {
        tx.failure();
        tx.close();
    }

}
