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
package org.liquigraph.trinity.http;

import java.util.Objects;

public final class TransactionUri {

    private final String uri;

    public TransactionUri(String uri) {
        this.uri = uri;
    }

    public String value() {
        return uri;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final TransactionUri other = (TransactionUri) obj;
        return Objects.equals(this.uri, other.uri);
    }

    @Override
    public String toString() {
        return "TransactionUri{" +
            "uri='" + uri + '\'' +
            '}';
    }
}
