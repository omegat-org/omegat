
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
 *         <element ref="{}descrip"/>
 *         <choice maxOccurs="unbounded" minOccurs="0">
 *           <element ref="{}descripNote"/>
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
    "descrip",
    "descripNoteOrAdminOrAdminGrp"
})
@XmlRootElement(name = "descripGrp")
public class DescripGrp {

    @XmlElement(required = true)
    protected Descrip descrip;
    @XmlElements({
        @XmlElement(name = "descripNote", type = DescripNote.class),
        @XmlElement(name = "admin", type = Admin.class),
        @XmlElement(name = "adminGrp", type = AdminGrp.class),
        @XmlElement(name = "transacGrp", type = TransacGrp.class),
        @XmlElement(name = "note", type = Note.class),
        @XmlElement(name = "ref", type = Ref.class),
        @XmlElement(name = "xref", type = Xref.class)
    })
    protected List<Object> descripNoteOrAdminOrAdminGrp;
    @XmlAttribute(name = "id")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;

    /**
     * Gets the value of the descrip property.
     * 
     * @return
     *     possible object is
     *     {@link Descrip }
     *     
     */
    public Descrip getDescrip() {
        return descrip;
    }

    /**
     * Sets the value of the descrip property.
     * 
     * @param value
     *     allowed object is
     *     {@link Descrip }
     *     
     */
    public void setDescrip(Descrip value) {
        this.descrip = value;
    }

    /**
     * Gets the value of the descripNoteOrAdminOrAdminGrp property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the descripNoteOrAdminOrAdminGrp property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getDescripNoteOrAdminOrAdminGrp().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Admin }
     * {@link AdminGrp }
     * {@link DescripNote }
     * {@link Note }
     * {@link Ref }
     * {@link TransacGrp }
     * {@link Xref }
     * </p>
     * 
     * 
     * @return
     *     The value of the descripNoteOrAdminOrAdminGrp property.
     */
    public List<Object> getDescripNoteOrAdminOrAdminGrp() {
        if (descripNoteOrAdminOrAdminGrp == null) {
            descripNoteOrAdminOrAdminGrp = new ArrayList<>();
        }
        return this.descripNoteOrAdminOrAdminGrp;
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
