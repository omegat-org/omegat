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

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Cooperative cancellation token for long-running tasks.
 * Task code should call {@link #throwIfCancelled()} at safe points.
 */
public class CancellationToken {
    private final AtomicBoolean cancelled = new AtomicBoolean(false);

    void cancel() {
        cancelled.set(true);
    }

    public boolean isCancelled() {
        return cancelled.get();
    }

    public void throwIfCancelled() throws LongProcessInterruptedException {
        if (isCancelled() || Thread.currentThread().isInterrupted()) {
            throw new LongProcessInterruptedException();
        }
    }

    public static CancellationToken none() {
        return new CancellationToken();
    }

    @Override
    public String toString() {
        return "CancellationToken(cancelled=" + cancelled.get() + ")";
    }
}
