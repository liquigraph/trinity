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
package org.liquigraph.trinity.bolt;

class BoltProperties {
    static final String AUTHENTICATION_TYPE = "cypher.bolt.auth.type";
    static final String USERNAME = "cypher.bolt.auth.username";
    static final String PASSWORD = "cypher.bolt.auth.password";
    static final String REALM = "cypher.bolt.auth.realm";
    static final String SCHEME = "cypher.bolt.auth.scheme";
    static final String ENCODED_TICKET = "cypher.bolt.auth.base64EncodedTicket";
    static final String BASE_URL = "cypher.bolt.baseurl";
    static final String CONNECTION_LIVENESS_CHECK_TIMEOUT_MILLISECONDS = "cypher.bolt.connection.liveness-check-timeout-milliseconds";
    static final String CONNECTION_TIMEOUT_MILLISECONDS = "cypher.bolt.connection.timeout-milliseconds";
    static final String ENABLE_ENCRYPTION = "cypher.bolt.enable-encryption";
    static final String ENABLE_LEAKING_SESSION_LOGGING = "cypher.bolt.enable-leaking-session-logging";
    static final String MAX_IDLE_SESSIONS = "cypher.bolt.max-idle-sessions";
    static final String MAX_TRANSACTION_RETRY_TIME_MILLISECONDS = "cypher.bolt.max-transaction-retry-time-milliseconds";
    static final String TRUST_STRATEGY_TYPE = "cypher.bolt.trust-strategy.type";
    static final String TRUST_STRATEGY_CUSTOM_CERTIFICATE_PATH = "cypher.bolt.trust-strategy.custom-certificate-path";
}
