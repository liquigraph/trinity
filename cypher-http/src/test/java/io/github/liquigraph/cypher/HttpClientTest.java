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
package io.github.liquigraph.cypher;

import io.github.liquigraph.cypher.internal.payload.TransactionDateFormatSupplier;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.LogManager;

import static io.github.liquigraph.cypher.Assertions.assertThat;
import static io.github.liquigraph.cypher.Header.header;
import static io.github.liquigraph.cypher.MockResponses.jsonOkResponse;
import static io.github.liquigraph.cypher.MockResponses.jsonResponse;
import static java.util.Arrays.asList;

public class HttpClientTest {

    private MockWebServer neo4jServer;
    private HttpClient subject;

    @BeforeClass
    public static void prepareAll() {
        LogManager.getLogManager().reset();
    }

    @Before
    public void prepare() throws IOException {
        neo4jServer = new MockWebServer();
        neo4jServer.start();
        subject = new HttpClient(String.format("http://localhost:%d", neo4jServer.getPort()));
    }

    @Test
    public void returns_several_rows_with_several_columns() throws IOException {
        neo4jServer.enqueue(jsonOkResponse("{\"results\": [{\"columns\": [\"item\", \"double\" ], \"data\": [{\"row\": [1, 2 ], \"meta\": [null, null ]}, {\"row\": [2, 4 ], \"meta\": [null, null ]}, {\"row\": [3, 6 ], \"meta\": [null, null ]}]}], \"errors\": []}"));

        Either<List<ResultError>, List<ResultData>> result = subject.runSingleTransaction("UNWIND [1,2,3] AS item RETURN item, 2*item AS double");

        assertThat(result).isRight();
        List<ResultData> resultData = result.getRight();
        assertThat(resultData).hasSize(1);
        ResultData firstResultData = resultData.iterator().next();
        assertThat(firstResultData.getColumns()).containsExactly("item", "double");
        assertThat(firstResultData.getRows())
                .hasSize(3)
                .containsExactly(
                        row(value("item", 1.0), value("double", 2.0)),
                        row(value("item", 2.0), value("double", 4.0)),
                        row(value("item", 3.0), value("double", 6.0))
                );
    }

    @Test
    public void returns_several_rows_from_several_statements_in_one_request() {
        neo4jServer.enqueue(jsonOkResponse("{\"results\": [" +
                "{\"columns\": [\"item\"], \"data\": [{\"row\": [1]}, {\"row\": [2]}, {\"row\": [3]}]}, " +
                "{\"columns\": [\"item*2\", \"item*3\"], \"data\": [{\"row\": [2, 3]}, {\"row\": [4, 6]}, {\"row\": [6, 9]}]}], \"errors\": []}"));

        Either<List<ResultError>, List<ResultData>> result = subject.runSingleTransaction(
                "UNWIND [1,2,3] AS item RETURN item",
                "UNWIND [1,2,3] AS item RETURN item*2,item*3");

        assertThat(result).isRight();
        List<ResultData> resultData = result.getRight();
        assertThat(resultData).hasSize(2);
        Iterator<ResultData> iterator = resultData.iterator();
        ResultData firstResultData = iterator.next();
        assertThat(firstResultData.getColumns()).containsExactly("item");
        assertThat(firstResultData.getRows()).containsExactly(
                row(value("item", 1.0)),
                row(value("item", 2.0)),
                row(value("item", 3.0))
        );
        ResultData secondResults = iterator.next();
        assertThat(secondResults.getColumns()).containsExactly("item*2", "item*3");
        assertThat(secondResults.getRows()).containsExactly(
                row(value("item*2", 2.0), value("item*3", 3.0)),
                row(value("item*2", 4.0), value("item*3", 6.0)),
                row(value("item*2", 6.0), value("item*3", 9.0))
        );
    }

    @Test
    public void explicitly_opens_one_transaction_and_commits_it_in_another_request() {
        neo4jServer.enqueue(jsonOkResponse("{\"commit\": \"http://localhost:7474/db/data/transaction/1/commit\", \"results\": [], \"transaction\": { \"expires\": \"Sun, 30 Jul 2017 14:45:11 +0000\" }, \"errors\": []}", header("Location", "http://localhost:7474/db/data/transaction/1")));

        Either<List<ResultError>, OngoingTransaction> result = subject.openTransaction();

        assertThat(result).isRight();
        OngoingTransaction transaction = result.getRight();
        long expiry = transaction.getExpiry();
        assertThat(expiry).isEqualTo(1501425911000L);
        assertThat(TransactionDateFormatSupplier.get().format(new Date(expiry))).isEqualTo("Sun, 30 Jul 2017 14:45:11 +0000");
        assertThat(transaction.getLocation()).isEqualTo("http://localhost:7474/db/data/transaction/1");
        assertThat(transaction.getCommitLocation()).isEqualTo("http://localhost:7474/db/data/transaction/1/commit");

        neo4jServer.enqueue(jsonOkResponse("{\"results\": [], \"errors\": []}"));
        subject.commit(transaction);
    }

    @Test
    public void spans_statements_over_several_requests() {
        neo4jServer.enqueue(jsonOkResponse(String.format("{\"commit\": \"http://localhost:%d/db/data/transaction/1/commit\",\n" +
                " \"results\": [\n" +
                "   {\"columns\": [\"item\"], \"data\": [{\"row\": [1]}, {\"row\": [2]}, {\"row\": [3]}]}, \n" +
                "   {\"columns\": [\"item*2\", \"item*3\"], \"data\": [{\"row\": [2, 3]}, {\"row\": [4, 6]}, {\"row\": [6, 9]}]}\n" +
                " ],\n" +
                " \"transaction\": { \"expires\": \"Sun, 30 Jul 2017 14:45:11 +0000\" },\n" +
                " \"errors\": []}", neo4jServer.getPort()), header("Location", String.format("http://localhost:%d/db/data/transaction/1", neo4jServer.getPort()))));
        OngoingTransaction transaction = subject.openTransaction(
                "UNWIND [1,2,3] AS item RETURN item",
                "UNWIND [1,2,3] AS item RETURN item*2,item*3"
        ).getRight();
        neo4jServer.enqueue(jsonOkResponse("{\"results\":[{\"columns\":[\"item\"],\"data\":[{\"row\":[1]}]},{\"columns\":[\"item\"],\"data\":[{\"row\":[3]},{\"row\":[2]}]}],\"errors\":[]}"));

        Either<List<ResultError>, List<ResultData>> result = subject.commit(
                transaction,
                "UNWIND [1,2,3] AS item RETURN item LIMIT 1",
                "UNWIND [1,2,3] AS item RETURN item ORDER BY item DESC LIMIT 2"
        );

        assertThat(result).isRight();
        List<ResultData> resultData = result.getRight();
        assertThat(resultData).hasSize(2);
        Iterator<ResultData> iterator = resultData.iterator();
        ResultData first = iterator.next();
        assertThat(first.getColumns()).containsExactly("item");
        assertThat(first.getRows()).containsExactly(row(value("item", 1.0)));
        ResultData second = iterator.next();
        assertThat(second.getColumns()).containsExactly("item");
        assertThat(second.getRows()).containsExactly(row(value("item", 3.0)), row(value("item", 2.0)));
    }

    @Test
    public void supports_rollback() {
        neo4jServer.enqueue(jsonOkResponse("{" +
                "\"commit\": \"http://localhost:7474/db/data/transaction/1/commit\"," +
                "\"results\": [{" +
                "\"columns\": [\"item\"], \"data\": [{\"row\": [1]}]" +
                "}]," +
                "\"transaction\": {\"expires\": \"Sun, 06 Aug 2017 12:54:03 +0000\"}," +
                "\"errors\": []}", header("Location", String.format("http://localhost:%d/db/data/transaction/1", neo4jServer.getPort()))));
        OngoingTransaction transaction = subject.openTransaction("UNWIND [1,2,3] AS item RETURN item LIMIT 1").getRight();
        neo4jServer.enqueue(jsonOkResponse("{\"results\": [], \"errors\": []}"));

        Either<List<ResultError>, ClosedTransaction> closedTransaction = subject.rollback(transaction);

        assertThat(closedTransaction).isRight();
    }

    @Test
    public void rejects_unauthenticated_queries() {
        neo4jServer.enqueue(
                jsonResponse("{\n" +
                        "  \"errors\": [\n" +
                        "    {\n" +
                        "      \"code\": \"Neo.ClientError.Security.Unauthorized\",\n" +
                        "      \"message\": \"No authentication header supplied.\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}")
                        .setResponseCode(401));

        Either<List<ResultError>, List<ResultData>> result = subject.runSingleTransaction("RETURN 42");

        assertThat(result).isLeft();
        assertThat(result.getLeft()).containsOnly(new ResultError("Neo.ClientError.Security.Unauthorized", "No authentication header supplied."));
    }


    private Row row(Value... values) {
        return new Row(asList(values));
    }

    private Value value(String item, Object value) {
        return new Value(item, value);
    }

}
