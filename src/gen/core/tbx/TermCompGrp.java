
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
 *         <element ref="{}termComp"/>
 *         <choice maxOccurs="unbounded" minOccurs="0">
 *           <element ref="{}termNote"/>
 *           <element ref="{}termNoteGrp"/>
 *         </choice>
 *         <choice maxOccurs="unbounded" minOccurs="0">
 *           <element ref="{}admin"/>
 *           <element ref="{}adminGrp"/>
 *           <element ref="{}transacGrp"/>
 *           <element ref="{}note"/>
 *           <element ref="{}ref"/>
 *           <element ref="{}xref"/>
 *         </choice>
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
    "termComp",
    "termNoteOrTermNoteGrp",
    "adminOrAdminGrpOrTransacGrp"
})
@XmlRootElement(name = "termCompGrp")
public class TermCompGrp {

    @XmlElement(required = true)
    protected TermComp termComp;
    @XmlElements({
        @XmlElement(name = "termNote", type = TermNote.class),
        @XmlElement(name = "termNoteGrp", type = TermNoteGrp.class)
    })
    protected List<Object> termNoteOrTermNoteGrp;
    @XmlElements({
        @XmlElement(name = "admin", type = Admin.class),
        @XmlElement(name = "adminGrp", type = AdminGrp.class),
        @XmlElement(name = "transacGrp", type = TransacGrp.class),
        @XmlElement(name = "note", type = Note.class),
        @XmlElement(name = "ref", type = Ref.class),
        @XmlElement(name = "xref", type = Xref.class)
    })
    protected List<Object> adminOrAdminGrpOrTransacGrp;
    @XmlAttribute(name = "id")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;

    /**
     * Gets the value of the termComp property.
     * 
     * @return
     *     possible object is
     *     {@link TermComp }
     *     
     */
    public TermComp getTermComp() {
        return termComp;
    }

    /**
     * Sets the value of the termComp property.
     * 
     * @param value
     *     allowed object is
     *     {@link TermComp }
     *     
     */
    public void setTermComp(TermComp value) {
        this.termComp = value;
    }

    /**
     * Gets the value of the termNoteOrTermNoteGrp property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the termNoteOrTermNoteGrp property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getTermNoteOrTermNoteGrp().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TermNote }
     * {@link TermNoteGrp }
     * </p>
     * 
     * 
     * @return
     *     The value of the termNoteOrTermNoteGrp property.
     */
    public List<Object> getTermNoteOrTermNoteGrp() {
        if (termNoteOrTermNoteGrp == null) {
            termNoteOrTermNoteGrp = new ArrayList<>();
        }
        return this.termNoteOrTermNoteGrp;
    }

    /**
     * Gets the value of the adminOrAdminGrpOrTransacGrp property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the adminOrAdminGrpOrTransacGrp property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getAdminOrAdminGrpOrTransacGrp().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Admin }
     * {@link AdminGrp }
     * {@link Note }
     * {@link Ref }
     * {@link TransacGrp }
     * {@link Xref }
     * </p>
     * 
     * 
     * @return
     *     The value of the adminOrAdminGrpOrTransacGrp property.
     */
    public List<Object> getAdminOrAdminGrpOrTransacGrp() {
        if (adminOrAdminGrpOrTransacGrp == null) {
            adminOrAdminGrpOrTransacGrp = new ArrayList<>();
        }
        return this.adminOrAdminGrpOrTransacGrp;
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
