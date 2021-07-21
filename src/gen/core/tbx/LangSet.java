
package gen.core.tbx;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


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
 *         &lt;choice&gt;
 *           &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *             &lt;element ref="{}descrip"/&gt;
 *             &lt;element ref="{}descripGrp"/&gt;
 *             &lt;element ref="{}admin"/&gt;
 *             &lt;element ref="{}adminGrp"/&gt;
 *             &lt;element ref="{}transacGrp"/&gt;
 *             &lt;element ref="{}note"/&gt;
 *             &lt;element ref="{}ref"/&gt;
 *             &lt;element ref="{}xref"/&gt;
 *           &lt;/choice&gt;
 *         &lt;/choice&gt;
 *         &lt;choice maxOccurs="unbounded"&gt;
 *           &lt;element ref="{}tig"/&gt;
 *           &lt;element ref="{}ntig"/&gt;
 *         &lt;/choice&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *       &lt;attribute ref="{http://www.w3.org/XML/1998/namespace}lang use="required""/&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
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
    @XmlAttribute(name = "lang", namespace = "http://www.w3.org/XML/1998/namespace", required = true)
    protected String lang;

    /**
     * Gets the value of the descripOrDescripGrpOrAdmin property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the descripOrDescripGrpOrAdmin property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDescripOrDescripGrpOrAdmin().add(newItem);
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
     * 
     * 
     */
    public List<Object> getDescripOrDescripGrpOrAdmin() {
        if (descripOrDescripGrpOrAdmin == null) {
            descripOrDescripGrpOrAdmin = new ArrayList<Object>();
        }
        return this.descripOrDescripGrpOrAdmin;
    }

    /**
     * Gets the value of the tigOrNtig property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the tigOrNtig property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTigOrNtig().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Ntig }
     * {@link Tig }
     * 
     * 
     */
    public List<Object> getTigOrNtig() {
        if (tigOrNtig == null) {
            tigOrNtig = new ArrayList<Object>();
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
     * Gets the value of the lang property.
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
     */
    public void setLang(String value) {
        this.lang = value;
    }

}
