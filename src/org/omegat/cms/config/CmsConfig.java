package org.omegat.cms.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import org.omegat.cms.dto.CmsTarget;

/**
 * XML root for CMS integration configuration persisted in cms.xml.
 */
@JacksonXmlRootElement(localName = "cms")
public class CmsConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    @JacksonXmlProperty(localName = "version")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String version;

    @JacksonXmlProperty(localName = "target")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<CmsTarget> targets = new ArrayList<>();

    public CmsConfig() {
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<CmsTarget> getTargets() {
        return targets;
    }

    public void setTargets(List<CmsTarget> targets) {
        this.targets = targets != null ? targets : new ArrayList<>();
    }
}
