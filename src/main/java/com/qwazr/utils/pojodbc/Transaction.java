/**   
 * License Agreement for OpenSearchServer Pojodbc
 *
 * Copyright 2008-2013 Emmanuel Keller / Jaeksoft
 * Copyright 2014-2015 OpenSearchServer Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.utils.pojodbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;

import com.qwazr.utils.pojodbc.connection.ConnectionManager;

/**
 * <p>
 * Represents a new database transaction. Currently, a transaction represents a
 * database connection. Further implementation could share same connection.
 * </p>
 * <p>
 * Transaction automatically closed every Query used.
 * </p>
 * <p>
 * That source code is our recommended way to use it. You have close the
 * transaction in a finally statement to be sure that the database connection
 * will be released.
 * </p>
 * 
 * <pre>
 * Transaction transaction = null;
 * try {
 * 	transaction = connectionManager.getNewTransaction(false,
 * 			javax.sql.Connection.TRANSACTION_READ_COMMITTED);
 * 	// ... do everything you need ...
 * } finally {
 * 	if (transaction != null)
 * 		transaction.close();
 * }
 * 
 * </pre>
 * 
 */
public class Transaction {

	private Connection cnx;
	private HashSet<Query> queries;

	public Transaction(Connection cnx, boolean autoCommit,
			Integer transactionIsolation) throws SQLException {
		this.cnx = cnx;
		if (transactionIsolation != null)
			cnx.setTransactionIsolation(transactionIsolation);
		cnx.setAutoCommit(autoCommit);
	}

	void closeQuery(Query query) {
		synchronized (this) {
			query.closeAll();
			queries.remove(query);
		}
	}

	private void closeQueries() {
		synchronized (this) {
			if (queries == null)
				return;
			for (Query query : queries)
				query.closeAll();
			queries.clear();
		}
	}

	/**
	 * Close all queries and the transaction. No commit or rollback are
	 * performed.
	 */
	public void close() {
		synchronized (this) {
			if (cnx == null)
				return;
			synchronized (cnx) {
				closeQueries();
				ConnectionManager.close(null, null, cnx);
				cnx = null;
			}
		}
	}

	/**
	 * Usual JDBC/SQL transaction rollback
	 * 
	 * @throws SQLException
	 *             if any JDBC error occurs
	 */
	public void rollback() throws SQLException {
		synchronized (cnx) {
			cnx.rollback();
		}
	}

	/**
	 * Usual JDBC/SQL transaction commit
	 * 
	 * @throws SQLException
	 *             if any JDBC error occurs
	 */
	public void commit() throws SQLException {
		synchronized (cnx) {
			cnx.commit();
		}
	}

	private void addQuery(Query query) {
		synchronized (this) {
			if (queries == null)
				queries = new HashSet<Query>();
			queries.add(query);
		}
	}

	/**
	 * Create a new Query
	 * 
	 * @param sql
	 *            The native SQL query
	 * @return a new Query instance
	 * @throws SQLException
	 *             if any JDBC error occurs
	 */
	public Query prepare(String sql) throws SQLException {
		Query query = new Query(cnx.prepareStatement(sql));
		addQuery(query);
		return query;
	}

	/**
	 * Create a new Query with autogeneratedkey flag
	 * 
	 * @param sql
	 *            The native SQL query
	 * @return a new Query instance
	 * @throws SQLException
	 *             if any JDBC error occurs
	 */
	public Query prepareWithKeys(String sql) throws SQLException {
		Query query = new Query(cnx.prepareStatement(sql,
				Statement.RETURN_GENERATED_KEYS));
		addQuery(query);
		return query;
	}

	/**
	 * Create a new Query with standard JDBC properties.
	 * <p>
	 * ResultSetType and ResultSetConcureny are JDBC standard parameters like
	 * ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.TYPE_FORWARD_ONLY,
	 * ResultSet.CONCUR_READ_ONLY, ResultSet.CONCUR_UPDATABLE, ...
	 * </p>
	 * 
	 * @param sql
	 *            The native SQL query
	 * @param resultSetType
	 *            A standard JDBC ResultSet type
	 * @param resultSetConcurency
	 *            A standard JDBC Result concurency property
	 * @return a new Query instance
	 * @throws SQLException
	 *             if any JDBC error occurs
	 */
	public Query prepare(String sql, int resultSetType, int resultSetConcurency)
			throws SQLException {
		Query query = new Query(cnx.prepareStatement(sql, resultSetType,
				resultSetConcurency));
		addQuery(query);
		return query;
	}

	/**
	 * A convenient way to directly execute an INSERT/UPDATE/DELETE SQL
	 * statement.
	 * 
	 * @param sql
	 *            The native SQL query
	 * @return the row count
	 * @throws SQLException
	 *             if any JDBC error occurs
	 */
	public int update(String sql) throws SQLException {
		return prepare(sql).update();
	}
}
