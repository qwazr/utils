/*
 * Copyright 2015-2020 Emmanuel Keller / QWAZR
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
package com.qwazr.utils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public interface TimeTracker {

    void next(String name);

    Status getStatus();

    static TimeTracker withDurations() {
        return new WithDurations();
    }

    static TimeTracker noDurations() {
        return new NoDurations();
    }

    class NoDurations implements TimeTracker {

        protected final Date startTime;
        protected volatile long time;

        protected NoDurations() {
            startTime = new Date();
            time = startTime.getTime();
        }

        @Override
        public void next(String name) {
            time = System.currentTimeMillis();
        }

        @Override
        public synchronized Status getStatus() {
            return new Status(startTime, time, null, null);
        }
    }

    class WithDurations extends NoDurations {

        private volatile long unknownTime;
        private final LinkedHashMap<String, Long> entries;

        /**
         * Initiate the time tracker and collect the starting time.
         */
        protected WithDurations() {
            entries = new LinkedHashMap<>();
            unknownTime = 0;
        }

        /**
         * Add a new entry, with the given name and the elapsed time.
         *
         * @param name the name of the time entry
         */
        public synchronized void next(final String name) {
            final long t = System.currentTimeMillis();
            final long elapsed = t - time;
            if (name != null) {
                Long duration = entries.get(name);
                if (duration == null)
                    duration = elapsed;
                else
                    duration += elapsed;
                entries.put(name, duration);
            } else
                unknownTime += elapsed;
            time = t;
        }

        public synchronized Status getStatus() {
            return new Status(startTime, time, unknownTime, new LinkedHashMap<>(entries));
        }
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(
            creatorVisibility = JsonAutoDetect.Visibility.NONE,
            getterVisibility = JsonAutoDetect.Visibility.NONE,
            setterVisibility = JsonAutoDetect.Visibility.NONE,
            isGetterVisibility = JsonAutoDetect.Visibility.NONE,
            fieldVisibility = JsonAutoDetect.Visibility.NONE)
    class Status extends Equalizer.Immutable<Status> {

        @JsonProperty("start_time")
        final public Date startTime;

        @JsonProperty("total_time")
        final public Long totalTime;

        @JsonProperty("unknown_time")
        final public Long unknownTime;

        @JsonProperty("durations")
        final public LinkedHashMap<String, Long> durations;

        @JsonCreator
        Status(@JsonProperty("start_time") Date startTime,
               @JsonProperty("total_time") Long totalTime,
               @JsonProperty("unknown_time") Long unknownTime,
               @JsonProperty("durations") LinkedHashMap<String, Long> durations) {
            super(Status.class);
            this.startTime = startTime;
            this.totalTime = totalTime;
            this.unknownTime = unknownTime;
            this.durations = durations;
        }

        public Date getStartTime() {
            return startTime == null ? null : new Date(startTime.getTime());
        }

        public Long getTotalTime() {
            return totalTime;
        }

        public Long getUnknownTime() {
            return unknownTime;
        }

        public Map<String, Long> getDurations() {
            return durations;
        }

        @Override
        protected int computeHashCode() {
            return Objects.hash(startTime, totalTime, unknownTime);
        }

        @Override
        protected boolean isEqual(final Status s) {
            return Objects.equals(startTime, s.startTime)
                    && Objects.equals(totalTime, s.totalTime)
                    && Objects.equals(unknownTime, s.unknownTime)
                    && Objects.equals(durations, s.durations);
        }
    }
}
