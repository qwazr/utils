/*
 * Copyright (C) 2017 Emmanuel Keller / JAEKSOFT SARL - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Emmanuel Keller <ekeller@open-search-server.com>
 */

package com.qwazr.utils;

import org.junit.Assert;
import org.junit.Test;

public class PagingTest {

	private void checkResult(Long totalHit, long currentStart, Integer expectedPrev, Integer expectedCurrent,
			Integer expectedNext, int... expectedPages) {
		final Paging paging = new Paging(totalHit, currentStart, 10, 10);
		if (expectedPages.length == 0) {
			Assert.assertNull(paging.getPages());
			Assert.assertNull(paging.getTotalPage());
		} else {
			Assert.assertEquals(expectedPages.length, paging.getTotalPage(), 0);
			Assert.assertEquals(paging.getPages().size(), expectedPages.length, 0);
			int pos = 0;
			for (int expectedPageNumber : expectedPages) {
				Paging.Page page = paging.getPages().get(pos++);
				Assert.assertEquals(expectedPageNumber, page.getNumber());
				Assert.assertEquals((expectedPageNumber - 1) * 10, page.getStart());
			}
		}
		if (expectedPrev == null)
			Assert.assertNull(paging.getPrev());
		else
			Assert.assertEquals(paging.of(expectedPrev - 1), paging.getPrev());
		if (expectedNext == null)
			Assert.assertNull(paging.getNext());
		else
			Assert.assertEquals(paging.of(expectedNext - 1), paging.getNext());
		if (expectedCurrent == null)
			Assert.assertNull(paging.getCurrent());
		else
			Assert.assertEquals(paging.of(expectedCurrent - 1), paging.getCurrent());
	}

	@Test
	public void testEmpty() {
		checkResult(null, 0, null, null, null);
		checkResult(-10L, 0, null, null, null);
		checkResult(0L, 0, null, null, null);
		checkResult(0L, 15, null, null, null);
	}

	@Test
	public void testOnePage() {
		checkResult(1L, 0, null, 1, null, 1);
		checkResult(1L, 5, null, 1, null, 1);
		checkResult(1L, 10, null, 1, null, 1);
		checkResult(1L, 15, null, 1, null, 1);
		checkResult(9L, 0, null, 1, null, 1);
		checkResult(9L, 5, null, 1, null, 1);
		checkResult(9L, 10, null, 1, null, 1);
		checkResult(9L, 15, null, 1, null, 1);
	}

	@Test
	public void testTwoPages() {
		checkResult(15L, 12, 1, 2, null, 1, 2);
		checkResult(15L, 10, 1, 2, null, 1, 2);
		checkResult(15L, 19, 1, 2, null, 1, 2);

	}

	@Test
	public void testMiddlePage() {
		checkResult(30L, 10, 1, 2, 3, 1, 2, 3);
		checkResult(30L, 11, 1, 2, 3, 1, 2, 3);
		checkResult(30L, 19, 1, 2, 3, 1, 2, 3);
	}

	@Test
	public void testLastPage() {
		checkResult(30L, 20, 2, 3, null, 1, 2, 3);
		checkResult(30L, 21, 2, 3, null, 1, 2, 3);
		checkResult(30L, 29, 2, 3, null, 1, 2, 3);
		checkResult(30L, 30, 2, 3, null, 1, 2, 3);
	}
}
