/*
 * Copyright 2018-2019 SEBA
 *
 * This file is part of the SEBA AML project.
 * The SEBA AML project is not open-source software. It is owned by SEBA.
 * The SEBA AML project can not be copied and/or distributed without the express permission of SEBA.
 * Any form of modification or reverse-engineering of the SEBA AML project is forbidden.
 */

package com.qwazr.utils;

import org.roaringbitmap.longlong.LongIterator;
import org.roaringbitmap.longlong.Roaring64NavigableMap;

import java.util.Iterator;

public class Bitmap64Partitioner implements Iterator<Roaring64NavigableMap>, Iterable<Roaring64NavigableMap> {

    private final int batchSize;
    private final LongIterator longIterator;

    public Bitmap64Partitioner(final int batchSize, final Roaring64NavigableMap bitmap) {
        if (batchSize <= 0)
            throw new IllegalArgumentException("batchsize must be greater than zero");
        this.batchSize = batchSize;
        longIterator = bitmap.getLongIterator();
    }

    @Override
    public boolean hasNext() {
        return longIterator.hasNext();
    }

    @Override
    public Roaring64NavigableMap next() {
        final Roaring64NavigableMap partition = new Roaring64NavigableMap();
        int i = batchSize;
        while (i-- > 0 && longIterator.hasNext()) {
            partition.addLong(longIterator.next());
        }
        return partition;
    }

    @Override
    public Bitmap64Partitioner iterator() {
        return this;
    }
}
