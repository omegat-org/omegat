/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Utils for calculate used memory.
 * 
 * Calculation of object size is not perfect. It doesn't support multiple links
 * to one object instance(object shares), memory alignments.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class MemoryUtils {
    /** JVM architecture. */
    final protected static boolean IS64 = !"32".equals(System.getProperty("sun.arch.data.model"));

    /** Object footprint - 16 for 64bit, 8 for 32 bit. */
    final protected static int SZ_OBJFOOT = IS64 ? 16 : 8;

    /** Link to object - 8 for 64bit, 4 for 32bit. */
    final protected static int SZ_OBJLINK = IS64 ? 8 : 4;

    /**
     * Get memory which used by jvm.
     * 
     * @return memory size
     */
    public static long getMemoryUsed() {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }

    /**
     * Get memory allocated by jvm.
     * 
     * @return memory size
     */
    public static long getMemoryAllocated() {
        return Runtime.getRuntime().totalMemory();
    }

    /**
     * Get maximum memory which can be allocated by jvm.
     * 
     * @return memory size
     */
    public static long getMemoryLimit() {
        return Runtime.getRuntime().maxMemory();
    }

    /**
     * Calculate memory usage for object.
     * 
     * @param obj
     *            object
     * @return memory size, or -1 if size is unknown
     * @throws Exception
     */
    public static long calcObjectSize(Object obj) {
        if (obj == null) {
            return 0;
        }

        Class<?> oc = obj.getClass();
        if (oc == String.class) {
            // make calculation for String faster
            String s = (String) obj;
            return SZ_OBJFOOT + SZ_OBJLINK + 4 + 4 + 4 + s.length() * 2;
        }

        long result;

        try {
            if (oc.isArray()) {
                result = getSimpleTypeSize(oc.getComponentType()) * Array.getLength(obj);
                if (!oc.getComponentType().isPrimitive()) {
                    for (int i = 0; i < Array.getLength(obj); i++) {
                        Object v = Array.get(obj, i);
                        result += calcObjectSize(v);
                    }
                }
            } else {
                result = SZ_OBJFOOT;
                Field[] fields = oc.getDeclaredFields();
                for (int i = 0; i < fields.length; i++) {
                    Field f = fields[i];
                    if (Modifier.isStatic(f.getModifiers())) {
                        continue; // static fields doesn't use memory
                    }

                    Class<?> fc = f.getType();
                    result += getSimpleTypeSize(fc);
                    if (!fc.isPrimitive()) {
                        boolean achanged = !f.isAccessible();
                        if (achanged) {
                            f.setAccessible(true);
                        }
                        Object v = f.get(obj);
                        if (achanged) {
                            f.setAccessible(false);
                        }

                        result += calcObjectSize(v);
                    }
                }
            }
        } catch (Exception ex) {
            result = -1;
        }

        return result;
    }

    /**
     * Calculate memory usage for simple types.
     * 
     * @param oc
     *            simple type class
     * @return memory size
     */
    protected static long getSimpleTypeSize(Class<?> oc) {
        long result;
        if (oc == byte.class) {
            result = 1; // 1 byte for byte
        } else if (oc == short.class) {
            result = 2; // 2 bytes for short
        } else if (oc == int.class) {
            result = 4; // 4 bytes for int
        } else if (oc == long.class) {
            result = 8; // 8 bytes for long
        } else if (oc == char.class) {
            result = 2; // 2 bytes for char
        } else if (oc == boolean.class) {
            result = 1; // 1 byte for boolean
        } else if (oc == float.class) {
            result = 4; // 4 bytes for float
        } else if (oc == double.class) {
            result = 8; // 8 bytes for double
        } else {
            // Unknown class - it's not a simple type, but an object.
            result = SZ_OBJLINK;
        }
        return result;
    }
}
