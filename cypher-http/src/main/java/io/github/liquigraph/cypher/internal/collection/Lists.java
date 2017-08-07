/*
 * Copyright 2014-2016 the original author or authors.
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
