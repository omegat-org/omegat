/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2007-2010 Didier Briel
               2010 Antonio Vilei
               2011-2016 Didier Briel
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

package org.omegat.filters3.xml.openxml;

import org.omegat.filters3.Attribute;
import org.omegat.filters3.Attributes;
import org.omegat.filters3.xml.DefaultXMLDialect;

/**
 * Dialect of Open XML files.
 *
 * @author Didier Briel
 * @author Antonio Vilei
 */
public class OpenXMLDialect extends DefaultXMLDialect {

    /**
     * Actually defines the dialect. It cannot be done during creation, because
     * options are not known at that step.
     */
    public void defineDialect(OpenXMLOptions options) {
        defineParagraphTags(new String[] {
                // Word
                "w:p", "w:tab", "dc:title", "dc:subject", "dc:creator",
                // Excel
                "si", "comment", "definedName",
                // PowerPoint
                "a:p", "c:v",
                // Visio
                "Text"});

        if (options.getBreakOnBr()) {
            defineParagraphTag("w:br"); // Word
        }

        if (options.getTranslateHiddenText()) {
            defineOutOfTurnTag("w:instrText");
        } else {
            defineIntactTag("w:instrText");
        }
        if (!options.getTranslateFallbackText()) { // Word
            defineIntactTag("mc:Fallback");
        }

        defineIntactTags(new String[] {
                // Excel
                "authors", "rPh", "definedNames",
                // PowerPoint
                "p:attrName", "a:tableStyleId",
                // Charts
                "c:f", "c:formatCode",
                // Word
                "wp:align", "wp:posOffset", "wp14:pctWidth", "wp14:pctHeight", "w:fldChar", "cp:lastModifiedBy",
                "cp:revision", "cp:lastPrinted", "dcterms:created", "dcterms:modified", "cp:version",
                // Drawings
                "xdr:col", "xdr:row", "xdr:colOff", "xdr:rowOff",
                // Visio (copied directly from the Visio filter. As far as we know, the dialect is the same)
                "DocumentProperties",
                "DocumentSettings",
                "Colors",
                "FaceNames",
                "StyleSheets",
                "DocumentSheet",
                "Masters",
                "Misc",
                "TextBlock",
                "Geom",
                "Para",
                "Char",
                "Connection",
                "XForm",
                "Line",
                "Fill",
                "Event",
                "PageSheet",
                "PageProps",
                "PageLayout",
                "PrintProps",
                "PageHeight",
                "PageWidth",
                "Image",
                "PinY",
                "Width",
                "Height",
                "XForm1D",
                "EndX",
                "LayerMem",
                "TextXForm",
                "Control",
                "ForeignData",
                "Foreign",
                "Menu",
                "Act",
                "User",
                "Help",
                "Copyright",
                "VBProjectData",
                "FooterMargin",
                "HeaderMargin",
                "HeaderFooter",
                "Window",
                "Windows",
                "EventList",
                "Scratch",
                "TextBlock",
                "Protection",
                "Layout",
                "Icon",
                "vx:Event",
                "v14:Geom",
                "vx:Fill",
                "PreviewPicture",
                "vx:Char",
                "vx:Color",
                "vx:Line",
                "FillForegnd",
                "XForm1D",
                "ShdwBkgnd",
                "TextBkgnd",
                "vx:TextBkgnd",
        });

        defineTranslatableTagAttribute("sheet", "name"); // Excel

        if (options.getTranslateWordArt()) {
            defineTranslatableTagAttribute("v:textpath", "string"); // WordArt
        }

        if (options.getTranslateLinks()) {
            defineTranslatableTagAttribute("Relationship", "Target"); // PowerPoint, only if TargetMode is External
        }

        boolean aggregationEnabled = options.getAggregateTags();
        /*
         * The current OpenXML filter finds too many tags, usually causing what
         * users call the "tag soup". Tags aggregation can help alleviate this
         * problem, but can sometimes lead to semantic issues. Aggregation is OK
         * only as a temporary hack, until we improve the OpenXML filter.
         */
        setTagsAggregationEnabled(aggregationEnabled);

        // If defined in the options, set space preserving for all tags
        setForceSpacePreserving(options.getSpacePreserving());
    }

    /**
     * Returns for a given attribute of a given tag if the attribute should be
     * translated with the given other attributes present. If the tagAttribute
     * is returned by getTranslatable(Tag)Attributes(), this function is called
     * to further test the attribute within its context. This allows for example
     * the OpenXML filter to not translate the value attribute of Target, except if TargetMode is External
     */
    @Override
    public Boolean validateTranslatableTagAttribute(String tag, String attribute, Attributes atts) {
        // special case:
        if ("Relationship".equalsIgnoreCase(tag) && attribute.equalsIgnoreCase("Target")) {

                for (int i = 0; i < atts.size(); i++) {
                    Attribute otherAttribute = atts.get(i);
                    if ("TargetMode".equalsIgnoreCase(otherAttribute.getName())
                            && "External".equalsIgnoreCase(otherAttribute.getValue())) {
                        return super.validateTranslatableTagAttribute(tag, attribute, atts);
                    }
                }
                // Do not translate if TargetMode is not External
                return false;
        }
        return super.validateTranslatableTagAttribute(tag, attribute, atts);
    }
}
