/*
 * FiltersBean.java
 *
 * Created on 2 Февраль 2005 г., 15:41
 */

package org.omegat.filters2.master;

import java.beans.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;

import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.omegat.filters2.text.TextFilter;
import org.omegat.filters2.text.bundles.ResourceBundleFilter;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.DocumentType;


/**
 * Wrapper around all the file filter classes.
 * Is a JavaBean, so that it's easy to write/read it to/from XML file
 * and provides a table model.
 *
 * @author Maxym Mykhalchuk
 */
public class Filters extends AbstractTableModel implements Serializable
{

    /**
     * Create new empty Filters storage backend.
     * Only for JavaBeans compliance here, do not call!
     * <p>
     * Call rather <code>FilterMaster.getInstance().getFilters()</code>.
     */
    public Filters()
    {
    }
    
    private ArrayList filters = new ArrayList();
    public OneFilter[] getFilter()
    {
        return (OneFilter[])filters.toArray(new OneFilter[0]);
    }
    public void setFilter(OneFilter[] filter)
    {
        filters = new ArrayList(Arrays.asList(filter));
    }
    
    public OneFilter getFilter(int index)
    {
        return (OneFilter)filters.get(index);
    }
    public void setFilter(int index, OneFilter filter)
    {
        while( index>=filters.size() )
            filters.add(null);
        filters.set(index, filter);
    }

    //////////////////////////////////////////////////////////////////////////
    //  TableModel implementation
    //////////////////////////////////////////////////////////////////////////

    public int getColumnCount()
    {
        return 2;
    }
    
    public String getColumnName(int columnIndex)
    {
        switch( columnIndex )
        {
            case 0:
                return "File Format";
            case 1:
                return "On";
        }
        return null;
    }
    
    public Class getColumnClass(int columnIndex)
    {
        switch( columnIndex )
        {
            case 0:
                return String.class;
            case 1:
                return Boolean.class;
        }
        return null;
    }

    public int getRowCount()
    {
        return getFilter().length;
    }

    public Object getValueAt(int rowIndex, int columnIndex)
    {
        OneFilter filter = getFilter(rowIndex);
        switch( columnIndex )
        {
            case 0:
                return filter.getHumanName();
            case 1:
                return new Boolean(filter.isOn());
        }
        return null;
    }
    
    public void setValueAt(Object aValue, int rowIndex, int columnIndex)
    {
        OneFilter filter = getFilter(rowIndex);
        switch( columnIndex )
        {
            case 1:
                filter.setOn(((Boolean)aValue).booleanValue());
                break;
            default:
                throw new IllegalArgumentException("Column Index must be equal to 1");
        }
    }
    
    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
        switch( columnIndex )
        {
            case 0:
                return false;
            case 1:
                return true;
        }
        return false;
    }
    
}
