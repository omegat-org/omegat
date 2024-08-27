
package gen.core.tbx;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for anonymous complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType>
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element ref="{}fileDesc"/>
 *         <element ref="{}encodingDesc" minOccurs="0"/>
 *         <element ref="{}revisionDesc" minOccurs="0"/>
 *       </sequence>
 *       <attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "fileDesc",
    "encodingDesc",
    "revisionDesc"
})
@XmlRootElement(name = "martifHeader")
public class MartifHeader {

    @XmlElement(required = true)
    protected FileDesc fileDesc;
    protected EncodingDesc encodingDesc;
    protected RevisionDesc revisionDesc;
    @XmlAttribute(name = "id")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;

    /**
     * Gets the value of the fileDesc property.
     * 
     * @return
     *     possible object is
     *     {@link FileDesc }
     *     
     */
    public FileDesc getFileDesc() {
        return fileDesc;
    }

    /**
     * Sets the value of the fileDesc property.
     * 
     * @param value
     *     allowed object is
     *     {@link FileDesc }
     *     
     */
    public void setFileDesc(FileDesc value) {
        this.fileDesc = value;
    }

    /**
     * Gets the value of the encodingDesc property.
     * 
     * @return
     *     possible object is
     *     {@link EncodingDesc }
     *     
     */
    public EncodingDesc getEncodingDesc() {
        return encodingDesc;
    }

    /**
     * Sets the value of the encodingDesc property.
     * 
     * @param value
     *     allowed object is
     *     {@link EncodingDesc }
     *     
     */
    public void setEncodingDesc(EncodingDesc value) {
        this.encodingDesc = value;
    }

    /**
     * Gets the value of the revisionDesc property.
     * 
     * @return
     *     possible object is
     *     {@link RevisionDesc }
     *     
     */
    public RevisionDesc getRevisionDesc() {
        return revisionDesc;
    }

    /**
     * Sets the value of the revisionDesc property.
     * 
     * @param value
     *     allowed object is
     *     {@link RevisionDesc }
     *     
     */
    public void setRevisionDesc(RevisionDesc value) {
        this.revisionDesc = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

}
