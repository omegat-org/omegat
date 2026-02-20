/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2011 Briac Pilpre (briacp@gmail.com)
               2013 Alex Buloichik
               2014 Briac Pilpre (briacp@gmail.com), Yu Tang
               2015 Yu Tang, Aaron Madlon-Kay
               2025 Hiroshi Miura
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
package org.omegat.gui.scripting;

import javax.swing.SwingWorker;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

public class ScriptWorker extends SwingWorker<String, Void> {

    private final ScriptingWindow scriptingWindow;
    private final String scriptString;
    private final ScriptItem scriptItem;
    private final Map<String, Object> bindings;
    private volatile long start;

    ScriptWorker(ScriptingWindow scriptingWindow, String scriptString, ScriptItem scriptItem,
            Map<String, Object> bindings) {
        this.scriptingWindow = scriptingWindow;
        this.scriptString = scriptString;
        this.scriptItem = scriptItem;
        this.bindings = bindings;
    }

    @Override
    protected String doInBackground() throws Exception {
        start = System.currentTimeMillis();
        return ScriptRunner.executeScript(scriptString, scriptItem, bindings);
    }

    @Override
    protected void done() {
        long duration = System.currentTimeMillis() - start;
        try {
            String result = get();
            scriptingWindow.logResult(result);
            scriptingWindow.logResultRB("SCW_SCRIPT_DONE", duration);
        } catch (CancellationException e) {
            scriptingWindow.logResultRB("SCW_SCRIPT_CANCELED", duration);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            scriptingWindow.logResultRB(e, "SCW_SCRIPT_ERROR");
        }
    }
}
