
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
 *         &lt;element ref="{}descrip"/&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element ref="{}descripNote"/&gt;
 *           &lt;element ref="{}admin"/&gt;
 *           &lt;element ref="{}adminGrp"/&gt;
 *           &lt;element ref="{}transacGrp"/&gt;
 *           &lt;element ref="{}note"/&gt;
 *           &lt;element ref="{}ref"/&gt;
 *           &lt;element ref="{}xref"/&gt;
 *         &lt;/choice&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
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
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the descripNoteOrAdminOrAdminGrp property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDescripNoteOrAdminOrAdminGrp().add(newItem);
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
     * 
     * 
     */
    public List<Object> getDescripNoteOrAdminOrAdminGrp() {
        if (descripNoteOrAdminOrAdminGrp == null) {
            descripNoteOrAdminOrAdminGrp = new ArrayList<Object>();
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
