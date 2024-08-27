
package gen.core.tbx;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
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
 *       <sequence>
 *         <element ref="{}martifHeader"/>
 *         <element ref="{}text"/>
 *       </sequence>
 *       <attribute name="type" use="required" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       <attribute ref="{http://www.w3.org/XML/1998/namespace}lang use="required""/>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "martifHeader",
    "text"
})
@XmlRootElement(name = "martif")
public class Martif {

    @XmlElement(required = true)
    protected MartifHeader martifHeader;
    @XmlElement(required = true)
    protected Text text;
    @XmlAttribute(name = "type", required = true)
    @XmlSchemaType(name = "anySimpleType")
    protected String type;
    /**
     * <pre>
     * &lt;?xml version="1.0" encoding="UTF-8"?&gt;&lt;div xmlns="http://www.w3.org/1999/xhtml" xmlns:xs="http://www.w3.org/2001/XMLSchema"&gt;
     *      
     *       &lt;h3&gt;lang (as an attribute name)&lt;/h3&gt;
     *       &lt;p&gt;
     * 
     *        denotes an attribute whose value
     *        is a language code for the natural language of the content of
     *        any element; its value is inherited.  This name is reserved
     *        by virtue of its definition in the XML specification.&lt;/p&gt;
     *      
     *     &lt;/div&gt;
     * </pre>
     * 
     *     
     * <pre>
     * &lt;?xml version="1.0" encoding="UTF-8"?&gt;&lt;div xmlns="http://www.w3.org/1999/xhtml" xmlns:xs="http://www.w3.org/2001/XMLSchema"&gt;
     *      &lt;h4&gt;Notes&lt;/h4&gt;
     *      &lt;p&gt;
     *       Attempting to install the relevant ISO 2- and 3-letter
     *       codes as the enumerated possible values is probably never
     *       going to be a realistic possibility.  
     *      &lt;/p&gt;
     *      &lt;p&gt;
     * 
     *       See BCP 47 at &lt;a href="http://www.rfc-editor.org/rfc/bcp/bcp47.txt"&gt;
     *        http://www.rfc-editor.org/rfc/bcp/bcp47.txt&lt;/a&gt;
     *       and the IANA language subtag registry at
     *       &lt;a href="http://www.iana.org/assignments/language-subtag-registry"&gt;
     *        http://www.iana.org/assignments/language-subtag-registry&lt;/a&gt;
     *       for further information.
     *      &lt;/p&gt;
     *      &lt;p&gt;
     * 
     *       The union allows for the 'un-declaration' of xml:lang with
     *       the empty string.
     *      &lt;/p&gt;
     *     &lt;/div&gt;
     * </pre>
     * 
     */
    @XmlAttribute(name = "lang", namespace = "http://www.w3.org/XML/1998/namespace", required = true)
    protected String lang;

    /**
     * Gets the value of the martifHeader property.
     * 
     * @return
     *     possible object is
     *     {@link MartifHeader }
     *     
     */
    public MartifHeader getMartifHeader() {
        return martifHeader;
    }

    /**
     * Sets the value of the martifHeader property.
     * 
     * @param value
     *     allowed object is
     *     {@link MartifHeader }
     *     
     */
    public void setMartifHeader(MartifHeader value) {
        this.martifHeader = value;
    }

    /**
     * Gets the value of the text property.
     * 
     * @return
     *     possible object is
     *     {@link Text }
     *     
     */
    public Text getText() {
        return text;
    }

    /**
     * Sets the value of the text property.
     * 
     * @param value
     *     allowed object is
     *     {@link Text }
     *     
     */
    public void setText(Text value) {
        this.text = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * <pre>
     * &lt;?xml version="1.0" encoding="UTF-8"?&gt;&lt;div xmlns="http://www.w3.org/1999/xhtml" xmlns:xs="http://www.w3.org/2001/XMLSchema"&gt;
     *      
     *       &lt;h3&gt;lang (as an attribute name)&lt;/h3&gt;
     *       &lt;p&gt;
     * 
     *        denotes an attribute whose value
     *        is a language code for the natural language of the content of
     *        any element; its value is inherited.  This name is reserved
     *        by virtue of its definition in the XML specification.&lt;/p&gt;
     *      
     *     &lt;/div&gt;
     * </pre>
     * 
     *     
     * <pre>
     * &lt;?xml version="1.0" encoding="UTF-8"?&gt;&lt;div xmlns="http://www.w3.org/1999/xhtml" xmlns:xs="http://www.w3.org/2001/XMLSchema"&gt;
     *      &lt;h4&gt;Notes&lt;/h4&gt;
     *      &lt;p&gt;
     *       Attempting to install the relevant ISO 2- and 3-letter
     *       codes as the enumerated possible values is probably never
     *       going to be a realistic possibility.  
     *      &lt;/p&gt;
     *      &lt;p&gt;
     * 
     *       See BCP 47 at &lt;a href="http://www.rfc-editor.org/rfc/bcp/bcp47.txt"&gt;
     *        http://www.rfc-editor.org/rfc/bcp/bcp47.txt&lt;/a&gt;
     *       and the IANA language subtag registry at
     *       &lt;a href="http://www.iana.org/assignments/language-subtag-registry"&gt;
     *        http://www.iana.org/assignments/language-subtag-registry&lt;/a&gt;
     *       for further information.
     *      &lt;/p&gt;
     *      &lt;p&gt;
     * 
     *       The union allows for the 'un-declaration' of xml:lang with
     *       the empty string.
     *      &lt;/p&gt;
     *     &lt;/div&gt;
     * </pre>
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLang() {
        return lang;
    }

    /**
     * Sets the value of the lang property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getLang()
     */
    public void setLang(String value) {
        this.lang = value;
    }

}
