/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2008 Alex Buloichik
               2018 Thomas Cordonnier
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

package org.omegat.core.segmentation;

import java.beans.ExceptionListener;
import java.beans.XMLDecoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.stream.XMLInputFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

import org.omegat.util.Language;
import org.omegat.util.Log;

import gen.core.segmentation.Languagemap;
import gen.core.segmentation.Languagerule;
import gen.core.segmentation.ObjectFactory;
import gen.core.segmentation.Srx;

/**
 * The class with all the segmentation data possible -- rules, languages, etc.
 * It loads and saves its data from/to SRX file.
 *
 * @author Maxym Mykhalchuk
 * @author Thomas Cordonnier
 */
public class SRX implements Serializable {

    private static final long serialVersionUID = 2182125877925944613L;

    public static final String CONF_SENTSEG = "segmentation.conf";
    public static final String SRX_SENTSEG = "segmentation.srx";
    private static final XmlMapper mapper;

    static {
        final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        // You should NOT use XMLInputFactor.getXMLInputFactory
        // that returns system default object.
        // Modifying a global object leads breakage of SuperTMXMerge
        // library.
        // https://sourceforge.net/p/omegat/bugs/1170/
        xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.TRUE);
        XmlFactory xmlFactory = new XmlFactory(xmlInputFactory);
        mapper = XmlMapper.builder(xmlFactory).defaultUseWrapper(false)
                .enable(MapperFeature.USE_WRAPPER_NAME_AS_PROPERTY_NAME).build();
        mapper.registerModule(new JaxbAnnotationModule());
        mapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Creates an empty SRX, without any rules.
     * <p>
     * Please do not call directly unless you know what you are doing.
     */
    public SRX() {
    }

    public SRX copy() {
        SRX result = new SRX();
        result.setVersion(version);
        result.setCascade(cascade);
        result.setSegmentSubflows(segmentSubflows);
        result.setIncludeIsolatedTags(includeIsolatedTags);
        result.setIncludeStartingTags(includeStartingTags);
        result.setIncludeEndingTags(includeEndingTags);
        result.mappingRules = new ArrayList<>(mappingRules.size());
        for (MapRule rule : mappingRules) {
            result.mappingRules.add(rule.copy());
        }
        return result;
    }

    /**
     * Saves segmentation rules into specified directory.
     * 
     * @param srx
     *            OmegaT object to be written; if null, means that we want to
     *            delete the file
     * @param outDir
     *            where to put the file. The file name is forced to
     *            {@link #SRX_SENTSEG} and will be in standard SRX format.
     */
    public static void saveToSrx(SRX srx, File outDir) throws IOException {
        File outFile = new File(outDir, SRX_SENTSEG);

        if (srx == null) {
            outFile.delete();
            new File(outDir, CONF_SENTSEG).delete();
            return;
        }

        ObjectFactory factory = new ObjectFactory();
        Srx jaxbObject = factory.createSrx();
        jaxbObject.setVersion("2.0");
        jaxbObject.setHeader(factory.createHeader());
        jaxbObject.getHeader().setSegmentsubflows(srx.segmentSubflows ? "yes" : "no");
        jaxbObject.getHeader().setCascade(srx.cascade ? "yes" : "no");
        jaxbObject.setBody(factory.createBody());
        jaxbObject.getBody().setMaprules(factory.createMaprules());
        jaxbObject.getBody().setLanguagerules(factory.createLanguagerules());
        for (MapRule mr : srx.getMappingRules()) {
            Languagemap map = new Languagemap();
            map.setLanguagerulename(mr.getLanguage());
            map.setLanguagepattern(mr.getPattern());
            jaxbObject.getBody().getMaprules().getLanguagemap().add(map);
            Languagerule lr = new Languagerule();
            lr.setLanguagerulename(mr.getLanguage());
            jaxbObject.getBody().getLanguagerules().getLanguagerule().add(lr);
            for (Rule rule : mr.getRules()) {
                gen.core.segmentation.Rule jaxbRule = factory.createRule();
                lr.getRule().add(jaxbRule);
                jaxbRule.setBreak(rule.isBreakRule() ? "yes" : "no");
                if (rule.getBeforebreak() != null) {
                    jaxbRule.setBeforebreak(factory.createBeforebreak());
                    jaxbRule.getBeforebreak().setContent(rule.getBeforebreak());
                }
                if (rule.getAfterbreak() != null) {
                    jaxbRule.setAfterbreak(factory.createAfterbreak());
                    jaxbRule.getAfterbreak().setContent(rule.getAfterbreak());
                }
            }
        }

        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            mapper.writerWithDefaultPrettyPrinter().writeValue(fos, jaxbObject);
        } catch (DatabindException e) {
            Log.logErrorRB("CORE_SRX_ERROR_SAVING_SEGMENTATION_CONFIG");
            throw new IOException(e);
        }
    }

    /**
     * Loads the local segmentation file. Accepts SRX (default) or old CONF
     * format. In case you use conf format, rules about old version remain
     * valid.
     **/
    public static SRX loadFromDir(File configDir) {
        File inFile;
        try {
            inFile = new File(configDir, SRX_SENTSEG);
            if (inFile.exists()) {
                return loadSrxFile(inFile.toURI());
            }
        } catch (Exception ignored) {
        }

        // If file was not present or not readable
        inFile = new File(configDir, CONF_SENTSEG);
        if (inFile.exists()) {
            SRX srx = loadConfFile(inFile);
            try {
                saveToSrx(srx, configDir);
            } catch (Exception o3) {
                Log.log(o3); // detail why conversion failed, but continue
            }
            return srx;
        }

        // If none of the files (conf and srx) are present,
        // return null to mimic behavior of previous method
        return null;
    }

    /**
     * Loads segmentation rules from an XML file. If there's an error loading a
     * file, it calls <code>getDefault</code>.
     * <p>
     * Since 1.6.0 RC8 it also checks if the version of segmentation rules saved
     * is older than that of the current OmegaT, and tries to merge the two sets
     * of rules.
     */
    private static SRX loadConfFile(File configFile) {
        SRX res;
        try {
            SRX.MyExceptionListener myel = new SRX.MyExceptionListener();
            try (XMLDecoder xmldec = new XMLDecoder(new FileInputStream(configFile), null, myel)) {
                res = (SRX) xmldec.readObject();
            }

            if (myel.isExceptionOccured()) {
                StringBuilder sb = new StringBuilder();
                for (Exception ex : myel.getExceptionsList()) {
                    sb.append("    ");
                    sb.append(ex);
                    sb.append("\n");
                }
                Log.logErrorRB("CORE_SRX_EXC_LOADING_SEG_RULES", sb.toString());
                return SRX.getDefault();
            }

            // checking the version
            if (CURRENT_VERSION.compareTo(res.getVersion()) > 0) {
                // yeap, the segmentation config file is of the older version

                // initing defaults
                SRX defaults = SRX.getDefault();
                // and merging them into loaded rules
                res = merge(res, defaults);
            }
            Log.log("using segmentation rules from " + configFile);
        } catch (Exception e) {
            // silently ignoring FNF
            if (!(e instanceof FileNotFoundException)) {
                Log.log(e);
            }
            res = SRX.getDefault();
        }
        return res;
    }

    private static SRX loadSrxFile(URI rulesUri) {
        try (InputStream inputStream = Files.newInputStream(Paths.get(rulesUri))) {
            return loadSrxInputStream(inputStream);
        } catch (Exception e) {
            Log.log(e);
        }
        return null;
    }

    private static SRX loadSrxInputStream(InputStream io) throws IOException {
        Srx srx = mapper.readValue(io, Srx.class);
        HashMap<String, List<Rule>> mapping = new HashMap<>();
        List<Languagerule> languageRuleList = srx.getBody().getLanguagerules().getLanguagerule();
        for (Languagerule languagerule : languageRuleList) {
            mapping.put(languagerule.getLanguagerulename(),
                    languagerule.getRule().stream().map(Rule::new).collect(Collectors.toList()));
        }
        SRX res = new SRX();
        res.setSegmentSubflows(!"no".equalsIgnoreCase(srx.getHeader().getSegmentsubflows()));
        res.setCascade(!"no".equalsIgnoreCase(srx.getHeader().getCascade()));
        res.setVersion(srx.getVersion());
        res.setMappingRules(srx.getBody().getMaprules().getLanguagemap().stream()
                .map(s -> new MapRule(s, mapping.get(s.getLanguagerulename()))).collect(Collectors.toList()));
        return res;
    }

    /**
     * Does a config file already exists for the project at the given location?
     * 
     * @param configDir
     *            the project directory for storage of settings file
     */
    public static boolean projectConfigFileExists(String configDir) {
        File configFile = new File(configDir + CONF_SENTSEG);
        return configFile.exists();
    }

    /** Merges two sets of segmentation rules together. */
    private static SRX merge(final SRX current, final SRX defaults) {
        SRX merged = upgrade(current, defaults);

        int defaultMapRulesN = defaults.getMappingRules().size();
        for (int i = 0; i < defaultMapRulesN; i++) {
            MapRule dmaprule = defaults.getMappingRules().get(i);
            String dcode = dmaprule.getLanguage();
            // trying to find
            boolean found = false;
            int currentMapRulesN = merged.getMappingRules().size();
            MapRule cmaprule = null;
            for (int j = 0; j < currentMapRulesN; j++) {
                cmaprule = merged.getMappingRules().get(j);
                String ccode = cmaprule.getLanguage();
                if (dcode.equals(ccode)) {
                    found = true;
                    break;
                }
            }

            if (found) {
                // merging -- adding those rules not there in current list
                List<Rule> crules = cmaprule.getRules();
                List<Rule> drules = dmaprule.getRules();
                for (Rule drule : drules) {
                    if (!crules.contains(drule)) {
                        if (drule.isBreakRule()) {
                            // breaks go to the end
                            crules.add(drule);
                        } else {
                            // exceptions go before the first break rule
                            int currentRulesN = crules.size();
                            int firstBreakRuleN = currentRulesN;
                            for (int k = 0; k < currentRulesN; k++) {
                                Rule crule = crules.get(k);
                                if (crule.isBreakRule()) {
                                    firstBreakRuleN = k;
                                    break;
                                }
                            }
                            crules.add(firstBreakRuleN, drule);
                        }
                    }
                }
            } else {
                // just adding before the default rules
                int englishN = currentMapRulesN;
                for (int j = 0; j < currentMapRulesN; j++) {
                    cmaprule = merged.getMappingRules().get(j);
                    String cpattern = cmaprule.getPattern();
                    if (DEFAULT_RULES_PATTERN.equals(cpattern)) {
                        englishN = j;
                        break;
                    }
                }
                merged.getMappingRules().add(englishN, dmaprule);
            }
        }
        return merged;
    }

    /** Implements some upgrade heuristics. */
    private static SRX upgrade(SRX current, SRX defaults) {
        // renaming "Default (English)" to "Default"
        // and removing English/Text/HTML-specific rules from there
        if (OT160RC9_VERSION.equals(CURRENT_VERSION)) {
            String def = "Default (English)";
            for (int i = 0; i < current.getMappingRules().size(); i++) {
                MapRule maprule = current.getMappingRules().get(i);
                if (def.equals(maprule.getLanguage())) {
                    maprule.setLanguage(LanguageCodes.DEFAULT_CODE);
                    maprule.getRules().removeAll(getRulesForLanguage(defaults, LanguageCodes.ENGLISH_CODE));
                    maprule.getRules().removeAll(getRulesForLanguage(defaults, LanguageCodes.F_TEXT_CODE));
                    maprule.getRules().removeAll(getRulesForLanguage(defaults, LanguageCodes.F_HTML_CODE));
                }
            }
        }
        return current;
    }

    /**
     * Find rules for specific language.
     *
     * @param source
     *            rules list
     * @param langName
     *            language name
     * @return list of rules
     */
    private static List<Rule> getRulesForLanguage(final SRX source, String langName) {
        for (MapRule mr : source.getMappingRules()) {
            if (langName.equals(mr.getLanguage())) {
                return mr.getRules();
            }
        }
        return null;
    }

    /**
     * My Own Class to listen to exceptions, occured while loading filters
     * configuration.
     */
    static class MyExceptionListener implements ExceptionListener {
        private List<Exception> exceptionsList = new ArrayList<Exception>();
        private boolean exceptionOccured = false;

        public void exceptionThrown(Exception e) {
            exceptionOccured = true;
            exceptionsList.add(e);
        }

        /**
         * Returns whether any exceptions occured.
         */
        public boolean isExceptionOccured() {
            return exceptionOccured;
        }

        /**
         * Returns the list of occured exceptions.
         */
        public List<Exception> getExceptionsList() {
            return exceptionsList;
        }
    }

    // Patterns
    private static final String DEFAULT_RULES_PATTERN = ".*";

    public static SRX getDefault() {
        try {
            SRX srx = loadSrxInputStream(SRX.class.getResourceAsStream("defaultRules.srx"));
            srx.includeEndingTags = true;
            srx.segmentSubflows = true;
            return srx;
        } catch (IOException e) {
            Log.log(e);
            throw new RuntimeException("Unrecoverable error occurred!");
        }
    }

    /**
     * Finds the rules for a certain language.
     * <p>
     * Usually (if the user didn't screw up the setup) there are default
     * segmentation rules, so it's a good idea to rely on this method always
     * returning at least some rules.
     * <p>
     * Or in case of a completely screwed setup -- an empty list without any
     * rules.
     */
    public List<Rule> lookupRulesForLanguage(Language srclang) {
        List<Rule> rules = new ArrayList<>();
        for (int i = 0; i < getMappingRules().size(); i++) {
            MapRule maprule = getMappingRules().get(i);
            if (maprule.getCompiledPattern().matcher(srclang.getLanguage()).matches()) {
                rules.addAll(maprule.getRules());
                if (!this.cascade) {
                    break; // non-cascading means: do not search for other
                           // patterns
                }
            }
        }
        return rules;
    }

    /**
     * Holds value of property cascade: true, unless we read an SRX where it was
     * set to false.
     */
    private boolean cascade = true;

    /**
     * Getter for property cascade.
     *
     * @return Value of property cascade.
     */
    public boolean isCascade() {
        return this.cascade;
    }

    /**
     * Setter for property cascade.
     *
     * @param cascade
     *            New value of property cascade.
     */
    public void setCascade(boolean cascade) {
        this.cascade = cascade;
    }

    /**
     * Holds value of property segmentSubflows.
     */
    private boolean segmentSubflows = true;

    /**
     * Getter for property segmentSubflows.
     *
     * @return Value of property segmentSubflows.
     */
    public boolean isSegmentSubflows() {

        return this.segmentSubflows;
    }

    /**
     * Setter for property segmentSubflows.
     *
     * @param segmentSubflows
     *            New value of property segmentSubflows.
     */
    public void setSegmentSubflows(boolean segmentSubflows) {

        this.segmentSubflows = segmentSubflows;
    }

    /**
     * Holds value of property includeStartingTags.
     */
    private boolean includeStartingTags;

    /**
     * Getter for property includeStartingTags.
     *
     * @return Value of property includeStartingTags.
     */
    public boolean isIncludeStartingTags() {

        return this.includeStartingTags;
    }

    /**
     * Setter for property includeStartingTags.
     *
     * @param includeStartingTags
     *            New value of property includeStartingTags.
     */
    public void setIncludeStartingTags(boolean includeStartingTags) {
        this.includeStartingTags = includeStartingTags;
    }

    /**
     * Holds value of property includeEndingTags.
     */
    private boolean includeEndingTags = true;

    /**
     * Getter for property includeEndingTags.
     *
     * @return Value of property includeEndingTags.
     */
    public boolean isIncludeEndingTags() {
        return this.includeEndingTags;
    }

    /**
     * Setter for property includeEndingTags.
     *
     * @param includeEndingTags
     *            New value of property includeEndingTags.
     */
    public void setIncludeEndingTags(boolean includeEndingTags) {
        this.includeEndingTags = includeEndingTags;
    }

    /**
     * Holds value of property includeIsolatedTags.
     */
    private boolean includeIsolatedTags;

    /**
     * Getter for property includeIsolatedTags.
     *
     * @return Value of property includeIsolatedTags.
     */
    public boolean isIncludeIsolatedTags() {

        return this.includeIsolatedTags;
    }

    /**
     * Setter for property includeIsolatedTags.
     *
     * @param includeIsolatedTags
     *            New value of property includeIsolatedTags.
     */
    public void setIncludeIsolatedTags(boolean includeIsolatedTags) {

        this.includeIsolatedTags = includeIsolatedTags;
    }

    /**
     * Correspondences between languages and their segmentation rules. Each
     * element is of class {@link MapRule}.
     */
    private List<MapRule> mappingRules = new ArrayList<MapRule>();

    /**
     * Returns all mapping rules (of class {@link MapRule}) at once:
     * correspondences between languages and their segmentation rules.
     */
    public List<MapRule> getMappingRules() {
        return mappingRules;
    }

    /**
     * Sets all mapping rules (of class {@link MapRule}) at once:
     * correspondences between languages and their segmentation rules.
     */
    public void setMappingRules(List<MapRule> rules) {
        mappingRules = rules;
    }

    // ////////////////////////////////////////////////////////////////
    // Versioning properties to detect version upgrades
    // and possibly do something if required

    /** Initial version of segmentation support (1.4.6 beta 4 -- 1.6.0 RC7). */
    public static final String INITIAL_VERSION = "0.2";
    /** Segmentation support of 1.6.0 RC8 (a bit more rules added). */
    public static final String OT160RC8_VERSION = "0.2.1";
    /** Segmentation support of 1.6.0 RC9 (rules separated). */
    public static final String OT160RC9_VERSION = "0.2.2";
    /** Currently supported segmentation support version. */
    public static final String CURRENT_VERSION = OT160RC9_VERSION;

    /** Version of OmegaT segmentation support. */
    private String version;

    /** Returns segmentation support version. */
    public String getVersion() {
        return version;
    }

    /** Sets segmentation support version. */
    public void setVersion(String value) {
        version = value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (includeEndingTags ? 1231 : 1237);
        result = prime * result + (includeIsolatedTags ? 1231 : 1237);
        result = prime * result + (includeStartingTags ? 1231 : 1237);
        result = prime * result + ((mappingRules == null) ? 0 : mappingRules.hashCode());
        result = prime * result + (segmentSubflows ? 1231 : 1237);
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SRX other = (SRX) obj;
        if (includeEndingTags != other.includeEndingTags) {
            return false;
        }
        if (includeIsolatedTags != other.includeIsolatedTags) {
            return false;
        }
        if (includeStartingTags != other.includeStartingTags) {
            return false;
        }
        if (mappingRules == null) {
            if (other.mappingRules != null) {
                return false;
            }
        } else if (!mappingRules.equals(other.mappingRules)) {
            return false;
        }
        if (segmentSubflows != other.segmentSubflows) {
            return false;
        }
        if (version == null) {
            if (other.version != null) {
                return false;
            }
        } else if (!version.equals(other.version)) {
            return false;
        }
        return true;
    }
}
