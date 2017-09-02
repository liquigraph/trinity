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

import io.github.liquigraph.cypher.CypherClientCreator;
import io.github.liquigraph.cypher.CypherTransport;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.config.Setting;
import org.neo4j.graphdb.factory.GraphDatabaseBuilder;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class EmbeddedClientCreator implements CypherClientCreator {

    private final Map<String, Setting<?>> settings;

    public EmbeddedClientCreator() {
        settings = populateSettings();
    }

    @Override
    public boolean supports(CypherTransport transport) {
        return CypherTransport.EMBEDDED_2 == transport;
    }

    @Override
    public EmbeddedClient create(Properties properties) {
        return new EmbeddedClient(newGraphDatabase(properties));
    }

    private static Map<String, Setting<?>> populateSettings() {
        Field[] fields = GraphDatabaseSettings.class.getFields();
        Map<String, Setting<?>> settings = new HashMap<>((int) Math.ceil(fields.length / 0.75));
        for (Field field : fields) {
            putField(field, settings);
        }
        return settings;
    }

    private GraphDatabaseService newGraphDatabase(Properties properties) {
        String path = readPath(properties);
        GraphDatabaseBuilder builder = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(path);
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            Setting<?> setting = settings.get(entry.getKey());
            if (setting == null) {
                continue;
            }
            builder.setConfig(setting, toString(entry.getValue()));
        }
        return builder.newGraphDatabase();
    }

    private static void putField(Field field, Map<String, Setting<?>> settings) {
        if (!Setting.class.isAssignableFrom(field.getType())) {
            return;
        }
        Setting<?> setting = readField(field);
        if (setting == null) {
            return;
        }
        settings.put(field.getName(), setting);
    }

    private static Setting<?> readField(Field field) {
        try {
            return (Setting<?>)field.get(null);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    private String readPath(Properties properties) {
        String property = properties.getProperty("cypher.embeddedv2.path");
        if (property == null) {
            throw new IllegalArgumentException("Path to Neo4j embedded instance should be set with 'neo4jv2.embedded.path'");
        }
        return property;
    }

    private String toString(Object value) {
        return value == null ? null : value.toString();
    }

}

