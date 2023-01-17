/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2005-2006 Henry Pijffers
               2006 Martin Wunderlich
               2006-2007 Didier Briel
               2008 Martin Fleurke, Didier Briel
               2009 Didier Briel, Arno Peters, Alex Buloichik
               2010 Alex Buloichik
               2011 Alex Buloichik, Didier Briel
               2012 Guido Leenders, Thomas Cordonnier
               2013 Alex Buloichik

               Home page: http://www.omegat.org/
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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.filters2.master;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FileUtils;
import org.omegat.filters2.AbstractFilter;
import org.omegat.filters2.FilterContext;
import org.omegat.filters2.IAlignCallback;
import org.omegat.filters2.IFilter;
import org.omegat.filters2.IParseCallback;
import org.omegat.filters2.ITranslateCallback;
import org.omegat.filters2.Instance;
import org.omegat.filters2.TranslationException;
import org.omegat.util.Language;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.StringUtil;

import gen.core.filters.Files;
import gen.core.filters.Filter;
import gen.core.filters.Filter.Option;
import gen.core.filters.Filters;

/**
 * A master class that registers and handles all the filters. Singleton - there
 * can be only one instance of this class.
 *
 * @author Maxym Mykhalchuk
 * @author Henry Pijffers
 * @author Martin Wunderlich
 * @author Didier Briel
 * @author Martin Fleurke
 * @author Arno Peters
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Guido Leenders
 * @author Thomas Cordonnier
 */
public class FilterMaster {
    /** name of the filter configuration file */
    public static final String FILE_FILTERS = "filters.xml";

    private static final JAXBContext CONFIG_CTX;

    /**
     * There was no version of file filters support (1.4.5 Beta 1 -- 1.6.0
     * RC12).
     */
    public static final String INITIAL_VERSION = "";
    /** File filters support of 1.6.0 RC12a: now upgrading the configuration. */
    public static final String OT160RC12A_VERSION = "1.6 RC12a";
    public static final String OT160FINAL_VERSION = "1.6.0";
    public static final String OT161_VERSION = "1.6.1";
    public static final String OT170_VERSION = "1.7.0";
    /** Currently file filters support version. */
    public static final String CURRENT_VERSION = "2.0";

    /** Filters config stored in XML file. */
    private final Filters config;

    /** Classes of all filters. */
    static List<Class<?>> filtersClasses = Collections.emptyList();

    static {
        try {
            CONFIG_CTX = JAXBContext.newInstance(Filters.class);
        } catch (Exception ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static void setFilterClasses(List<Class<?>> classes) {
        filtersClasses = new ArrayList<>(classes);
    }

    /**
     * Create a new FilterMaster.
     */
    public FilterMaster(Filters config) {
        this.config = config;
    }

    public Filters getConfig() {
        return config;
    }

    /**
     * Adds new filters(which was not exist in config yet) into config.
     */
    private static boolean addNewFiltersToConfig(final Filters conf) {
        boolean result = false;
        for (Class<?> fclass : filtersClasses) {
            boolean found = false;
            for (Filter fc : conf.getFilters()) {
                if (fclass.getName().equals(fc.getClassName())) {
                    // filter already exist in config
                    found = true;
                    break;
                }
            }
            if (!found) {
                // filter not found in config
                Filter f = getDefaultSettingsFromFilter(fclass.getName());
                if (f != null) {
                    conf.getFilters().add(f);
                    result = true;
                }
            }
        }
        return result;
    }

    /**
     * Get filter's instance by filter class name.
     *
     * @param classname
     *            filter's class name
     * @return filter instance
     */
    public static IFilter getFilterInstance(final String classname) {
        for (Class<?> f : filtersClasses) {
            if (f.getName().equals(classname)) {
                try {
                    return (IFilter) f.getDeclaredConstructor().newInstance();
                } catch (Exception ex) {
                    Log.log("Failed to instantiate filter: " + classname);
                    Log.log(ex);
                }
            }
        }
        return null;
    }

    /**
     * OmegaT core calls this method to load a source file.
     *
     * @param filename
     *            The name of the source file to load.
     * @return Whether the file was handled by one of OmegaT filters.
     * @see #translateFile(String, String, String, FilterContext,
     *      ITranslateCallback)
     */
    public IFilter loadFile(String filename, FilterContext fc, IParseCallback parseCallback)
            throws IOException, TranslationException {
        IFilter filterObject = null;
        try {
            LookupInformation lookup = lookupFilter(new File(filename), fc);
            if (lookup == null) {
                return null;
            }
            File inFile = new File(filename);
            fc.setInEncoding(lookup.outFilesInfo.getSourceEncoding());
            fc.setOutEncoding(lookup.outFilesInfo.getTargetEncoding());

            filterObject = lookup.filterObject;

            filterObject.parseFile(inFile, lookup.config, fc, parseCallback);
        } catch (Exception ioe) {
            throw new IOException(filename + "\n" + ioe, ioe);
        }
        return filterObject;
    }

    /**
     * OmegaT core calls this method to translate a source file.
     * <ul>
     * <li>OmegaT first looks through registered filter instances to find
     * filter(s) that can handle this file.
     * <li>Tests if filter(s) want to handle it.
     * <li>If the filter accepts the file,
     * <li>Filter is asked to process the file.
     * </ul>
     * If no filter is found, that processes this file, we simply copy it to
     * target folder.
     *
     * @param sourcedir
     *            The folder of the source inFile.
     * @param filename
     *            The name of the source inFile to process (only the part,
     *            relative to source folder).
     * @param targetdir
     *            The folder to place the translated inFile to.
     * @param fc
     *            Filter context.
     */
    public void translateFile(String sourcedir, String filename, String targetdir, FilterContext fc,
            ITranslateCallback translateCallback) throws IOException, TranslationException {
        LookupInformation lookup = lookupFilter(new File(sourcedir, filename), fc);
        if (lookup == null) {
            // The file is not supported by any of the filters.
            // Copying it
            FileUtils.copyFile(new File(sourcedir, filename), new File(targetdir, filename));
            return;
        }

        File inFile = new File(sourcedir, filename).getCanonicalFile();
        File outFile = new File(targetdir, getTargetForSource(filename, lookup, fc.getTargetLang()))
                .getCanonicalFile();

        if (inFile.equals(outFile)) {
            throw new TranslationException(StringUtil
                    .format(OStrings.getString("FILTERMASTER_ERROR_SRC_TRG_SAME_FILE"), inFile.getPath()));
        }

        fc.setInEncoding(lookup.outFilesInfo.getSourceEncoding());
        fc.setOutEncoding(lookup.outFilesInfo.getTargetEncoding());

        IFilter filterObject = lookup.filterObject;
        try {
            filterObject.translateFile(inFile, outFile, lookup.config, fc, translateCallback);
        } catch (Exception ex) {
            Log.log(ex);
        }
    }

    public void alignFile(String sourceDir, String fileName, String targetdir, FilterContext fc,
            IAlignCallback alignCallback) throws Exception {

        LookupInformation lookup = lookupFilter(new File(sourceDir, fileName), fc);
        if (lookup == null) {
            // The file is not supported by any of the filters.
            // Skip it
            return;
        }

        File inFile = new File(sourceDir, fileName);
        File outFile = new File(targetdir, getTargetForSource(fileName, lookup, fc.getTargetLang()));

        if (!outFile.exists()) {
            // out file not exist - skip
            return;
        }

        fc.setInEncoding(lookup.outFilesInfo.getSourceEncoding());
        fc.setOutEncoding(lookup.outFilesInfo.getTargetEncoding());

        IFilter filterObject = lookup.filterObject;
        try {
            filterObject.alignFile(inFile, outFile, lookup.config, fc, alignCallback);
        } catch (Exception ex) {
            Log.log(ex);
        }
    }

    static class LookupInformation {
        public final Files outFilesInfo;
        public final IFilter filterObject;
        public final Map<String, String> config;

        LookupInformation(IFilter filterObject, Files outFilesInfo, Map<String, String> config) {
            this.filterObject = filterObject;
            this.outFilesInfo = outFilesInfo;
            this.config = config;
        }
    }

    /**
     * Gets the filter according to the source filename provided. In case of
     * failing to find a filter to handle the file returns <code>null</code>.
     *
     * In case of finding an appropriate filter it
     * <ul>
     * <li>Creates the filter (use <code>OneFilter.getFilter()</code> to get it)
     * <li>Creates a reader (use <code>OneFilter.getReader()</code> to get it)
     * <li>Checks whether the filter supports the file.
     * </ul>
     *
     * @param inFile
     *            The full path to the source file
     * @return The corresponding LookupInformation
     */
    private LookupInformation lookupFilter(File inFile, FilterContext fc)
            throws TranslationException, IOException {
        for (Filter f : config.getFilters()) {
            if (!f.isEnabled()) {
                continue;
            }
            for (Files ff : f.getFiles()) {
                if (!matchesMask(inFile.getName(), ff.getSourceFilenameMask())) {
                    continue;
                }
                IFilter filterObject = getFilterInstance(f.getClassName());
                if (filterObject == null) {
                    continue;
                }
                fc.setInEncoding(ff.getSourceEncoding());
                fc.setOutEncoding(ff.getTargetEncoding());
                // only for exist filters
                Map<String, String> config = forFilter(f.getOption());
                if (!filterObject.isFileSupported(inFile, config, fc)) {
                    break;
                }
                return new LookupInformation(filterObject, ff, config);
            }
        }
        return null;
    }

    /**
     * Check to see if a file is supported by any filter. When
     * <code>quick</code> is true, only the filename will be checked to see if
     * it matches known supported patterns. When false, the filter may have to
     * actually load some or all of the file in order to determine whether or
     * not it is supported.
     *
     * @param file
     *            The file to check
     * @param quick
     *            When true, check only the file name
     * @return Whether or not the file is supported
     */
    public boolean isFileSupported(File file, boolean quick) {
        FilterContext fc = new FilterContext(null, null, true);
        for (Filter f : config.getFilters()) {
            if (!f.isEnabled()) {
                continue;
            }
            for (Files ff : f.getFiles()) {
                boolean matchesMask = matchesMask(file.getName(), ff.getSourceFilenameMask());
                if (!matchesMask) {
                    continue;
                }
                if (quick && matchesMask) {
                    return true;
                }
                IFilter filterObject = getFilterInstance(f.getClassName());
                if (filterObject == null) {
                    continue;
                }
                fc.setInEncoding(ff.getSourceEncoding());
                fc.setOutEncoding(ff.getTargetEncoding());
                // only for exist filters
                Map<String, String> config = forFilter(f.getOption());
                if (!filterObject.isFileSupported(file, config, fc)) {
                    break;
                }
                return true;
            }
        }
        return false;
    }

    public boolean isBilingualFile(File file) throws Exception {
        FilterContext fc = new FilterContext(null, null, true);
        LookupInformation info = lookupFilter(file, fc);
        return info.filterObject.isBilingual();
    }

    private static List<String> supportedEncodings = null;

    /**
     * Queries JRE for the list of supported encodings. Also adds the human name
     * for no/automatic inEncoding.
     *
     *
     * @return names of all the encodings in an array
     */
    public static List<String> getSupportedEncodings() {
        if (supportedEncodings == null) {
            ArrayList<String> list = new ArrayList<String>();
            list.add(AbstractFilter.ENCODING_AUTO_HUMAN);
            list.addAll(Charset.availableCharsets().keySet());
            supportedEncodings = list;
        }
        return supportedEncodings;
    }

    // ////////////////////////////////////////////////////////////////////////
    // Filters
    // ////////////////////////////////////////////////////////////////////////

    /**
     * Reverts Filter Configuration to Default values. Basically
     * <ul>
     * <li>Sets up built-in filters
     * <li>Reloads the plugins
     * <li>Loads filters from plugins
     * <li>Saves the configuration
     * </ul>
     */
    public static Filters createDefaultFiltersConfig() {
        Filters c = new Filters();
        addNewFiltersToConfig(c);
        return c;
    }

    /**
     * Loads information about the filters from an XML file. If there's an error
     * loading a file, it calls <code>setupDefaultFilters</code>.
     *
     * @throws IOException
     */
    public static Filters loadConfig(File configFile) throws IOException {
        if (!configFile.exists()) {
            return null;
        }
        Filters result;
        try {
            Unmarshaller unm = CONFIG_CTX.createUnmarshaller();
            result = (Filters) unm.unmarshal(configFile);
        } catch (Exception e) {
            Log.logErrorRB("FILTERMASTER_ERROR_LOADING_FILTERS_CONFIG");
            Log.log(e);
            result = new Filters();
        }

        if (addNewFiltersToConfig(result)) {
            saveConfig(result, configFile);
        }

        return result;
    }

    /**
     * Saves information about the filters to an XML file.
     *
     * @throws IOException
     */
    public static void saveConfig(Filters config, File configFile) throws IOException {
        if (config == null) {
            configFile.delete();
            return;
        }
        try {
            Marshaller m = CONFIG_CTX.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            m.marshal(config, configFile);
        } catch (Exception e) {
            Log.logErrorRB("FILTERMASTER_ERROR_SAVING_FILTERS_CONFIG");
            Log.log(e);
            throw new IOException(e);
        }
    }

    // ////////////////////////////////////////////////////////////////////////
    // Static Utility Methods
    // ////////////////////////////////////////////////////////////////////////

    /**
     * Whether the mask matches the filename. Filename should be "name.ext",
     * without path.
     *
     * @param filename
     *            The filename to check
     * @param mask
     *            The mask, against which the filename is tested
     * @return Whether the mask matches the filename.
     */
    private boolean matchesMask(String filename, String mask) {
        mask = mask.replaceAll("\\.", "\\\\.");
        mask = mask.replaceAll("\\*", ".*");
        mask = mask.replaceAll("\\?", ".");
        return filename.matches("(?iu)" + mask);
    }

    /**
     * Return current system time in the specified date format.
     *
     * @param dateFormat
     *            Date format for java.text.SimpleDateFormat.
     */
    public static String now(String dateFormat) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        return sdf.format(cal.getTime());
    }

    /**
     * Calculate the target path corresponding to the given source file.
     * 
     * @param sourceDir
     *            Path to the project's <code>source</code> dir
     * @param srcRelPath
     *            Relative path under <code>sourceDir</code> of the source file
     * @param fc
     *            Filter context
     * @return The relative path under <code>target</code> of the corresponding
     *         target file
     * @throws IOException
     * @throws TranslationException
     */
    public String getTargetForSource(String sourceDir, String srcRelPath, FilterContext fc)
            throws IOException, TranslationException {
        File srcFile = new File(sourceDir, srcRelPath);
        if (!srcFile.isFile()) {
            throw new IllegalArgumentException(
                    "The sourceDir and srcRelPath arguments must together point to an existing file.");
        }
        LookupInformation lookup = lookupFilter(srcFile, fc);
        return getTargetForSource(srcRelPath, lookup, fc.getTargetLang());
    }

    private static String getTargetForSource(String srcRelPath, LookupInformation lookup,
            Language targetLang) {
        File srcRelFile = new File(srcRelPath);
        return new File(srcRelFile.getParent(),
                constructTargetFilename(lookup.outFilesInfo.getSourceFilenameMask(), srcRelFile.getName(),
                        lookup.outFilesInfo.getTargetFilenamePattern(), targetLang,
                        lookup.outFilesInfo.getSourceEncoding(), lookup.outFilesInfo.getTargetEncoding(),
                        lookup.filterObject.getFileFormatName())).getPath();
    }

    /**
     * Construct a target filename according to pattern from a file's name.
     * Filename should be "name.ext", without path.
     * <p>
     * Output filename pattern is pretty complex. <br>
     * It may consist of normal characters and some substituted variables. They
     * have the format <code>${variableName}</code> and are case insensitive.
     * <br>
     * There're such variables:
     * <ul>
     * <li><code>${filename}</code> - full filename of the input file, both name
     * and extension (default)
     * <li><code>${nameOnly}</code> - only the name of the input file without
     * extension part
     * <li><code>${extension}</code> - the extension of the input file
     * <li><code>${nameOnly-1}</code> - only the name of the input file with
     * first extension
     * <li><code>${extension-1}</code> - the extensions, without the first one
     * <li><code>${targetLocale}</code> - target locale code (of a form "xx_YY")
     * <li><code>${targetLanguage}</code> - the target language and country code
     * together (of a form "XX-YY")
     * <li><code>${targetLanguageCode}</code> - the target language only ("XX")
     * <li><code>${targetCountryCode}</code> - the target country only ("YY")
     * <li><code>${1}, ${2}, ...</code> - variables captured by jokers (* or ?)
     * </ul>
     * <p>
     * Most file filters will use default "<code>${filename}</code>, that leads
     * to the name of translated file being the same as the name of source file.
     * But for example the Java(TM) Resource Bundles file filter will have the
     * pattern equal to "<code>${nameonly}_${targetlanguage}.${extension}</code>
     * ".
     * <p>
     * E.g. if you have
     * <ul>
     * <li>a source filename mask "*.ext1.ext2"
     * <li>file name "thisisfile.ext1.ext2"
     * </ul>
     * Then
     * <ul>
     * <li><code>${nameOnly}</code> will be equal to "thisisfile"
     * <li>and <code>${extension}</code> - "ext1.ext2"
     * <li><code>${nameOnly-1}</code> will be equal to "thisisfile.ext1"
     * <li>and <code>${extension-1}</code> - "ext2"
     * </ul>
     *
     * @param filename
     *            Filename to change
     * @param pattern
     *            Pattern, according to which we change the filename
     * @return The changed filename
     */
    private static String constructTargetFilename(String sourceMask, String filename, String pattern,
            Language targetLang, String sourceEncoding, String targetEncoding, String filterFormatName) {
        int lastStarPos = sourceMask.lastIndexOf('*');
        int dot = 0;
        if (lastStarPos >= 0) {
            // bugfix #1204740
            // so where's the dot next to the star
            int lastDotPos = sourceMask.indexOf('.', lastStarPos);
            // counting chars after the dot
            int extlength = sourceMask.length() - lastDotPos;
            // going forward this many chars
            // and finding the dot we looked for
            dot = filename.length() - extlength;
        } else {
            dot = filename.lastIndexOf('.');
        }

        String nameOnly = filename;
        String extension = "";
        if (dot >= 0) {
            nameOnly = filename.substring(0, dot);
            extension = filename.substring(dot + 1);
        }

        String res = pattern;
        res = res.replace(AbstractFilter.TFP_FILENAME, filename);
        res = res.replace(AbstractFilter.TFP_NAMEONLY, nameOnly);
        res = res.replace(AbstractFilter.TFP_EXTENSION, extension);

        res = res.replace(AbstractFilter.TFP_TARGET_LOCALE, targetLang.getLocaleCode());
        res = res.replace(AbstractFilter.TFP_TARGET_LANGUAGE, targetLang.getLanguage());
        res = res.replace(AbstractFilter.TFP_TARGET_LANG_CODE, targetLang.getLanguageCode());
        res = res.replace(AbstractFilter.TFP_TARGET_COUNTRY_CODE, targetLang.getCountryCode());
        // Replace also old variable spelling
        res = res.replace(AbstractFilter.TFP_TARGET_COUTRY_CODE, targetLang.getCountryCode());
        res = res.replace(AbstractFilter.TFP_TARGET_LOCALE_LCID, targetLang.getLocaleLCID());
        //
        // System generation time
        //
        res = res.replace(AbstractFilter.TFP_TIMESTAMP_LA, now("a"));
        res = res.replace(AbstractFilter.TFP_TIMESTAMP_LD, now("d"));
        res = res.replace(AbstractFilter.TFP_TIMESTAMP_LDD, now("dd"));
        res = res.replace(AbstractFilter.TFP_TIMESTAMP_LH, now("h"));
        res = res.replace(AbstractFilter.TFP_TIMESTAMP_LHH, now("hh"));
        res = res.replace(AbstractFilter.TFP_TIMESTAMP_LM, now("m"));
        res = res.replace(AbstractFilter.TFP_TIMESTAMP_LMM, now("mm"));
        res = res.replace(AbstractFilter.TFP_TIMESTAMP_LS, now("s"));
        res = res.replace(AbstractFilter.TFP_TIMESTAMP_LSS, now("ss"));
        res = res.replace(AbstractFilter.TFP_TIMESTAMP_LYYYY, now("yyyy"));
        res = res.replace(AbstractFilter.TFP_TIMESTAMP_UD, now("D"));
        res = res.replace(AbstractFilter.TFP_TIMESTAMP_UEEE, now("EEE"));
        res = res.replace(AbstractFilter.TFP_TIMESTAMP_UEEEE, now("EEEE"));
        res = res.replace(AbstractFilter.TFP_TIMESTAMP_UH, now("H"));
        res = res.replace(AbstractFilter.TFP_TIMESTAMP_UHH, now("HH"));
        res = res.replace(AbstractFilter.TFP_TIMESTAMP_UM, now("M"));
        res = res.replace(AbstractFilter.TFP_TIMESTAMP_UMM, now("MM"));
        res = res.replace(AbstractFilter.TFP_TIMESTAMP_UMMM, now("MMM"));
        //
        // Workstation properties
        //
        res = res.replace(AbstractFilter.TFP_SYSTEM_OS_NAME, System.getProperty("os.name"));
        res = res.replace(AbstractFilter.TFP_SYSTEM_OS_VERSION, System.getProperty("os.arch"));
        res = res.replace(AbstractFilter.TFP_SYSTEM_OS_ARCH, System.getProperty("os.version"));
        res = res.replace(AbstractFilter.TFP_SYSTEM_USER_NAME, System.getProperty("user.name"));
        if (res.contains(AbstractFilter.TFP_SYSTEM_HOST_NAME)) {
            String hostName = null;
            try {
                // This is expensive! Only do it if necessary!
                hostName = java.net.InetAddress.getLocalHost().getHostName();
            } catch (java.net.UnknownHostException uhe) {
                hostName = "";
            }
            res = res.replace(AbstractFilter.TFP_SYSTEM_HOST_NAME, hostName);
        }
        //
        // File properties.
        //
        String sourceEncodingText = "auto";
        if (sourceEncoding != null) {
            sourceEncodingText = sourceEncoding;
        }
        res = res.replace(AbstractFilter.TFP_FILE_SOURCE_ENCODING, sourceEncodingText);
        //
        String targetEncodingText = "auto";
        if (targetEncoding != null) {
            targetEncodingText = targetEncoding;
        }
        res = res.replace(AbstractFilter.TFP_FILE_TARGET_ENCODING, targetEncodingText);
        //
        res = res.replace(AbstractFilter.TFP_FILE_FILTER_NAME, filterFormatName);
        //

        String sourceMaskPattern = sourceMask.replaceAll("\\?", "(.)").replaceAll("\\*", "(.*?)");
        java.util.regex.Matcher sourceMatcher = Pattern.compile(sourceMaskPattern).matcher(filename);
        if (sourceMatcher.find()) {
            for (int i = 1; i <= sourceMatcher.groupCount(); i++) {
                res = res.replaceAll("\\$\\{" + i + "\\}", sourceMatcher.group(i));
            }
        }

        String[] splitName = filename.split("\\.");
        StringBuilder nameOnlyBuf = new StringBuilder(splitName[0]);
        StringBuilder extensionBuf = new StringBuilder(splitName[splitName.length - 1]);
        for (int i = 0; i < splitName.length; i++) {
            res = res.replaceAll("\\$\\{nameOnly-" + i + "\\}", nameOnlyBuf.toString());
            res = res.replaceAll("\\$\\{extension-" + i + "\\}", extensionBuf.toString());
            if (i + 1 < splitName.length) {
                nameOnlyBuf.append(".").append(splitName[i + 1]);
                extensionBuf.insert(0, splitName[splitName.length - i - 2] + '.');
            }
        }

        return res;
    }

    /**
     * Clone config for editing
     *
     * @return new config instance
     */
    public static Filters cloneConfig(Filters orig) {
        Filters c = new Filters();
        c.setRemoveTags(orig.isRemoveTags());
        c.setRemoveSpacesNonseg(orig.isRemoveSpacesNonseg());
        c.setPreserveSpaces(orig.isPreserveSpaces());
        c.setIgnoreFileContext(orig.isIgnoreFileContext());
        for (Filter f : orig.getFilters()) {
            c.getFilters().add(cloneFilter(f));
        }
        return c;
    }

    /**
     * Clone one filter's config for editing.
     *
     * @param f
     *            one filter's config
     * @return new config instance
     */
    public static Filter cloneFilter(Filter filter) {
        Filter f = new Filter();
        f.setClassName(filter.getClassName());
        f.setEnabled(filter.isEnabled());
        for (Files ff : filter.getFiles()) {
            f.getFiles().add(cloneFiles(ff));
        }
        for (Option o : filter.getOption()) {
            Option fo = new Option();
            fo.setName(o.getName());
            fo.setValue(o.getValue());
            f.getOption().add(fo);
        }
        return f;
    }

    /**
     * Clone one filter's instance config for editing.
     *
     * @param f
     *            new filter's instance config
     * @return new config instance
     */
    private static Files cloneFiles(Files files) {
        Files ff = new Files();
        ff.setSourceEncoding(files.getSourceEncoding());
        ff.setSourceFilenameMask(files.getSourceFilenameMask());
        ff.setTargetEncoding(files.getTargetEncoding());
        ff.setTargetFilenamePattern(files.getTargetFilenamePattern());
        return ff;
    }

    /**
     * Create default filter's config.
     *
     * @param filterClassname
     *            filter's classname
     * @return default filter's config
     */
    public static Filter getDefaultSettingsFromFilter(final String filterClassname) {
        IFilter f = getFilterInstance(filterClassname);
        if (f == null) {
            return null;
        }
        Filter fc = new Filter();
        fc.setClassName(f.getClass().getName());
        fc.setEnabled(f.isEnabledInDefault());
        for (Instance ins : f.getDefaultInstances()) {
            Files ff = new Files();
            ff.setSourceEncoding(ins.getSourceEncoding());
            ff.setSourceFilenameMask(ins.getSourceFilenameMask());
            ff.setTargetEncoding(ins.getTargetEncoding());
            ff.setTargetFilenamePattern(ins.getTargetFilenamePattern());
            fc.getFiles().add(ff);
        }
        return fc;
    }

    /**
     * Convert options from xml for filter usage.
     *
     * @param options
     *            xml options
     * @return options for filter usage
     */
    public static Map<String, String> forFilter(List<Option> options) {
        final Map<String, String> result = new TreeMap<String, String>();
        for (Option opt : options) {
            result.put(opt.getName(), opt.getValue());
        }
        return result;
    }

    /**
     * Convert options to xml from map.
     *
     * @param f
     *            filter
     * @param newOptions
     *            options
     */
    public static void setOptions(Filter f, Map<String, String> newOptions) {
        f.getOption().clear();
        for (Map.Entry<String, String> en : newOptions.entrySet()) {
            Filter.Option opt = new Filter.Option();
            opt.setName(en.getKey());
            opt.setValue(en.getValue());
            f.getOption().add(opt);
        }
    }
}
