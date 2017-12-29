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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.neo4j.driver.v1.Config;
import org.neo4j.driver.v1.Config.TrustStrategy;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.liquigraph.trinity.Assertions.assertThat;

public class BoltDriverConfigurationSupplierTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void sets_all_trust_strategy_type() {
        Properties props = new Properties();
        props.setProperty(BoltProperties.TRUST_STRATEGY_TYPE, "all");
        BoltDriverConfigurationSupplier supplier = new BoltDriverConfigurationSupplier(props);

        Config configuration = supplier.get();

        assertThat(configuration.trustStrategy())
            .isEqualToComparingFieldByField(TrustStrategy.trustAllCertificates());
    }

    @Test
    public void sets_system_trust_strategy_type() {
        Properties props = new Properties();
        props.setProperty(BoltProperties.TRUST_STRATEGY_TYPE, "system");
        BoltDriverConfigurationSupplier supplier = new BoltDriverConfigurationSupplier(props);

        Config configuration = supplier.get();

        assertThat(configuration.trustStrategy())
            .isEqualToComparingFieldByField(TrustStrategy.trustSystemCertificates());
    }

    @Test
    public void sets_custom_trust_strategy_type() throws IOException {
        File certFile = temporaryFolder.newFile();
        Properties props = new Properties();
        props.setProperty(BoltProperties.TRUST_STRATEGY_TYPE, "custom");
        props.setProperty(BoltProperties.TRUST_STRATEGY_CUSTOM_CERTIFICATE_PATH, certFile.getPath());
        BoltDriverConfigurationSupplier supplier = new BoltDriverConfigurationSupplier(props);

        Config configuration = supplier.get();

        assertThat(configuration.trustStrategy())
            .isEqualToComparingFieldByField(TrustStrategy.trustCustomCertificateSignedBy(certFile));
    }

    @Test
    public void fails_to_set_custom_trust_strategy_type_when_required_cert_file_is_missing() {
        Properties props = new Properties();
        props.setProperty(BoltProperties.TRUST_STRATEGY_TYPE, "custom");
        BoltDriverConfigurationSupplier supplier = new BoltDriverConfigurationSupplier(props);

        assertThatIllegalArgumentException()
            .isThrownBy(supplier::get)
            .withMessage("Cypher Bolt Client setting 'cypher.bolt.trust-strategy.custom-certificate-path' must not be null");
    }

    @Test
    public void fails_to_set_unsupported_trust_strategy_type() {
        Properties props = new Properties();
        props.setProperty(BoltProperties.TRUST_STRATEGY_TYPE, "oopsie");
        BoltDriverConfigurationSupplier supplier = new BoltDriverConfigurationSupplier(props);

        assertThatIllegalArgumentException()
            .isThrownBy(supplier::get)
            .withMessage("Unsupported trust strategy type: oopsie. Expected one of: all, system, custom");
    }

    @Test
    public void sets_max_idle_session() {
        Properties props = new Properties();
        props.setProperty(BoltProperties.MAX_IDLE_SESSIONS, "123");
        BoltDriverConfigurationSupplier supplier = new BoltDriverConfigurationSupplier(props);

        Config configuration = supplier.get();

        assertThat(configuration.maxIdleConnectionPoolSize()).isEqualTo(123);
    }

    @Test
    public void fails_to_sets_max_idle_session_when_setting_value_is_not_a_long() {
        Properties props = new Properties();
        props.setProperty(BoltProperties.MAX_IDLE_SESSIONS, "123.23");
        BoltDriverConfigurationSupplier supplier = new BoltDriverConfigurationSupplier(props);

        assertThatIllegalArgumentException()
            .isThrownBy(supplier::get)
            .withMessageContaining("123.23");
    }

    @Test
    public void sets_leaking_session_enablement() {
        Properties props = new Properties();
        props.setProperty(BoltProperties.ENABLE_LEAKING_SESSION_LOGGING, "true");
        BoltDriverConfigurationSupplier supplier = new BoltDriverConfigurationSupplier(props);

        Config configuration = supplier.get();

        assertThat(configuration.logLeakedSessions()).isTrue();
    }

    @Test
    public void sets_leaking_session_enablement_with_any_value() {
        Properties props = new Properties();
        props.setProperty(BoltProperties.ENABLE_LEAKING_SESSION_LOGGING, "repgrego");
        BoltDriverConfigurationSupplier supplier = new BoltDriverConfigurationSupplier(props);

        Config configuration = supplier.get();

        assertThat(configuration.logLeakedSessions()).isFalse();
    }

    @Test
    public void sets_encryption_enablement() {
        Properties props = new Properties();
        props.setProperty(BoltProperties.ENABLE_ENCRYPTION, "true");
        BoltDriverConfigurationSupplier supplier = new BoltDriverConfigurationSupplier(props);

        Config configuration = supplier.get();

        assertThat(configuration.encrypted()).isTrue();
    }

    @Test
    public void sets_encryption_enablement_with_any_value() {
        Properties props = new Properties();
        props.setProperty(BoltProperties.ENABLE_ENCRYPTION, "gjreogjero geoi");
        BoltDriverConfigurationSupplier supplier = new BoltDriverConfigurationSupplier(props);

        Config configuration = supplier.get();

        assertThat(configuration.encrypted()).isFalse();
    }

    @Test
    public void sets_connection_timeout() {
        Properties props = new Properties();
        props.setProperty(BoltProperties.CONNECTION_TIMEOUT_MILLISECONDS, "2345");
        BoltDriverConfigurationSupplier supplier = new BoltDriverConfigurationSupplier(props);

        Config configuration = supplier.get();

        assertThat(configuration.connectionTimeoutMillis()).isEqualTo(2345);
    }

    @Test
    public void fails_to_set_connection_timeout_when_setting_value_is_not_a_long() {
        Properties props = new Properties();
        props.setProperty(BoltProperties.CONNECTION_TIMEOUT_MILLISECONDS, "627832.9");
        BoltDriverConfigurationSupplier supplier = new BoltDriverConfigurationSupplier(props);

        assertThatIllegalArgumentException()
            .isThrownBy(supplier::get)
            .withMessageContaining("627832.9");
    }

    @Test
    public void sets_liveness_check_timeout() {
        Properties props = new Properties();
        props.setProperty(BoltProperties.CONNECTION_LIVENESS_CHECK_TIMEOUT_MILLISECONDS, "456765");
        BoltDriverConfigurationSupplier supplier = new BoltDriverConfigurationSupplier(props);

        Config configuration = supplier.get();

        assertThat(configuration.idleTimeBeforeConnectionTest()).isEqualTo(456765);
    }

    @Test
    public void fails_to_set_liveness_check_when_setting_value_is_not_a_long() {
        Properties props = new Properties();
        props.setProperty(BoltProperties.CONNECTION_LIVENESS_CHECK_TIMEOUT_MILLISECONDS, "987654.9");
        BoltDriverConfigurationSupplier supplier = new BoltDriverConfigurationSupplier(props);

        assertThatIllegalArgumentException()
            .isThrownBy(supplier::get)
            .withMessageContaining("987654.9");
    }
}
