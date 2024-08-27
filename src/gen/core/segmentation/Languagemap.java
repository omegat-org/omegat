
package gen.core.segmentation;

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
 *       <attribute name="languagerulename" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="languagepattern" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "languagemap")
public class Languagemap {

    /**
     * The name of the language rule to use when the languagepattern
     *                         regular expression is satisfied
     * 
     */
    @XmlAttribute(name = "languagerulename", required = true)
    protected String languagerulename;
    /**
     * The regular expression pattern match for the language
     *                         code
     * 
     */
    @XmlAttribute(name = "languagepattern", required = true)
    protected String languagepattern;

    /**
     * The name of the language rule to use when the languagepattern
     *                         regular expression is satisfied
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
     * @see #getLanguagerulename()
     */
    public void setLanguagerulename(String value) {
        this.languagerulename = value;
    }

    /**
     * The regular expression pattern match for the language
     *                         code
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
     * @see #getLanguagepattern()
     */
    public void setLanguagepattern(String value) {
        this.languagepattern = value;
    }

}
