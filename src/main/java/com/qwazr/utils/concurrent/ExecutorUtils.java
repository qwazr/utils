package com.qwazr.utils.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class ExecutorUtils {

    public static void close(final ExecutorService executorService, final long timeOut, final TimeUnit timeUnit)
            throws InterruptedException {
        if (executorService == null)
            return;
        if (executorService.isTerminated())
            return;
        if (!executorService.isShutdown())
            executorService.shutdown();
        executorService.awaitTermination(timeOut, timeUnit);
    }
}
