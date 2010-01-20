/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Wildrich Fourie
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

package org.omegat.gui.glossary;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.LayeredHighlighter;
import javax.swing.text.Position;
import javax.swing.text.View;
import org.omegat.util.Preferences;

/**
 * Underlines all the terms in the SourceTextEntry that has matches in the Glossary.
 * @author W. Fourie
 */
public class TransTipsUnderliner
{
    // Container for the Highlighter class
    private Highlighter highlighter;
    // The current textComponent on which the lines should be drawn
    protected JTextComponent comp;
    // Container for the painter
    protected Highlighter.HighlightPainter painter;

    // List containing the characters that may be found at the end of a word, that
    // might result in the word not being underlined
    private static List<Character> afterExcludeCases = new ArrayList<Character>();
    static
    {
        afterExcludeCases.add('.');
        afterExcludeCases.add('!');
        afterExcludeCases.add('?');
        afterExcludeCases.add(':');
        afterExcludeCases.add(',');
        afterExcludeCases.add(';');
        afterExcludeCases.add('<');
        afterExcludeCases.add(')');
        afterExcludeCases.add('-');
    };

    // List containing the characters that may be found at the beginning of a word, that
    // might result in the word not being underlined
    private static List<Character> beforeExcludeCases = new ArrayList<Character>();
    static
    {
        beforeExcludeCases.add('>');
        beforeExcludeCases.add('(');
        beforeExcludeCases.add(' ');
    };

    /**
     * Initialises the TransTips underliner.
     * @param comp
     * @param colour
     */
    public TransTipsUnderliner(JTextComponent comp, Color colour)
    {
        this.comp = comp;
        this.painter = new Underliner.UnderlineHighlightPainter(colour);
        highlighter = comp.getHighlighter();

        // Remove any existing highlights
        Highlighter.Highlight[] highlights = highlighter.getHighlights();
        for (int i = 0; i < highlights.length; i++)
        {
            Highlighter.Highlight h = highlights[i];
            if (h.getPainter() instanceof Underliner.UnderlineHighlightPainter)
                highlighter.removeHighlight(h);
        }
    }


    /**
     * Search for a word and returns the offset of the first occurrence.
     * Highlights are added for all occurrences found.
     * @param word To be searched
     * @param start Starting position
     * @param end Ending position
     * @return The offset of the first occurrence
     */
    public int search(String word, int start, int end)
    {
        int firstOffset = -1;
        // Test for invalid word.
        if (word == null || word.equals(""))
        {
            return -1;
        }

        // Search for the word.
        String content = null;
        try
        {
            Document d = comp.getDocument();
            content = d.getText(0, d.getLength()).toLowerCase();
        }
        catch (BadLocationException e)
        {
            // Unthrowable
            return -1;
        }

        word = word.toLowerCase();        
        int lastIndex = start;
        int wordSize = word.length();

        // Test for BadLocation
        while (((lastIndex = content.indexOf(word, lastIndex)) != -1)
                && (lastIndex <= end) && (lastIndex >= start))
        {
            int endIndex = lastIndex + wordSize;
            if(Preferences.isPreference(Preferences.TRANSTIPS_EXACT_SEARCH))
            {
                if(isWordAlone(content, lastIndex, word))
                {
                    try
                    {
                        highlighter.addHighlight(lastIndex, endIndex, painter);
                    }
                    catch (BadLocationException e){/* Unthrowable */}

                    if (firstOffset == -1)
                        firstOffset = lastIndex;
                }
            }
            else
            {
                try
                {
                    highlighter.addHighlight(lastIndex, endIndex, painter);
                }
                catch (BadLocationException e){/* Unthrowable */}

                if (firstOffset == -1)
                    firstOffset = lastIndex;

            }

            lastIndex = endIndex;
        }
        return 0;
    }

    /**
     * Determines if the word is surrounded by whitespace characters.
     * @param sourceText
     * @param testIndex
     * @param glosSrc
     * @return True if the word is alone; False if the word is contained within another word
     */
    public boolean isWordAlone(String sourceText, int testIndex, String glosSrc)
    {
        // Remove directional characters
        sourceText = sourceText.replaceAll("[\u202A|\u202B|\u202C]", " ");

        // Check that word stands alone
        char before;
        try
        {
            before = sourceText.toLowerCase().charAt(testIndex - 1);
        }
        catch (Exception ex)
        {
            before = ' ';
        }

        // Test the end of the string
        char after;
        try
        {
            after = sourceText.toLowerCase().charAt(testIndex + glosSrc.length());
        }
        catch (Exception cp)
        {
            after = ' ';
        }

        // After Exclude Cases.
        if(afterExcludeCases.contains(after))
            after = ' ';

        // Before Exclude Cases
        if(beforeExcludeCases.contains(before))
            before = ' ';

        if ((before == ' ') && (after == ' '))
        {
            return true;
        }
        else
        {
            return false;
        }
    }    
}

/**
 * The underliner class.
 * @author W. Fourie
 */
class Underliner extends DefaultHighlighter
{
    public Underliner(Color c)
    {
        painter = (c == null ? sharedPainter : new UnderlineHighlightPainter(c));
    }

    // Add a highlight.
    public Object addHighlight(int p0, int p1) throws BadLocationException
    {
        return addHighlight(p0, p1, painter);
    }

    @Override
    public void setDrawsLayeredHighlights(boolean newValue)
    {        
        super.setDrawsLayeredHighlights(true);
    }

    // Paints the underlines.
    public static class UnderlineHighlightPainter extends LayeredHighlighter.LayerPainter
    {
        public UnderlineHighlightPainter(Color c)
        {
            color = c;
        }

        @Override
        public Shape paintLayer(Graphics g, int offs0, int offs1, Shape bounds,
                JTextComponent c, View view)
        {
            g.setColor(color == null ? c.getSelectionColor() : color);

            Rectangle rect = null;
            if (offs0 == view.getStartOffset() && offs1 == view.getEndOffset())
            {
                if (bounds instanceof Rectangle)
                    rect = (Rectangle) bounds;
                else
                    rect = bounds.getBounds();
            }
            else
            {
                try
                {
                    Shape shape = view.modelToView(offs0,
                            Position.Bias.Forward, offs1,
                            Position.Bias.Backward, bounds);
                    rect = (shape instanceof Rectangle) ? (Rectangle) shape
                            : shape.getBounds();
                }
                catch (BadLocationException e)
                {
                    return null;
                }
            }

            FontMetrics fm = c.getFontMetrics(c.getFont());
            int baseline = rect.y + rect.height - fm.getDescent() + 1;
            g.drawLine(rect.x, baseline, rect.x + rect.width, baseline);
            g.drawLine(rect.x, baseline + 1, rect.x + rect.width,
                    baseline + 1);

            return rect;
        }
        protected Color color; // The underline color

        public void paint(Graphics g, int p0, int p1, Shape bounds, JTextComponent c) {
            // Unused method, but still has to be overridden.
        }
    }

    // Shared painter
    protected static final Highlighter.HighlightPainter sharedPainter =
            new UnderlineHighlightPainter(null);
    // This highlighter
    protected Highlighter.HighlightPainter painter;
}