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
package org.liquigraph.trinity.bolt;

import org.junit.Test;
import org.neo4j.driver.v1.AuthTokens;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.util.Maps.newHashMap;

public class BoltAuthTokenSupplierTest {

    @Test
    public void supplies_no_auth_token_when_no_explicit_configuration_is_provided() {
        BoltAuthTokenSupplier supplier = new BoltAuthTokenSupplier(new Properties());

        assertThat(supplier.get())
            .overridingErrorMessage("Expected no-auth token")
            .isEqualTo(AuthTokens.none());
    }

    @Test
    public void supplies_no_auth_token_when_such_configuration_is_provided() {
        Properties props = new Properties();
        props.setProperty(BoltProperties.AUTHENTICATION_TYPE, "none");
        BoltAuthTokenSupplier supplier = new BoltAuthTokenSupplier(props);

        assertThat(supplier.get())
            .overridingErrorMessage("Expected no-auth token")
            .isEqualTo(AuthTokens.none());
    }

    @Test
    public void supplies_basic_auth_token_when_such_configuration_is_provided() {
        Properties props = new Properties();
        props.setProperty(BoltProperties.AUTHENTICATION_TYPE, "basic");
        props.setProperty(BoltProperties.USERNAME, "user");
        props.setProperty(BoltProperties.PASSWORD, "password");
        BoltAuthTokenSupplier supplier = new BoltAuthTokenSupplier(props);

        assertThat(supplier.get())
            .overridingErrorMessage("Expected basic auth token")
            .isEqualTo(AuthTokens.basic("user", "password"));
    }

    @Test
    public void supplies_basic_auth_token_with_custom_realm_when_such_configuration_is_provided() {
        Properties props = new Properties();
        props.setProperty(BoltProperties.AUTHENTICATION_TYPE, "basic");
        props.setProperty(BoltProperties.USERNAME, "user");
        props.setProperty(BoltProperties.PASSWORD, "password");
        props.setProperty(BoltProperties.REALM, "realm");
        BoltAuthTokenSupplier supplier = new BoltAuthTokenSupplier(props);

        assertThat(supplier.get())
            .overridingErrorMessage("Expected basic auth token with custom realm")
            .isEqualTo(AuthTokens.basic("user", "password", "realm"));
    }

    @Test
    public void fails_to_supply_basic_authentication_when_required_username_is_missing() {
        Properties missingUsername = new Properties();
        missingUsername.setProperty(BoltProperties.AUTHENTICATION_TYPE, "basic");
        missingUsername.setProperty(BoltProperties.PASSWORD, "password");

        assertThatIllegalArgumentException()
            .isThrownBy(new BoltAuthTokenSupplier(missingUsername)::get)
            .withMessage("Cypher Bolt Client setting 'cypher.bolt.auth.username' must not be null");
    }

    @Test
    public void fails_to_supply_basic_authentication_when_required_password_is_missing() {
        Properties missingPassword = new Properties();
        missingPassword.setProperty(BoltProperties.AUTHENTICATION_TYPE, "basic");
        missingPassword.setProperty(BoltProperties.USERNAME, "user");

        assertThatIllegalArgumentException()
            .isThrownBy(new BoltAuthTokenSupplier(missingPassword)::get)
            .withMessage("Cypher Bolt Client setting 'cypher.bolt.auth.password' must not be null");

    }

    @Test
    public void supplies_kerberos_auth_token_when_such_configuration_is_provided() {
        String encodedTicket = base64("encoded ticket");
        Properties props = new Properties();
        props.setProperty(BoltProperties.AUTHENTICATION_TYPE, "kerberos");
        props.setProperty(BoltProperties.ENCODED_TICKET, encodedTicket);
        BoltAuthTokenSupplier supplier = new BoltAuthTokenSupplier(props);

        assertThat(supplier.get())
            .overridingErrorMessage("Expected kerberos auth token")
            .isEqualTo(AuthTokens.kerberos(encodedTicket));
    }

    @Test
    public void fails_to_supply_kerberos_auth_token_when_required_ticket_is_not_provided() {
        Properties missingTicket = new Properties();
        missingTicket.setProperty(BoltProperties.AUTHENTICATION_TYPE, "kerberos");

        assertThatIllegalArgumentException()
            .isThrownBy(new BoltAuthTokenSupplier(missingTicket)::get)
            .withMessage("Cypher Bolt Client setting 'cypher.bolt.auth.base64EncodedTicket' must not be null");
    }

    @Test
    public void supplies_custom_auth_token_when_such_configuration_is_provided() {
        Properties props = new Properties();
        props.setProperty(BoltProperties.AUTHENTICATION_TYPE, "custom");
        props.setProperty(BoltProperties.USERNAME, "user");
        props.setProperty(BoltProperties.PASSWORD, "password");
        props.setProperty(BoltProperties.REALM, "realm");
        props.setProperty(BoltProperties.SCHEME, "scheme");
        BoltAuthTokenSupplier supplier = new BoltAuthTokenSupplier(props);

        assertThat(supplier.get())
            .overridingErrorMessage("Expected custom auth token")
            .isEqualTo(AuthTokens.custom("user", "password", "realm", "scheme"));
    }

    @Test
    public void fails_to_supply_custom_auth_token_when_required_username_is_missing() {
        Properties missingUsername = new Properties();
        missingUsername.setProperty(BoltProperties.AUTHENTICATION_TYPE, "custom");
        missingUsername.setProperty(BoltProperties.PASSWORD, "password");
        missingUsername.setProperty(BoltProperties.REALM, "realm");
        missingUsername.setProperty(BoltProperties.SCHEME, "scheme");

        assertThatIllegalArgumentException()
            .isThrownBy(new BoltAuthTokenSupplier(missingUsername)::get)
            .withMessage("Cypher Bolt Client setting 'cypher.bolt.auth.username' must not be null");
    }

    @Test
    public void fails_to_supply_custom_auth_token_when_required_password_is_missing() {
        Properties missingPassword = new Properties();
        missingPassword.setProperty(BoltProperties.AUTHENTICATION_TYPE, "custom");
        missingPassword.setProperty(BoltProperties.USERNAME, "user");
        missingPassword.setProperty(BoltProperties.REALM, "realm");
        missingPassword.setProperty(BoltProperties.SCHEME, "scheme");

        assertThatIllegalArgumentException()
            .isThrownBy(new BoltAuthTokenSupplier(missingPassword)::get)
            .withMessage("Cypher Bolt Client setting 'cypher.bolt.auth.password' must not be null");
    }

    @Test
    public void fails_to_supply_custom_auth_token_when_required_realm_is_missing() {
        Properties missingRealm = new Properties();
        missingRealm.setProperty(BoltProperties.AUTHENTICATION_TYPE, "custom");
        missingRealm.setProperty(BoltProperties.USERNAME, "user");
        missingRealm.setProperty(BoltProperties.PASSWORD, "password");
        missingRealm.setProperty(BoltProperties.SCHEME, "scheme");

        assertThatIllegalArgumentException()
            .isThrownBy(new BoltAuthTokenSupplier(missingRealm)::get)
            .withMessage("Cypher Bolt Client setting 'cypher.bolt.auth.realm' must not be null");
    }

    @Test
    public void fails_to_supply_custom_auth_token_when_required_scheme_is_missing() {
        Properties missingScheme = new Properties();
        missingScheme.setProperty(BoltProperties.AUTHENTICATION_TYPE, "custom");
        missingScheme.setProperty(BoltProperties.USERNAME, "user");
        missingScheme.setProperty(BoltProperties.PASSWORD, "password");
        missingScheme.setProperty(BoltProperties.REALM, "realm");

        assertThatIllegalArgumentException()
            .isThrownBy(new BoltAuthTokenSupplier(missingScheme)::get)
            .withMessage("Cypher Bolt Client setting 'cypher.bolt.auth.scheme' must not be null");
    }

    @Test
    public void supplies_custom_auth_token_with_extra_settings_when_such_configuration_is_provided() {
        Properties props = new Properties();
        props.setProperty(BoltProperties.AUTHENTICATION_TYPE, "custom");
        props.setProperty(BoltProperties.USERNAME, "user");
        props.setProperty(BoltProperties.PASSWORD, "password");
        props.setProperty(BoltProperties.REALM, "realm");
        props.setProperty(BoltProperties.SCHEME, "scheme");
        props.setProperty("some.extra.settings", "extraordinary");
        BoltAuthTokenSupplier supplier = new BoltAuthTokenSupplier(props);

        assertThat(supplier.get())
            .overridingErrorMessage("Expected custom auth token with extra settings")
            .isEqualTo(AuthTokens.custom("user", "password", "realm", "scheme", newHashMap("some.extra.settings", "extraordinary")));
    }

    private String base64(String payload) {
        return Base64.getEncoder().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
    }
}
