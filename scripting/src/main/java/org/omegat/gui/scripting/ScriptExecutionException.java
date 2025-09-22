package org.omegat.gui.scripting;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class ScriptExecutionException extends IOException {

    public static class ScriptError {
        final ScriptItem scriptItem;
        final IOException error;

        ScriptError(ScriptItem scriptItem, IOException error) {
            this.scriptItem = scriptItem;
            this.error = error;
        }
    }

    private final List<ScriptError> errors;

    public ScriptExecutionException(String message, List<ScriptError> errors) {
        super(message);
        this.errors = errors;
    }

    public List<ScriptError> getErrors() {
        return Collections.unmodifiableList(errors);
    }
}

