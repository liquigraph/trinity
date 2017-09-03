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
package io.github.liquigraph.cypher.http;

import io.github.liquigraph.cypher.Data;
import io.github.liquigraph.cypher.OngoingTransaction;

import java.util.List;
import java.util.Objects;

public class OngoingRemoteTransaction implements OngoingTransaction {
    private final TransactionUri location;
    private final long expiry;
    private final TransactionUri commitLocation;
    private final List<Data> data;

    public OngoingRemoteTransaction(TransactionUri location, long expiry, TransactionUri commitLocation, List<Data> data) {
        this.location = location;
        this.expiry = expiry;
        this.commitLocation = commitLocation;
        this.data = data;
    }

    public TransactionUri getLocation() {
        return location;
    }

    public long getExpiry() {
        return expiry;
    }

    public TransactionUri getCommitLocation() {
        return commitLocation;
    }

    @Override
    public List<Data> getData() {
        return data;
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, expiry, commitLocation, data);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final OngoingRemoteTransaction other = (OngoingRemoteTransaction) obj;
        return Objects.equals(this.location, other.location)
            && Objects.equals(this.expiry, other.expiry)
            && Objects.equals(this.commitLocation, other.commitLocation)
            && Objects.equals(this.data, other.data);
    }

    @Override
    public String toString() {
        return "OngoingRemoteTransaction{" +
            "location=" + location +
            ", expiry=" + expiry +
            ", commitLocation=" + commitLocation +
            ", data=" + data +
            '}';
    }
}
