/*
 * Copyright 2018-2019 SEBA
 *
 * This file is part of the SEBA AML project.
 * The SEBA AML project is not open-source software. It is owned by SEBA.
 * The SEBA AML project can not be copied and/or distributed without the express permission of SEBA.
 * Any form of modification or reverse-engineering of the SEBA AML project is forbidden.
 */
package com.qwazr.utils;

import org.roaringbitmap.longlong.Roaring64NavigableMap;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public class Bitmap64Provider implements
        MessageBodyReader<Roaring64NavigableMap>,
        MessageBodyWriter<Roaring64NavigableMap> {

    @Override
    public boolean isReadable(final Class<?> type, Type genericType,
                              final Annotation[] annotations,
                              final MediaType mediaType) {
        return type == Roaring64NavigableMap.class && mediaType.isCompatible(MediaType.APPLICATION_OCTET_STREAM_TYPE);
    }

    @Override
    public Roaring64NavigableMap readFrom(final Class<Roaring64NavigableMap> type,
                                          final Type genericType,
                                          final Annotation[] annotations,
                                          final MediaType mediaType,
                                          final MultivaluedMap<String, String> httpHeaders,
                                          final InputStream entityStream) throws IOException, WebApplicationException {
        try (final DataInputStream input = new DataInputStream(entityStream)) {
            final Roaring64NavigableMap bitmap = new Roaring64NavigableMap();
            bitmap.deserialize(input);
            return bitmap;
        }
    }

    @Override
    public boolean isWriteable(final Class<?> type,
                               final Type genericType,
                               final Annotation[] annotations,
                               final MediaType mediaType) {
        return type == Roaring64NavigableMap.class && mediaType.isCompatible(MediaType.APPLICATION_OCTET_STREAM_TYPE);
    }

    @Override
    public void writeTo(final Roaring64NavigableMap bitmap,
                        final Class<?> type,
                        final Type genericType,
                        final Annotation[] annotations,
                        final MediaType mediaType,
                        final MultivaluedMap<String, Object> httpHeaders,
                        final OutputStream entityStream) throws IOException, WebApplicationException {
        try (final DataOutputStream output = new DataOutputStream(entityStream)) {
            bitmap.runOptimize();
            bitmap.serialize(output);
        }
    }
}
