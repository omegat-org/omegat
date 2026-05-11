/*
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2026 Hiroshi Miura
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
 */
package org.omegat.core.threads;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Centralized executor for long-running background work.
 * <p>
 * Why this exists:
 * - Avoids ad-hoc Thread subclasses
 * - Provides a uniform cancellation and completion story
 * - Makes testing and waiting consistent (via CompletableFuture)
 */
public final class LongProcessExecutor implements AutoCloseable {

    private static final int CORE_POOL_SIZE = 5;
    private static final int MAX_POOL_SIZE = 20;
    private static final long KEEP_ALIVE_SECONDS = 60L;

    @FunctionalInterface
    public interface CancellableSupplier<T> {
        T get(CancellationToken token) throws Exception;
    }

    private final ExecutorService executor;

    public LongProcessExecutor(String threadNamePrefix) {
        Objects.requireNonNull(threadNamePrefix, "threadNamePrefix");
        this.executor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_SECONDS,
                TimeUnit.SECONDS, new SynchronousQueue<>(), new NamedThreadFactory(threadNamePrefix));
    }

    public <T> LongProcessHandle<T> submit(CancellableSupplier<T> work) {
        Objects.requireNonNull(work, "work");

        CancellationToken token = new CancellationToken();
        CompletableFuture<T> completion = new CompletableFuture<>();

        Future<?> future = executor.submit(() -> {
            try {
                T result = work.get(token);
                completion.complete(result);
            } catch (Throwable t) {
                completion.completeExceptionally(t);
            }
        });

        return new LongProcessHandle<>(token, future, completion);
    }

    @Override
    public void close() {
        executor.shutdownNow();
    }

    /**
     * Custom thread factory for debugging and monitoring.
     */
    private static class NamedThreadFactory implements ThreadFactory {
        private final AtomicInteger counter = new AtomicInteger(0);
        private final String prefix;

        NamedThreadFactory(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, prefix + "-" + counter.incrementAndGet());
            t.setDaemon(true);  // Don't prevent JVM shutdown
            return t;
        }
    }
}
