
package gen.core.tmx14;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="unicode" use="required" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
 *       &lt;attribute name="code" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
 *       &lt;attribute name="ent" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
 *       &lt;attribute name="subst" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "map")
public class Map {

    @XmlAttribute(name = "unicode", required = true)
    @XmlSchemaType(name = "anySimpleType")
    protected String unicode;
    @XmlAttribute(name = "code")
    @XmlSchemaType(name = "anySimpleType")
    protected String code;
    @XmlAttribute(name = "ent")
    @XmlSchemaType(name = "anySimpleType")
    protected String ent;
    @XmlAttribute(name = "subst")
    @XmlSchemaType(name = "anySimpleType")
    protected String subst;

    /**
     * Gets the value of the unicode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUnicode() {
        return unicode;
    }

    /**
     * Sets the value of the unicode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUnicode(String value) {
        this.unicode = value;
    }

    /**
     * Gets the value of the code property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCode() {
        return code;
    }

    /**
     * Sets the value of the code property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCode(String value) {
        this.code = value;
    }

    /**
     * Gets the value of the ent property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEnt() {
        return ent;
    }

    /**
     * Sets the value of the ent property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEnt(String value) {
        this.ent = value;
    }

    /**
     * Gets the value of the subst property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSubst() {
        return subst;
    }

    /**
     * Sets the value of the subst property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSubst(String value) {
        this.subst = value;
    }

}
