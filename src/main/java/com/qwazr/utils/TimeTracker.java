/*
 * Copyright 2015-2018 Emmanuel Keller / QWAZR
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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class TimeTracker {

    private final long startTime;
    private volatile long time;
    private volatile long unknownTime;

    private final LinkedHashMap<String, Long> entries;
    private volatile LinkedHashMap<String, Long> cachedEntries;

    /**
     * Initiate the time tracker and collect the starting time.
     */
    public TimeTracker() {
        entries = new LinkedHashMap<>();
        startTime = time = System.currentTimeMillis();
        unknownTime = 0;
        cachedEntries = null;
    }

    /**
     * Add a new entry, with the given name and the elapsed time.
     *
     * @param name the name of the time entry
     */
    public synchronized void next(String name) {
        final long t = System.currentTimeMillis();
        final long elapsed = t - time;
        if (name != null) {
            Long duration = entries.get(name);
            if (duration == null)
                duration = elapsed;
            else
                duration += elapsed;
            entries.put(name, duration);
            cachedEntries = null;
        } else
            unknownTime += elapsed;
        time = t;
    }

    /**
     * @return the backed map
     */
    private synchronized LinkedHashMap<String, Long> buildCache() {
        if (cachedEntries == null)
            cachedEntries = new LinkedHashMap<>(entries);
        return cachedEntries;
    }

    public Status getStatus() {
        return new Status(this);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Status {

        @JsonProperty("start_time")
        final public Date startTime;

        @JsonProperty("total_time")
        final public Long totalTime;

        @JsonProperty("unknown_time")
        final public Long unknownTime;

        final public LinkedHashMap<String, Long> durations;

        @JsonCreator
        Status(@JsonProperty("start_time") Date startTime, @JsonProperty("total_time") Long totalTime,
               @JsonProperty("unknown_time") Long unknownTime,
               @JsonProperty("durations") LinkedHashMap<String, Long> durations) {
            this.startTime = startTime;
            this.totalTime = totalTime;
            this.unknownTime = unknownTime;
            this.durations = durations;
        }

        private Status(TimeTracker timeTracker) {
            this(new Date(timeTracker.startTime), timeTracker.time - timeTracker.startTime, timeTracker.unknownTime,
                    timeTracker.buildCache());
        }

        @JsonIgnore
        public Date getStartTime() {
            return startTime == null ? null : new Date(startTime.getTime());
        }

        @JsonIgnore
        public Long getTotalTime() {
            return totalTime;
        }

        @JsonIgnore
        public Long getUnknownTime() {
            return unknownTime;
        }

        public Map<String, Long> getDurations() {
            return durations;
        }

        @Override
        public int hashCode() {
            return Objects.hash(startTime, totalTime, unknownTime);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Status))
                return false;
            if (o == this)
                return true;
            final Status s = (Status) o;
            return Objects.equals(startTime, s.startTime) && Objects.equals(totalTime, s.totalTime) &&
                    Objects.equals(unknownTime, s.unknownTime) && CollectionsUtils.equals(durations, s.durations);
        }
    }
}
