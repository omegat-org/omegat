/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2008 Alex Buloichik
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
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import gen.core.segmentation.ObjectFactory;
import org.omegat.util.Language;
import org.omegat.util.Log;

import gen.core.segmentation.Languagemap;
import gen.core.segmentation.Languagerule;
import gen.core.segmentation.Srx;

/**
 * The class with all the segmentation data possible -- rules, languages, etc.
 * It loads and saves its data from/to SRX file.
 *
 * @author Maxym Mykhalchuk
 */
public class SRX implements Serializable {

    private static final long serialVersionUID = 2182125877925944613L;

    public static final String SRX_SENTSEG = "segmentation.srx";
    public static final String CONF_SENTSEG = "segmentation.conf";

    /** Context for JAXB rules processing. */
    protected static final JAXBContext SRX_JAXB_CONTEXT;

    static {
        try {
            SRX_JAXB_CONTEXT = JAXBContext.newInstance(Srx.class);
        } catch (JAXBException ex) {
            if (ex.getMessage() != null) {
                throw new ExceptionInInitializerError(ex.getMessage());
            }
            if (ex.getCause() != null) {
                throw new ExceptionInInitializerError(ex.getCause().getClass().getName() + ": "
                        + ex.getCause().getMessage());
            }
            throw new ExceptionInInitializerError(ex.getClass().getName());
        }
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
        result.mappingRules = new ArrayList<>(mappingRules.size());
        for (MapRule rule : mappingRules) {
            result.mappingRules.add(rule.copy());
        }
        result.cascade = cascade;
        result.segmentSubflows = segmentSubflows;
        result.includeStartingTags = includeStartingTags;
        result.includeEndingTags = includeEndingTags;
        result.version = version;
        return result;
    }

    /**
     * Saves segmentation rules into specified srx file.
     */
    public static SRX loadSrxFile(URI rulesUri) {
        try {
            List<MapRule> newMap = new ArrayList<>();
            Srx data = (Srx) SRX_JAXB_CONTEXT.createUnmarshaller().unmarshal(rulesUri.toURL());

            // Correction: in SRX, the same "languagerulename" can be used more than once
            Map<String,List<Rule>> mapping = new HashMap<>();

            for (Languagerule rules : data.getBody().getLanguagerules().getLanguagerule()) {
                List<Rule> rulesList = new ArrayList<>(rules.getRule().size());
                for (gen.core.segmentation.Rule r : rules.getRule()) {
                    boolean isBreak = "yes".equalsIgnoreCase(r.getBreak());
                    rulesList.add(new Rule(isBreak, r.getBeforebreak().getContent(), r.getAfterbreak()
                            .getContent()));
                }

                mapping.put(rules.getLanguagerulename(), rulesList);
            }

            for (Languagemap lm : data.getBody().getMaprules().getLanguagemap()) {
                newMap.add(new MapRule(lm.getLanguagerulename(), lm.getLanguagepattern(), mapping.get(lm.getLanguagerulename())));
            }

            Log.log("using segmentation rules from " + rulesUri);
            // set rules only if no errors
            SRX res = new SRX();
            res.setMappingRules(newMap);
            res.setCascade(! ((data.getHeader() != null) && ("no".equals(data.getHeader().getCascade())))); 	// in OmegaT, defaults to true
            res.setSegmentSubflows((data.getHeader() != null) && ("yes".equals(data.getHeader().getSegmentsubflows())));	// not really used
            res.setVersion(data.getVersion());
            return res;
        } catch (Exception ex) {
            Log.log(ex);
            return null;
        }
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
            if (outFile.exists()) {
                Files.delete(outFile.toPath());
            }
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
            String pattern = mr.getPattern();
            // we use standard name
            String language = LanguageCodes.getLanguageCodeByPattern(pattern);
            if (language == null) {
                language = LanguageCodes.getLanguageCodeByName(mr.getLanguage());
            }
            if (language == null) {
                language = mr.getLanguage();
            }
            map.setLanguagerulename(language);
            map.setLanguagepattern(pattern);
            jaxbObject.getBody().getMaprules().getLanguagemap().add(map);
            Languagerule lr = new Languagerule();
            lr.setLanguagerulename(language);
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

        try {
            Marshaller m = SRX_JAXB_CONTEXT.createMarshaller();
            // m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            try (FileOutputStream fos = new FileOutputStream(outFile)) {
                m.marshal(jaxbObject, fos);
            }
        } catch (JAXBException ioe) {
            Log.logErrorRB("CORE_SRX_ERROR_SAVING_SEGMENTATION_CONFIG");
            Log.log(ioe);
            throw new IOException(ioe);
        }
    }

    public static SRX loadFromDir(File configDir) {
        File inFile;
        try {
            inFile = new File(configDir, SRX_SENTSEG);
            if (inFile.exists()) {
                return loadSrxFile(inFile.toURI());
            }
        } catch (Exception o2) {

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
     * file, it calls <code>initDefaults</code>.
     * <p>
     * Since 1.6.0 RC8 it also checks if the version of segmentation rules saved
     * is older than that of the current OmegaT, and tries to merge the two sets
     * of rules.
     */
    private static SRX loadConfFile(File configFile) {
        if (!configFile.exists()) {
            return null;
        }
        SRX res;
        try {

            MyExceptionListener myel = new MyExceptionListener();
            XMLDecoder xmldec = new XMLDecoder(new FileInputStream(configFile), null, myel);
            res = (SRX) xmldec.readObject();
            xmldec.close();

            if (myel.isExceptionOccured()) {
                StringBuilder sb = new StringBuilder();
                for (Exception ex : myel.getExceptionsList()) {
                    sb.append("    ");
                    sb.append(ex);
                    sb.append("\n");
                }
                Log.logErrorRB("CORE_SRX_EXC_LOADING_SEG_RULES", sb.toString());
                res = SRX.getDefault();
                return res;
            }

        } catch (Exception e) {
            // silently ignoring FNF
            if (!(e instanceof FileNotFoundException)) {
                Log.log(e);
            }
            res = SRX.getDefault();
        }
        return res;
    }

    /**
     * Does a config file already exists for the project at the given location?
     * @param configDir the project directory for storage of settings file
     */
    public static boolean projectConfigFileExists(String configDir) {
        File configFile = new File(configDir + CONF_SENTSEG);
        return configFile.exists();
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

    public static SRX getDefault() {
        SRX srx = null;
        try {
            srx = loadSrxFile(SRX.class.getResource("defaultRules.srx").toURI());
            srx.includeEndingTags = true;
            srx.segmentSubflows = true;
        } catch (URISyntaxException e) {
            Log.log(e);
            System.exit(1);
        }
        return srx;
    }

    /**
     * Finds the rules for a certain language.
     * <p>
     * Usually (if the user didn't screw up the setup) there're a default
     * segmentation rules, so it's a good idea to rely on this method always
     * returning at least some rules.
     * <p>
     * Or in case of a completely screwed setup -- an empty list without any
     * rules.
     */
    public List<Rule> lookupRulesForLanguage(Language srclang) {
        List<Rule> rules = new ArrayList<Rule>();
        for (int i = 0; i < getMappingRules().size(); i++) {
            MapRule maprule = getMappingRules().get(i);
            if (maprule.getCompiledPattern().matcher(srclang.getLanguage()).matches()) {
                rules.addAll(maprule.getRules());
                if (!this.cascade) {
                    break; // non-cascading means: do not search for other patterns
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
