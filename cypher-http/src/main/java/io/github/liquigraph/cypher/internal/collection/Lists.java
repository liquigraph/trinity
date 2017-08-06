package io.github.liquigraph.cypher.internal.collection;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class Lists {

    public static final List<String> concatenate(String query, String... queries) {
        List<String> result = new ArrayList<>(1 + queries.length);
        result.add(query);
        result.addAll(asList(queries));
        return result;
    }
}
