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

public class Optional<T> {

    private static final Optional<?> EMPTY = new Optional<>(null);

    private final T value;

    private Optional(T value) {
        this.value = value;
    }

    public static <T> Optional<T> ofNullable(T value) {
        if (value == null) {
            return empty();
        }
        return new Optional<>(value);
    }

    public static <T> Optional<T> of(T value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }
        return new Optional<>(value);
    }

    @SuppressWarnings("unchecked")
    public static <T> Optional<T> empty() {
        return (Optional<T>) EMPTY;
    }

    public T orElse(T defaultValue) {
        if (!isPresent()) {
            return defaultValue;
        }
        return value;
    }

    public T get() {
        if (!isPresent()) {
            throw new IllegalStateException("Cannot call get() on empty optional");
        }
        return value;
    }

    public boolean isPresent() {
        return value != null;
    }
}
