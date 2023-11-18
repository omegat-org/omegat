/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Chihiro Hio, Aaron Madlon-Kay
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
package org.omegat.util.gui;

import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
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

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.validator.routines.UrlValidator;

import org.omegat.util.Log;
import org.omegat.util.OStrings;

/**
 * Adapted from omegat-plugin-linkbuilder by Chihiro Hio, provided under GPLv3.
 *
 * @see <a href="https://github.com/hiohiohio/omegat-plugin-linkbuilder">
 *      Original</a>
 *
 * @author Chihiro Hio
 * @author Aaron Madlon-Kay
 */
public final class JTextPaneLinkifier {

    private JTextPaneLinkifier() {
    }

    private static final String ATTR_LINK = "linkbuilder_link";

    public static void linkify(JTextPane jTextPane) {
        linkify(jTextPane, false);
    }

    public static void linkify(JTextPane jTextPane, boolean extended) {
        final MouseAdapter mouseAdapter = new AttributeInserterMouseListener(jTextPane);

        // Adding mouse listener for actions
        jTextPane.addMouseListener(mouseAdapter);

        // settings for mouseover (changing cursor)
        jTextPane.addMouseMotionListener(mouseAdapter);

        // Those are the main called points from user's activities.
        setDocumentFilter(jTextPane, extended);

        jTextPane.addPropertyChangeListener("document", evt -> {
            Object source = evt.getSource();
            if (source instanceof JTextPane) {
                setDocumentFilter((JTextPane) source, extended);
            }
        });
    }

    private static void setDocumentFilter(JTextPane textPane, boolean extended) {
        final StyledDocument doc = textPane.getStyledDocument();
        if (doc instanceof AbstractDocument) {
            final AbstractDocument abstractDocument = (AbstractDocument) doc;
            abstractDocument.setDocumentFilter(new AttributeInserterDocumentFilter(doc, extended));
        }
    }

    private interface IAttributeAction {
        void execute();
    }

    private static class AttributeInserterMouseListener extends MouseAdapter {

        private final JTextPane jTextPane;

        AttributeInserterMouseListener(final JTextPane jTextPane) {
            this.jTextPane = jTextPane;
        }

        @Override
        public void mouseClicked(final MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                final StyledDocument doc = jTextPane.getStyledDocument();
                final Element characterElement = doc
                        .getCharacterElement(jTextPane.viewToModel2D(e.getPoint()));
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
            final Element characterElement = doc.getCharacterElement(jTextPane.viewToModel2D(e.getPoint()));
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

        private static final int REFRESH_DELAY = 200;

        private static final AttributeSet LINK_ATTRIBUTES;

        static {
            MutableAttributeSet tmp = new SimpleAttributeSet();
            StyleConstants.setUnderline(tmp, true);
            StyleConstants.setForeground(tmp, Styles.EditorColor.COLOR_HYPERLINK.getColor());
            StyleConstants.setBackground(tmp, Styles.EditorColor.COLOR_BACKGROUND.getColor());
            LINK_ATTRIBUTES = tmp;
        }

        private final StyledDocument doc;
        private final Timer timer;
        private final Pattern[] urlPatterns;

        // as default constructor
        AttributeInserterDocumentFilter(StyledDocument doc, boolean extended) {
            this.doc = doc;
            Pattern urlPattern = Pattern.compile("\\bhttps?://\\S+\\b", Pattern.CASE_INSENSITIVE);
            if (extended) {
                Pattern filePattern = Pattern.compile(
                        "\\\\bfile://[-A-Za-z0-9+$&@#/%?=~_|!:,.;]*[-A-Za-z0-9+$&@#/%=~_|]\\b",
                        Pattern.CASE_INSENSITIVE);
                urlPatterns = new Pattern[] { urlPattern, filePattern };
            } else {
                urlPatterns = new Pattern[] { urlPattern };
            }
            timer = new Timer(REFRESH_DELAY, e -> refreshPane());
            timer.setRepeats(false);
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                throws BadLocationException {
            super.insertString(fb, offset, string, attr);

            if (attr != null && attr.isDefined(StyleConstants.ComposedTextAttribute)) {
                // ignore
            } else {
                SwingUtilities.invokeLater(this::refreshPane);
            }
        }

        @Override
        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
            boolean refresh = true;
            final AttributeSet attr = ((StyledDocument) fb.getDocument()).getCharacterElement(offset)
                    .getAttributes();
            if (attr != null && attr.isDefined(StyleConstants.ComposedTextAttribute)) {
                refresh = false;
            }

            super.remove(fb, offset, length);

            if (refresh && length != 0 && fb.getDocument().getLength() != 0) {
                timer.restart();
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {
            super.replace(fb, offset, length, text, attrs);

            if (fb.getDocument().getLength() != 0) {
                timer.restart();
            }
        }

        private void refreshPane() {
            if (doc.getLength() == 0) {
                return;
            }
            try {
                // URL detection
                URLCodec codec = new URLCodec("UTF-8");
                UrlValidator urlValidator = new UrlValidator();
                for (Pattern pattern : urlPatterns) {
                    int shift = 0;
                    final String text = doc.getText(0, doc.getLength());
                    final Matcher matcher = pattern.matcher(text);
                    while (matcher.find()) {
                        final int offset = matcher.start() + shift;
                        if (doc.getCharacterElement(offset).getAttributes().containsAttributes(LINK_ATTRIBUTES)) {
                            continue;
                        }
                        final int targetLength = matcher.end() - matcher.start();
                        final String uri = matcher.group();
                        if (urlValidator.isValid(uri)) {
                            try {
                                // Transform into clickable and readable text
                                String decoded = codec.decode(uri);
                                if (decoded.length() == uri.length()) {
                                    SimpleAttributeSet atts = new SimpleAttributeSet(doc.getCharacterElement(offset).getAttributes());
                                    setLinkAttribute(atts, new URI(uri));
                                    doc.setCharacterAttributes(offset, targetLength, atts, true);
                                } else {
                                    shift += decoded.length() - targetLength;
                                    doc.remove(offset, targetLength);
                                    SimpleAttributeSet atts = new SimpleAttributeSet();
                                    setLinkAttribute(atts, new URI(uri));
                                    doc.insertString(offset, decoded, atts);
                                }
                            } catch (DecoderException | URISyntaxException  ex) {
                                Log.logWarningRB("TPL_ERROR_URL", matcher.group());
                            }
                        }
                    }
                }

            } catch (BadLocationException ex) {
                Log.log(ex);
            }
        }

        private void setLinkAttribute(SimpleAttributeSet atts, URI target) {
            atts.addAttributes(LINK_ATTRIBUTES);
            atts.addAttribute(ATTR_LINK, (IAttributeAction) () -> {
                try {
                    DesktopWrapper.browse(target);
                } catch (Exception e) {
                    JOptionPane.showConfirmDialog(null, e.getLocalizedMessage(),
                            OStrings.getString("ERROR_TITLE"), JOptionPane.ERROR_MESSAGE);
                    Log.log(e);
                }
            });
        }
    }
}
