/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Alex Buloichik
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

package org.omegat.gui.common;

import javax.swing.SwingUtilities;

import org.omegat.core.data.StringEntry;

/**
 * Base class for search info about current entry in the separate thread.
 * 
 * Implementation must check isEntryChanged method and exit if antry changed,
 * against create multimple threads when user travels by entries fast.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 * 
 * @param <T>
 *            result type of found data
 */
public abstract class EntryInfoSearchThread<T> extends Thread {
	private final EntryInfoPane<T> pane;

	/**
	 * Entry which processed currently.
	 * 
	 * If entry in pane was changed, it means user was moved to other entry, and
	 * there is no sense to continue search.
	 */
	private final StringEntry currentlyProcessedEntry;

	/**
	 * Constructor.
	 * 
	 * @param pane
	 *            entry info pane
	 * @param entry
	 *            current entry
	 */
	public EntryInfoSearchThread(final EntryInfoPane<T> pane,
			final StringEntry entry) {
		this.pane = pane;
		this.currentlyProcessedEntry = entry;

	}

	/**
	 * Check if current entry was changed. If user moved to other entry, there
	 * is no sence to continue search.
	 * 
	 * @return true if current entry was changed
	 */
	protected boolean isEntryChanged() {
		return currentlyProcessedEntry != pane.currentlyProcessedEntry;
	}

	@Override
	public void run() {
		if (isEntryChanged()) {
			return;
		}
		final T result = search();
		if (isEntryChanged()) {
			return;
		}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (isEntryChanged()) {
					return;
				}
				pane.setFoundResult(result);
			}
		});
	}

	/**
	 * Implementation-dependent method for search info.
	 * 
	 * If entry changed, method can return null.
	 * 
	 * @return result of search
	 */
	protected abstract T search();
}
