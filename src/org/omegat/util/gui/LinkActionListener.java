/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2021 Hiroshi Miura
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
package org.omegat.util.gui;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.SwingWorker;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 * Handles A tag with file:// media link.
 *
 * @author Hiroshi Miura
 */
public class LinkActionListener implements HyperlinkListener {

    /**
     * Play specified file on default device.
     * @param file media file.
     */
    public static synchronized void playSound(final File file) {
        new SwingWorker<Void, Void>() {
            Clip clip;
            @Override
            protected Void doInBackground() throws Exception {
                clip = AudioSystem.getClip();
                AudioInputStream inputStream = AudioSystem.getAudioInputStream(file);
                clip.open(inputStream);
                clip.start();
                return null;
            }
            @Override
            protected void done() {
                clip.drain();
                clip.close();
            }
        }.execute();
    }

    /**
     * When got .wav or *.WAV file, play it.
     * <p>
     * Unknown file types and protocols silently ignored.
     *
     * @param hyperlinkEvent event on editor panes such as dictionary.
     */
    @Override
    public void hyperlinkUpdate(final HyperlinkEvent hyperlinkEvent) {
        if (HyperlinkEvent.EventType.ACTIVATED.equals(hyperlinkEvent.getEventType())) {
            URL url = hyperlinkEvent.getURL();
            if ("file".equals(url.getProtocol())) {
                try {
                    String path = url.toURI().getPath();
                    if (path != null && (path.endsWith(".wav") || path.endsWith(".WAV"))) {
                        playSound(new File(path));
                    }
                } catch (URISyntaxException ignored) {
                }
            }
        }
    }
}
