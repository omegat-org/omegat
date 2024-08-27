
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
 *           <element ref="{}termComp"/>
 *           <element ref="{}termCompGrp"/>
 *         </choice>
 *       </sequence>
 *       <attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *       <attribute name="type" use="required" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
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
    "termCompOrTermCompGrp"
})
@XmlRootElement(name = "termCompList")
public class TermCompList {

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
        @XmlElement(name = "termComp", type = TermComp.class),
        @XmlElement(name = "termCompGrp", type = TermCompGrp.class)
    })
    protected List<Object> termCompOrTermCompGrp;
    @XmlAttribute(name = "id")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;
    @XmlAttribute(name = "type", required = true)
    @XmlSchemaType(name = "anySimpleType")
    protected String type;

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
     * Gets the value of the termCompOrTermCompGrp property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the termCompOrTermCompGrp property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getTermCompOrTermCompGrp().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TermComp }
     * {@link TermCompGrp }
     * </p>
     * 
     * 
     * @return
     *     The value of the termCompOrTermCompGrp property.
     */
    public List<Object> getTermCompOrTermCompGrp() {
        if (termCompOrTermCompGrp == null) {
            termCompOrTermCompGrp = new ArrayList<>();
        }
        return this.termCompOrTermCompGrp;
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

}
