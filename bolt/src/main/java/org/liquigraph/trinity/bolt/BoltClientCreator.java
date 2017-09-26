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

import org.liquigraph.trinity.CypherClientCreator;
import org.liquigraph.trinity.CypherTransport;
import org.neo4j.driver.v1.AuthToken;
import org.neo4j.driver.v1.Config;
import org.neo4j.driver.v1.GraphDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.liquigraph.trinity.bolt.BoltProperties.BASE_URL;

public class BoltClientCreator implements CypherClientCreator {
    private static final Logger LOGGER = LoggerFactory.getLogger(BoltClient.class);

    private final Function<Properties, Supplier<AuthToken>> authTokenSupplier;
    private final Function<Properties, Supplier<Config>> driverConfigSupplier;

    public BoltClientCreator() {
        this(BoltAuthTokenSupplier::new, BoltDriverConfigurationSupplier::new);
    }

    BoltClientCreator(Function<Properties, Supplier<AuthToken>> authTokenSupplier,
                      Function<Properties, Supplier<Config>> driverConfigSupplier) {

        this.authTokenSupplier = authTokenSupplier;
        this.driverConfigSupplier = driverConfigSupplier;
    }

    @Override
    public boolean supports(CypherTransport transport) {
        return CypherTransport.BOLT == transport;
    }

    @Override
    public BoltClient create(Properties properties) {
        LOGGER.trace("About to instantiate the Bolt client");

        return new BoltClient(GraphDatabase.driver(
            PropertiesReader.readNullableProperty(properties, BASE_URL),
            authTokenSupplier.apply(properties).get(),
            driverConfigSupplier.apply(properties).get()));
    }


}
