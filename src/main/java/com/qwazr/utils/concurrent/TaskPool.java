/*
 * Copyright 2016-2019 Emmanuel Keller / QWAZR
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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public interface TaskPool {

    int DEFAULT_MAX_CONCURRENT_TASKS = Runtime.getRuntime().availableProcessors() + 1;

    default Future submit(final Runnable task) {
        return submit((Callable<?>) () -> {
            task.run();
            return null;
        });
    }

    <RESULT> Future<RESULT> submit(final Callable<RESULT> task);

    default void onTaskError(Exception exception) {
    }

    int getConcurrentTasks();

    void awaitTermination() throws InterruptedException;

    static TaskPool of() {
        return new WithExecutor();
    }

    static TaskPool of(final int maxConcurrentTasks) {
        return new WithExecutor(maxConcurrentTasks);
    }

    static TaskPool of(final ExecutorService executorService) {
        return new Base(executorService);
    }

    static TaskPool of(final ExecutorService executorService, final int maxConcurrentTasks) {
        return new Base(executorService, maxConcurrentTasks);
    }

    class Base implements TaskPool {

        private final int maxConcurrentTasks;
        private final Semaphore tasksSemaphore;
        private final ExecutorService executorService;

        protected Base(final ExecutorService executorService, final int maxConcurrentTasks) {
            this.maxConcurrentTasks = maxConcurrentTasks;
            this.tasksSemaphore = new Semaphore(maxConcurrentTasks, true);
            this.executorService = executorService;
        }

        protected Base(final ExecutorService executorService) {
            this(executorService, DEFAULT_MAX_CONCURRENT_TASKS);
        }

        protected ExecutorService getExecutorService() {
            return executorService;
        }

        public int getConcurrentTasks() {
            return maxConcurrentTasks - tasksSemaphore.availablePermits();
        }

        @Override
        public <RESULT> Future<RESULT> submit(final Callable<RESULT> task) {
            try {
                tasksSemaphore.acquire();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            try {
                return executorService.submit(() -> {
                    try {
                        return task.call();
                    } catch (final Exception exception) {
                        onTaskError(exception);
                        return null;
                    } finally {
                        tasksSemaphore.release();
                    }
                });
            } catch (
                    RuntimeException e) {
                tasksSemaphore.release();
                throw e;
            }
        }

        @Override
        public synchronized void awaitTermination() throws InterruptedException {
            tasksSemaphore.acquire(maxConcurrentTasks);
            tasksSemaphore.release(maxConcurrentTasks);
        }
    }

    class WithExecutor extends Base implements AutoCloseable {

        protected WithExecutor(int maxConcurrentTasks) {
            super(Executors.newFixedThreadPool(maxConcurrentTasks), maxConcurrentTasks);
        }

        protected WithExecutor() {
            super(Executors.newCachedThreadPool());
        }

        @Override
        public void close() throws Exception {
            ExecutorUtils.close(getExecutorService(), 1, TimeUnit.DAYS);
        }
    }
}
