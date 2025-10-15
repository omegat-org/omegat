/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Aaron Madlon-Kay
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


package org.omegat.gui.scripting.runner;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.util.Map;

public class StandardScriptRunner extends AbstractScriptRunner {

    @Override
    protected Object doExecuteScript(String script, ScriptEngine engine,
                                     Map<String, Object> additionalBindings) throws ScriptException {

        Bindings bindings = setupBindings(engine, additionalBindings);
        engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);

        Object result = engine.eval(script);

        if (engine instanceof Invocable) {
            invokeGuiScript((Invocable) engine);
        }

        return result;
    }
}
