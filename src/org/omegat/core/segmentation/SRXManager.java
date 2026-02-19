/*
 * OmegaT - Computer Assisted Translation (CAT) tool
 *          with fuzzy matching, translation memory, keyword search,
 *          glossaries, and translation leveraging into updated projects.
 *
 * Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
 *               2008 Alex Buloichik
 *               2018 Thomas Cordonnier
 *               2025-2026 Hiroshi MIura
 *               Home page: https://www.omegat.org/
 *               Support center: https://omegat.org/support
 *
 * This file is part of OmegaT.
 *
 * OmegaT is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OmegaT is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.omegat.core.segmentation;

import javax.xml.stream.XMLInputFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationModule;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import gen.core.segmentation.Languagemap;
import gen.core.segmentation.Languagerule;
import gen.core.segmentation.ObjectFactory;
import gen.core.segmentation.Srx;
import org.omegat.util.Language;
import org.omegat.util.Log;

/**
 * Utility class for SRX segmentation rules.
 *
 * @author Maxym Mykhalchuk
 * @author Thomas Cordonnier
 * @author Hiroshi Miura
 */
@NullMarked
public final class SRXManager {

    public static final String CONF_SENTSEG = "segmentation.conf";
    public static final String SRX_SENTSEG = "segmentation.srx";
    private static final XmlMapper MAPPER;

    static {
        final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
        xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
        xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.FALSE);
        XmlFactory xmlFactory = new XmlFactory(xmlInputFactory);
        MAPPER = XmlMapper.builder(xmlFactory).defaultUseWrapper(false)
                .enable(MapperFeature.USE_WRAPPER_NAME_AS_PROPERTY_NAME)
                .enable(SerializationFeature.INDENT_OUTPUT)
                .enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION)
                .defaultPropertyInclusion(JsonInclude.Value.construct(JsonInclude.Include.NON_EMPTY,
                        JsonInclude.Include.NON_EMPTY))
                .addModule(new JakartaXmlBindAnnotationModule()).build();
    }

    private SRXManager() {
        // Utility class don't have public ctor
    }

    public static SRX getDefault() throws IOException {
        SRX srx = loadSrxInputStream(Objects.requireNonNull(
                SRXManager.class.getResourceAsStream("/org/omegat/core/segmentation/defaultRules.srx")));
        srx.setIncludeEndingTags(true);
        srx.setSegmentSubflows(true);
        return srx;
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

        ObjectFactory factory = new ObjectFactory();
        Srx jaxbObject = factory.createSrx();
        jaxbObject.setVersion("2.0");
        jaxbObject.setHeader(factory.createHeader());
        jaxbObject.getHeader().setSegmentsubflows(srx.isSegmentSubflows() ? "yes" : "no");
        jaxbObject.getHeader().setCascade(srx.isCascade() ? "yes" : "no");
        jaxbObject.setBody(factory.createBody());
        jaxbObject.getBody().setMaprules(factory.createMaprules());
        jaxbObject.getBody().setLanguagerules(factory.createLanguagerules());
        for (MapRule mr : srx.getMappingRules()) {
            Languagemap map = new Languagemap();
            String pattern = mr.getPattern();
            // we use standard name
            String language = LanguageCodes.getInstance().getStandardNameFromMapRule(mr);
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

        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            MAPPER.writerWithDefaultPrettyPrinter().writeValue(fos, jaxbObject);
        } catch (DatabindException e) {
            throw new IOException(e);
        }
    }

    public static @Nullable SRX loadSrxFile(URI rulesUri) {
        try (InputStream inputStream = Files.newInputStream(Paths.get(rulesUri))) {
            return loadSrxInputStream(inputStream);
        } catch (Exception e) {
            Log.logDebug("Error loading segmentation rules from file: {0}\n{1}", rulesUri, e.getMessage());
        }
        return null;
    }

    /**
     * Loads the local segmentation file. Accepts SRX (default) or old CONF
     * format. In case you use a conf format, rules about an old version remain
     * valid.
     **/
    public static @org.jetbrains.annotations.Nullable SRX loadFromDir(File configDir) {
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
            try {
                return loadConfFile(inFile, configDir);
            } catch (Exception ex) {
                return SRX.getDefault();
            }
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
    static @Nullable SRX loadConfFile(File configFile, File configDir) throws Exception {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setFeature(javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING, true);
            transformerFactory.setAttribute("http://javax.xml.XMLConstants/property/accessExternalDTD", "");
            transformerFactory.setAttribute("http://javax.xml.XMLConstants/property/accessExternalStylesheet",
                    "");
            // add XSLT in Transformer
            Transformer transformer = transformerFactory.newTransformer(new StreamSource(SRX.class
                    .getClassLoader().getResourceAsStream("org/omegat/core/segmentation/java2srx.xsl")));
            File dest = new File(configDir, SRX_SENTSEG);
            try (FileOutputStream fos = new FileOutputStream(dest)) {
                transformer.transform(new StreamSource(configFile), new StreamResult(fos));
            }
            Files.deleteIfExists(Paths.get(configFile.toURI()));
            try (FileInputStream fis = new FileInputStream(dest)) {
                return loadSrxInputStream(fis);
            }
        } catch (Exception e) {
            // silently ignoring FNF
            if (!(e instanceof FileNotFoundException)) {
                Log.log(e);
                return null;
            } else {
                throw e;
            }
        }
    }

    public static SRX loadSrxInputStream(InputStream io) throws IOException {
        Srx srx = MAPPER.readValue(io, Srx.class);
        final Map<String, List<Rule>> mapping = new HashMap<>();
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
                .map(languagemap -> new MapRule(languagemap, mapping.get(languagemap.getLanguagerulename())))
                .collect(Collectors.toList()));
        return res;
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
    public static List<Rule> lookupRulesForLanguage(SRX srx, Language srclang) {
        List<Rule> rules = new ArrayList<>();
        for (int i = 0; i < srx.getMappingRules().size(); i++) {
            MapRule maprule = srx.getMappingRules().get(i);
            if (maprule.getCompiledPattern().matcher(srclang.getLanguage()).matches()) {
                rules.addAll(maprule.getRules());
                if (!srx.isCascade()) {
                    // non-cascading means: do not search for other patterns
                    break;
                }
            }
        }
        return rules;
    }
}
