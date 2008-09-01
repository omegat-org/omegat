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

package org.omegat.gui.editor;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.omegat.core.Core;
import org.omegat.util.Log;
import org.omegat.util.Token;

/**
 * Separate thread for check spelling. All words for displayed file will be
 * cached, because check spelling is enough long operation.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class SpellCheckerThread extends Thread {
    /** Queue for check. */
    private final Queue<String> forCheck = new LinkedList<String>();

    /** Cache of correct words. */
    private final Set<String> correctWords = new HashSet<String>();
    /** Cache of incorrect words. */
    private final Set<String> incorrectWords = new HashSet<String>();

    /** Locks. */
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock r = rwl.readLock();
    private final Lock w = rwl.writeLock();

    /**
     * Add string into check queue.
     * 
     * @param str
     *            string for check
     */
    public synchronized void addForCheck(String str) {
        forCheck.add(str);
        notifyAll();
    }

    /**
     * Reset cache of checked words.
     */
    public void resetCache() {
        w.lock();
        try {
            incorrectWords.clear();
            correctWords.clear();
        } finally {
            w.unlock();
        }
    }

    /**
     * Check is word incorrect ? This method reads cache, then call check if
     * there is no such word in cache.
     * 
     * @param word
     *            word for check
     * @return true if incorrect
     */
    public boolean isIncorrect(String word) {
        r.lock();
        try {
            if (incorrectWords.contains(word)) {
                return true;
            } else if (correctWords.contains(word)) {
                return false;
            }
        } finally {
            r.unlock();
        }
        // we are passing here if word there is no in cache. then check it
        return checkWordSpelling(word);
    }

    /**
     * Process checking queue.
     */
    @Override
    public void run() {
        Thread.currentThread().setPriority(MIN_PRIORITY);
        Thread.currentThread().setName(this.getClass().getSimpleName());

        try {
            while (true) {
                String str;
                synchronized (this) {
                    str = forCheck.poll();
                    if (str == null) {
                        // there is no strings in queue
                        wait();
                    }
                }
                if (str == null) {
                    continue;
                }
                for (Token tok : Core.getTokenizer().tokenizeWordsForSpelling(
                        str)) {
                    // get next word from string
                    String word = str.substring(tok.getOffset(), tok
                            .getOffset()
                            + tok.getLength());
                    r.lock();
                    try {
                        // check - if word already checked ?
                        if (correctWords.contains(word)
                                || incorrectWords.contains(word)) {
                            // don't need to check it again
                            continue;
                        }
                    } finally {
                        r.unlock();
                    }
                    checkWordSpelling(word);
                }
            }
        } catch (InterruptedException ex) {
            Log.log(ex);
        }
    }

    /**
     * Check word in spell checker.
     * 
     * @param word
     * @return
     */
    private boolean checkWordSpelling(String word) {
        w.lock();
        try {
            if (incorrectWords.contains(word)) {
                return false;
            } else if (correctWords.contains(word)) {
                return true;
            }
            // check word's spelling
            boolean isCorrect = Core.getSpellChecker().isCorrect(word);
            // remember in cache
            if (isCorrect) {
                correctWords.add(word);
            } else {
                incorrectWords.add(word);
            }
            return isCorrect;
        } finally {
            w.unlock();
        }
    }
}
