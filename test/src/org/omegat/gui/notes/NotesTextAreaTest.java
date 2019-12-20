/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Aaron Madlon-Kay
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

package org.omegat.gui.notes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import javax.swing.SwingUtilities;

import org.junit.Test;
import org.omegat.core.Core;
import org.omegat.core.TestCore;

/**
 *
 * @author Aaron Madlon-Kay
 */
public class NotesTextAreaTest extends TestCore {

    @Test
    public void testSetNote() throws Exception {
        final INotes nta = new NotesTextArea(Core.getMainWindow());
        final ResultHolder<String> holder = new ResultHolder<String>();

        final String s = "foobar";
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                nta.setNoteText(s);
            }
        });
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                holder.result = nta.getNoteText();
            }
        });
        assertEquals(s, holder.result);

        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                nta.setNoteText("");
            }
        });
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                holder.result = nta.getNoteText();
            }
        });
        assertNull(holder.result);
    }

    private class ResultHolder<T> {
        public T result;
    }

    @Test
    public void testClear() throws Exception {
        final INotes nta = new NotesTextArea(Core.getMainWindow());
        final ResultHolder<String> holder = new ResultHolder<String>();

        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                nta.setNoteText("foobar");
            }
        });
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                nta.clear();
            }
        });
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                holder.result = nta.getNoteText();
            }
        });
        assertNull(holder.result);
    }
}
