
package gen.core.tmx14;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
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
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element ref="{}note"/&gt;
 *           &lt;element ref="{}prop"/&gt;
 *         &lt;/choice&gt;
 *         &lt;element ref="{}tuv" maxOccurs="unbounded"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="tuid" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
 *       &lt;attribute name="o-encoding" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
 *       &lt;attribute name="datatype" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
 *       &lt;attribute name="usagecount" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
 *       &lt;attribute name="lastusagedate" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
 *       &lt;attribute name="creationtool" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
 *       &lt;attribute name="creationtoolversion" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
 *       &lt;attribute name="creationdate" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
 *       &lt;attribute name="creationid" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
 *       &lt;attribute name="changedate" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
 *       &lt;attribute name="segtype"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN"&gt;
 *             &lt;enumeration value="block"/&gt;
 *             &lt;enumeration value="sentence"/&gt;
 *             &lt;enumeration value="phrase"/&gt;
 *             &lt;enumeration value="paragraph"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *       &lt;attribute name="changeid" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
 *       &lt;attribute name="o-tmf" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
 *       &lt;attribute name="srclang" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "noteOrProp",
    "tuv"
})
@XmlRootElement(name = "tu")
public class Tu {

    @XmlElements({
        @XmlElement(name = "note", type = Note.class),
        @XmlElement(name = "prop", type = Prop.class)
    })
    protected List<Object> noteOrProp;
    @XmlElement(required = true)
    protected List<Tuv> tuv;
    @XmlAttribute(name = "tuid")
    @XmlSchemaType(name = "anySimpleType")
    protected String tuid;
    @XmlAttribute(name = "o-encoding")
    @XmlSchemaType(name = "anySimpleType")
    protected String oEncoding;
    @XmlAttribute(name = "datatype")
    @XmlSchemaType(name = "anySimpleType")
    protected String datatype;
    @XmlAttribute(name = "usagecount")
    @XmlSchemaType(name = "anySimpleType")
    protected String usagecount;
    @XmlAttribute(name = "lastusagedate")
    @XmlSchemaType(name = "anySimpleType")
    protected String lastusagedate;
    @XmlAttribute(name = "creationtool")
    @XmlSchemaType(name = "anySimpleType")
    protected String creationtool;
    @XmlAttribute(name = "creationtoolversion")
    @XmlSchemaType(name = "anySimpleType")
    protected String creationtoolversion;
    @XmlAttribute(name = "creationdate")
    @XmlSchemaType(name = "anySimpleType")
    protected String creationdate;
    @XmlAttribute(name = "creationid")
    @XmlSchemaType(name = "anySimpleType")
    protected String creationid;
    @XmlAttribute(name = "changedate")
    @XmlSchemaType(name = "anySimpleType")
    protected String changedate;
    @XmlAttribute(name = "segtype")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String segtype;
    @XmlAttribute(name = "changeid")
    @XmlSchemaType(name = "anySimpleType")
    protected String changeid;
    @XmlAttribute(name = "o-tmf")
    @XmlSchemaType(name = "anySimpleType")
    protected String oTmf;
    @XmlAttribute(name = "srclang")
    @XmlSchemaType(name = "anySimpleType")
    protected String srclang;

    /**
     * Gets the value of the noteOrProp property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the noteOrProp property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNoteOrProp().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Note }
     * {@link Prop }
     * 
     * 
     */
    public List<Object> getNoteOrProp() {
        if (noteOrProp == null) {
            noteOrProp = new ArrayList<Object>();
        }
        return this.noteOrProp;
    }

    /**
     * Gets the value of the tuv property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the tuv property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTuv().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Tuv }
     * 
     * 
     */
    public List<Tuv> getTuv() {
        if (tuv == null) {
            tuv = new ArrayList<Tuv>();
        }
        return this.tuv;
    }

    /**
     * Gets the value of the tuid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTuid() {
        return tuid;
    }

    /**
     * Sets the value of the tuid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTuid(String value) {
        this.tuid = value;
    }

    /**
     * Gets the value of the oEncoding property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOEncoding() {
        return oEncoding;
    }

    /**
     * Sets the value of the oEncoding property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOEncoding(String value) {
        this.oEncoding = value;
    }

    /**
     * Gets the value of the datatype property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDatatype() {
        return datatype;
    }

    /**
     * Sets the value of the datatype property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDatatype(String value) {
        this.datatype = value;
    }

    /**
     * Gets the value of the usagecount property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUsagecount() {
        return usagecount;
    }

    /**
     * Sets the value of the usagecount property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUsagecount(String value) {
        this.usagecount = value;
    }

    /**
     * Gets the value of the lastusagedate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLastusagedate() {
        return lastusagedate;
    }

    /**
     * Sets the value of the lastusagedate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLastusagedate(String value) {
        this.lastusagedate = value;
    }

    /**
     * Gets the value of the creationtool property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCreationtool() {
        return creationtool;
    }

    /**
     * Sets the value of the creationtool property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCreationtool(String value) {
        this.creationtool = value;
    }

    /**
     * Gets the value of the creationtoolversion property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCreationtoolversion() {
        return creationtoolversion;
    }

    /**
     * Sets the value of the creationtoolversion property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCreationtoolversion(String value) {
        this.creationtoolversion = value;
    }

    /**
     * Gets the value of the creationdate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCreationdate() {
        return creationdate;
    }

    /**
     * Sets the value of the creationdate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCreationdate(String value) {
        this.creationdate = value;
    }

    /**
     * Gets the value of the creationid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCreationid() {
        return creationid;
    }

    /**
     * Sets the value of the creationid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCreationid(String value) {
        this.creationid = value;
    }

    /**
     * Gets the value of the changedate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getChangedate() {
        return changedate;
    }

    /**
     * Sets the value of the changedate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setChangedate(String value) {
        this.changedate = value;
    }

    /**
     * Gets the value of the segtype property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSegtype() {
        return segtype;
    }

    /**
     * Sets the value of the segtype property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSegtype(String value) {
        this.segtype = value;
    }

    /**
     * Gets the value of the changeid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getChangeid() {
        return changeid;
    }

    /**
     * Sets the value of the changeid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setChangeid(String value) {
        this.changeid = value;
    }

    /**
     * Gets the value of the oTmf property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOTmf() {
        return oTmf;
    }

    /**
     * Sets the value of the oTmf property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOTmf(String value) {
        this.oTmf = value;
    }

    /**
     * Gets the value of the srclang property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSrclang() {
        return srclang;
    }

    /**
     * Sets the value of the srclang property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSrclang(String value) {
        this.srclang = value;
    }

}
