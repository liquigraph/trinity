package io.github.liquigraph.cypher.internal.payload;

import io.github.liquigraph.cypher.ResultData;
import io.github.liquigraph.cypher.Row;
import io.github.liquigraph.cypher.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CypherExecutionResults {

    private List<CypherExecutionResult> results = new ArrayList<>();
    private List<CypherExecutionError> errors = new ArrayList<>();
    private String commit;
    private CypherTransaction transaction;

    public List<CypherExecutionResult> getResults() {
        return results;
    }

    public void setResults(List<CypherExecutionResult> results) {
        this.results = results;
    }

    public boolean hasErrors() {
        return !getErrors().isEmpty();
    }

    public List<CypherExecutionError> getErrors() {
        return errors;
    }

    public void setErrors(List<CypherExecutionError> errors) {
        this.errors = errors;
    }

    public String getCommit() {
        return commit;
    }

    public void setCommit(String commit) {
        this.commit = commit;
    }

    public CypherTransaction getTransaction() {
        return transaction;
    }

    public void setTransaction(CypherTransaction transaction) {
        this.transaction = transaction;
    }

    public List<ResultData> explode() {
        List<ResultData> resultData = new ArrayList<>(results.size());
        for (CypherExecutionResult queryResult : results) {
            resultData.add(new ResultData(queryResult.getColumns(), populateRows(queryResult)));
        }
        return resultData;
    }

    private List<Row> populateRows(CypherExecutionResult singleResult) {
        List<Row> result = new ArrayList<>();
        List<String> columns = singleResult.getColumns();

        for (CypherExecutionData row : singleResult.getData()) {
            List<Object> values = row.getRow();
            List<Value> resultValues = new ArrayList<>();
            for (int i = 0; i < columns.size(); i++) {
                resultValues.add(new Value(columns.get(i), values.get(i)));
            }
            result.add(new Row(resultValues));
        }
        return result;
    }

    @Override
    public int hashCode() {
        return Objects.hash(results, errors);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final CypherExecutionResults other = (CypherExecutionResults) obj;
        return Objects.equals(this.results, other.results)
                && Objects.equals(this.errors, other.errors);
    }
}
