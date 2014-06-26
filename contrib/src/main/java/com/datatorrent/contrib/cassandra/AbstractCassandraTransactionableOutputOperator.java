/*
 * Copyright (c) 2014 DataTorrent, Inc. ALL Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.datatorrent.contrib.cassandra;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.exceptions.DriverException;
import com.datatorrent.lib.db.AbstractBatchTransactionableStoreOutputOperator;

/**
 * <p>
 * Generic Cassandra Output Adaptor which creates a transaction at the start of window.<br/>
 * Executes batch of cql updates and closes the transaction at the end of the window.
 * </p>
 *
 * <p>
 * Each tuple corresponds to an CQL update statement. The operator groups the updates in a batch
 * and submits them with one call to the database. Batch processing improves performance considerably and also provides atomicity.<br/>
 * The size of a batch is equal to the size of the window.
 * </p>
 *
 * <p>
 * The tuples in a window are stored in check-pointed collection which is cleared in the endWindow().
 * This is needed for the recovery. The operator writes a tuple exactly once in the database, which is why
 * only when all the updates are executed, the transaction is committed in the end window call.
 * </p>
 *
 * @param <T>type of tuple</T>
 */
public abstract class AbstractCassandraTransactionableOutputOperator<T> extends AbstractBatchTransactionableStoreOutputOperator<T, CassandraTransactionalStore> {

	public AbstractCassandraTransactionableOutputOperator(){
		super();
	}

	/**
	 * Sets the parameter of the insert/update statement with values from the tuple.
	 *
	 * @param tuple     tuple
   * @return statement The statement to excecute
	 * @throws DriverException
	 */
	protected abstract Statement getUpdateStatement(T tuple) throws DriverException;

	@Override
	public void processBatch(){
    BatchStatement batchCommand = store.getBatchCommand();
    for(T tuple: tuples)
    {
      batchCommand.add(getUpdateStatement(tuple));
    }
	}

}