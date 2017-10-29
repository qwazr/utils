/*
 * Copyright (C) 2017 Emmanuel Keller / JAEKSOFT SARL - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Emmanuel Keller <ekeller@open-search-server.com>
 */
package com.qwazr.utils;

import java.util.ArrayList;
import java.util.List;

public class Paging {

	private final Page prev;
	private final Page next;
	private final Page current;
	private final List<Page> pages;
	private final Integer totalPages;
	private final Integer pageSize;

	public Paging(Long totalHit, long currentStart, int pageSize, int numberOfPages) {
		this.pageSize = pageSize;
		if (totalHit == null || totalHit <= 0) {
			prev = null;
			next = null;
			current = null;
			pages = null;
			totalPages = null;
			return;
		}

		totalPages = (int) ((totalHit + (pageSize - 1)) / pageSize);

		int currentPage = currentStart == 0 ? 0 : (int) currentStart / pageSize;
		if (currentPage >= totalPages)
			currentPage = totalPages - 1;

		int startPage = currentPage - (numberOfPages / 2);
		if (startPage < 0)
			startPage = 0;

		int endPage = currentPage + pageSize;
		if (endPage > totalPages)
			endPage = totalPages;

		this.pages = new ArrayList<>(endPage - startPage);

		for (int i = startPage; i < endPage; i++) {
			final Page page = new Page(i);
			pages.add(page);
			if (page.start + pageSize >= totalHit)
				break;
		}
		this.current = new Page(currentPage);
		this.prev = currentPage > 0 ? new Page(currentPage - 1) : null;
		this.next = currentPage + 1 >= totalPages ? null : new Page(currentPage + 1);
	}

	public Page getPrev() {
		return prev;
	}

	public Page getNext() {
		return next;
	}

	public Page getCurrent() {
		return current;
	}

	public Integer getTotalPage() {
		return totalPages;
	}

	public List<Page> getPages() {
		return pages;
	}

	Page of(int page) {
		return new Page(page);
	}

	public class Page {

		private final long start;
		private final int number;

		Page(int page) {
			this.start = page * pageSize;
			this.number = page + 1;
		}

		public long getStart() {
			return start;
		}

		public int getNumber() {
			return number;
		}

		public boolean isCurrent() {
			return equals(current);
		}

		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof Page))
				return false;
			if (o == this)
				return true;
			final Page p = (Page) o;
			return p.start == start && p.number == number;
		}

		@Override
		public String toString() {
			return number + " - " + start;
		}

	}
}
