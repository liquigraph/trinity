package io.github.liquigraph.cypher;

import com.google.gson.Gson;
import io.github.liquigraph.cypher.internal.payload.CypherExecutionError;
import io.github.liquigraph.cypher.internal.payload.CypherExecutionResults;
import io.github.liquigraph.cypher.internal.payload.CypherStatements;
import io.github.liquigraph.cypher.internal.payload.TransactionDateFormatSupplier;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.github.liquigraph.cypher.internal.collection.Lists.concatenate;
import static io.github.liquigraph.cypher.internal.http.Endpoints.openTransactionUri;
import static io.github.liquigraph.cypher.internal.http.Endpoints.singleTransactionUri;
import static io.github.liquigraph.cypher.internal.http.RequestBuilders.json;
import static java.util.Arrays.asList;

public class HttpClient {

    private static final MediaType JSON = MediaType.parse("application/json;charset=UTF-8");

    private final OkHttpClient httpClient;
    private final Gson gson;
    private final Request.Builder singleTransaction;
    private final Request.Builder openTransaction;

    public HttpClient(String baseUrl) {
        this(baseUrl, null, null);
    }

    public HttpClient(String baseUrl, String username, String password) {
        httpClient = createHttpClient(username, password);
        gson = new Gson();
        singleTransaction = json().url(singleTransactionUri(baseUrl));
        openTransaction = json().url(openTransactionUri(baseUrl));
    }

    public Either<List<ResultError>, List<ResultData>> runSingleTransaction(String query, String... queries) {
        RequestBody requestBody = requestBody(serializeQueries(concatenate(query, queries)));
        Request request = singleTransaction.post(requestBody).build();
        return executeRequest(request);
    }

    public Either<List<ResultError>, OngoingTransaction> openTransaction(String... queries) {
        RequestBody requestBody = requestBody(serializeQueries(asList(queries)));
        Request request = openTransaction.post(requestBody).build();
        try (Response httpResponse = httpClient.newCall(request).execute()) {
            Either<IOException, CypherExecutionResults> response = deserializeResponse(httpResponse.body());
            if (response.isLeft()) {
                return this.leftIoException(response.getLeft());
            }
            CypherExecutionResults payload = response.getRight();
            return Either.right(new OngoingTransaction(
                    httpResponse.header("Location"),
                    expiryTime(payload),
                    payload.getCommit(),
                    payload.explode()
            ));
        } catch (IOException e) {
            return this.leftIoException(e);
        }
    }

    public Either<List<ResultError>, List<ResultData>> commit(OngoingTransaction transaction, String... queries) {
        RequestBody body = requestBody(serializeQueries(asList(queries)));
        String url = transaction.getCommitLocation();
        return executeRequest(json().url(url).post(body).build());
    }

    public Either<List<ResultError>, ClosedTransaction> rollback(OngoingTransaction transaction) {
        Request rollback = json().url(transaction.getLocation()).delete().build();
        try {
            httpClient.newCall(rollback).execute();
            return Either.right(new ClosedTransaction());
        } catch (IOException e) {
            return this.leftIoException(e);
        }
    }

    private static OkHttpClient createHttpClient(String username, String password) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (username != null && password != null) {
            builder = builder.authenticator(new BasicAuthenticator(username, password));
        }
        return builder.build();
    }

    private static RequestBody requestBody(String serialize) {
        return RequestBody.create(JSON, serialize);
    }

    private Either<List<ResultError>, List<ResultData>> executeRequest(Request request) {
        try (Response response = httpClient.newCall(request).execute()) {
            return parseResponse(response);
        } catch (IOException e) {
            return leftIoException(e);
        }
    }

    private Either<List<ResultError>, List<ResultData>> parseResponse(Response response) {
        Either<IOException, CypherExecutionResults> payloadResult = deserializeResponse(response.body());
        if (payloadResult.isLeft()) {
            return leftIoException(payloadResult.getLeft());
        }
        CypherExecutionResults results = payloadResult.getRight();
        if (results.hasErrors()) {
            return Either.left(mapErrors(results.getErrors()));
        }
        return Either.right(results.explode());
    }

    private List<ResultError> mapErrors(List<CypherExecutionError> errors) {
        List<ResultError> result = new ArrayList<>();
        for (CypherExecutionError error : errors) {
            result.add(new ResultError(error.getCode(), error.getMessage()));
        }
        return result;
    }

    private Either<IOException, CypherExecutionResults> deserializeResponse(ResponseBody body) {
        try (Reader reader = body.charStream()) {
            return Either.right(gson.fromJson(reader, CypherExecutionResults.class));
        } catch (IOException e) {
            return Either.left(e);
        }
    }

    private <R> Either<List<ResultError>, R> leftIoException(IOException e) {
        ResultError error = new ResultError("HttpClient.Error.IOException", e.getMessage());
        return Either.left(Collections.singletonList(error));
    }

    private static long expiryTime(CypherExecutionResults response) {
        try {
            return tryParseTransactionDate(response.getTransaction().getExpires());
        } catch (ParseException e) {
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