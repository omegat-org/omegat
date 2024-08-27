
package gen.core.tbx;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElementRef;
import jakarta.xml.bind.annotation.XmlElementRefs;
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlMixed;
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
 *       <choice maxOccurs="unbounded" minOccurs="0">
 *         <element ref="{}hi"/>
 *         <element ref="{}foreign"/>
 *         <element ref="{}bpt"/>
 *         <element ref="{}ept"/>
 *         <element ref="{}ph"/>
 *       </choice>
 *       <attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *       <attribute name="type">
 *         <simpleType>
 *           <restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *             <enumeration value="XCSURI"/>
 *             <enumeration value="DCSName"/>
 *             <enumeration value="XCSContent"/>
 *           </restriction>
 *         </simpleType>
 *       </attribute>
 *       <attribute ref="{http://www.w3.org/XML/1998/namespace}lang"/>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "content"
})
@XmlRootElement(name = "p")
public class P {

    @XmlElementRefs({
        @XmlElementRef(name = "hi", type = Hi.class, required = false),
        @XmlElementRef(name = "foreign", type = Foreign.class, required = false),
        @XmlElementRef(name = "bpt", type = Bpt.class, required = false),
        @XmlElementRef(name = "ept", type = Ept.class, required = false),
        @XmlElementRef(name = "ph", type = Ph.class, required = false)
    })
    @XmlMixed
    protected List<Object> content;
    @XmlAttribute(name = "id")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;
    @XmlAttribute(name = "type")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
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
    @XmlAttribute(name = "lang", namespace = "http://www.w3.org/XML/1998/namespace")
    protected String lang;

    /**
     * Gets the value of the content property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the content property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getContent().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Bpt }
     * {@link Ept }
     * {@link Foreign }
     * {@link Hi }
     * {@link Ph }
     * {@link String }
     * </p>
     * 
     * 
     * @return
     *     The value of the content property.
     */
    public List<Object> getContent() {
        if (content == null) {
            content = new ArrayList<>();
        }
        return this.content;
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
