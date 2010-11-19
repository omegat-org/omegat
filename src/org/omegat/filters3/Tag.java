/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
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

package org.omegat.filters3;


/**
 * A tag in a source text.
 *
 * @author Maxym Mykhalchuk
 */
public abstract class Tag implements Element
{
    /** Begin of a paired tag. */
    public static final int TYPE_BEGIN = 1;
    /** End of a paired tag. */
    public static final int TYPE_END = 2;
    /** Standalone tag. */
    public static final int TYPE_ALONE = 3;
    
    private String tag;
    /** Returns this tag. */
    public String getTag() 
    {
        return tag;
    }

    private String shortcut;
    /** Returns the short form of this tag, most often -- the first letter. */
    public String getShortcut() 
    {
        if (shortcut!=null)
            return shortcut;
        else
            return Character.toString(getTag().charAt(0));
    }
    
    private int type;
    /** Returns type of this tag. */
    public int getType() 
    {
        return type;
    }
    /** Sets type of this tag. */
    public void setType(int type)
    {
        this.type = type;
    }
    
    private Attributes attributes;
    /** Returns tag's attributes. */
    public Attributes getAttributes()
    {
        return attributes;
    }
    
    private int index;
    /** Returns the index of this tag in the entry. */
    public int getIndex()
    {
        return index;
    }
    /**
     * Sets the index of the tag in the entry for proper shortcutization.
     * E.g. if called for &lt;strong&gt; tag with shortcut=3, 
     * {@link #toShortcut()} will return &lt;s3&gt; and {@link #toTMX()}
     * will return &lt;bpt i="3"&gt;&amp;lt;strong&amp;gt;&lt;/bpt&gt;.
     */
    public void setIndex(int shortcut)
    {
        this.index = shortcut;
    }

    /** Creates a new instance of Tag */
    public Tag(String tag, String shortcut, int type, Attributes attributes)
    {
        this.tag = tag;
        this.shortcut = shortcut;
        this.type = type;
        this.attributes = attributes;
    }

    /**
     * Returns long XML-encoded representation of the tag to store in TMX.
     * This implementation encloses {@link toPartialTMX()} in &lt;bpt&gt;, 
     * &lt;ept&gt; or &lt;ph&gt;.
     * Can be overriden in ancestors if needed, but most probably you won't
     * ever need to override this method, and override {@link toPartialTMX()}
     * instead.
     * E.g. for &lt;strong&gt; tag should return 
     * &lt;bpt i="3"&gt;&amp;lt;strong&amp;gt;&lt;/bpt&gt;.
     */
    public String toTMX() 
    {
        String tmxtag;
        switch(getType())
        {
            case TYPE_BEGIN:
                tmxtag = "bpt";                                                 
                break;
            case TYPE_END:
                tmxtag = "ept";                                                 
                break;
            case TYPE_ALONE:
                tmxtag = "ph";                                                  
                break;
            default:
                throw new RuntimeException("Shouldn't hapen!");                 
        }
        
        StringBuffer buf = new StringBuffer();
        
        buf.append("<");                                                        
        buf.append(tmxtag);
        buf.append(" i=\"");                                                    
        buf.append(getIndex());
        buf.append("\">");                                                      

        buf.append(toPartialTMX());
        
        buf.append("</");                                                       
        buf.append(tmxtag);
        buf.append(">");                                                        
        
        return buf.toString();
    }
    
    /**
     * Returns short XML-encoded representation of the tag to store in TMX,
     * without enclosing &lt;bpt&gt;, &lt;ept&gt; or &lt;ph&gt;.
     * Can be overriden in ancestors if needed.
     * E.g. for &lt;strong&gt; tag should return 
     * &amp;lt;strong&amp;gt;
     */
    protected String toPartialTMX()
    {
        StringBuffer buf = new StringBuffer();
        
        buf.append("&amp;lt;");                                                 
        if (TYPE_END==getType())
            buf.append("/");                                                    
        buf.append(getTag());
        buf.append(getAttributes().toString());
        if (TYPE_ALONE==getType())
            buf.append("/");                                                    
        buf.append("&amp;gt;");                                                 
        
        return buf.toString();
    }

    /**
     * Returns shortcut string representation of the element. 
     * E.g. for &lt;strong&gt; tag should return &lt;s3&gt;.
     */
    public String toShortcut() 
    {
        StringBuffer buf = new StringBuffer();
        
        buf.append("<");                                                        
        if (TYPE_END==getType())
            buf.append("/");                                                    
        buf.append(getShortcut());
        buf.append(getIndex());
        if (TYPE_ALONE==getType())
            buf.append("/");                                                    
        buf.append(">");                                                        
        
        return buf.toString();
    }
    
    /**
     * Returns the tag in its original form as it was in original document.
     * Must be overriden by ancestors.
     * E.g. for &lt;strong&gt; tag should return 
     * &lt;bpt i="3"&gt;&amp;lt;strong&amp;gt;&lt;/bpt&gt;.
     */
    public abstract String toOriginal();
}

