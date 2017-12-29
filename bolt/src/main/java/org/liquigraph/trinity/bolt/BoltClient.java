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

import org.liquigraph.trinity.ClosedTransaction;
import org.liquigraph.trinity.CypherClient;
import org.liquigraph.trinity.Data;
import org.liquigraph.trinity.Either;
import org.liquigraph.trinity.Fault;
import org.liquigraph.trinity.Row;
import org.liquigraph.trinity.internal.FunctionalEither;
import org.liquigraph.trinity.internal.collection.Lists;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.exceptions.ClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BoltClient implements CypherClient<BoltTransaction>, AutoCloseable {

   static {
      SLF4JBridgeHandler.removeHandlersForRootLogger();
      SLF4JBridgeHandler.install();
   }

   private static final Logger LOGGER = LoggerFactory.getLogger(BoltClient.class);
   private final Driver driver;

   public BoltClient(Driver driver) {
      this.driver = driver;
   }

   @Override
   public FunctionalEither<List<Fault>, List<Data>> runSingleTransaction(String query, String... queries) {
      try (Session session = driver.session();
           Transaction tx = session.beginTransaction()) {
         List<String> allQueries = Lists.prepend(query, queries);
         LOGGER.debug("About to run {} queries in a single transaction", allQueries.size());
         FunctionalEither<List<Fault>, List<Data>> result = executeQueriesInTransaction(tx, allQueries);
         tx.success();
         return result;
      }
   }

   @Override
   public FunctionalEither<List<Fault>, BoltTransaction> openTransaction(String... queries) {
      Session session = driver.session();
      Transaction tx = session.beginTransaction();
      List<String> allQueries = Arrays.asList(queries);
      LOGGER.debug("About to open a transaction and run {} queries", allQueries.size());
      return executeQueriesInTransaction(tx, allQueries)
               .mapRight(data -> new BoltTransaction(session, tx, data));
   }

   @Override
   public FunctionalEither<List<Fault>, BoltTransaction> execute(BoltTransaction transaction, String... queries) {
      Transaction tx = transaction.getTransaction();
      List<String> allQueries = Arrays.asList(queries);
      LOGGER.debug("About to run {} queries in currently open transaction", allQueries.size());
      return executeQueriesInTransaction(tx, allQueries)
            .mapRight(data -> new BoltTransaction(transaction.getSession(), tx, data));
   }

   @Override
   public FunctionalEither<List<Fault>, ClosedTransaction> commit(BoltTransaction transaction, String... queries) {
      try (Session ignored = transaction.getSession();
           Transaction tx = transaction.getTransaction()) {

         List<String> allQueries = Arrays.asList(queries);
         LOGGER.debug("About to run {} queries and commit open transaction", allQueries.size());
         return executeQueriesInTransaction(tx, allQueries)
               .mapRight(data -> {
                  tx.success();
                  return new ClosedTransaction(data, false);
               });
      }
   }

   @Override
   public FunctionalEither<List<Fault>, ClosedTransaction> rollback(BoltTransaction transaction) {
      LOGGER.debug("About to roll back open transaction");
      try (Session ignored = transaction.getSession();
           Transaction tx = transaction.getTransaction()) {

         tx.failure();
         return FunctionalEither.right(ClosedTransaction.ROLLED_BACK);
      }
   }

   @Override
   public void close() {
      driver.close();
   }

   private FunctionalEither<List<Fault>, List<Data>> executeQueriesInTransaction(Transaction tx, List<String> allQueries) {
      List<Either<Fault, Data>> results = allQueries.stream()
            .map(cypherQuery -> executeQuery(tx, cypherQuery))
            .map(executionResult -> executionResult.mapRight(this::toData))
            .collect(Collectors.toList());

      List<Fault> faults = results.stream()
            .filter(Either::isLeft)
            .map(Either::getLeft)
            .collect(Collectors.toList());

      if (!faults.isEmpty()) {
         LOGGER.warn("{} / {} query executions failed", faults.size(), allQueries.size());
         return FunctionalEither.left(faults);
      }
      return FunctionalEither.right(results.stream()
            .filter(Either::isRight)
            .map(Either::getRight)
            .collect(Collectors.toList()));
   }

   private FunctionalEither<Fault, StatementResult> executeQuery(Transaction transaction, String cypherQuery) {
      try {
         StatementResult result = transaction.run(cypherQuery);
         if (!result.hasNext() /* force eager execution */) {
            LOGGER.trace("Nothing to fetch.");
         }
         return FunctionalEither.right(result);
      } catch (ClientException ce) {
         LOGGER.error("An unexpected error happened while executing the query", ce);
         return FunctionalEither.left(new Fault(ce.code(), ce.getMessage()));
      }
   }

   private Data toData(StatementResult input) {
      return new Data(
          input.keys(),
          input.list().stream().map(record -> new Row(record.asMap()))
              .collect(Collectors.toList())
      );
   }
}
