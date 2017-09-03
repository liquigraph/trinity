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

import io.github.liquigraph.cypher.Fault;
import org.neo4j.cypher.CypherException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public enum CypherExceptionConverter {
    INSTANCE;

    private final Method statusGetter;

    CypherExceptionConverter() {
        this.statusGetter = method(CypherException.class, "status");
    }

    public Fault convert(CypherException cypherException) {
        if (statusGetter != null) {
            return tryBuildFromStatus(cypherException);
        }
        return new Fault("Neo.PreV202Error.CypherException.Unknown", cypherException.getMessage());

    }

    private Fault tryBuildFromStatus(CypherException cypherException) {
        try {
            return buildFromStatus(cypherException);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            cypherException.addSuppressed(e);
            return new Fault("NeoUnknownVersion.Error.CypherException", cypherException.getMessage());
        }
    }

    private static Method method(Class<CypherException> type, String name) {
        try {
            return type.getMethod(name);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private Fault buildFromStatus(CypherException cypherException) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Object status = statusGetter.invoke(cypherException);
        // TODO: cache this
        Object code = status.getClass().getMethod("code").invoke(status);
        Class<?> codeClass = code.getClass();
        return new Fault(
            (String) codeClass.getMethod("serialize").invoke(code),
            String.format(
                "%s%n%s",
                codeClass.getMethod("description").invoke(code),
                cypherException.getMessage()
            ));
    }
}
