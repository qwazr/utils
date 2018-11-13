/*
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.utils;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class Equalizer<T extends Equalizer> {

    protected Class<T> ownClass;

    protected Equalizer(Class<T> ownClass) {
        this.ownClass = ownClass;
    }

    @JsonIgnore
    protected abstract boolean isEqual(T query);

    @Override
    public int hashCode() {
        return ownClass.hashCode();
    }

    @Override
    public final boolean equals(final Object o) {
        return ownClass.isInstance(o) && (o == this || isEqual(ownClass.cast(o)));
    }
}
