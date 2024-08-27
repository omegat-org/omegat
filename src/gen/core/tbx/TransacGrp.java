
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
 *         <element ref="{}transac"/>
 *         <choice maxOccurs="unbounded" minOccurs="0">
 *           <element ref="{}transacNote"/>
 *           <element ref="{}date"/>
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
    "transac",
    "transacNoteOrDateOrNote"
})
@XmlRootElement(name = "transacGrp")
public class TransacGrp {

    @XmlElement(required = true)
    protected Transac transac;
    @XmlElements({
        @XmlElement(name = "transacNote", type = TransacNote.class),
        @XmlElement(name = "date", type = Date.class),
        @XmlElement(name = "note", type = Note.class),
        @XmlElement(name = "ref", type = Ref.class),
        @XmlElement(name = "xref", type = Xref.class)
    })
    protected List<Object> transacNoteOrDateOrNote;
    @XmlAttribute(name = "id")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;

    /**
     * Gets the value of the transac property.
     * 
     * @return
     *     possible object is
     *     {@link Transac }
     *     
     */
    public Transac getTransac() {
        return transac;
    }

    /**
     * Sets the value of the transac property.
     * 
     * @param value
     *     allowed object is
     *     {@link Transac }
     *     
     */
    public void setTransac(Transac value) {
        this.transac = value;
    }

    /**
     * Gets the value of the transacNoteOrDateOrNote property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the transacNoteOrDateOrNote property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getTransacNoteOrDateOrNote().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Date }
     * {@link Note }
     * {@link Ref }
     * {@link TransacNote }
     * {@link Xref }
     * </p>
     * 
     * 
     * @return
     *     The value of the transacNoteOrDateOrNote property.
     */
    public List<Object> getTransacNoteOrDateOrNote() {
        if (transacNoteOrDateOrNote == null) {
            transacNoteOrDateOrNote = new ArrayList<>();
        }
        return this.transacNoteOrDateOrNote;
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
