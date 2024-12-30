/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Yu Tang
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

package org.omegat.gui.editor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.TextLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.omegat.core.Core;
import org.omegat.util.BiDiUtils;
import org.omegat.util.Preferences;
import org.omegat.util.StaticUtils;
import org.omegat.util.gui.UIThreadsUtil;

/**
 * Popup panel for displaying alphabetical markers.
 *
 * @author Yu Tang
 */
@SuppressWarnings("serial")
public abstract class AlphabeticalMarkers extends JPanel {

    private static final String DEFAULT_MARKER_FONT_NAME = "Century";
    private static final int FIRST_TITLE_LETTER = 'a';

    private final ColorScheme colorScheme;

    private final Font titleFont = getTitleFont();
    private final int boxSize = getBoxSize(titleFont);
    private final Polygon markerShape = createMarkerShape(boxSize);
    private final Rectangle guidingSquare = new Rectangle(boxSize, boxSize);
    private List<Marker> markers = null;
    private final JLayeredPane parent;
    private final JScrollPane scrollPane;
    private final boolean sourceLangIsRTL;

    AlphabeticalMarkers(JScrollPane scrollPane) {
        this.parent = Core.getMainWindow().getApplicationFrame().getLayeredPane();
        this.scrollPane = scrollPane;
        String sourceLang = Core.getProject().getProjectProperties()
                .getSourceLanguage().getLanguageCode();
        this.sourceLangIsRTL = BiDiUtils.isRtl(sourceLang);
        this.colorScheme = createColorScheme(scrollPane.getViewport().getView().getBackground());
    }

    private ColorScheme createColorScheme(final Color editorBackground) {
        int minimumVisibility = 0x8000;
        int distanceToLightScheme = calculateSSD(editorBackground, Color.YELLOW);
        if (distanceToLightScheme >= minimumVisibility) {
            // Use the light scheme: background, foreground, border
            return new ColorScheme(Color.YELLOW, Color.RED, Color.ORANGE);
        } else {
            // Use the dark scheme
            return new ColorScheme(Color.DARK_GRAY, Color.GREEN, Color.MAGENTA);
        }
    }

    // calcurate SSD (Sum of Squared Difference) for colors
    private int calculateSSD(final Color a, final Color b) {
        int db = a.getBlue() - b.getBlue();
        int dg = a.getGreen() - b.getGreen();
        int dr = a.getRed() - b.getRed();
        return db * db + dg * dg + dr * dr;
    }

    @Override
    public void paint(Graphics g) {
        try {
            // translate location
            Point srcLocation = new Point(0, 0);
            Point dstLocation = SwingUtilities.convertPoint(scrollPane, srcLocation, this);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.translate(dstLocation.x, dstLocation.y);

            // enable AntiAliasing
            if (!g2.getFontRenderContext().isAntiAliased()) {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                      RenderingHints.VALUE_ANTIALIAS_ON);
            }

            // draw marker
            for (Marker marker : markers) {
                drawMarker(g2, marker.location, String.valueOf(Character.toChars(marker.title)));
            }

            g2.dispose();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void drawMarker(Graphics2D g2, Point location, String title) {
        // map location to the appropriate corner of the guiding square.
        Point boxLocation = new Point(location);
        if (sourceLangIsRTL) {
            boxLocation.translate(-boxSize, -boxSize); // right-bottom corner
        } else {
            boxLocation.translate(0, -boxSize); // left-bottom corner
        }
        guidingSquare.setLocation(boxLocation);

        // create TextLayout
        TextLayout layout = new TextLayout(title, titleFont, g2.getFontRenderContext());
        Rectangle pixelBounds = layout.getPixelBounds(null, location.x, location.y);
        Dimension diffForCentered = getCenteredDimension(pixelBounds, guidingSquare);

        // set marker shape's position
        markerShape.translate(boxLocation.x, boxLocation.y);

        // fill rectangle
        g2.setColor(colorScheme.background);
        g2.fill(markerShape);

        // draw rectangle
        g2.setColor(colorScheme.border);
        g2.draw(markerShape);

        // hideMarkers position for next time
        markerShape.translate(-boxLocation.x, -boxLocation.y);

        // draw title letter
        g2.setColor(colorScheme.foreground);
        layout.draw(g2, location.x + diffForCentered.width, location.y + diffForCentered.height);
    }

    //  +------+
    //  |      |
    //  |      |
    //  +--  --+
    //     \/
    private static Polygon createMarkerShape(int boxSize) {
        Polygon poly = new Polygon();
        poly.addPoint(0, boxSize);
        poly.addPoint(boxSize / 3, boxSize);
        poly.addPoint(boxSize / 2, boxSize + boxSize / 3);
        poly.addPoint(boxSize - boxSize / 3, boxSize);
        poly.addPoint(boxSize, boxSize);
        poly.addPoint(boxSize, 0);
        poly.addPoint(0, 0);
        return poly;
    }

    private static Dimension getCenteredDimension(Rectangle target, Rectangle base) {
        double baseCenterX = base.getCenterX();
        double baseCenterY = base.getCenterY();
        double targetCenterX = target.getCenterX();
        double targetCenterY = target.getCenterY();
        double diffX = 0, diffY = 0;
        if (baseCenterX != targetCenterX) {
            diffX = baseCenterX - targetCenterX;
        }
        if (baseCenterY != targetCenterY) {
            diffY = baseCenterY - targetCenterY;
        }
        return new Dimension((int) diffX, (int) diffY);
    }

    private static Font getTitleFont() {
        boolean fontAvailable = Arrays.asList(StaticUtils.getFontNames())
                .contains(DEFAULT_MARKER_FONT_NAME);
        String fontName = fontAvailable ? DEFAULT_MARKER_FONT_NAME : Font.SERIF;
        int fontSize = Preferences.getPreferenceDefault(
                Preferences.TF_SRC_FONT_SIZE, Preferences.TF_FONT_SIZE_DEFAULT);
        return new Font(fontName, Font.BOLD, fontSize);
    }

    private static int getBoxSize(Font baseFont) {
        return (int) (baseFont.getSize2D() * 1.4f);
    }

    /**
     * Makes the alphabetical markers visible.
     */
    public void showMarkers() {
        UIThreadsUtil.mustBeSwingThread();

        markers = createMarkers(getViewableSegmentLocations());
        if (markers.isEmpty()) {
            return;
        }

        setSize(parent.getWidth() - 1, parent.getHeight() - 1);
        parent.add(this, JLayeredPane.POPUP_LAYER, 0); // top most
        parent.validate();
        parent.repaint();
    }

    /**
     * Makes the alphabetical markers invisible.
     */
    public void hideMarkers() {
        UIThreadsUtil.mustBeSwingThread();

        if (markers != null && !markers.isEmpty()) {
            parent.remove(this);
            parent.validate();
            parent.repaint();
            markers.clear();
        }
        markers = null;
    }

    private static List<Marker> createMarkers(Map<Integer, Point> map) {
        List<Marker> list = new ArrayList<Marker>();
        int title = FIRST_TITLE_LETTER;
        for (Entry<Integer, Point> entry : map.entrySet()) {
            Marker marker = new Marker();
            marker.segmentNumber = entry.getKey();
            marker.location = entry.getValue();
            marker.title = title++;
            list.add(marker);
        }
        return list;
    }

    protected abstract Map<Integer, Point> getViewableSegmentLocations();

    /**
     * Translate a marker title letter to a segment number. If the letter
     * found, it will be converted to actual segment number string.
     * @param inputValue
     * @return if the letter found translated string, otherwise inputValue.
     */
    public String translateSegmentNumber(String inputValue) {
        try {
            Marker marker = findMarkerByTitle(inputValue);
            return String.valueOf(marker.segmentNumber);
        } catch (Exception ex) {
            return inputValue;
        }
    }

     /**
     * Returns <tt>true</tt> if this list contains the specified title.
     * @param title as segment shortcut letter
     * @return <tt>true</tt> if this list contains the specified title
     */
    public boolean containsTitle(int title) {
        try {
            findMarkerByTitle(title);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private Marker findMarkerByTitle(String title) {
        String trimmed = title.trim();
        if (trimmed.codePointCount(0, trimmed.length()) == 1) {
            int cp = trimmed.codePointAt(0);
            return findMarkerByTitle(cp);
        }
        throw new RuntimeException("Marker with the title '" + title + "' is not found");
    }

    private Marker findMarkerByTitle(int title) {
        for (Marker marker : markers) {
            if (title == marker.title) {
                return marker;
            }
        }
        throw new RuntimeException(
                "Marker with the title '" + String.valueOf(Character.toChars(title)) + "' is not found");
    }

    private static class Marker {
        int segmentNumber = 0;
        Point location = null;
        int title = 0;

        @Override
        public String toString() {
            return this.getClass().getSimpleName() + " {"
                    + this.segmentNumber + ", '"
                    + this.title + "', "
                    + this.location.toString() + "}";
        }
    }

    private static class ColorScheme {
        final Color background;
        final Color foreground;
        final Color border;

        ColorScheme(Color background, Color foreground, Color border) {
            this.background = background;
            this.foreground = foreground;
            this.border = border;
        }
    }
}
