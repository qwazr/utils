/*
 * Copyright 2017-2020 Emmanuel Keller / QWAZR
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

package com.qwazr.utils.test;

import com.qwazr.utils.WaitFor;
import com.qwazr.utils.concurrent.AutoLockSemaphore;
import com.qwazr.utils.concurrent.ExecutorUtils;
import com.qwazr.utils.concurrent.ThreadUtils;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;

public class AutoLockSemaphoreTest {

    void action(final AutoLockSemaphore semaphore,
                final AtomicBoolean done,
                final AtomicInteger runningCount,
                final AtomicInteger concurrentCount,
                final AtomicInteger maxConcurrentCount) {
        runningCount.incrementAndGet();
        try (final AutoLockSemaphore.Lock lock = semaphore.acquire()) {
            final int max = concurrentCount.incrementAndGet();
            synchronized (maxConcurrentCount) {
                maxConcurrentCount.set(Math.max(max, maxConcurrentCount.get()));
            }
            while (!done.get()) {
                ThreadUtils.sleep(RandomUtils.nextInt(250, 500), TimeUnit.MICROSECONDS);
            }
            concurrentCount.decrementAndGet();
        }
    }

    int doTest(final AutoLockSemaphore semaphore, final int threadPoolSize)
            throws InterruptedException {

        final AtomicInteger runningCount = new AtomicInteger(0);
        final ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);
        try {

            final AtomicBoolean done = new AtomicBoolean(false);
            final AtomicInteger concurrentCount = new AtomicInteger(0);
            final AtomicInteger maxConcurrentCount = new AtomicInteger(0);

            for (int i = 0; i < threadPoolSize; i++)
                executorService.submit(() -> action(semaphore, done, runningCount, concurrentCount, maxConcurrentCount));

            WaitFor.of().timeOut(TimeUnit.SECONDS, 1).until(() -> runningCount.get() == threadPoolSize);
            done.set(true);
            WaitFor.of().timeOut(TimeUnit.SECONDS, 1).until(() -> concurrentCount.get() == 0);
            return maxConcurrentCount.get();
        } finally {
            ExecutorUtils.close(executorService, 3, TimeUnit.MINUTES);
            Assert.assertEquals(runningCount.get(), threadPoolSize);
        }
    }

    @Test
    public void testUnlimited() throws InterruptedException {
        Assert.assertEquals(doTest(AutoLockSemaphore.UNLIMITED, 20), 20);
    }

    @Test
    public void testNoPermit() throws InterruptedException {
        Assert.assertEquals(doTest(AutoLockSemaphore.of(0), 20), 0);
    }

    @Test
    public void test1Permits() throws InterruptedException {
        Assert.assertEquals(doTest(AutoLockSemaphore.of(1), 15), 1);
    }

    @Test
    public void test2Permits() throws InterruptedException {
        Assert.assertEquals(doTest(AutoLockSemaphore.of(2), 15), 2);
    }

    @Test
    public void test100Permits() throws InterruptedException {
        Assert.assertEquals(doTest(AutoLockSemaphore.of(100), 200), 100);
    }

}
