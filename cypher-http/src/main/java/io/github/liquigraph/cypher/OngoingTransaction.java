/*
 * Copyright 2014-2016 the original author or authors.
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

import java.util.List;
import java.util.Objects;

public final class OngoingTransaction {
    private final String location;
    private final long expiry;
    private final String commitLocation;
    private final List<ResultData> resultData;

    public OngoingTransaction(String location, long expiry, String commitLocation, List<ResultData> resultData) {
        this.location = location;
        this.expiry = expiry;
        this.commitLocation = commitLocation;
        this.resultData = resultData;
    }

    public String getLocation() {
        return location;
    }

    public long getExpiry() {
        return expiry;
    }

    public String getCommitLocation() {
        return commitLocation;
    }

    public List<ResultData> getResultData() {
        return resultData;
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, expiry, commitLocation, resultData);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final OngoingTransaction other = (OngoingTransaction) obj;
        return Objects.equals(this.location, other.location)
                && Objects.equals(this.expiry, other.expiry)
                && Objects.equals(this.commitLocation, other.commitLocation)
                && Objects.equals(this.resultData, other.resultData);
    }
}
