/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2017 Aaron Madlon-Kay
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * A thin wrapper around the built-in Nashorn JavaScript engine, providing a convenience method for
 * <code>Java.asJSONCompatible(JSON.parse(json))</code>. Use this instead of merely <code>eval</code>-ing
 * untrusted input.
 * <p>
 * <b>Note:</b> Java.asJSONCompatible is only available in Java 1.8.0_60 or later.
 *
 * @see <a href="http://www.oracle.com/technetwork/java/javase/8u60-relnotes-2620227.html">Java 8u60 release
 *      notes</a>
 * @author Aaron Madlon-Kay
 */
public class JsonParser {

    private static final Invocable INVOCABLE;
    static {
        Invocable invocable = null;
        try {
            ScriptEngine jsEngine = new ScriptEngineManager().getEngineByName("javascript");
            jsEngine.eval("function parse(json) { return Java.asJSONCompatible(JSON.parse(json)) }");
            invocable = (Invocable) jsEngine;
        } catch (ScriptException e) {
            Logger.getLogger(JsonParser.class.getName()).log(Level.SEVERE, "Unable to initialize JSON parser",
                    e);
        }
        INVOCABLE = invocable;
    }

    public static Object parse(String json) throws Exception {
        return INVOCABLE.invokeFunction("parse", json);
    }
}
