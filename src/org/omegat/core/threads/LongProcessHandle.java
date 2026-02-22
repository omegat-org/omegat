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
import java.util.concurrent.Future;

/**
 * A handle returned to callers when a long-running task is started.
 * Provides a single, consistent way to cancel and to await completion.
 */
public final class LongProcessHandle<T> {
    private final CancellationToken token;
    private final Future<?> future;
    private final CompletableFuture<T> completion;

    LongProcessHandle(CancellationToken token, Future<?> future, CompletableFuture<T> completion) {
        this.token = Objects.requireNonNull(token, "token");
        this.future = Objects.requireNonNull(future, "future");
        this.completion = Objects.requireNonNull(completion, "completion");
    }

    public CancellationToken token() {
        return token;
    }

    public CompletableFuture<T> completion() {
        return completion;
    }

    /**
     * Requests cancellation cooperatively and interrupts the worker thread.
     */
    public void cancel() {
        token.cancel();
        future.cancel(true);
    }
}
