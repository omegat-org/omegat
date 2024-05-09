/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2007-2008 Martin Fleurke
               2012 Didier Briel
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

package org.omegat.filters2.html2;

import java.awt.Window;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.htmlparser.Parser;
import org.htmlparser.util.ParserException;

import org.omegat.core.Core;
import org.omegat.filters2.AbstractFilter;
import org.omegat.filters2.FilterContext;
import org.omegat.filters2.Instance;
import org.omegat.filters2.TranslationException;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.StringUtil;

/**
 * A filter to translate HTML and XHTML files.
 * <p>
 * Some useful discussion why HTML filter should behave like it does, happened
 * on a <a href="https://sourceforge.net/p/omegat/bugs/108/">bug report</a>
 * devoted to compressing space.
 *
 * @author Maxym Mykhalchuk
 * @author Martin Fleurke
 * @author Didier Briel
 */
public class HTMLFilter2 extends AbstractFilter {
    /** Creates a new instance of HTMLFilter2 */
    public HTMLFilter2() {
    }

    /**
     * Register plugin into OmegaT.
     */
    public static void loadPlugins() {
        Core.registerFilterClass(HTMLFilter2.class);
    }

    public static void unloadPlugins() {
    }

    /** Stores the source encoding of HTML file. */
    private String sourceEncoding;

    /** Stores the target encoding of HTML file. */
    private String targetEncoding;

    /**
     * A regular Expression Pattern to be matched to the strings to be
     * translated. If there is a match, the string should not be translated
     */
    private Pattern skipRegExpPattern;

    /**
     * A map of attribute-name and attribute value pairs that, if it exist in a
     * meta-tag, indicates that the meta-tag should not be translated
     */
    private HashMap<String, String> skipMetaAttributes;

    /**
     * A map of attribute-name and attribute value pairs that, if exist in a
     * tag, indicate that this tag should not be translated
     */
    private HashMap<String, String> ignoreTagsAttributes;

    @Override
    protected boolean requirePrevNextFields() {
        return true;
    }

    @Override
    protected String getInputEncoding(FilterContext filterContext, File infile) throws IOException {
        String encoding = filterContext.getInEncoding();
        if (encoding == null && isSourceEncodingVariable()) {
            try (HTMLReader hreader = new HTMLReader(infile.getAbsolutePath(), StandardCharsets.UTF_8.name())) {
                encoding = hreader.getEncoding();
            }
        }
        return encoding;
    }

    /**
     * Customized version of creating input reader for HTML files, aware of
     * encoding by using <code>EncodingAwareReader</code> class.
     *
     * @see HTMLReader
     */
    @Override
    public BufferedReader createReader(File infile, String encoding) throws IOException {
        HTMLReader hreader = new HTMLReader(infile.getAbsolutePath(), encoding);
        sourceEncoding = hreader.getEncoding();
        return new BufferedReader(hreader);
    }

    /**
     * Customized version of creating an output stream for HTML files, appending
     * charset meta by using <code>HTMLWriter</code> class.
     *
     * @see HTMLWriter
     */
    @Override
    public BufferedWriter createWriter(File outfile, String encoding) throws IOException {
        HTMLWriter hwriter;
        HTMLOptions options = new HTMLOptions(processOptions);
        if (encoding == null) {
            this.targetEncoding = sourceEncoding;
        } else {
            this.targetEncoding = encoding;
        }
        hwriter = new HTMLWriter(outfile.getAbsolutePath(), this.targetEncoding, options);
        return new BufferedWriter(hwriter);
    }

    @Override
    public void processFile(BufferedReader infile, BufferedWriter outfile,
            org.omegat.filters2.FilterContext fc) throws IOException, TranslationException {
        StringBuilder all = null;
        try {
            all = new StringBuilder();
            char[] cbuf = new char[1000];
            int len = -1;
            while ((len = infile.read(cbuf)) > 0) {
                all.append(cbuf, 0, len);
            }
        } catch (OutOfMemoryError e) {
            // out of memory?
            all = null;
            System.gc();
            throw new IOException(OStrings.getString("HTML__FILE_TOO_BIG"));
        }

        HTMLOptions options = new HTMLOptions(processOptions);

        // Prepare matcher
        String skipRegExp = options.getSkipRegExp();
        if (!StringUtil.isEmpty(skipRegExp)) {
            try {
                this.skipRegExpPattern = Pattern.compile(skipRegExp, Pattern.CASE_INSENSITIVE);
            } catch (PatternSyntaxException e) {
                Log.log(e);
            }
        }

        // prepare set of attributes that indicate not to translate a meta-tag
        String skipMetaString = options.getSkipMeta();
        skipMetaAttributes = new HashMap<String, String>();
        String[] skipMetaAttributesStringarray = skipMetaString.split(",");
        for (int i = 0; i < skipMetaAttributesStringarray.length; i++) {
            String keyvalue = skipMetaAttributesStringarray[i].trim().toUpperCase(Locale.ENGLISH);
            skipMetaAttributes.put(keyvalue, "");
        }

        // Prepare set of attributes that indicate not to translate a tag
        String ignoreTagString = options.getIgnoreTags();
        ignoreTagsAttributes = new HashMap<String, String>();
        String[] ignoreTagsAttributesStringarray = ignoreTagString.split(",");
        for (int i = 0; i < ignoreTagsAttributesStringarray.length; i++) {
            String keyvalue = ignoreTagsAttributesStringarray[i].trim().toUpperCase(Locale.ENGLISH);
            ignoreTagsAttributes.put(keyvalue, "");
        }

        Parser parser = new Parser();
        try {
            parser.setInputHTML(all.toString());
            parser.visitAllNodesWith(new FilterVisitor(this, outfile, options));
        } catch (ParserException pe) {
            Log.logErrorRB(pe, "HTML_EXCEPTION_PARSER");
        } catch (StringIndexOutOfBoundsException se) {
            throw new StringIndexOutOfBoundsException(OStrings.getString("HTML__INVALID_HTML"));
        }
    }

    // ////////////////////////////////////////////////////////////////////////

    /** Package-internal processEntry to give it to FilterVisitor */
    public String privateProcessEntry(String entry, String comment) {
        if (skipRegExpPattern != null) {
            if (skipRegExpPattern.matcher(entry).matches()) {
                return entry;
            } else {
                return super.processEntry(entry, comment);
            }
        }
        return super.processEntry(entry, comment);
    }

    // ////////////////////////////////////////////////////////////////////////

    public boolean isTargetEncodingVariable() {
        return true;
    }

    public boolean isSourceEncodingVariable() {
        return true;
    }

    public String getFileFormatName() {
        return OStrings.getString("HTML__FILTER_NAME");
    }

    public Instance[] getDefaultInstances() {
        return new Instance[] { new Instance("*.htm", null, "UTF-8"), new Instance("*.html", null, "UTF-8"),
                new Instance("*.xhtml", null, "UTF-8"), new Instance("*.xht", null, "UTF-8") };
    }

    /**
     * Returns the editing hint for HTML filter.
     * <p>
     * In English, the hint is as follows: <br>
     * Note: Source File Encoding setting affects only the HTML files that have
     * no encoding declaration inside. If HTML file has the encoding
     * declaration, it will be used disregarding any value you set in this
     * dialog.
     */
    @Override
    public String getHint() {
        return OStrings.getString("HTML_NOTE");
    }

    /**
     * Returns true to indicate that (X)HTML filter has options.
     *
     * @return True, because (X)HTML filter has options.
     */
    @Override
    public boolean hasOptions() {
        return true;
    }

    /**
     * (X)HTML Filter shows a <b>modal</b> dialog to edit its own options.
     *
     * @param parent
     *            parent window
     * @param config
     *            Current options to edit.
     * @return Updated filter options if user confirmed the changes, and current
     *         options otherwise.
     */
    @Override
    public Map<String, String> changeOptions(Window parent, Map<String, String> config) {
        try {
            EditOptionsDialog dialog = new EditOptionsDialog(parent, config);
            dialog.setVisible(true);
            if (EditOptionsDialog.RET_OK == dialog.getReturnStatus()) {
                return dialog.getOptions().getOptionsMap();
            } else {
                return null;
            }
        } catch (Exception e) {
            Log.logErrorRB("HTML_EXC_EDIT_OPTIONS");
            Log.log(e);
            return null;
        }
    }

    /**
     * Returns the encoding of the html writer (if already set)
     *
     * @return the target encoding
     */
    public String getTargetEncoding() {
        return this.targetEncoding;
    }

    public boolean checkDoSkipMetaTag(String key, String value) {
        return skipMetaAttributes
                .containsKey(key.toUpperCase(Locale.ENGLISH) + "=" + value.toUpperCase(Locale.ENGLISH));
    }

    public boolean checkIgnoreTags(String key, String value) {
        return ignoreTagsAttributes
                .containsKey(key.toUpperCase(Locale.ENGLISH) + "=" + value.toUpperCase(Locale.ENGLISH));
    }

    @Override
    public String getInEncodingLastParsedFile() {
        return sourceEncoding;
    }

}
