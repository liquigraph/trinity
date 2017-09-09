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
package org.liquigraph.trinity;

public enum CypherTransport {
    /**
     * Executes Cypher queries with Bolt protocol
     * Requires a JRE 8 and Neo4j v3
     */
    BOLT,
    /**
     * Executes Cypher queries via the REST API
     * Requires at least a JRE 7 and Neo4j v2
     */
    HTTP,
    /**
     * Executes Cypher queries via an embedded Neo4j instance
     * Requires a JRE 7 and Neo4j v2
     */
    EMBEDDED_2,
    /**
     * Executes Cypher queries via an embedded Neo4j instance
     * Requires a JRE 8 and Neo4j v3
     */
    EMBEDDED_3;
}
