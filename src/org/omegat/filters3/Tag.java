/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
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

package org.omegat.filters3;

import org.omegat.util.StaticUtils;

/**
 * A tag in a source text.
 *
 * @author Maxym Mykhalchuk
 */
public abstract class Tag implements Element {
    public enum Type {
        /** Begin of a paired tag. */
        BEGIN,
        /** End of a paired tag. */
        END,
        /** Standalone tag. */
        ALONE
    };

    private String tag;

    /** Returns this tag. */
    public String getTag() {
        return tag;
    }

    private String shortcut;

    /** Returns the short form of this tag, most often -- the first letter. */
    public String getShortcut() {
        if (shortcut != null) {
            return shortcut;
        } else {
            return String.valueOf(Character.toChars(getTag().codePointAt(0)));
        }
    }

    private Type type;

    /** Returns type of this tag. */
    public Type getType() {
        return type;
    }

    /** Sets type of this tag. */
    public void setType(Type type) {
        this.type = type;
    }

    private Attributes attributes;

    /** Returns tag's attributes. */
    public Attributes getAttributes() {
        return attributes;
    }

    /**
     * Returns attribute value by name.
     */
    public String getAttribute(String name) {
        Attribute attr = getAttributeObject(name);
        return attr == null ? null : attr.getValue();
    }

    /**
     * Returns attribute object by name.
     */
    public Attribute getAttributeObject(String name) {
        for (Attribute a : attributes.list) {
            if (name.equals(a.getName())) {
                return a;
            }
        }
        return null;
    }

    /** Attributes of correspondent start tag. */
    private Attributes startAttributes;

    /** Returns tag's attributes. */
    public Attributes getStartAttributes() {
        return startAttributes;
    }

    public void setStartAttributes(Attributes startAttributes) {
        this.startAttributes = startAttributes;
    }

    private int index;

    /** Returns the index of this tag in the entry. */
    public int getIndex() {
        return index;
    }

    /**
     * Sets the index of the tag in the entry for proper shortcutization. E.g.
     * if called for &lt;strong&gt; tag with shortcut=3, {@link #toShortcut()}
     * will return &lt;s3&gt; and {@link #toTMX()} will return &lt;bpt
     * i="3"&gt;&amp;lt;strong&amp;gt;&lt;/bpt&gt;.
     */
    public void setIndex(int shortcut) {
        this.index = shortcut;
    }

    /** Creates a new instance of Tag */
    public Tag(String tag, String shortcut, Type type, Attributes attributes) {
        this.tag = tag;
        this.shortcut = shortcut;
        this.type = type;
        this.attributes = attributes;
    }

    /**
     * Returns long XML-encoded representation of the tag to store in TMX. This
     * implementation encloses {@link #toPartialTMX()} in &lt;bpt&gt;,
     * &lt;ept&gt; or &lt;ph&gt;. Can be overriden in ancestors if needed, but
     * most probably you won't ever need to override this method, and override
     * {@link #toPartialTMX()} instead. E.g. for &lt;strong&gt; tag should
     * return &lt;bpt i="3"&gt;&amp;lt;strong&amp;gt;&lt;/bpt&gt;.
     */
    public String toTMX() {
        String tmxtag;
        switch (getType()) {
        case BEGIN:
            tmxtag = "bpt";
            break;
        case END:
            tmxtag = "ept";
            break;
        case ALONE:
            tmxtag = "ph";
            break;
        default:
            throw new RuntimeException("Shouldn't hapen!");
        }

        StringBuilder buf = new StringBuilder();

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
     * without enclosing &lt;bpt&gt;, &lt;ept&gt; or &lt;ph&gt;. Can be
     * overriden in ancestors if needed. E.g. for &lt;strong&gt; tag should
     * return &amp;lt;strong&amp;gt;
     */
    protected String toPartialTMX() {
        StringBuilder buf = new StringBuilder();

        buf.append("&amp;lt;");
        if (Type.END == getType()) {
            buf.append("/");
        }
        buf.append(getTag());
        buf.append(getAttributes().toString());
        if (Type.ALONE == getType()) {
            buf.append("/");
        }
        buf.append("&amp;gt;");

        return buf.toString();
    }

    /**
     * Returns shortcut string representation of the element. E.g. for
     * &lt;strong&gt; tag should return &lt;s3&gt;.
     */
    public String toShortcut() {
        StringBuilder buf = new StringBuilder();

        buf.append('<');
        if (Type.END == getType()) {
            buf.append('/');
        }
        buf.append(getShortcut());
        buf.append(getIndex());
        if (Type.ALONE == getType()) {
            buf.append('/');
        }
        buf.append('>');

        return buf.toString();
    }

    public String toSafeCalcShortcut() {
        return StaticUtils.TAG_REPLACEMENT_CHAR + getShortcut().replace('<', '_').replace('>', '_')
                + StaticUtils.TAG_REPLACEMENT_CHAR;
    }

    /**
     * Returns the tag in its original form as it was in original document. Must
     * be overriden by ancestors. E.g. for &lt;strong&gt; tag should return
     * &lt;bpt i="3"&gt;&amp;lt;strong&amp;gt;&lt;/bpt&gt;.
     */
    public abstract String toOriginal();
}
