/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Chihiro Hio, Aaron Madlon-Kay
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
package org.omegat.util.gui;

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.omegat.util.Log;

/**
 * Adapted from omegat-plugin-linkbuilder by Chihiro Hio, provided under GPLv3.
 * 
 * @see original https://github.com/hiohiohio/omegat-plugin-linkbuilder
 * 
 * @author Chihiro Hio
 * @author Aaron Madlon-Kay
 */
public class JTextPaneLinkifier {

    private static final String ATTR_LINK = "linkbuilder_link";

    public static void linkify(JTextPane jTextPane) {
        JTextPaneLinkifier inserter = new JTextPaneLinkifier(jTextPane);
        inserter.register();
    }
    
    private final JTextPane jTextPane;

    public JTextPaneLinkifier(final JTextPane pane) {
        this.jTextPane = pane;
    }

    public void register() {
        final MouseAdapter mouseAdapter = new AttributeInserterMouseListener(jTextPane);

        // Adding mouse listner for actions
        jTextPane.addMouseListener(mouseAdapter);

        // settings for mouseover (changing cursor)
        jTextPane.addMouseMotionListener(mouseAdapter);

        // Those are the main called points from user's activities.
        setDocumentFilter(jTextPane);

        jTextPane.addPropertyChangeListener("document", new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                Object source = evt.getSource();
                if (source instanceof JTextPane) {
                    setDocumentFilter((JTextPane) source);
                }
            }
        });
    }

    private static void setDocumentFilter(final JTextPane textPane) {
        final StyledDocument doc = textPane.getStyledDocument();
        if (doc instanceof AbstractDocument) {
            final AbstractDocument abstractDocument = (AbstractDocument) doc;
            abstractDocument.setDocumentFilter(new AttributeInserterDocumentFilter(doc));
        }
    }

    private interface IAttributeAction {
        public void execute();
    }
    
    private static class AttributeInserterMouseListener extends MouseAdapter {

        private final JTextPane jTextPane;

        public AttributeInserterMouseListener(final JTextPane jTextPane) {
            this.jTextPane = jTextPane;
        }

        @Override
        public void mouseClicked(final MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                final StyledDocument doc = jTextPane.getStyledDocument();
                final Element characterElement = doc.getCharacterElement(jTextPane.viewToModel(e.getPoint()));
                final AttributeSet as = characterElement.getAttributes();
                final Object attr = as.getAttribute(ATTR_LINK);
                if (attr instanceof IAttributeAction) {
                    ((IAttributeAction) attr).execute();
                }
            } else {
                super.mouseClicked(e);
            }
        }

        @Override
        public void mouseMoved(final MouseEvent e) {
            final StyledDocument doc = jTextPane.getStyledDocument();
            final Element characterElement = doc.getCharacterElement(jTextPane.viewToModel(e.getPoint()));
            final AttributeSet as = characterElement.getAttributes();
            final Object attr = as.getAttribute(ATTR_LINK);
            if (attr instanceof IAttributeAction) {
                jTextPane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            } else {
                jTextPane.setCursor(Cursor.getDefaultCursor());
            }
        }
    }

    private static class AttributeInserterDocumentFilter extends DocumentFilter {

        private static final int REFRESH_DELAY = 2000;

        // Regular Expression for URL validation
        // From https://gist.github.com/dperini/729294
        // See lib/Licenses.txt
        private static final String REGEX_URL = "(?:(?:https?|ftp):\\/\\/)(?:\\S+(?::\\S*)?@)?(?:(?!(?:10|127)(?:\\.\\d{1,3}){3})(?!(?:169\\.254|192\\.168)(?:\\.\\d{1,3}){2})(?!172\\.(?:1[6-9]|2\\d|3[0-1])(?:\\.\\d{1,3}){2})(?:[1-9]\\d?|1\\d\\d|2[01]\\d|22[0-3])(?:\\.(?:1?\\d{1,2}|2[0-4]\\d|25[0-5])){2}(?:\\.(?:[1-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-4]))|(?:(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)(?:\\.(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)*(?:\\.(?:[a-z\\u00a1-\\uffff]{2,}))\\.?)(?::\\d{2,5})?(?:[/?#]\\S*)?";
        private static final Pattern URL_PATTERN = Pattern.compile(REGEX_URL, Pattern.CASE_INSENSITIVE);
        private static final AttributeSet DEFAULT_ATTRIBUTES = new SimpleAttributeSet();
        private static final AttributeSet LINK_ATTRIBUTES;
        
        static {
            MutableAttributeSet tmp = new SimpleAttributeSet();
            tmp = new SimpleAttributeSet();
            StyleConstants.setUnderline(tmp, true);
            StyleConstants.setForeground(tmp, Styles.EditorColor.COLOR_HYPERLINK.getColor());
            LINK_ATTRIBUTES = tmp;
        }

        private final StyledDocument doc;
        private final Timer timer;

        // as default constructor
        public AttributeInserterDocumentFilter(StyledDocument doc) {
            this.doc = doc;
            timer = new Timer(REFRESH_DELAY, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    refreshPane();
                }
            });
            timer.setRepeats(false);
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            super.insertString(fb, offset, string, attr);

            if (attr != null && attr.isDefined(StyleConstants.ComposedTextAttribute)) {
                // ignore
            } else {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        refreshPane();
                    }
                });
            }
        }

        @Override
        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
            boolean refresh = true;
            final AttributeSet attr = ((StyledDocument) fb.getDocument()).getCharacterElement(offset).getAttributes();
            if (attr != null && attr.isDefined(StyleConstants.ComposedTextAttribute)) {
                refresh = false;
            }

            super.remove(fb, offset, length);

            if (refresh && length != 0 && fb.getDocument().getLength() != 0) {
                timer.restart();
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            super.replace(fb, offset, length, text, attrs);

            if (fb.getDocument().getLength() != 0) {
                timer.restart();
            }
        }

        private void refreshPane() {
            final int docLength = doc.getLength();
            if (docLength == 0) {
                return;
            }
            try {
                // clear attributes
                for (int i = 0; i < docLength; ++i) {
                    if (doc.getCharacterElement(i).getAttributes().containsAttributes(LINK_ATTRIBUTES)) {
                        doc.setCharacterAttributes(i, 1, DEFAULT_ATTRIBUTES, true);
                    }
                }

                // URL detection
                final String text = doc.getText(0, docLength);
                final Matcher matcher = URL_PATTERN.matcher(text);
                while (matcher.find()) {
                    final int offset = matcher.start();
                    final int targetLength = matcher.end() - offset;

                    try {
                        // Transform into clickable text
                        AttributeSet atts = makeAttributes(offset, new URI(matcher.group()));
                        doc.setCharacterAttributes(offset, targetLength, atts, true);
                    } catch (URISyntaxException ex) {
                        Log.log(ex);
                    }
                }

            } catch (BadLocationException ex) {
                Log.log(ex);
            }
        }
        
        private AttributeSet makeAttributes(final int offset, final URI target) {
            SimpleAttributeSet atts = new SimpleAttributeSet(doc.getCharacterElement(offset).getAttributes());
            atts.addAttributes(LINK_ATTRIBUTES);
            atts.addAttribute(ATTR_LINK, new IAttributeAction() {
                @Override
                public void execute() {
                    try {
                        Desktop.getDesktop().browse(target);
                    } catch (IOException e) {
                        Log.log(e);
                    }
                }
            });  
            return atts;
        }
    }
}
