
package gen.core.tbx;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
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
 *         <choice>
 *           <choice maxOccurs="unbounded" minOccurs="0">
 *             <element ref="{}descrip"/>
 *             <element ref="{}descripGrp"/>
 *             <element ref="{}admin"/>
 *             <element ref="{}adminGrp"/>
 *             <element ref="{}transacGrp"/>
 *             <element ref="{}note"/>
 *             <element ref="{}ref"/>
 *             <element ref="{}xref"/>
 *           </choice>
 *         </choice>
 *         <choice maxOccurs="unbounded">
 *           <element ref="{}tig"/>
 *           <element ref="{}ntig"/>
 *         </choice>
 *       </sequence>
 *       <attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
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
    "descripOrDescripGrpOrAdmin",
    "tigOrNtig"
})
@XmlRootElement(name = "langSet")
public class LangSet {

    @XmlElements({
        @XmlElement(name = "descrip", type = Descrip.class),
        @XmlElement(name = "descripGrp", type = DescripGrp.class),
        @XmlElement(name = "admin", type = Admin.class),
        @XmlElement(name = "adminGrp", type = AdminGrp.class),
        @XmlElement(name = "transacGrp", type = TransacGrp.class),
        @XmlElement(name = "note", type = Note.class),
        @XmlElement(name = "ref", type = Ref.class),
        @XmlElement(name = "xref", type = Xref.class)
    })
    protected List<Object> descripOrDescripGrpOrAdmin;
    @XmlElements({
        @XmlElement(name = "tig", type = Tig.class),
        @XmlElement(name = "ntig", type = Ntig.class)
    })
    protected List<Object> tigOrNtig;
    @XmlAttribute(name = "id")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;
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
     * Gets the value of the descripOrDescripGrpOrAdmin property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the descripOrDescripGrpOrAdmin property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getDescripOrDescripGrpOrAdmin().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Admin }
     * {@link AdminGrp }
     * {@link Descrip }
     * {@link DescripGrp }
     * {@link Note }
     * {@link Ref }
     * {@link TransacGrp }
     * {@link Xref }
     * </p>
     * 
     * 
     * @return
     *     The value of the descripOrDescripGrpOrAdmin property.
     */
    public List<Object> getDescripOrDescripGrpOrAdmin() {
        if (descripOrDescripGrpOrAdmin == null) {
            descripOrDescripGrpOrAdmin = new ArrayList<>();
        }
        return this.descripOrDescripGrpOrAdmin;
    }

    /**
     * Gets the value of the tigOrNtig property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the tigOrNtig property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getTigOrNtig().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Ntig }
     * {@link Tig }
     * </p>
     * 
     * 
     * @return
     *     The value of the tigOrNtig property.
     */
    public List<Object> getTigOrNtig() {
        if (tigOrNtig == null) {
            tigOrNtig = new ArrayList<>();
        }
        return this.tigOrNtig;
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
