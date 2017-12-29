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
package org.liquigraph.trinity.neo4jv3;

import org.liquigraph.trinity.Data;
import org.liquigraph.trinity.OngoingTransaction;
import org.neo4j.graphdb.Transaction;

import java.util.List;

public class OngoingLocalTransaction implements OngoingTransaction {

    private final Transaction transaction;
    private final List<Data> data;

    public OngoingLocalTransaction(Transaction transaction, List<Data> data) {
        this.transaction = transaction;
        this.data = data;
    }

    @Override
    public List<Data> getData() {
        return data;
    }

    public Transaction getTransaction() {
        return transaction;
    }
}
