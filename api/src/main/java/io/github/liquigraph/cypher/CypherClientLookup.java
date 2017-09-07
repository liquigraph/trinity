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
package io.github.liquigraph.cypher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.ServiceLoader;

public final class CypherClientLookup {
    private static final Logger LOGGER = LoggerFactory.getLogger(CypherClientLookup.class);

    private final Iterator<CypherClientCreator> services;

    public CypherClientLookup() {
        services = ServiceLoader.load(CypherClientCreator.class).iterator();
    }

    public CypherClient<? extends OngoingTransaction> getInstance(CypherTransport transport, Properties configuration) {
        LOGGER.debug("Starting Cypher client discovery for transport {}", transport);
        while (services.hasNext()) {
            CypherClientCreator creator = services.next();
            if (creator.supports(transport)) {
                LOGGER.info("Found implementation of type {} for transport {}", creator.getClass(), transport);
                return creator.create(configuration);
            }
        }
        LOGGER.error("No implementations could be found for {}", transport);
        throw new NoSuchElementException(
            String.format("Could not find any Cypher Client supporting transport: %s", transport)
        );
    }
}
