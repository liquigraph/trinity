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

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class ClosedTransaction {

    private final List<ResultData> resultData;
    private final boolean rolledBack;

    public static final ClosedTransaction ROLLED_BACK = new ClosedTransaction(Collections.<ResultData>emptyList(), true);

    public ClosedTransaction(List<ResultData> resultData, boolean rolledBack) {
        this.resultData = resultData;
        this.rolledBack = rolledBack;
    }

    public List<ResultData> getResultData() {
        return resultData;
    }

    public boolean isRolledBack() {
        return rolledBack;
    }

    @Override
    public int hashCode() {
        return Objects.hash(resultData, rolledBack);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final ClosedTransaction other = (ClosedTransaction) obj;
        return Objects.equals(this.resultData, other.resultData)
            && Objects.equals(this.rolledBack, other.rolledBack);
    }
}
