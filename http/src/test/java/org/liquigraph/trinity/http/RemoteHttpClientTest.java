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

import okhttp3.OkHttpClient;
import org.assertj.core.data.MapEntry;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.liquigraph.trinity.ClosedTransaction;
import org.liquigraph.trinity.CypherClient;
import org.liquigraph.trinity.Data;
import org.liquigraph.trinity.Either;
import org.liquigraph.trinity.Fault;
import org.liquigraph.trinity.Row;
import org.liquigraph.trinity.http.internal.payload.TransactionDateFormatSupplier;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.LogManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;
import static org.liquigraph.trinity.Assertions.assertThat;

public class RemoteHttpClientTest {

    private CypherClient<OngoingRemoteTransaction> subject;

    @BeforeClass
    public static void prepareAll() {
        Assume.assumeTrue(
            "Neo4j remote instance is provisioned with Docker",
            "true".equals(System.getenv("WITH_DOCKER"))
        );
        LogManager.getLogManager().reset();
    }

    @Before
    public void prepare() throws IOException {
        subject = new HttpClient("http://localhost:7474", okHttpClient("neo4j", "j4oen"));
    }

    @Test
    public void returns_several_rows_with_several_columns() throws IOException {
        Either<List<Fault>, List<Data>> result = subject.runSingleTransaction("UNWIND [1,2,3] AS item RETURN item, 2*item AS double");

        assertThat(result).isRight();
        List<Data> data = result.getRight();
        assertThat(data).hasSize(1);
        Data firstResultData = data.iterator().next();
        assertThat(firstResultData.getColumns()).containsExactly("item", "double");
        assertThat(firstResultData.getRows())
            .hasSize(3)
            .containsExactly(
                row(entry("item", (Object) 1.0), entry("double", (Object) 2.0)),
                row(entry("item", (Object) 2.0), entry("double", (Object) 4.0)),
                row(entry("item", (Object) 3.0), entry("double", (Object) 6.0))
            );
    }

    @Test
    public void returns_several_rows_from_several_statements_in_one_request() {
        Either<List<Fault>, List<Data>> result = subject.runSingleTransaction(
            "UNWIND [1,2,3] AS item RETURN item",
            "UNWIND [1,2,3] AS item RETURN item*2,item*3");

        assertThat(result).isRight();
        List<Data> data = result.getRight();
        assertThat(data).hasSize(2);
        Iterator<Data> iterator = data.iterator();
        Data firstResultData = iterator.next();
        assertThat(firstResultData.getColumns()).containsExactly("item");
        assertThat(firstResultData.getRows()).containsExactly(
            new Row("item", 1.0),
            new Row("item", 2.0),
            new Row("item", 3.0)
        );
        Data secondResults = iterator.next();
        assertThat(secondResults.getColumns()).containsExactly("item*2", "item*3");
        assertThat(secondResults.getRows()).containsExactly(
            row(entry("item*2", (Object) 2.0), entry("item*3", (Object) 3.0)),
            row(entry("item*2", (Object) 4.0), entry("item*3", (Object) 6.0)),
            row(entry("item*2", (Object) 6.0), entry("item*3", (Object) 9.0))
        );
    }

    @Test
    public void explicitly_opens_one_transaction_and_commits_it_in_another_request() {
        Either<List<Fault>, OngoingRemoteTransaction> result = subject.openTransaction();

        assertThat(result).isRight();
        OngoingRemoteTransaction transaction = result.getRight();
        long expiry = transaction.getExpiry();
        assertThat(expiry).isGreaterThan(0);
        assertThat(TransactionDateFormatSupplier.get().format(new Date(expiry))).isNotBlank();
        assertThat(transaction.getLocation().value()).startsWith("http://localhost:7474/db/data/transaction/");
        assertThat(transaction.getCommitLocation().value())
            .startsWith("http://localhost:7474/db/data/transaction/")
            .endsWith("/commit");

        subject.commit(transaction);
    }

    @Test
    public void spans_statements_over_several_requests() {
        OngoingRemoteTransaction transaction = subject.openTransaction(
            "UNWIND [1,2,3] AS item RETURN item",
            "UNWIND [1,2,3] AS item RETURN item*2,item*3"
        ).getRight();

        Either<List<Fault>, ClosedTransaction> result = subject.commit(
            transaction,
            "UNWIND [1,2,3] AS item RETURN item LIMIT 1",
            "UNWIND [1,2,3] AS item RETURN item ORDER BY item DESC LIMIT 2"
        );

        assertThat(result).isRight();
        ClosedTransaction closedTransaction = result.getRight();
        assertThat(closedTransaction.isRolledBack()).isFalse();
        List<Data> data = closedTransaction.getData();
        assertThat(data).hasSize(2);
        Iterator<Data> iterator = data.iterator();
        Data first = iterator.next();
        assertThat(first.getColumns()).containsExactly("item");
        assertThat(first.getRows()).containsExactly(row(entry("item", (Object) 1.0)));
        Data second = iterator.next();
        assertThat(second.getColumns()).containsExactly("item");
        assertThat(second.getRows()).containsExactly(row(entry("item", (Object) 3.0)), row(entry("item", (Object) 2.0)));
    }

    @Test
    public void supports_rollback() {
        OngoingRemoteTransaction transaction = subject.openTransaction("UNWIND [1,2,3] AS item RETURN item LIMIT 1").getRight();

        Either<List<Fault>, ClosedTransaction> closedTransaction = subject.rollback(transaction);

        assertThat(closedTransaction).isRight();
        assertThat(closedTransaction.getRight().isRolledBack()).isTrue();
    }

    @Test
    public void sends_queries_in_open_transaction() {
        Either<List<Fault>, OngoingRemoteTransaction> result = subject.openTransaction();
        OngoingRemoteTransaction ongoingTransaction = result.getRight();
        assertThat(result).isRight();
        String transactionCommitUri = ongoingTransaction.getCommitLocation().value();
        assertThat(transactionCommitUri)
            .startsWith("http://localhost:7474/db/data/transaction/")
            .endsWith("/commit");

        result = subject.execute(ongoingTransaction, "RETURN 123 AS result");

        assertThat(result).isRight();
        ongoingTransaction = result.getRight();
        assertThat(ongoingTransaction.getCommitLocation().value()).isEqualTo(transactionCommitUri);
        List<Data> data = ongoingTransaction.getData();
        assertThat(data).hasSize(1);
        Iterator<Data> iterator = data.iterator();
        Data first = iterator.next();
        assertThat(first.getColumns()).containsExactly("result");
        assertThat(first.getRows()).containsExactly(row(entry("result", (Object) 123.0)));
    }

    private OkHttpClient okHttpClient(String username, String password) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder = builder.authenticator(new BasicAuthenticator(username, password));
        return builder.build();
    }

    @SafeVarargs
    private final Row row(MapEntry<String, Object>... entries) {
        return new Row(asMap(entries));
    }

    @SafeVarargs
    private final Map<String,Object> asMap(MapEntry<String, Object>... entries) {
        Map<String, Object> result = new HashMap<>((int) (float) (entries.length / 0.75) + 1);
        for (MapEntry entry : entries) {
            result.put((String) entry.getKey(), entry.getValue());
        }
        return result;
    }
}
