/**************************************************************************
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
 **************************************************************************/
package org.omegat.core.threads;

/**
 * Represents the completion status of a task,
 * including success, cancellation, or failure.
 *
 * @author Hiroshi Miura
 */
public final class Completion {

    /**
     * Enumeration of possible completion statuses.
     */
    public enum Status {
        /**
         * Represents a successful completion.
         */
        SUCCESS,
        /**
         * Represents a cancelled.
         */
        CANCELLED,
        /**
         * Represents a failed.
         */
        FAILED
    }

    private final Completion.Status status;
    private final Throwable error;

    private Completion(Status status, Throwable error) {
        this.status = status;
        this.error = error;
    }

    /**
     * Check if status is SUCCESS.
     *
     * @return true if the completion status is SUCCESS, false otherwise
     */
    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    /**
     * Check if status is CANCELLED.
     * @return true if the completion status is CANCELLED, false otherwise
     */
    public boolean isCancelled() {
        return status == Status.CANCELLED;
    }

    /**
     * Check if status is FAILED.
     * @return true if the completion status is FAILED, false otherwise
     */
    public boolean isFailed() {
        return status == Status.FAILED;
    }

    /**
     * Returns the cause of the failure, if any.
     *
     * @return the {@link Throwable} describing the cause of failure,
     * or null if the completion was successful or canceled
     */
    public Throwable getCause() {
        return error;
    }

    /**
     * Creates and returns a {@link Completion} instance representing a successful completion.
     *
     * @return a {@link Completion} object with a {@code Status.SUCCESS} state and no error.
     */
    public static Completion success() {
        return new Completion(Status.SUCCESS, null);
    }

    /**
     * Creates and returns a {@link Completion} instance representing a cancelled operation.
     *
     * @return a {@link Completion} object with a {@code Status.CANCELLED} state and no associated error.
     */
    public static Completion cancelled() {
        return new Completion(Status.CANCELLED, null);
    }

    /**
     * Creates and returns a {@link Completion} instance representing a failed operation.
     *
     * @param error the {@link Throwable} describing the cause of failure, may be null
     * @return a {@link Completion} object with a {@code Status.FAILED} state and the provided error
     */
    public static Completion failed(Throwable error) {
        return new Completion(Status.FAILED, error);
    }
}
