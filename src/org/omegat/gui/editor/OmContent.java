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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import javax.swing.undo.UndoableEdit;

import org.omegat.util.gui.UIThreadsUtil;

/**
 * Class for implement AbstractDocument's text storage.
 * 
 * It's better way is to use own implementation instead GapContent and
 * StringContent, because our editing is enough specific - we can edit only text
 * inside one segment's translation.
 * 
 * The main idea is to split full text into three pieces - one piece is
 * translation which user will be change, and two pieces(before translation and
 * after translation), which user will not change on editing. So, when user are
 * editing translation, he will change only one short string, i.e. there will
 * not be massive arrays copy operations on each char.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class OmContent implements AbstractDocument.Content {
    /** Local logger. */
    private static final Logger LOGGER = Logger.getLogger(OmContent.class
            .getName());

    /**
     * Where text of marks placed.
     */
    enum POSITION_TYPE {
        /** Before editable translation. */
        BEFORE_EDITABLE,
        /** Inside editable translation. */
        INSIDE_EDITABLE,
        /** After editable translation. */
        AFTER_EDITABLE
    };

    /**
     * True means what user can edit translation, false - we are reorganized
     * segments.
     */
    protected boolean editableMode;

    protected OmDocument doc;

    /** Text before editable translation. */
    protected final StringBuilder beforeEditable = new StringBuilder();
    /** Text inside editable translation, i.e. full translation text. */
    protected final StringBuilder insideEditable = new StringBuilder();
    /** Text after editable translation. */
    protected final StringBuilder afterEditable = new StringBuilder();

    /** Position marks before editable translation. */
    protected final List<WeakReference<Mark>> positionsBeforeEditable = new ArrayList<WeakReference<Mark>>();
    /** Position marks inside editable translation. */
    protected final List<WeakReference<Mark>> positionsAfterEditable = new ArrayList<WeakReference<Mark>>();
    /** Position marks after editable translation. */
    protected final List<WeakReference<Mark>> positionsInsideEditable = new ArrayList<WeakReference<Mark>>();

    /** 'Unflushed' position marks. */
    protected final List<WeakReference<Mark>> positionsUnflushed = new ArrayList<WeakReference<Mark>>();

    public void setDocument(final OmDocument doc) {
        this.doc = doc;
    }

    /**
     * Set editable range of full text. Used when segment activated for editing.
     * In this time, full text and position marks rearranged into three
     * pieces(before/inside/after translation).
     * 
     * @param start
     *            start of editable range from document's begin
     * @param end
     *            end of editable range from document's begin
     */
    public void setEditableRange(int start, int end) {
        UIThreadsUtil.mustBeSwingThread();

        int allCharactersSize = beforeEditable.length()
                + insideEditable.length() + afterEditable.length();

        LOGGER.finest("Set editable range from " + start + " to " + end
                + ", doc size is " + allCharactersSize);

        editableMode = false;

        // move all data to one buffer
        StringBuilder allCharacters = new StringBuilder(allCharactersSize);
        allCharacters.append(beforeEditable);
        allCharacters.append(insideEditable);
        allCharacters.append(afterEditable);

        // move all positions to one list
        int allPositionsCount = positionsBeforeEditable.size()
                + positionsInsideEditable.size()
                + positionsAfterEditable.size();
        List<WeakReference<Mark>> allPositions = new ArrayList<WeakReference<Mark>>(
                allPositionsCount);
        int offset = 0;
        for (WeakReference<Mark> pos : positionsBeforeEditable) {
            Mark m = pos.get();
            if (m != null) {
                m.positionType = POSITION_TYPE.BEFORE_EDITABLE;
                m.offset = m.offset + offset;
                allPositions.add(pos);
            }
        }
        offset = beforeEditable.length();
        for (WeakReference<Mark> pos : positionsInsideEditable) {
            Mark m = pos.get();
            if (m != null) {
                m.positionType = POSITION_TYPE.BEFORE_EDITABLE;
                m.offset = m.offset + offset;
                allPositions.add(pos);
            }
        }
        offset = beforeEditable.length() + insideEditable.length();
        for (WeakReference<Mark> pos : positionsAfterEditable) {
            Mark m = pos.get();
            if (m != null) {
                m.positionType = POSITION_TYPE.BEFORE_EDITABLE;
                m.offset = m.offset + offset;
                allPositions.add(pos);
            }
        }

        beforeEditable.setLength(0);
        insideEditable.setLength(0);
        afterEditable.setLength(0);

        positionsBeforeEditable.clear();
        positionsInsideEditable.clear();
        positionsAfterEditable.clear();

        // rearrange parts
        beforeEditable.append(allCharacters, 0, start);
        insideEditable.append(allCharacters, start, end);
        afterEditable.append(allCharacters, end, allCharacters.length());

        for (WeakReference<Mark> pos : allPositions) {
            Mark m = pos.get();
            if (m != null) {
                m.positionType = calculatePositionType(m.offset);
                m.offset = calculateRelativeOffset(m.offset, m.positionType);
                switch (m.positionType) {
                case BEFORE_EDITABLE:
                    positionsBeforeEditable.add(pos);
                    break;
                case INSIDE_EDITABLE:
                    positionsInsideEditable.add(pos);
                    break;
                case AFTER_EDITABLE:
                    positionsAfterEditable.add(pos);
                    break;
                default:
                    throw new IllegalArgumentException();
                }
            }
        }

        editableMode = start > 0 && end > 0;
    }

    /**
     * Create position and store it into 'unflushed' list. Positions will be
     * added to main list after 'flush'.
     * 
     * @param offset
     * @param preferredPositionType
     * @return
     */
    public Position createPosition(int offset) {
        UIThreadsUtil.mustBeSwingThread();
        Mark mark = new Mark();
        // created by standard behavior
        mark.positionType = calculatePositionType(offset);
        switch (mark.positionType) {
        case BEFORE_EDITABLE:
            mark.offset = offset;
            positionsBeforeEditable.add(new WeakReference<Mark>(mark));
            break;
        case INSIDE_EDITABLE:
            mark.offset = offset - beforeEditable.length();
            positionsInsideEditable.add(new WeakReference<Mark>(mark));
            break;
        case AFTER_EDITABLE:
            mark.offset = offset - beforeEditable.length()
                    - insideEditable.length();
            positionsAfterEditable.add(new WeakReference<Mark>(mark));
            break;
        }
        LOGGER.finest("create position at " + offset + ": " + mark);
        return new StickyPosition(mark);
    }

    /**
     * Create position and store it into 'unflushed' list. Positions will be
     * added to main list after 'flush'.
     * 
     * @param offset
     * @param preferredPositionType
     * @return
     */
    public Position createUnflushedPosition(int offset) {
        UIThreadsUtil.mustBeSwingThread();
        Mark mark = new Mark();
        // initialize content or replace one segment
        mark.positionType = POSITION_TYPE.AFTER_EDITABLE;
        mark.offset = offset;
        positionsUnflushed.add(new WeakReference<Mark>(mark));
        LOGGER.finest("create unflushed position at " + offset + ": " + mark);
        return new StickyPosition(mark);
    }

    /**
     * Get part of text in specified range.
     * 
     * @param where
     *            range start
     * @param len
     *            rangle length
     * @param txt
     *            output
     */
    public void getChars(int where, int len, Segment txt)
            throws BadLocationException {
        UIThreadsUtil.mustBeSwingThread();
        POSITION_TYPE positionType = calculatePositionType(where);
        int relativeOffset = calculateRelativeOffset(where, positionType);
        txt.array = new char[len];
        txt.offset = 0;
        txt.count = len;
        try {
            switch (positionType) {
            case BEFORE_EDITABLE:
                beforeEditable.getChars(relativeOffset, relativeOffset + len,
                        txt.array, 0);
                break;
            case INSIDE_EDITABLE:
                insideEditable.getChars(relativeOffset, relativeOffset + len,
                        txt.array, 0);
                break;
            case AFTER_EDITABLE:
                afterEditable.getChars(relativeOffset, relativeOffset + len,
                        txt.array, 0);
                break;
            default:
                throw new IllegalArgumentException();
            }
        } catch (StringIndexOutOfBoundsException ex) {
            // copy many parts - it can happen of "copy" operation of full doc
            StringBuilder all = new StringBuilder();
            all.append(beforeEditable);
            all.append(insideEditable);
            all.append(afterEditable);
            all.getChars(where, where + len, txt.array, 0);
        }
    }

    /**
     * Get part of text in specified range.
     * 
     * @param where
     *            range start
     * @param len
     *            rangle length
     * @return output
     */
    public String getString(int where, int len) throws BadLocationException {
        UIThreadsUtil.mustBeSwingThread();
        LOGGER.finest("get string from " + where + " length=" + len);
        POSITION_TYPE positionType = calculatePositionType(where);
        int relativeOffset = calculateRelativeOffset(where, positionType);

        try {
            switch (positionType) {
            case BEFORE_EDITABLE:
                return beforeEditable.substring(relativeOffset, relativeOffset
                        + len);
            case INSIDE_EDITABLE:
                return insideEditable.substring(relativeOffset, relativeOffset
                        + len);
            case AFTER_EDITABLE:
                return afterEditable.substring(relativeOffset, relativeOffset
                        + len);
            default:
                throw new IllegalArgumentException();
            }
        } catch (StringIndexOutOfBoundsException ex) {
            // copy many parts - it can happen of "copy" operation of full doc
            StringBuilder all = new StringBuilder();
            all.append(beforeEditable);
            all.append(insideEditable);
            all.append(afterEditable);
            return all.substring(where, where + len);
        }
    }

    /**
     * Flush changes in content.
     * 
     * @param text
     *            changed text
     * @param pos
     *            position to insert
     * @param lengthToRemove
     *            length of replaced text
     */
    protected void flush(StringBuilder text, int pos, int lengthToRemove) {
        String strText = text.toString();
        text.setLength(0);

        if (editableMode) {
            throw new RuntimeException("OmContent flush is in editable mode");
        }

        // marks inside [pos,pos+lengthToRemove] are marks for old elements
        int shift = strText.length() - lengthToRemove;
        shiftMarks(positionsAfterEditable, pos + lengthToRemove, shift);
        afterEditable.replace(pos, pos + lengthToRemove, strText);

        shiftMarks(positionsUnflushed, 0, pos);
        positionsAfterEditable.addAll(positionsUnflushed);
        positionsUnflushed.clear();
    }

    /**
     * Flush changes in translation.
     * 
     * @param text
     *            new text or null if text shouldn't be changed
     * @param startPos
     *            start position to update
     * @param endPos
     *            end position to update
     */
    protected void flushTranslationElements(StringBuilder text, int startPos,
            int endPos) {
        if (!editableMode) {
            throw new RuntimeException(
                    "OmContent flush is not in editable mode");
        }

        if (text != null) {
            /* need to use new text. just replace inside editable fragment */
            int begMarkLen = beforeEditable.length() - startPos;
            int endMarkLen = endPos - beforeEditable.length()
                    - insideEditable.length();
            insideEditable.setLength(0);
            insideEditable.append(text.substring(begMarkLen, text.length()
                    - endMarkLen));
            for (WeakReference<Mark> ref : positionsInsideEditable) {
                Mark mark = ref.get();
                if (mark != null) {
                    mark.offset = 0;
                }
            }
        }

        for (WeakReference<Mark> ref : positionsUnflushed) {
            Mark mark = ref.get();
            if (mark != null) {
                mark.positionType = calculatePositionType(startPos
                        + mark.offset);
                switch (mark.positionType) {
                case BEFORE_EDITABLE:
                    mark.offset = mark.offset + startPos;
                    positionsBeforeEditable.add(ref);
                    break;
                case INSIDE_EDITABLE:
                    mark.offset = mark.offset + startPos
                            - beforeEditable.length();
                    positionsInsideEditable.add(ref);
                    break;
                case AFTER_EDITABLE:
                    mark.offset = mark.offset + startPos
                            - beforeEditable.length() - insideEditable.length();
                    positionsAfterEditable.add(ref);
                    break;
                }
            }
        }
        positionsUnflushed.clear();
    }

    /**
     * Insert text in the specified position and move marks.
     * 
     * @param where
     *            position to insert
     * @param str
     *            inserted text
     */
    public UndoableEdit insertString(int where, String str)
            throws BadLocationException {
        UIThreadsUtil.mustBeSwingThread();
        LOGGER.finest("insert string at " + where + " length=" + str.length());

        UndoableEdit undo = doc.createUndo();

        POSITION_TYPE positionType = calculatePositionType(where);
        int relativeOffset = calculateRelativeOffset(where, positionType);
        if (positionType != POSITION_TYPE.INSIDE_EDITABLE) {
            throw new BadLocationException("Invalid mark processing", where);
        }

        insideEditable.insert(relativeOffset, str);
        shiftMarks(positionsInsideEditable, relativeOffset, str.length());

        return undo;
    }

    /**
     * Remove text from the specified position and move marks.
     * 
     * @param where
     *            position to remove
     * @param nitems
     *            length of removed text
     */
    public UndoableEdit remove(int where, int nitems)
            throws BadLocationException {
        UIThreadsUtil.mustBeSwingThread();
        LOGGER.finest("remove string at " + where + " length=" + nitems);

        UndoableEdit undo = doc.createUndo();

        POSITION_TYPE positionType = calculatePositionType(where);
        int relativeOffset = calculateRelativeOffset(where, positionType);
        if (positionType != POSITION_TYPE.INSIDE_EDITABLE) {
            throw new BadLocationException("Invalid mark processing", where);
        }
        insideEditable.delete(relativeOffset, relativeOffset + nitems);
        shiftMarks(positionsInsideEditable, relativeOffset + nitems, -nitems);

        return undo;
    }

    public int length() {
        UIThreadsUtil.mustBeSwingThread();
        return beforeEditable.length() + insideEditable.length()
                + afterEditable.length();
    }

    /**
     * Shift marks on the content edit.
     * 
     * @param positionType
     * @param relativeOffset
     * @param shiftValue
     */
    private void shiftMarks(List<WeakReference<Mark>> marksList,
            int relativeOffset, int shiftValue) {
        for (WeakReference<Mark> ref : marksList) {
            Mark mark = ref.get();
            if (mark != null) {
                if (mark.offset >= relativeOffset) {
                    mark.offset += shiftValue;
                }
            }
        }
    }

    /**
     * Calculate - where offset placed.
     * 
     * @param offset
     *            offset
     * @return part - before/inside/after editable
     */
    private POSITION_TYPE calculatePositionType(int offset) {
        POSITION_TYPE result;
        if (offset < beforeEditable.length()) {
            result = POSITION_TYPE.BEFORE_EDITABLE;
        } else if (offset < beforeEditable.length() + insideEditable.length()) {
            result = POSITION_TYPE.INSIDE_EDITABLE;
        } else {
            result = POSITION_TYPE.AFTER_EDITABLE;
        }
        return result;
    }

    /**
     * Calculate relation offset from position type part.
     * 
     * @param absoluteOffset
     *            absolute offset, i.e. from document begin
     * @param positionType
     *            position type
     * @return relation offset
     */
    private int calculateRelativeOffset(int absoluteOffset,
            POSITION_TYPE positionType) {
        switch (positionType) {
        case BEFORE_EDITABLE:
            return absoluteOffset;
        case INSIDE_EDITABLE:
            return absoluteOffset - beforeEditable.length();
        case AFTER_EDITABLE:
            return absoluteOffset - beforeEditable.length()
                    - insideEditable.length();
        default:
            throw new IllegalArgumentException();
        }
    }

    /**
     * Class for remember position outside content, i.e. in Elements, etc.
     */
    protected class StickyPosition implements Position {
        private final Mark mark;

        public StickyPosition(Mark mark) {
            this.mark = mark;
        }

        public int getOffset() {
            UIThreadsUtil.mustBeSwingThread();
            switch (mark.positionType) {
            case BEFORE_EDITABLE:
                return mark.offset;
            case INSIDE_EDITABLE:
                return beforeEditable.length() + mark.offset;
            case AFTER_EDITABLE:
                return beforeEditable.length() + insideEditable.length()
                        + mark.offset;
            }
            throw new IllegalArgumentException();
        }

        @Override
        public String toString() {
            return getOffset() + "(" + mark.positionType + " " + mark.offset
                    + ")";
        }
    }

    /**
     * Class for remember position inside content, i.e. for move these positions
     * on document's text change.
     */
    private static class Mark {
        POSITION_TYPE positionType;
        int offset;

        @Override
        public String toString() {
            return (positionType != null ? positionType.name() : "default")
                    + ": " + offset;
        }
    }
}
