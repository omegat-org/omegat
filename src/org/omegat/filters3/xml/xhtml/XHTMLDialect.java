/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2007-2008 Didier Briel, Alex Buloichik, Martin Fleurke
               2012 Didier Briel
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

package org.omegat.filters3.xml.xhtml;

import java.net.URL;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.omegat.filters3.Attribute;
import org.omegat.filters3.Attributes;
import org.omegat.filters3.xml.DefaultXMLDialect;
import org.omegat.util.Log;
import org.xml.sax.InputSource;

/**
 * This class specifies XHTML dialect of XML.
 * 
 * @author Maxym Mykhalchuk
 * @author Didier Briel
 * @author Alex Buloichik
 * @author Martin Fleurke
 */
public class XHTMLDialect extends DefaultXMLDialect {
    private static final Pattern XHTML_PUBLIC_DTD = Pattern.compile("-//W3C//DTD XHTML.*");

    public XHTMLDialect() {
        defineConstraint(CONSTRAINT_PUBLIC_DOCTYPE, XHTML_PUBLIC_DTD);
    }

    private static final Pattern PUBLIC_XHTML = Pattern.compile("-//W3C//DTD\\s+XHTML.+");

    private static final String DTD = "/org/omegat/filters3/xml/xhtml/res/xhtml2-flat.dtd";

    private Boolean translateValue = false;
    private Boolean translateButtonValue = false;

    /**
     * A regular Expression Pattern to be matched to the strings to be
     * translated. If there is a match, the string should not be translated
     */
    private Pattern skipRegExpPattern;

    /**
     * A map of attribute-name and attribute value pairs that, if exist in a
     * meta-tag, indicate that the meta-tag should not be translated
     */
    private HashMap<String, String> skipMetaAttributes;

    /**
     * A map of attribute-name and attribute value pairs that, if exist in a
     * tag, indicate that this tag should not be translated
     */
    private HashMap<String, String> ignoreTagsAttributes;

    /**
     * Resolves external entites if child filter needs it. Default
     * implementation returns <code>null</code>.
     */
    @Override
    public InputSource resolveEntity(String publicId, String systemId) {
        if (publicId != null && PUBLIC_XHTML.matcher(publicId).matches() && systemId.endsWith(".dtd")) {
            URL dtdresource = XHTMLDialect.class.getResource(DTD);
            return new InputSource(dtdresource.toExternalForm());
        } else
            return null;
    }

    /**
     * Actually defines the dialect. It cannot be done during creation, because
     * options are not known at that step.
     */
    public void defineDialect(XHTMLOptions options) {
        defineParagraphTags(new String[] { "html", "head", "title", "body", "address", "blockquote",
                "center", "div", "h1", "h2", "h3", "h4", "h5", "table", "th", "tr", "td", "p", "ol", "ul",
                "li", "dl", "dt", "dd", "form", "textarea", "fieldset", "legend", "label", "select",
                "option", "hr" });
        // Optional paragraph on BR
        if (options.getParagraphOnBr())
            defineParagraphTag("br");

        defineShortcut("br", "br");

        definePreformatTags(new String[] { "textarea", "pre", });

        defineIntactTags(new String[] { "style", "script", "object", "embed", });

        defineTranslatableAttributes(new String[] { "abbr", "alt", "content", "summary", "title", "placeholder"});

        if (options.getTranslateHref())
            defineTranslatableAttribute("href");
        if (options.getTranslateSrc())
            defineTranslatableTagAttribute("img", "src");
        if (options.getTranslateLang())
            defineTranslatableAttributes(new String[] { "lang", "xml:lang", });
        if (options.getTranslateHreflang())
            defineTranslatableAttribute("hreflang");
        if ((this.translateValue = options.getTranslateValue())
                || (this.translateButtonValue = options.getTranslateButtonValue()))
            defineTranslatableTagAttribute("input", "value");

        // Prepare matcher
        String skipRegExp = options.getSkipRegExp();
        if (skipRegExp != null && skipRegExp.length() > 0) {
            try {
                this.skipRegExpPattern = Pattern.compile(skipRegExp, Pattern.CASE_INSENSITIVE);
            } catch (PatternSyntaxException e) {
                Log.log(e);
            }
        }

        // Prepare set of attributes that indicate not to translate a meta-tag
        String skipMetaString = options.getSkipMeta();
        skipMetaAttributes = new HashMap<String, String>();
        String[] skipMetaAttributesStringarray = skipMetaString.split(",");
        for (int i = 0; i < skipMetaAttributesStringarray.length; i++) {
            String keyvalue = skipMetaAttributesStringarray[i].trim().toUpperCase();
            skipMetaAttributes.put(keyvalue, "");
        }

        // Prepare set of attributes that indicate that a tag should be intact
        String ignoreTagsString = options.getIgnoreTags();
        ignoreTagsAttributes = new HashMap<String, String>();
        String[] ignoreTagsAttributesStringarray = ignoreTagsString.split(",");
        for (int i = 0; i < ignoreTagsAttributesStringarray.length; i++) {
            String keyvalue = ignoreTagsAttributesStringarray[i].trim().toUpperCase();
            ignoreTagsAttributes.put(keyvalue, "");
        }

    }

    /**
     * Returns for a given attribute of a given tag if the attribute should be
     * translated with the given other attributes present. If the tagAttribute
     * is returned by getTranslatable(Tag)Attributes(), this function is called
     * to further test the attribute within its context. This allows for example
     * the XHTML filter to not translate the value attribute of an
     * input-element, except when it is a button or submit or reset.
     */
    @Override
    public Boolean validateTranslatableTagAttribute(String tag, String attribute, Attributes atts) {
        // special case:
        if ("INPUT".equalsIgnoreCase(tag) && attribute.equalsIgnoreCase("value")) {
            // special handling of input tags value attribute.
            if (this.translateValue)
                return true;
            else if (this.translateButtonValue) {
                // translate the value only for buttons
                for (int i = 0; i < atts.size(); i++) {
                    Attribute otherAttribute = atts.get(i);
                    if ("type".equalsIgnoreCase(otherAttribute.getName())
                            && ("button".equalsIgnoreCase(otherAttribute.getValue())
                                    || "submit".equalsIgnoreCase(otherAttribute.getValue()) || "reset"
                                    .equalsIgnoreCase(otherAttribute.getValue()))) {
                        return super.validateTranslatableTagAttribute(tag, attribute, atts);
                    }
                }
                // don't translate for other input elements
                return false;
            } else
                // should not be possible, because
                // validateTranslatableTagAttribute
                // is only called when input.value is in
                // translatable(Tag)Attributes.
                return super.validateTranslatableTagAttribute(tag, attribute, atts);
        } else if ("META".equalsIgnoreCase(tag) && "content".equalsIgnoreCase(attribute)) {
            // Special handling of meta-tag: depending on the other attributes
            // the content attribute should or should not be translated.
            // The group of attribute-value pairs indicating non-translation
            // are stored in the configuration
            boolean doSkipMetaTag = false;
            for (int i = 0; i < atts.size(); i++) {
                Attribute otherAttribute = atts.get(i);
                String name = otherAttribute.getName();
                String value = otherAttribute.getValue();
                if (name == null || value == null)
                    continue;
                doSkipMetaTag = checkDoSkipMetaTag(name, value);
                if (doSkipMetaTag)
                    break;
            }
            if (doSkipMetaTag) {
                return false;
            } else {
                return super.validateTranslatableTagAttribute(tag, attribute, atts);
            }
        } else {
            // default:
            return super.validateTranslatableTagAttribute(tag, attribute, atts);
        }
    }

    public Pattern getSkipRegExpPattern() {
        return skipRegExpPattern;
    }

    public HashMap<String, String> getSkipMetaAttributes() {
        return skipMetaAttributes;
    }

    public boolean checkDoSkipMetaTag(String key, String value) {
        return skipMetaAttributes.containsKey(key.toUpperCase() + "=" + value.toUpperCase());
    }

    private boolean checkIgnoreTags(String key, String value) {
        return ignoreTagsAttributes.containsKey(key.toUpperCase() + "=" + value.toUpperCase());
    }

    /**
     * In the XHTML filter, content should be translated in the
     * following condition: The pair attribute-value should not have been
     * declared as untranslatable in the options
     *
     * @param tag
     *            An XML tag
     * @param atts
     *            The attributes associated with the tag
     *@return <code>false</code> if the content of this tag should be
     *         translated, <code>true</code> otherwise
     */
    @Override
    public Boolean validateIntactTag(String tag, Attributes atts) {
        if (atts != null) {
            for (int i = 0; i < atts.size(); i++) {
                Attribute oneAttribute = atts.get(i);
                if (checkIgnoreTags(oneAttribute.getName(), oneAttribute.getValue())) {
                    return true;
                }
            }
        }
        // If no key=value pair is found, the tag can be translated
        return false;
    }

}
