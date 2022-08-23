
package gen.core.project;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;all&gt;
 *         &lt;element name="source_dir" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="source_dir_excludes" type="{}masks" minOccurs="0"/&gt;
 *         &lt;element name="target_dir" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="tm_dir" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="glossary_dir" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="glossary_file" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="dictionary_dir" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="export_tm_dir" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="export_tm_levels" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="source_lang" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="target_lang" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="source_tok" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="target_tok" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="sentence_seg" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/&gt;
 *         &lt;element name="support_default_translations" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/&gt;
 *         &lt;element name="remove_tags" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/&gt;
 *         &lt;element name="external_command" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="repositories" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="repository" type="{}RepositoryDefinition" maxOccurs="unbounded" minOccurs="0"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/all&gt;
 *       &lt;attribute name="version" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {

})
@XmlRootElement(name = "project")
public class Project {

    @XmlElement(name = "source_dir", required = true)
    protected String sourceDir;
    @XmlElement(name = "source_dir_excludes")
    protected Masks sourceDirExcludes;
    @XmlElement(name = "target_dir", required = true)
    protected String targetDir;
    @XmlElement(name = "tm_dir", required = true)
    protected String tmDir;
    @XmlElement(name = "glossary_dir", required = true)
    protected String glossaryDir;
    @XmlElement(name = "glossary_file", required = true)
    protected String glossaryFile;
    @XmlElement(name = "dictionary_dir", required = true)
    protected String dictionaryDir;
    @XmlElement(name = "export_tm_dir", required = true)
    protected String exportTmDir;
    @XmlElement(name = "export_tm_levels", required = true)
    protected String exportTmLevels;
    @XmlElement(name = "source_lang", required = true)
    protected String sourceLang;
    @XmlElement(name = "target_lang", required = true)
    protected String targetLang;
    @XmlElement(name = "source_tok", required = true)
    protected String sourceTok;
    @XmlElement(name = "target_tok", required = true)
    protected String targetTok;
    @XmlElement(name = "sentence_seg")
    protected Boolean sentenceSeg;
    @XmlElement(name = "support_default_translations")
    protected Boolean supportDefaultTranslations;
    @XmlElement(name = "remove_tags")
    protected Boolean removeTags;
    @XmlElement(name = "external_command")
    protected String externalCommand;
    protected Project.Repositories repositories;
    @XmlAttribute(name = "version", required = true)
    protected String version;

    /**
     * Gets the value of the sourceDir property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSourceDir() {
        return sourceDir;
    }

    /**
     * Sets the value of the sourceDir property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSourceDir(String value) {
        this.sourceDir = value;
    }

    /**
     * Gets the value of the sourceDirExcludes property.
     * 
     * @return
     *     possible object is
     *     {@link Masks }
     *     
     */
    public Masks getSourceDirExcludes() {
        return sourceDirExcludes;
    }

    /**
     * Sets the value of the sourceDirExcludes property.
     * 
     * @param value
     *     allowed object is
     *     {@link Masks }
     *     
     */
    public void setSourceDirExcludes(Masks value) {
        this.sourceDirExcludes = value;
    }

    /**
     * Gets the value of the targetDir property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTargetDir() {
        return targetDir;
    }

    /**
     * Sets the value of the targetDir property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTargetDir(String value) {
        this.targetDir = value;
    }

    /**
     * Gets the value of the tmDir property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTmDir() {
        return tmDir;
    }

    /**
     * Sets the value of the tmDir property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTmDir(String value) {
        this.tmDir = value;
    }

    /**
     * Gets the value of the glossaryDir property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGlossaryDir() {
        return glossaryDir;
    }

    /**
     * Sets the value of the glossaryDir property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGlossaryDir(String value) {
        this.glossaryDir = value;
    }

    /**
     * Gets the value of the glossaryFile property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGlossaryFile() {
        return glossaryFile;
    }

    /**
     * Sets the value of the glossaryFile property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGlossaryFile(String value) {
        this.glossaryFile = value;
    }

    /**
     * Gets the value of the dictionaryDir property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDictionaryDir() {
        return dictionaryDir;
    }

    /**
     * Sets the value of the dictionaryDir property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDictionaryDir(String value) {
        this.dictionaryDir = value;
    }

    /**
     * Gets the value of the exportTmDir property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExportTmDir() {
        return exportTmDir;
    }

    /**
     * Sets the value of the exportTmDir property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExportTmDir(String value) {
        this.exportTmDir = value;
    }

    /**
     * Gets the value of the exportTmLevels property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExportTmLevels() {
        return exportTmLevels;
    }

    /**
     * Sets the value of the exportTmLevels property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExportTmLevels(String value) {
        this.exportTmLevels = value;
    }

    /**
     * Gets the value of the sourceLang property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSourceLang() {
        return sourceLang;
    }

    /**
     * Sets the value of the sourceLang property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSourceLang(String value) {
        this.sourceLang = value;
    }

    /**
     * Gets the value of the targetLang property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTargetLang() {
        return targetLang;
    }

    /**
     * Sets the value of the targetLang property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTargetLang(String value) {
        this.targetLang = value;
    }

    /**
     * Gets the value of the sourceTok property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSourceTok() {
        return sourceTok;
    }

    /**
     * Sets the value of the sourceTok property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSourceTok(String value) {
        this.sourceTok = value;
    }

    /**
     * Gets the value of the targetTok property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTargetTok() {
        return targetTok;
    }

    /**
     * Sets the value of the targetTok property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTargetTok(String value) {
        this.targetTok = value;
    }

    /**
     * Gets the value of the sentenceSeg property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isSentenceSeg() {
        return sentenceSeg;
    }

    /**
     * Sets the value of the sentenceSeg property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSentenceSeg(Boolean value) {
        this.sentenceSeg = value;
    }

    /**
     * Gets the value of the supportDefaultTranslations property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isSupportDefaultTranslations() {
        return supportDefaultTranslations;
    }

    /**
     * Sets the value of the supportDefaultTranslations property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSupportDefaultTranslations(Boolean value) {
        this.supportDefaultTranslations = value;
    }

    /**
     * Gets the value of the removeTags property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isRemoveTags() {
        return removeTags;
    }

    /**
     * Sets the value of the removeTags property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setRemoveTags(Boolean value) {
        this.removeTags = value;
    }

    /**
     * Gets the value of the externalCommand property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExternalCommand() {
        return externalCommand;
    }

    /**
     * Sets the value of the externalCommand property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExternalCommand(String value) {
        this.externalCommand = value;
    }

    /**
     * Gets the value of the repositories property.
     * 
     * @return
     *     possible object is
     *     {@link Project.Repositories }
     *     
     */
    public Project.Repositories getRepositories() {
        return repositories;
    }

    /**
     * Sets the value of the repositories property.
     * 
     * @param value
     *     allowed object is
     *     {@link Project.Repositories }
     *     
     */
    public void setRepositories(Project.Repositories value) {
        this.repositories = value;
    }

    /**
     * Gets the value of the version property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVersion(String value) {
        this.version = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element name="repository" type="{}RepositoryDefinition" maxOccurs="unbounded" minOccurs="0"/&gt;
     *       &lt;/sequence&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "repository"
    })
    public static class Repositories {

        protected List<RepositoryDefinition> repository;

        /**
         * Gets the value of the repository property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the repository property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getRepository().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link RepositoryDefinition }
         * 
         * 
         */
        public List<RepositoryDefinition> getRepository() {
            if (repository == null) {
                repository = new ArrayList<RepositoryDefinition>();
            }
            return this.repository;
        }

    }

}
