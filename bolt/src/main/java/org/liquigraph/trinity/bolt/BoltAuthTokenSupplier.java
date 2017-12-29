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

import org.neo4j.driver.v1.AuthToken;
import org.neo4j.driver.v1.AuthTokens;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.util.Arrays.asList;
import static org.liquigraph.trinity.bolt.BoltProperties.AUTHENTICATION_TYPE;
import static org.liquigraph.trinity.bolt.BoltProperties.ENCODED_TICKET;
import static org.liquigraph.trinity.bolt.BoltProperties.PASSWORD;
import static org.liquigraph.trinity.bolt.BoltProperties.REALM;
import static org.liquigraph.trinity.bolt.BoltProperties.SCHEME;
import static org.liquigraph.trinity.bolt.BoltProperties.USERNAME;
import static org.liquigraph.trinity.bolt.PropertiesReader.readNonNullableProperty;
import static org.liquigraph.trinity.bolt.PropertiesReader.readNullableProperty;

class BoltAuthTokenSupplier implements Supplier<AuthToken> {
    private static final Logger LOGGER = LoggerFactory.getLogger(BoltAuthTokenSupplier.class);

    private static final List<String> CUSTOM_AUTH_NON_EXTRA_PARAMETERS = asList(
        AUTHENTICATION_TYPE,
        USERNAME,
        PASSWORD,
        REALM,
        SCHEME);

    private final Properties configuration;
    private final Map<String, Function<Properties, AuthToken>> tokenCreators;

    public BoltAuthTokenSupplier(Properties configuration) {
        this.configuration = configuration;
        this.tokenCreators = initTokenCreators();
    }

    @Override
    public AuthToken get() {
        String authenticationType = readNullableProperty(configuration, AUTHENTICATION_TYPE);
        if (authenticationType == null) {
            LOGGER.trace("Targetting Neo4j with disabled authentication");
            return AuthTokens.none();
        }
        return tokenCreators
            .getOrDefault(authenticationType.toLowerCase(Locale.ENGLISH), throwUnsupported(authenticationType))
            .apply(configuration);
    }

    private Function<Properties, AuthToken> throwUnsupported(String authenticationType) {
        LOGGER.error("Unsupported authentication type: {}", authenticationType);
        return (ignored) -> {
            throw new IllegalArgumentException(
                String.format("Unsupported authentication type: %s. Only 'none','basic','kerberos','custom' are supported",
                    authenticationType));
        };
    }

    private final Map<String, Function<Properties, AuthToken>> initTokenCreators() {
        Map<String, Function<Properties, AuthToken>> tokenCreators = new HashMap<>((int) Math.ceil(4 / 0.75));
        tokenCreators.put("none", (ignored) -> AuthTokens.none());
        tokenCreators.put("basic", this::basicAuthToken);
        tokenCreators.put("kerberos", this::kerberosAuthToken);
        tokenCreators.put("custom", this::customAuthToken);
        return tokenCreators;
    }

    private AuthToken basicAuthToken(Properties properties) {
        String username = readNonNullableProperty(properties, USERNAME);
        String password = readNonNullableProperty(properties, PASSWORD);
        String realm = readNullableProperty(properties, REALM);
        if (realm == null) {
            LOGGER.debug("Targetting Neo4j with basic authentication (without realm)");
            return AuthTokens.basic(username, password);
        }
        LOGGER.debug("Targetting Neo4j with basic authentication (with realm)");
        return AuthTokens.basic(username, password, realm);
    }

    private AuthToken kerberosAuthToken(Properties properties) {
        LOGGER.debug("Targetting Neo4j with Kerberos authentication");
        return AuthTokens.kerberos(readNonNullableProperty(properties, ENCODED_TICKET));
    }

    private AuthToken customAuthToken(Properties properties) {
        LOGGER.debug("Targetting Neo4j with custom authentication");
        String username = readNonNullableProperty(properties, USERNAME);
        String password = readNonNullableProperty(properties, PASSWORD);
        String realm = readNonNullableProperty(properties, REALM);
        String scheme = readNonNullableProperty(properties, SCHEME);
        Map<String, Object> extraParameters = customExtraParameters(properties);

        if (extraParameters.isEmpty()) {
            return AuthTokens.custom(username, password, realm, scheme);
        }
        return AuthTokens.custom(username, password, realm, scheme, extraParameters);
    }

    private Map<String, Object> customExtraParameters(Properties properties) {
        Predicate<String> contains = CUSTOM_AUTH_NON_EXTRA_PARAMETERS::contains;
        Map<String, Object> result = new HashMap<>();
        properties.stringPropertyNames()
            .stream()
            .filter(contains.negate())
            .forEach(k -> result.put(k, properties.getProperty(k)));
        return result;
    }
}
