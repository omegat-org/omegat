/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2017 Aaron Madlon-Kay
               Home page: http://www.omegat.org/
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
public final class JsonParser {

    private JsonParser() {
    }

    private static final Invocable INVOCABLE;
    static {
        Invocable invocable = null;
        try {
            ScriptEngine jsEngine = new ScriptEngineManager().getEngineByName("javascript");
            jsEngine.eval("function parse(json) { return Java.asJSONCompatible(JSON.parse(json)) }");
            invocable = (Invocable) jsEngine;
        } catch (ScriptException e) {
            Logger.getLogger(JsonParser.class.getName()).log(Level.SEVERE, "Unable to initialize JSON parser", e);
        }
        INVOCABLE = invocable;
    }

    public static Object parse(String json) throws Exception {
        return INVOCABLE.invokeFunction("parse", json);
    }

    /** Returns a quoted String suitable to use in JSON. */
    public static String quote(String string) {

        if (string == null || string.isEmpty()) {
            return "\"\"";
        }
        StringBuilder sb = new StringBuilder();
        sb.append('"');

        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            // https://tools.ietf.org/html/rfc7159.html#section-7
            // unescaped = %x20-21 / %x23-5B / %x5D-10FFFF
            if (c >= 0x20 && c != 0x22 && c != 0x5c && c <= 0x10ffff) {
                sb.append(c);
            } else {
                switch (c) {
                case '"':
                case '\\':
                    sb.append('\\');
                    sb.append(c);
                    break;
                case '\b':
                    sb.append('\\');
                    sb.append('b');
                    break;
                case '\f':
                    sb.append('\\');
                    sb.append('f');
                    break;
                case '\n':
                    sb.append('\\');
                    sb.append('n');
                    break;
                case '\r':
                    sb.append('\\');
                    sb.append('r');
                    break;
                case '\t':
                    sb.append('\\');
                    sb.append('t');
                    break;
                default:
                    String hex = "000" + Integer.toHexString(c);
                    sb.append("\\u").append(hex.substring(hex.length() - 4));
                }
            }
        }

        sb.append('"');
        return sb.toString();
    }

}
