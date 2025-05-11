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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
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
 * It loads and saves its data from/to the SRX file.
 * <p>
 * When creating an SRX object with the default constructor, you get an empty
 * SRX without any rules. Please do not use default constructor, unless you
 * know what you are doing.
 *
 * @author Maxym Mykhalchuk
 * @author Thomas Cordonnier
 */
public class SRX implements Serializable {

    private static final long serialVersionUID = 2182125877925944613L;

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
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
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
            Files.delete(outFile.toPath());
            return;
        }
        Srx jaxbSrxObject = createSrxJaxbObject(srx);
        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            mapper.writerWithDefaultPrettyPrinter().writeValue(fos, jaxbSrxObject);
        } catch (DatabindException e) {
            Log.logErrorRB("CORE_SRX_ERROR_SAVING_SEGMENTATION_CONFIG");
            throw new IOException(e);
        }
    }

    // helper methods for saving SRX files
    private static Srx createSrxJaxbObject(SRX srx) {
        ObjectFactory factory = new ObjectFactory();
        Srx jaxbObject = factory.createSrx();
        jaxbObject.setVersion("2.0");
        jaxbObject.setHeader(factory.createHeader());
        jaxbObject.getHeader().setSegmentsubflows(srx.segmentSubflows ? "yes" : "no");
        jaxbObject.getHeader().setCascade(srx.cascade ? "yes" : "no");
        jaxbObject.setBody(factory.createBody());
        jaxbObject.getBody().setMaprules(factory.createMaprules());
        jaxbObject.getBody().setLanguagerules(factory.createLanguagerules());
        for (MapRule mappingRule: srx.getMappingRules()) {
            String pattern = mappingRule.getPattern();
            String languageName = resolveLanguageName(mappingRule, pattern);
            Languagemap map = createLanguageMap(languageName, pattern);
            jaxbObject.getBody().getMaprules().getLanguagemap().add(map);
            Languagerule languageRule = createLanguageRule(languageName);
            jaxbObject.getBody().getLanguagerules().getLanguagerule().add(languageRule);
            for (Rule rule : mappingRule.getRules()) {
                gen.core.segmentation.Rule jaxbRule = createJaxbRule(factory, rule);
                languageRule.getRule().add(jaxbRule);
            }
        }
        return jaxbObject;
    }

    private static String resolveLanguageName(MapRule mappingRule, String pattern) {
        // we use standard name
        String language = LanguageCodes.getLanguageCodeByPattern(pattern);
        if (language == null) {
            language = LanguageCodes.getLanguageCodeByName(mappingRule.getLanguage());
        }
        if (language == null) {
            language = mappingRule.getLanguage();
        }
        return language;
    }

    private static Languagemap createLanguageMap(String languageName, String pattern) {
        Languagemap map = new Languagemap();
        map.setLanguagerulename(languageName);
        map.setLanguagepattern(pattern);
        return map;
    }

    private static Languagerule createLanguageRule(String languageName) {
        Languagerule lr = new Languagerule();
        lr.setLanguagerulename(languageName);
        return lr;
    }

    private static gen.core.segmentation.Rule createJaxbRule(ObjectFactory factory, Rule rule) {
        gen.core.segmentation.Rule jaxbRule = factory.createRule();
        jaxbRule.setBreak(rule.isBreakRule() ? "yes" : "no");
        if (rule.getBeforebreak() != null) {
            jaxbRule.setBeforebreak(factory.createBeforebreak());
            jaxbRule.getBeforebreak().setContent(rule.getBeforebreak());
        }
        if (rule.getAfterbreak() != null) {
            jaxbRule.setAfterbreak(factory.createAfterbreak());
            jaxbRule.getAfterbreak().setContent(rule.getAfterbreak());
        }
        return jaxbRule;
    }

    /**
     * Loads the local segmentation file. Accepts SRX (default) or old CONF
     * format. In case you use a conf format, rules about an old version remain
     * valid.
     **/
    public static SRX loadFromDir(File configDir) {
        try {
            Path inPath = configDir.toPath().resolve(SRX_SENTSEG);
            if (inPath.toFile().exists()) {
                return loadSrxFile(inPath.toUri());
            }
        } catch (Exception e) {
            Log.log(e);
            // fallback to default.
        }
        return SRX.getDefault();
    }

    /**
     * Loads an SRX (Segmentation Rules eXchange) file from a given URI.
     * <p>
     * This is accessible as package-private for testing purposes.
     *
     * @param rulesUri the URI of the SRX file to be loaded
     * @return the SRX object representing the segmentation rules loaded from the file
     * @throws IOException if an I/O error occurs while accessing the SRX file
     */
    static SRX loadSrxFile(URI rulesUri) throws IOException {
        try (InputStream inputStream = Files.newInputStream(Paths.get(rulesUri))) {
            return loadSrxInputStream(inputStream);
        }
    }

    /**
     * Reads and parses an SRX segmentation rules input stream, converting it into an SRX object.
     * <p>
     * This method processes the input stream to populate the segmentation rules,
     * language rules, and mapping rules into an SRX object suitable for segmentation purposes.
     * It also sets specific SRX configurations like cascade and segmentSubflows flags based on the input data.
     *
     * @param io the input stream containing SRX data
     * @return an SRX object representing the segmentation rules and configurations extracted from the input stream
     * @throws IOException if an error occurs during reading or processing the input stream
     */
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
                .map(languagemap -> new MapRule(languagemap.getLanguagerulename(),
                        languagemap.getLanguagepattern(), mapping.get(languagemap.getLanguagerulename())))
                .collect(Collectors.toList()));
        return res;
    }

    /**
     * Retrieves the default SRX segmentation rules.
     * The default rules are loaded from the "defaultRules.srx" resource.
     * Configurations such as including ending tags and segmenting subflows are set to true.
     *
     * @return the default SRX object with pre-configured segmentation rules and settings
     * @throws RuntimeException if an error occurs during the loading process
     */
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
                    // non-cascading means: do not search for other patterns
                    break;
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
    private List<MapRule> mappingRules = new ArrayList<>();

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
