/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2026 Stephan Pakebusch
               Home page: https://www.omegat.org/
               Support center: https://omegat.org/support

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 OmegaT is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <https://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.gui.editor;

import java.util.LinkedHashMap;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import org.omegat.util.Log;

/**
 * Aggregated render timing of the editor and the segment metadata gutter,
 * for hunting paint cost while typing in large documents.
 *
 * Enabled with the system property omegat.renderlog=true; without it every
 * call is a cheap no-op behind a constant, so shipping the probes costs
 * nothing. When enabled, the probes aggregate counts, total and worst-case
 * durations per phase and flush one summary line per second to the log,
 * so fast typing does not flood it. A phase measurement brackets a paint
 * (begin/end), a counter tallies events inside it, and the type-to-paint
 * latency spans from a document change to the end of the next editor paint.
 *
 * The probes are called from the paint and document paths, mostly but not
 * exclusively on the event dispatch thread (repaint may run anywhere); the
 * fields are synchronized so no probe can corrupt the aggregates.
 *
 * @author Stephan Pakebusch stephan.pakebusch at zollsoft.de
 */
final class EditorRenderStats {

    /** Compile-time style switch: false means every probe is a no-op. */
    static final boolean ENABLED = Boolean.getBoolean("omegat.renderlog");

    private static final long FLUSH_INTERVAL_NANOS = 1_000_000_000L;
    private static final Object LOCK = new Object();

    /** name -> {count, total nanos, max nanos}. */
    private static final Map<String, long[]> PHASES = new LinkedHashMap<>();
    /** name -> count. */
    private static final Map<String, long[]> COUNTERS = new LinkedHashMap<>();
    /** {count, total nanos, max nanos} of the type-to-paint latency. */
    private static final long[] LATENCY = new long[3];

    private static long lastFlush = System.nanoTime();
    /** When the pending document change happened, 0 without one. */
    private static long pendingChange;
    /** When the culprit of a full repaint request was last logged. */
    private static long lastRequesterLog;

    private EditorRenderStats() {
    }

    /** Starts a phase measurement; pass the token to {@link #end}. */
    static long begin() {
        return ENABLED ? System.nanoTime() : 0;
    }

    /** Ends a phase measurement started with {@link #begin}. */
    static void end(String phase, long token) {
        if (!ENABLED) {
            return;
        }
        long duration = System.nanoTime() - token;
        String flushed;
        synchronized (LOCK) {
            long[] slot = PHASES.computeIfAbsent(phase, k -> new long[3]);
            slot[0]++;
            slot[1] += duration;
            slot[2] = Math.max(slot[2], duration);
            flushed = maybeFlush();
        }
        // The log I/O runs outside the lock, so concurrent probes never
        // wait for it.
        if (flushed != null) {
            Log.log(flushed);
        }
    }

    /** Adds to a counter, e.g. rows painted or layout calls. */
    static void count(String counter, long n) {
        if (!ENABLED) {
            return;
        }
        synchronized (LOCK) {
            COUNTERS.computeIfAbsent(counter, k -> new long[1])[0] += n;
        }
    }

    /** A document change happened; the next editor paint answers it. */
    static void documentChanged() {
        if (!ENABLED) {
            return;
        }
        synchronized (LOCK) {
            // Only the first of a burst counts: the paint answers them all.
            if (pendingChange == 0) {
                pendingChange = System.nanoTime();
            }
        }
    }

    /** An editor paint finished; closes a pending type-to-paint latency. */
    static void paintDone() {
        if (!ENABLED) {
            return;
        }
        synchronized (LOCK) {
            if (pendingChange == 0) {
                return;
            }
            long latency = System.nanoTime() - pendingChange;
            pendingChange = 0;
            LATENCY[0]++;
            LATENCY[1] += latency;
            LATENCY[2] = Math.max(LATENCY[2], latency);
        }
    }

    /**
     * A repaint request spanning the whole viewport was scheduled; logs who
     * asked for it, at most once a second: the request runs on the culprit's
     * stack, so this names the code that forces the expensive full paints.
     */
    static void fullRepaintRequested(int requestHeight, int visibleHeight) {
        if (!ENABLED || visibleHeight <= 0 || requestHeight < visibleHeight) {
            return;
        }
        synchronized (LOCK) {
            COUNTERS.computeIfAbsent("fullRepaintRequests", k -> new long[1])[0]++;
            long now = System.nanoTime();
            if (now - lastRequesterLog < FLUSH_INTERVAL_NANOS) {
                return;
            }
            lastRequesterLog = now;
        }
        StringBuilder line = new StringBuilder("RENDER full repaint requested by:");
        StackTraceElement[] stack = new Throwable().getStackTrace();
        // The first frames are this probe and the repaint funnel itself.
        for (int i = 1; i < Math.min(stack.length, 16); i++) {
            line.append("\n    ").append(stack[i]);
        }
        Log.log(line.toString());
    }

    /** Builds the summary line once a second; the caller logs it. */
    private static @Nullable String maybeFlush() {
        long now = System.nanoTime();
        if (now - lastFlush < FLUSH_INTERVAL_NANOS) {
            return null;
        }
        long window = now - lastFlush;
        lastFlush = now;
        StringBuilder line = new StringBuilder("RENDER ");
        line.append(window / 1_000_000).append("ms:");
        for (Map.Entry<String, long[]> phase : PHASES.entrySet()) {
            long[] slot = phase.getValue();
            line.append(' ').append(phase.getKey()).append(' ').append(slot[0]).append("x/")
                    .append(slot[1] / 1_000_000).append("ms(max ")
                    .append(slot[2] / 1_000_000).append("ms)");
        }
        for (Map.Entry<String, long[]> counter : COUNTERS.entrySet()) {
            line.append(' ').append(counter.getKey()).append('=')
                    .append(counter.getValue()[0]);
        }
        if (LATENCY[0] > 0) {
            line.append(" type->paint ").append(LATENCY[0]).append("x avg ")
                    .append(LATENCY[1] / LATENCY[0] / 1_000_000).append("ms max ")
                    .append(LATENCY[2] / 1_000_000).append("ms");
        }
        PHASES.clear();
        COUNTERS.clear();
        LATENCY[0] = 0;
        LATENCY[1] = 0;
        LATENCY[2] = 0;
        return line.toString();
    }
}
