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

package org.omegat.filters2.master;

import gen.core.filters.Files;
import gen.core.filters.Filter;
import gen.core.filters.Filter.Option;
import gen.core.filters.Filters;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JOptionPane;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.omegat.filters2.AbstractFilter;
import org.omegat.filters2.FilterContext;
import org.omegat.filters2.IAlignCallback;
import org.omegat.filters2.IFilter;
import org.omegat.filters2.IParseCallback;
import org.omegat.filters2.ITranslateCallback;
import org.omegat.filters2.Instance;
import org.omegat.filters2.TranslationException;
import org.omegat.util.LFileCopy;
import org.omegat.util.Language;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.StaticUtils;

/**
 * A master class that registers and handles all the filters. Singleton - there can be only one instance of
 * this class.
 * 
 * @author Maxym Mykhalchuk
 * @author Henry Pijffers
 * @author Martin Wunderlich
 * @author Didier Briel
 * @author Martin Fleurke
 * @author Arno Peters
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class FilterMaster {
    /** name of the filter configuration file */
    private final static String FILE_FILTERS = "filters.xml";

    private static final JAXBContext CONFIG_CTX;

    /**
     * There was no version of file filters support (1.4.5 Beta 1 -- 1.6.0 RC12).
     */
    public static String INITIAL_VERSION = new String();
    /** File filters support of 1.6.0 RC12a: now upgrading the configuration. */
    public static String OT160RC12a_VERSION = "1.6 RC12a";
    public static String OT160FINAL_VERSION = "1.6.0";
    public static String OT161_VERSION = "1.6.1";
    public static String OT170_VERSION = "1.7.0";
    /** Currently file filters support version. */
    public static String CURRENT_VERSION = "2.0";

    /** FilterMaster instance. */
    private static FilterMaster master = null;

    /** Config file. */
    private File configFile = new File(StaticUtils.getConfigDir() + FILE_FILTERS);

    /** Filters config stored in XML file. */
    private Filters config;

    /** Instances of all filter classes. */
    private List<IFilter> filtersInstances;

    static {
        try {
            CONFIG_CTX = JAXBContext.newInstance(Filters.class);
        } catch (Exception ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * Create a new FilterMaster.
     */
    private FilterMaster() {
        filtersInstances = new ArrayList<IFilter>();
        for (Class<?> c : PluginUtils.getFilterClasses()) {
            try {
                filtersInstances.add((IFilter) c.newInstance());
            } catch (Exception ex) {
                // error instantiate filter
                Log.log(ex);
            }
        }

        loadConfig();

        addNewFiltersToConfig(config);

        saveConfig();
    }

    /**
     * Adds new filters(which was not exist in config yet) into config.
     */
    private void addNewFiltersToConfig(final Filters conf) {
        for (IFilter f : filtersInstances) {
            boolean found = false;
            for (Filter fc : conf.getFilter()) {
                if (f.getClass().getName().equals(fc.getClassName())) {
                    // filter already exist in config
                    found = true;
                    break;
                }
            }
            if (!found) {
                // filter not found in config
                conf.getFilter().add(getDefaultSettingsFromFilter(f.getClass().getName()));
            }
        }
    }

    /**
     * Returns the only instance of this class.
     */
    public static FilterMaster getInstance() {
        if (master == null)
            master = new FilterMaster();
        return master;
    }

    /**
     * Get filter's instance by filter class name.
     * 
     * @param classname
     *            filter's class name
     * @return filter instance
     */
    public IFilter getFilterInstance(final String classname) {
        for (IFilter f : filtersInstances) {
            if (f.getClass().getName().equals(classname)) {
                return f;
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
     * @see #translateFile(String, String, String)
     */
    public boolean loadFile(String filename, FilterContext fc, IParseCallback parseCallback)
            throws IOException, TranslationException {
        try {
            LookupInformation lookup = lookupFilter(filename, fc);
            if (lookup == null)
                return false;

            File inFile = new File(filename);
            fc.setInEncoding(lookup.outFilesInfo.getSourceEncoding());
            fc.setOutEncoding(lookup.outFilesInfo.getTargetEncoding());

            IFilter filterObject = lookup.filterObject;

            filterObject.parseFile(inFile, lookup.config, fc, parseCallback);
        } catch (Exception ioe) {
            ioe.printStackTrace();
            throw new IOException(filename + "\n" + ioe);
        }
        return true;
    }

    /**
     * OmegaT core calls this method to translate a source file.
     * <ul>
     * <li>OmegaT first looks through registered filter instances to find filter(s) that can handle this file.
     * <li>Tests if filter(s) want to handle it.
     * <li>If the filter accepts the file,
     * <li>Filter is asked to process the file.
     * </ul>
     * If no filter is found, that processes this file, we simply copy it to target folder.
     * 
     * @param sourcedir
     *            The folder of the source inFile.
     * @param filename
     *            The name of the source inFile to process (only the part, relative to source folder).
     * @param targetdir
     *            The folder to place the translated inFile to.
     * @param fc
     *            Filter context.
     */
    public void translateFile(String sourcedir, String filename, String targetdir, FilterContext fc,
            ITranslateCallback translateCallback) throws IOException, TranslationException {
        LookupInformation lookup = lookupFilter(sourcedir + File.separator + filename, fc);
        if (lookup == null) {
            // The file is not supported by any of the filters.
            // Copying it
            LFileCopy.copy(sourcedir + File.separator + filename, targetdir + File.separator + filename);
            return;
        }

        File inFile = new File(sourcedir + File.separator + filename);

        String name = inFile.getName();
        String path = filename.substring(0, filename.length() - name.length());

        File outFile = new File(targetdir
                + File.separator
                + path
                + File.separator
                + constructTargetFilename(lookup.outFilesInfo.getSourceFilenameMask(), name,
                        lookup.outFilesInfo.getTargetFilenamePattern(), fc.getTargetLang()));

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

        LookupInformation lookup = lookupFilter(sourceDir + File.separator + fileName, fc);
        if (lookup == null) {
            // The file is not supported by any of the filters.
            // Skip it
            return;
        }

        File inFile = new File(sourceDir + File.separator + fileName);

        String name = inFile.getName();
        String path = fileName.substring(0, fileName.length() - name.length());

        File outFile = new File(targetdir
                + File.separator
                + path
                + File.separator
                + constructTargetFilename(lookup.outFilesInfo.getSourceFilenameMask(), name,
                        lookup.outFilesInfo.getTargetFilenamePattern(), fc.getTargetLang()));

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

    class LookupInformation {
        public final Files outFilesInfo;
        public final IFilter filterObject;
        public final Map<String, String> config;

        public LookupInformation(IFilter filterObject, Files outFilesInfo, Map<String, String> config) {
            this.filterObject = filterObject;
            this.outFilesInfo = outFilesInfo;
            this.config = config;
        }
    }

    /**
     * Gets the filter according to the source filename provided. In case of failing to find a filter to
     * handle the file returns <code>null</code>.
     * 
     * In case of finding an appropriate filter it
     * <ul>
     * <li>Creates the filter (use <code>OneFilter.getFilter()</code> to get it)
     * <li>Creates a reader (use <code>OneFilter.getReader()</code> to get it)
     * <li>Checks whether the filter supports the file.
     * </ul>
     * It <b>does not</b> check whether the filter supports the inFile, i.e. it doesn't call
     * <code>isFileSupported</code>
     * 
     * 
     * @param filename
     *            The source filename.
     * @return The filter to handle the inFile.
     */
    private LookupInformation lookupFilter(String filename, FilterContext fc) throws TranslationException,
            IOException {
        File inFile = new File(filename);
        String name = inFile.getName();
        String path = inFile.getParent();
        if (path == null)
            path = "";

        for (Filter f : config.getFilter()) {
            if (!f.isEnabled()) {
                continue;
            }
            for (Files ff : f.getFiles()) {
                if (matchesMask(name, ff.getSourceFilenameMask())) {
                    IFilter filterObject;
                    filterObject = getFilterInstance(f.getClassName());

                    if (filterObject != null) {
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
            }
        }
        return null;
    }

    private static List<String> supportedEncodings = null;

    /**
     * Queries JRE for the list of supported encodings. Also adds the human name for no/automatic inEncoding.
     * 
     * 
     * @return names of all the encodings in an array
     */
    public static List<String> getSupportedEncodings() {
        if (supportedEncodings == null) {
            supportedEncodings = new ArrayList<String>();
            supportedEncodings.add(AbstractFilter.ENCODING_AUTO_HUMAN);
            supportedEncodings.addAll(Charset.availableCharsets().keySet());
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
    public Filters createDefaultFiltersConfig() {
        Filters c = new Filters();
        addNewFiltersToConfig(c);
        return c;
    }

    /**
     * Loads information about the filters from an XML file. If there's an error loading a file, it calls
     * <code>setupDefaultFilters</code>.
     */
    public void loadConfig() {
        if (!configFile.exists()) {
            config = new Filters();
            return;
        }
        try {
            Unmarshaller unm = CONFIG_CTX.createUnmarshaller();
            config = (Filters) unm.unmarshal(configFile);
        } catch (Exception e) {
            Log.logErrorRB("FILTERMASTER_ERROR_LOADING_FILTERS_CONFIG");
            Log.log(e);
            config = new Filters();
        }
    }

    /**
     * Saves information about the filters to an XML file.
     */
    public void saveConfig() {
        try {
            Marshaller m = CONFIG_CTX.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            m.marshal(config, configFile);
        } catch (Exception e) {
            Log.logErrorRB("FILTERMASTER_ERROR_SAVING_FILTERS_CONFIG");
            Log.log(e);
            JOptionPane.showMessageDialog(null,
                    OStrings.getString("FILTERMASTER_ERROR_SAVING_FILTERS_CONFIG") + "\n" + e,
                    OStrings.getString("ERROR_TITLE"), JOptionPane.ERROR_MESSAGE);
        }
    }

    // ////////////////////////////////////////////////////////////////////////
    // Static Utility Methods
    // ////////////////////////////////////////////////////////////////////////

    /**
     * Whether the mask matches the filename. Filename should be "name.ext", without path.
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
     * Construct a target filename according to pattern from a file's name. Filename should be "name.ext",
     * without path.
     * <p>
     * Output filename pattern is pretty complex. <br>
     * It may consist of normal characters and some substituted variables. They have the format
     * <code>${variableName}</code> and are case insensitive. <br>
     * There're such variables:
     * <ul>
     * <li><code>${filename}</code> - full filename of the input file, both name and extension (default)
     * <li><code>${nameOnly}</code> - only the name of the input file without extension part
     * <li><code>${extension}</code> - the extension of the input file
     * <li><code>${targetLocale}</code> - target locale code (of a form "xx_YY")
     * <li><code>${targetLanguage}</code> - the target language and country code together (of a form "XX-YY")
     * <li><code>${targetLanguageCode}</code> - the target language only ("XX")
     * <li><code>${targetCoutryCode}</code> - the target country only ("YY")
     * </ul>
     * <p>
     * Most file filters will use default "<code>${filename}</code>, that leads to the name of translated file
     * being the same as the name of source file. But for example the Java(TM) Resource Bundles file filter
     * will have the pattern equal to "<code>${nameonly}_${targetlanguage}.${extension}</code> ".
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
     * </ul>
     * 
     * @param filename
     *            Filename to change
     * @param pattern
     *            Pattern, according to which we change the filename
     * @return The changed filename
     */
    private String constructTargetFilename(String sourceMask, String filename, String pattern,
            Language targetLang) {
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

        return res;
    }

    /**
     * Set new config. Used by filter's editor.
     * 
     * @param config
     *            new config
     */
    public void setConfig(final Filters config) {
        this.config = config;
    }

    /**
     * Clone config for editing
     * 
     * @return new config instance
     */
    public Filters cloneConfig() {
        Filters c = new Filters();
        for (Filter f : config.getFilter()) {
            c.getFilter().add(cloneFilter(f));
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
    public Filter cloneFilter(Filter filter) {
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
    public Files cloneFiles(Files files) {
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
    public Filter getDefaultSettingsFromFilter(final String filterClassname) {
        IFilter f = getFilterInstance(filterClassname);
        Filter fc = new Filter();
        fc.setClassName(f.getClass().getName());
        fc.setEnabled(true);
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
