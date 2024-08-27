
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
 *         <element ref="{}term"/>
 *         <choice maxOccurs="unbounded" minOccurs="0">
 *           <element ref="{}termNote"/>
 *           <element ref="{}termNoteGrp"/>
 *         </choice>
 *         <choice maxOccurs="unbounded" minOccurs="0">
 *           <element ref="{}termCompList"/>
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
    "term",
    "termNoteOrTermNoteGrp",
    "termCompList"
})
@XmlRootElement(name = "termGrp")
public class TermGrp {

    @XmlElement(required = true)
    protected Term term;
    @XmlElements({
        @XmlElement(name = "termNote", type = TermNote.class),
        @XmlElement(name = "termNoteGrp", type = TermNoteGrp.class)
    })
    protected List<Object> termNoteOrTermNoteGrp;
    protected List<TermCompList> termCompList;
    @XmlAttribute(name = "id")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;

    /**
     * Gets the value of the term property.
     * 
     * @return
     *     possible object is
     *     {@link Term }
     *     
     */
    public Term getTerm() {
        return term;
    }

    /**
     * Sets the value of the term property.
     * 
     * @param value
     *     allowed object is
     *     {@link Term }
     *     
     */
    public void setTerm(Term value) {
        this.term = value;
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
     * Gets the value of the termCompList property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the termCompList property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getTermCompList().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TermCompList }
     * </p>
     * 
     * 
     * @return
     *     The value of the termCompList property.
     */
    public List<TermCompList> getTermCompList() {
        if (termCompList == null) {
            termCompList = new ArrayList<>();
        }
        return this.termCompList;
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
