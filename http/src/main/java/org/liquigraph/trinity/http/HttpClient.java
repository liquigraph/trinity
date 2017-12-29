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
package org.liquigraph.trinity.http;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static java.util.Arrays.asList;
import static org.liquigraph.trinity.http.internal.http.Endpoints.openTransactionUri;
import static org.liquigraph.trinity.http.internal.http.Endpoints.singleTransactionUri;
import static org.liquigraph.trinity.http.internal.http.RequestBuilders.json;
import static org.liquigraph.trinity.internal.collection.Lists.prepend;

import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.liquigraph.trinity.ClosedTransaction;
import org.liquigraph.trinity.CypherClient;
import org.liquigraph.trinity.Data;
import org.liquigraph.trinity.DefaultEither;
import org.liquigraph.trinity.Either;
import org.liquigraph.trinity.Fault;
import org.liquigraph.trinity.http.internal.payload.CypherExecutionError;
import org.liquigraph.trinity.http.internal.payload.CypherExecutionResults;
import org.liquigraph.trinity.http.internal.payload.CypherStatements;
import org.liquigraph.trinity.http.internal.payload.TransactionDateFormatSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;

public final class HttpClient implements CypherClient<OngoingRemoteTransaction> {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClient.class);
    private static final MediaType JSON = MediaType.parse("application/json;charset=UTF-8");

    private OkHttpClient httpClient;
    private Gson gson;
    private Request.Builder singleTransaction;
    private Request.Builder openTransaction;

    public HttpClient(String baseUrl) {
        this(baseUrl, new OkHttpClient());
    }

    public HttpClient(String baseUrl, OkHttpClient client) {
        httpClient = client;
        gson = new Gson();
        singleTransaction = json().url(singleTransactionUri(baseUrl));
        openTransaction = json().url(openTransactionUri(baseUrl));
        LOGGER.info("HTTP client targets {}", baseUrl);
    }

    @Override
    public Either<List<Fault>, List<Data>> runSingleTransaction(String query, String... queries) {
        List<String> allQueries = prepend(query, queries);
        LOGGER.debug("About to run {} queries in a single transaction", allQueries.size());
        RequestBody requestBody = requestBody(serializeQueries(allQueries));
        Request request = singleTransaction.post(requestBody).build();
        return executeRequest(request);
    }

    @Override
    public Either<List<Fault>, OngoingRemoteTransaction> openTransaction(String... queries) {
        List<String> allQueries = asList(queries);
        LOGGER.debug("About to open a transaction and run {} queries", allQueries.size());
        RequestBody requestBody = requestBody(serializeQueries(allQueries));
        Request request = openTransaction.post(requestBody).build();
        try (Response httpResponse = httpClient.newCall(request).execute()) {
            Either<IOException, CypherExecutionResults> response = deserializeResponse(httpResponse.body());
            if (response.isLeft()) {
                return this.leftIoException(response.getLeft());
            }
            TransactionUri location = new TransactionUri(httpResponse.header("Location"));
            LOGGER.info("Transaction has been successfully open at URI {}", location.value());
            CypherExecutionResults payload = response.getRight();
            return DefaultEither.right(new OngoingRemoteTransaction(
                  location,
                  expiryTime(payload),
                  new TransactionUri(payload.getCommit()),
                  payload.explode()
            ));
        } catch (IOException e) {
            return this.leftIoException(e);
        }
    }

    @Override
    public Either<List<Fault>, OngoingRemoteTransaction> execute(OngoingRemoteTransaction transaction, String... queries) {
        List<String> allQueries = asList(queries);
        TransactionUri location = transaction.getLocation();
        LOGGER.debug("About to run {} queries in currently open transaction at URI {}", allQueries.size(), location.value());
        RequestBody body = requestBody(serializeQueries(allQueries));
        Request request = json().url(location.value()).post(body).build();

        try (Response httpResponse = httpClient.newCall(request).execute()) {
            Either<IOException, CypherExecutionResults> response = deserializeResponse(httpResponse.body());
            if (response.isLeft()) {
                return this.leftIoException(response.getLeft());
            }
            CypherExecutionResults payload = response.getRight();
            return DefaultEither.right(new OngoingRemoteTransaction(
                  location,
                  expiryTime(payload),
                  new TransactionUri(payload.getCommit()),
                  payload.explode()));
        } catch (IOException e) {
            return this.leftIoException(e);
        }
    }

    @Override
    public Either<List<Fault>, ClosedTransaction> commit(OngoingRemoteTransaction transaction, String... queries) {
        List<String> allQueries = asList(queries);
        RequestBody body = requestBody(serializeQueries(allQueries));
        TransactionUri location = transaction.getCommitLocation();
        String commitUri = location.value();
        LOGGER.debug("About to run {} queries and commit open transaction at URI {}", allQueries.size(), commitUri);
        Either<List<Fault>, List<Data>> result = executeRequest(json().url(commitUri).post(body).build());

        if (result.isLeft()) {
            return DefaultEither.left(result.getLeft());
        }
        return DefaultEither.right(new ClosedTransaction(result.getRight(), false));
    }

    @Override
    public Either<List<Fault>, ClosedTransaction> rollback(OngoingRemoteTransaction transaction) {
        String uri = transaction.getLocation().value();
        LOGGER.debug("About to roll back open transaction at URI {}", uri);
        Request rollback = json().url(uri).delete().build();
        try {
            httpClient.newCall(rollback).execute();
            return DefaultEither.right(ClosedTransaction.ROLLED_BACK);
        } catch (IOException e) {
            return this.leftIoException(e);
        }
    }

    private static RequestBody requestBody(String serialize) {
        return RequestBody.create(JSON, serialize);
    }

    private Either<List<Fault>, List<Data>> executeRequest(Request request) {
        try (Response response = httpClient.newCall(request).execute()) {
            return parseResponse(response);
        } catch (IOException e) {
            return leftIoException(e);
        }
    }

    private Either<List<Fault>, List<Data>> parseResponse(Response response) {
        Either<IOException, CypherExecutionResults> payloadResult = deserializeResponse(response.body());
        if (payloadResult.isLeft()) {
            return leftIoException(payloadResult.getLeft());
        }
        CypherExecutionResults results = payloadResult.getRight();
        if (results.hasErrors()) {
            return DefaultEither.left(mapErrors(results.getErrors()));
        }
        return DefaultEither.right(results.explode());
    }

    private List<Fault> mapErrors(List<CypherExecutionError> errors) {
        List<Fault> result = new ArrayList<>(errors.size());
        for (CypherExecutionError error : errors) {
            result.add(new Fault(error.getCode(), error.getMessage()));
        }
        return result;
    }

    private Either<IOException, CypherExecutionResults> deserializeResponse(ResponseBody body) {
        try (Reader reader = body.charStream()) {
            return DefaultEither.right(gson.fromJson(reader, CypherExecutionResults.class));
        } catch (IOException e) {
            LOGGER.error("An unexpected error happened while deserializing the HTTP response", e);
            return DefaultEither.left(e);
        }
    }

    private <R> Either<List<Fault>, R> leftIoException(IOException e) {
        LOGGER.error("An unexpected error happened while sending the HTTP request", e);
        Fault error = new Fault("HttpClient.Error.IOException", e.getMessage());
        return DefaultEither.left(Collections.singletonList(error));
    }

    private static long expiryTime(CypherExecutionResults response) {
        try {
            return tryParseTransactionDate(response.getTransaction().getExpires());
        } catch (ParseException e) {
            LOGGER.error("An unexpected error happened while parsing the transaction expiry time", e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private static long tryParseTransactionDate(String expires) throws ParseException {
        return TransactionDateFormatSupplier.get().parse(expires).getTime();
    }

    private String serializeQueries(List<String> queries) {
        return gson.toJson(CypherStatements.create(queries));
    }
}
