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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class PropertiesReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(BoltDriverConfigurationSupplier.class);

    public static String readNullableProperty(Properties properties, String name) {
        return readProperty(properties, name, true);
    }

    public static String readNonNullableProperty(Properties properties, String name) {
        return readProperty(properties, name, false);
    }

    private static String readProperty(Properties properties, String name, boolean nullable) {
        String property = properties.getProperty(name);
        if (property == null && !nullable) {
            LOGGER.error("Non-nullable property {} is null", name);
            throw new IllegalArgumentException(String.format("Cypher Bolt Client setting '%s' must not be null", name));
        }
        return property;
    }
}
