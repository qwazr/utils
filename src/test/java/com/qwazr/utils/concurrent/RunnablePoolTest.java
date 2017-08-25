package com.qwazr.utils.concurrent;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

	private void test(RunnablePool<Integer> pool, int count, int expectedTotal) {
		final AtomicInteger counter = new AtomicInteger();
		final AtomicInteger total = new AtomicInteger();
		for (int i = 0; i < count; i++) {
			pool.submit(() -> {
				ThreadUtils.sleep(1, TimeUnit.SECONDS);
				return counter.incrementAndGet();
			});
			pool.collect(total::addAndGet, null);
		}
		Assert.assertEquals(count, counter.get());
		Assert.assertEquals(expectedTotal, total.get());
	}

	@Test
	public void internalExecutorTest() throws IOException {
		try (RunnablePool<Integer> pool = new RunnablePool<>()) {
			test(pool, 8, 36);
		}
	}

	@Test
	public void externalExecutorTest() throws IOException {
		try (RunnablePool<Integer> pool = new RunnablePool<>(executorService)) {
			test(pool, 10, 55);
		}
		Assert.assertFalse(executorService.isShutdown());

	}

	@Test
	public void exceptionsCollectTest() throws IOException {
		try (RunnablePool<Integer> pool = new RunnablePool<>()) {
			pool.submit(() -> {
				throw new RuntimeException("exceptionCollectTest");
			});
			final List<Exception> exceptions = new ArrayList<>();
			pool.collect(null, exceptions::add);
			Assert.assertEquals(1, exceptions.size());
			Assert.assertEquals("exceptionCollectTest", exceptions.get(0).getCause().getMessage());
		}
	}

	@Test(expected = RunnablePoolException.class)
	public void exceptionTest() throws IOException {
		try (RunnablePool<Integer> pool = new RunnablePool<>()) {
			pool.submit(() -> {
				throw new RuntimeException("Test");
			});
		}
	}

	@Test
	public void exceptionsTest() throws IOException {
		try (RunnablePool<Integer> pool = new RunnablePool<>()) {
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
