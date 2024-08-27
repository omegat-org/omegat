
package gen.core.filters;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType>
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <attribute name="sourceFilenameMask" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="targetFilenamePattern" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="sourceEncoding" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="targetEncoding" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "files")
public class Files {

    @XmlAttribute(name = "sourceFilenameMask", required = true)
    protected String sourceFilenameMask;
    @XmlAttribute(name = "targetFilenamePattern")
    protected String targetFilenamePattern;
    @XmlAttribute(name = "sourceEncoding")
    protected String sourceEncoding;
    @XmlAttribute(name = "targetEncoding")
    protected String targetEncoding;

    /**
     * Gets the value of the sourceFilenameMask property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSourceFilenameMask() {
        return sourceFilenameMask;
    }

    /**
     * Sets the value of the sourceFilenameMask property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSourceFilenameMask(String value) {
        this.sourceFilenameMask = value;
    }

    /**
     * Gets the value of the targetFilenamePattern property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTargetFilenamePattern() {
        return targetFilenamePattern;
    }

    /**
     * Sets the value of the targetFilenamePattern property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTargetFilenamePattern(String value) {
        this.targetFilenamePattern = value;
    }

    /**
     * Gets the value of the sourceEncoding property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSourceEncoding() {
        return sourceEncoding;
    }

    /**
     * Sets the value of the sourceEncoding property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSourceEncoding(String value) {
        this.sourceEncoding = value;
    }

    /**
     * Gets the value of the targetEncoding property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTargetEncoding() {
        return targetEncoding;
    }

    /**
     * Sets the value of the targetEncoding property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTargetEncoding(String value) {
        this.targetEncoding = value;
    }

}
