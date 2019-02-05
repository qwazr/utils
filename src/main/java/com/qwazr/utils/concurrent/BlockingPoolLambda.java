/*
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.utils.concurrent;

import com.qwazr.utils.LoggerUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BlockingPoolLambda<T> extends TaskPool.WithExecutor implements AutoCloseable {

    private final static Logger LOGGER = LoggerUtils.getLogger(BlockingPoolLambda.class);

    private final Logger logger;
    private final List<Future<T>> futures;
    private final ConsumerEx<Collection<T>, Exception> resultsConsumer;
    private volatile int futureSize;

    public BlockingPoolLambda(final Logger logger, final int size, final ConsumerEx<Collection<T>, Exception> results) {
        super(size);
        this.logger = logger == null ? LOGGER : logger;
        futures = new ArrayList<>();
        resultsConsumer = results == null ? t -> {
        } : results;
        futureSize = 0;
    }

    public BlockingPoolLambda(int size, ConsumerEx<Collection<T>, Exception> results) {
        this(null, size, results);
    }

    private void collect(boolean checkAll) throws Exception {
        final Collection<T> results = new ArrayList<>();
        synchronized (futures) {
            final Iterator<Future<T>> i = futures.iterator();
            while (i.hasNext()) {
                final Future<T> f = i.next();
                if (checkAll || f.isDone()) {
                    try {
                        results.add(f.get());
                    } catch (ExecutionException e) {
                        logger.log(Level.WARNING, e.getMessage(), e.getCause());
                    }
                    i.remove();
                    futureSize--;
                }
            }
        }
        resultsConsumer.accept(results);
    }

    public void submit(SupplierEx<T, Exception> callable) throws Exception {
        synchronized (futures) {
            futures.add(super.submit(callable::get));
            futureSize = futures.size();
        }
        collect(false);
    }

    public int size() {
        return futureSize;
    }

    @Override
    public synchronized void close() throws Exception {
        super.close();
        try {
            collect(true);
        } catch (final IOException e) {
            throw e;
        } catch (final Exception e) {
            throw new IOException(e);
        }
    }
}
