package com.qwazr.utils.concurrent;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class RunnablePoolTest {

	private static ExecutorService executorService;

	@BeforeClass
	public static void setup() {
		executorService = Executors.newCachedThreadPool();
	}

	@AfterClass
	public static void cleanup() {
		executorService.shutdown();
	}

	@Test
	public void internalExecutorTest() throws IOException {
		final AtomicInteger counter = new AtomicInteger();
		try (RunnablePool pool = new RunnablePool()) {
			for (int i = 0; i < 10; i++) {
				pool.submit(() -> {
					ThreadUtils.sleep(1, TimeUnit.SECONDS);
					counter.incrementAndGet();
				});
			}
		}
		Assert.assertEquals(10, counter.get());
	}

	@Test
	public void externalExecutorTest() throws IOException {
		AtomicInteger counter = new AtomicInteger();
		try (RunnablePool pool = new RunnablePool(executorService)) {
			for (int i = 0; i < 8; i++) {
				pool.submit(() -> {
					ThreadUtils.sleep(1, TimeUnit.SECONDS);
					counter.incrementAndGet();
				});
			}
		}
		Assert.assertEquals(8, counter.get());
		Assert.assertFalse(executorService.isShutdown());
	}

	@Test(expected = RunnablePoolException.class)
	public void exceptionTest() throws IOException {
		try (RunnablePool pool = new RunnablePool()) {
			pool.submit(() -> {
				throw new RuntimeException("Test");
			});
		}
	}

	@Test
	public void exceptionsTest() throws IOException {
		try (RunnablePool pool = new RunnablePool()) {
			pool.submit(() -> {
				throw new RuntimeException("Error Test");
			});
		} catch (RunnablePoolException e) {
			Assert.assertEquals(1, e.getExceptions().size());
			Assert.assertEquals("Error Test", e.getExceptions().get(0).getCause().getMessage());
			return;
		}
		Assert.fail("RunnablePoolException not thrown");
	}
}
