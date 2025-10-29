package org.omegat.cms.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

import org.omegat.cms.dto.CmsTarget;
import org.omegat.util.Log;
import org.omegat.util.StaticUtils;

/**
 * Persists CMS configuration to an XML file located under the user config directory (cms.xml).
 */
public final class CmsXmlStore {
    private static final String FILE_NAME = "cms.xml";
    private static final XmlMapper MAPPER;

    static {
        MAPPER = new XmlMapper();
        MAPPER.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    private CmsXmlStore() {}

    private static File getFile() {
        return new File(StaticUtils.getConfigDir(), FILE_NAME);
    }

    /** Load list of CMS targets from cms.xml, or migrate from legacy preferences if file doesn't exist. */
    public static List<CmsTarget> loadTargets() {
        File file = getFile();
        if (file.isFile()) {
            try {
                CmsConfig cfg = MAPPER.readValue(file, CmsConfig.class);
                return cfg != null && cfg.getTargets() != null ? cfg.getTargets() : new ArrayList<>();
            } catch (IOException e) {
                Log.log(e);
            }
        }
        return new ArrayList<>();
    }

    /** Save the provided list of targets to cms.xml. Creates parent dir if needed. */
    public static boolean saveTargets(List<CmsTarget> targets) {
        File file = getFile();
        try {
            Files.createDirectories(file.getParentFile().toPath());
            CmsConfig cfg = new CmsConfig();
            cfg.setVersion("1");
            cfg.setTargets(targets != null ? targets : new ArrayList<>());
            MAPPER.writeValue(file, cfg);
            return true;
        } catch (IOException e) {
            Log.log(e);
            return false;
        }
    }
}
