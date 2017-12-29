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

import org.neo4j.driver.v1.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;
import static org.liquigraph.trinity.bolt.BoltProperties.CONNECTION_LIVENESS_CHECK_TIMEOUT_MILLISECONDS;
import static org.liquigraph.trinity.bolt.BoltProperties.CONNECTION_TIMEOUT_MILLISECONDS;
import static org.liquigraph.trinity.bolt.BoltProperties.ENABLE_ENCRYPTION;
import static org.liquigraph.trinity.bolt.BoltProperties.ENABLE_LEAKING_SESSION_LOGGING;
import static org.liquigraph.trinity.bolt.BoltProperties.MAX_IDLE_SESSIONS;
import static org.liquigraph.trinity.bolt.BoltProperties.MAX_TRANSACTION_RETRY_TIME_MILLISECONDS;
import static org.liquigraph.trinity.bolt.BoltProperties.TRUST_STRATEGY_CUSTOM_CERTIFICATE_PATH;
import static org.liquigraph.trinity.bolt.BoltProperties.TRUST_STRATEGY_TYPE;
import static org.liquigraph.trinity.bolt.PropertiesReader.readNonNullableProperty;
import static org.liquigraph.trinity.bolt.PropertiesReader.readNullableProperty;

class BoltDriverConfigurationSupplier implements Supplier<Config> {
    private static final Logger LOGGER = LoggerFactory.getLogger(BoltDriverConfigurationSupplier.class);

    private final Properties configuration;

    public BoltDriverConfigurationSupplier(Properties configuration) {
        this.configuration = configuration;
    }

    @Override
    public Config get() {
        Config.ConfigBuilder builder = Config.build();
        builder = setTrustStrategyType(builder);
        builder = setMaxTransactionRetryTime(builder);
        builder = setMaxIdleSessions(builder);
        builder = setLeakingSessionLoggingEnabled(builder);
        builder = setEncryptionEnabled(builder);
        builder = setConnectionTimeout(builder);
        builder = setLivenessCheckTimeout(builder);
        return builder.toConfig();
    }

    private Config.ConfigBuilder setTrustStrategyType(Config.ConfigBuilder builder) {
        String trustStrategyType = readNullableProperty(configuration, TRUST_STRATEGY_TYPE);
        if (trustStrategyType != null) {
            LOGGER.debug("Setting specific trust strategy type");
            builder = builder.withTrustStrategy(trustStrategy(trustStrategyType));
        }
        return builder;
    }

    private Config.ConfigBuilder setMaxTransactionRetryTime(Config.ConfigBuilder builder) {
        String maxTransactionRetryTimeMilliseconds = readNullableProperty(configuration, MAX_TRANSACTION_RETRY_TIME_MILLISECONDS);
        if (maxTransactionRetryTimeMilliseconds != null) {
            LOGGER.debug("Setting specific max transaction retry type");
            builder = builder.withMaxTransactionRetryTime(parseLong(maxTransactionRetryTimeMilliseconds, 10), TimeUnit.MILLISECONDS);
        }
        return builder;
    }

    private Config.ConfigBuilder setMaxIdleSessions(Config.ConfigBuilder builder) {
        String maxIdleSessions = readNullableProperty(configuration,
            MAX_IDLE_SESSIONS
        );
        if (maxIdleSessions != null) {
            LOGGER.debug("Setting specific max idle session");
            builder = builder.withMaxIdleSessions(parseInt(maxIdleSessions, 10));
        }
        return builder;
    }

    private Config.ConfigBuilder setLeakingSessionLoggingEnabled(Config.ConfigBuilder builder) {
        String enableLeakingSessionLogging = readNullableProperty(configuration, ENABLE_LEAKING_SESSION_LOGGING);
        if (enableLeakingSessionLogging != null && Boolean.parseBoolean(enableLeakingSessionLogging)) {
            LOGGER.debug("Enabling leaking session logging");
            builder = builder.withLeakedSessionsLogging();
        }
        return builder;
    }

    private Config.ConfigBuilder setEncryptionEnabled(Config.ConfigBuilder builder) {
        String enableEncryption = readNullableProperty(configuration, ENABLE_ENCRYPTION);
        if (enableEncryption != null) {
            if (Boolean.parseBoolean(enableEncryption)) {
                LOGGER.debug("Enabling encryption");
                builder = builder.withEncryption();
            } else {
                LOGGER.debug("Disabling encryption");
                builder = builder.withoutEncryption();
            }
        }
        return builder;
    }

    private Config.ConfigBuilder setConnectionTimeout(Config.ConfigBuilder builder) {
        String timeout = readNullableProperty(configuration, CONNECTION_TIMEOUT_MILLISECONDS);
        if (timeout != null) {
            LOGGER.debug("Setting connection timeout");
            builder = builder.withConnectionTimeout(parseLong(timeout, 10), TimeUnit.MILLISECONDS);
        }
        return builder;
    }

    private Config.ConfigBuilder setLivenessCheckTimeout(Config.ConfigBuilder builder) {
        String livenessCheckTimeout = readNullableProperty(configuration, CONNECTION_LIVENESS_CHECK_TIMEOUT_MILLISECONDS);

        if (livenessCheckTimeout != null) {
            LOGGER.debug("Setting connection liveness check timeout");
            builder = builder.withConnectionLivenessCheckTimeout(parseLong(livenessCheckTimeout, 10), TimeUnit.MILLISECONDS);
        }
        return builder;
    }

    private Config.TrustStrategy trustStrategy(String trustStrategyType) {
        switch (trustStrategyType.toLowerCase(Locale.ENGLISH)) {
            case "all":
                return Config.TrustStrategy.trustAllCertificates();
            case "system":
                return Config.TrustStrategy.trustSystemCertificates();
            case "custom":
                return Config.TrustStrategy.trustCustomCertificateSignedBy(new File(
                    readNonNullableProperty(configuration, TRUST_STRATEGY_CUSTOM_CERTIFICATE_PATH)));
        }
        LOGGER.error("Unsupported trust strategy type: {}", trustStrategyType);
        throw new IllegalArgumentException(String.format("Unsupported trust strategy type: %s. Expected one of: all, system, custom", trustStrategyType));
    }
}
