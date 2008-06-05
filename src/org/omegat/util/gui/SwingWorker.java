/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 **************************************************************************/

package org.omegat.util.gui;

import javax.swing.SwingUtilities;

import org.omegat.core.Core;

/**
 * Class provides functionality for call long time tasks from GUI. It creates
 * new thread and run task in this thread.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public abstract class SwingWorker<T> {
    /** Task result object. */
    private T result;
    /** Exception which throwed in 'doInBackground'. */
    private Exception ex;

    /** Main process task method. */
    protected abstract T doInBackground() throws Exception;

    /** Callback which will be called in UI thread after task processed. */
    protected void done() {
    }

    /**
     * Get task result, or throw exception from 'doInBackground'.
     * 
     * @return result
     * @throws Exception
     *                 exception
     */
    protected final T get() throws Exception {
        if (ex != null) {
            throw ex;
        }
        return result;
    }

    /**
     * Execute task.
     */
    public final void execute() {
        Core.getMainWindow().lockUI();
        new Thread() {
            public void run() {
                try {
                    result = doInBackground();
                } catch (Exception e) {
                    ex = e;
                }
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        try {
                            done();
                        } finally {
                            Core.getMainWindow().unlockUI();
                        }
                    }
                });
            }
        }.start();
    }
}
