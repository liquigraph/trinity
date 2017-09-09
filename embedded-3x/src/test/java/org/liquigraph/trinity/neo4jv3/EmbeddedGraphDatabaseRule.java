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
package org.liquigraph.trinity.neo4jv3;

import org.liquigraph.trinity.SemanticVersion;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.function.BiFunction;

public class EmbeddedGraphDatabaseRule implements TestRule {
    private static final MethodType APPLY_SIGNATURE = MethodType.methodType(Statement.class, Statement.class, Description.class);
    private static final SemanticVersion NEO4J_300 = SemanticVersion.parse("3.0.0");
    private static final SemanticVersion NEO4J_310 = SemanticVersion.parse("3.1.0");
    private final Object embeddedDatabaseRule;
    private final MethodHandle applyMethod;

    public EmbeddedGraphDatabaseRule(SemanticVersion neo4jVersion) {
        if (neo4jVersion.compareTo(NEO4J_300) < 0) {
            throw new IllegalArgumentException("Only Neo4j 3.x is supported for this rule");
        }
        embeddedDatabaseRule = instantiateRule(neo4jVersion);
        applyMethod = findApplyMethod(embeddedDatabaseRule.getClass());
    }

    @Override
    public Statement apply(Statement base, Description description) {
        try {
            return (Statement) applyMethod.invoke(embeddedDatabaseRule, base, description);
        } catch (Throwable e) {
            throw propagate(e.getMessage(), e);
        }
    }

    public <T> T doInTransaction(BiFunction<Transaction, GraphDatabaseService, T> function) {
        GraphDatabaseService graphDatabaseService = getGraphDatabaseService();
        try (Transaction transaction = graphDatabaseService.beginTx()) {
            return function.apply(transaction, graphDatabaseService);
        }
    }

    public GraphDatabaseService getGraphDatabaseService() {
        return (GraphDatabaseService) embeddedDatabaseRule;
    }

    private static Object instantiateRule(SemanticVersion neo4jVersion) {
        try {
            return loadClass(neo4jVersion).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw propagate(e.getMessage(), e);
        }
    }

    private static MethodHandle findApplyMethod(Class<?> type) {
        try {
            return MethodHandles.publicLookup().findVirtual(type, "apply", APPLY_SIGNATURE);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw propagate(e.getMessage(), e);
        }
    }

    private static Class<?> loadClass(SemanticVersion neo4jVersion) throws ClassNotFoundException {
        if (neo4jVersion.compareTo(NEO4J_310) >= 0) {
            return Class.forName("org.neo4j.test.rule.EmbeddedDatabaseRule");
        }
        return Class.forName("org.neo4j.test.EmbeddedDatabaseRule");
    }

    private static RuntimeException propagate(String message, Throwable e) {
        if (e instanceof RuntimeException) {
            return (RuntimeException) e;
        }
        return new RuntimeException(message, e);
    }
}
