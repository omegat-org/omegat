
package gen.core.segmentation;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
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
 *       &lt;attribute name="languagerulename" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="languagepattern" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "languagemap")
public class Languagemap {

    @XmlAttribute(name = "languagerulename", required = true)
    protected String languagerulename;
    @XmlAttribute(name = "languagepattern", required = true)
    protected String languagepattern;

    /**
     * Gets the value of the languagerulename property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLanguagerulename() {
        return languagerulename;
    }

    /**
     * Sets the value of the languagerulename property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLanguagerulename(String value) {
        this.languagerulename = value;
    }

    /**
     * Gets the value of the languagepattern property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLanguagepattern() {
        return languagepattern;
    }

    /**
     * Sets the value of the languagepattern property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLanguagepattern(String value) {
        this.languagepattern = value;
    }

}
