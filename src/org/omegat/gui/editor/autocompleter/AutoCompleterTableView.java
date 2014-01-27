/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Zoltan Bartko, Aaron Madlon-Kay
               2014 Aaron Madlon-Kay
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

package org.omegat.gui.editor.autocompleter;

import java.awt.Component;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.JTable;

import org.omegat.util.StaticUtils;

/**
 * Table-based auto-completer view
 * 
 * @author bartkoz
 * @author Aaron Madlon-Kay
 */
public abstract class AutoCompleterTableView extends AbstractAutoCompleterView {
    
    /**
     * the table. Use getTable() to access the value;
     */
    private static JTable table;
    
    public AutoCompleterTableView(String name, AutoCompleter completer) {
        super(name,completer);
        getTable().changeSelection(0, 0, false, false);
    }
    
    /**
     * Set the selection.
     * @param p the new point
     */
    public void setSelection(Point p) {
        getTable().changeSelection(p.y, p.x, false, false);
    }
    
    public JTable getTable() {
        if (table == null) {
            table = new JTable();
            table.setCellSelectionEnabled(true);
            table.setFocusable(false);
            table.setTableHeader(null);
        }
        return table;
    }
    
    @Override
    public Component getViewContent() {
        return getTable();
    }
    
    /**
     * Get the point selected in the table.
     * @return 
     */
    public Point getSelectionPoint() {
        return new Point(getTable().getSelectedColumn(), getTable().getSelectedRow());
    }
    
    @Override
    public boolean processKeys(KeyEvent e, boolean visible) {
        
        if (StaticUtils.isKey(e, KeyEvent.VK_UP, 0)) {
            // process key UP
            if (visible) {
                selectPreviousPossibleValueUp();
            }
            return true;
        }

        if (StaticUtils.isKey(e, KeyEvent.VK_LEFT, 0)) {
            // process key LEFT
            if (visible) {
                selectPreviousPossibleValueLeft();
            }
            return true;
        }
        
        if (StaticUtils.isKey(e, KeyEvent.VK_DOWN, 0)) {
            // process key DOWN
            if (visible) {
                selectNextPossibleValueDown();
            }
            return true;
        }

        if (StaticUtils.isKey(e, KeyEvent.VK_RIGHT, 0)) {
            // process key RIGHT
            if (visible) {
                selectNextPossibleValueRight();
            }
            return true;
        }
        
        if (StaticUtils.isKey(e, KeyEvent.VK_PAGE_UP, 0)) {
            if (visible) {
                selectPreviousPossibleValueByPage();
            }
            return true;
        }

        if (StaticUtils.isKey(e, KeyEvent.VK_PAGE_DOWN, 0)) {
            if (visible) {
                selectNextPossibleValueByPage();
            }
            return true;
        }
        
        if (StaticUtils.isKey(e, KeyEvent.VK_HOME, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())) {
            if (visible) {
                selectFirstPossibleValue();
            }
            return true;
        }

        if (StaticUtils.isKey(e, KeyEvent.VK_END, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())) {
            if (visible) {
                selectLastPossibleValue();
            }
            return true;
        }
        
        if (StaticUtils.isKey(e, KeyEvent.VK_HOME, 0)) {
            if (visible) {
                selectFirstPossibleValueInLine();
            }
            return true;
        }

        if (StaticUtils.isKey(e, KeyEvent.VK_END, 0)) {
            if (visible) {
                selectLastPossibleValueInLine();
            }
            return true;
        }
        
        return false;
    } 
    
    /** 
     * Selects the next item in the list.
     */ 
    protected void selectNextPossibleValueDown() { 
        Point p = getSelectionPoint();
        
        int y = (p.y + 1) % getTable().getModel().getRowCount();
        
        setSelection(new Point(p.x, y));
    }
    
    /**
     * Select the first value in the table (top left).
     */
    protected void selectFirstPossibleValue() {
        setSelection(new Point(0, 0));
    }
    
    /**
     * Select the last value in the table (bottom right).
     */
    protected void selectLastPossibleValue() {
        setSelection(new Point(getTable().getModel().getColumnCount() - 1, 
                getTable().getModel().getRowCount() - 1));
    }
    
    /**
     * Select the first value in the current line.
     */
    protected void selectFirstPossibleValueInLine() {
        setSelection(new Point(0, getTable().getSelectedRow()));
    }
    
    /**
     * Select the last value in the current line.
     */
    protected void selectLastPossibleValueInLine() {
        setSelection(new Point(getTable().getModel().getColumnCount() - 1, 
                getTable().getSelectedRow()));
    }
    
    /**
     * Go one cell to the right.
     */
    protected void selectNextPossibleValueRight() { 
        Point p = getSelectionPoint();
        
        int x = (p.x + 1) % getTable().getModel().getColumnCount();
        
        setSelection(new Point(x, p.y));
    }
    
    /** 
     * Selects the item in the list following the current one by completer.pageRowCount items or go to the first item. 
     * currently selected item is already the last item. 
     */ 
    protected void selectNextPossibleValueByPage() { 
        Point p = getSelectionPoint();
        
        int size = getTable().getModel().getRowCount();
        setSelection(new Point(
                p.x,
                Math.min(p.y + AutoCompleter.pageRowCount, size - 1)));
    }

    /** 
     * Selects the previous item in the list.
     */ 
    protected void selectPreviousPossibleValueUp() { 
        Point p = getSelectionPoint();
        
        int size = getTable().getModel().getRowCount();
        int y = (p.y - 1 + size) % size;
        
        setSelection(new Point(p.x, y));
    } 
    
    /**
     * Go one cell to the left.
     */
    protected void selectPreviousPossibleValueLeft() { 
        Point p = getSelectionPoint();
        
        int size = getTable().getModel().getColumnCount();
        int x = (p.x - 1 + size) % size;
        
        setSelection(new Point(x, p.y));
    }
    
    /** 
     * Selects the item in the list preceding the current one by completer.pageRowCount items or go to the first item.  It won't change the selection if the 
     * currently selected item is already the first item. 
     */ 
    protected void selectPreviousPossibleValueByPage() { 
        Point p = getSelectionPoint();
        
        setSelection(new Point(
                p.x,
                Math.max(p.y - AutoCompleter.pageRowCount, 0)));
    }

    @Override
    public int getRowCount() {
        return getTable().getModel().getRowCount();
    }
    
    @Override
    public int getPreferredHeight() {
        int height = getModifiedRowCount() * getTable().getRowHeight();
        return Math.max(height, 50);
    }
    
    @Override
    public AutoCompleterItem getSelectedValue() {
        Point p = getSelectionPoint();
        Object selection = getTable().getModel().getValueAt(p.y, p.x);
        if (selection instanceof Character) {
            return new AutoCompleterItem(selection.toString(), null);
        }
        return new AutoCompleterItem((String) selection, null);
    }
    
    @Override
    public void setData(List<AutoCompleterItem> entryList) {
        
    }
}
