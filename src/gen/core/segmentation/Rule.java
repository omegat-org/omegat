
package gen.core.segmentation;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
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
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.lisa.org/srx20}beforebreak" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.lisa.org/srx20}afterbreak" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="break"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *             &lt;enumeration value="yes"/&gt;
 *             &lt;enumeration value="no"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "beforebreak",
    "afterbreak"
})
@XmlRootElement(name = "rule")
public class Rule {

    protected Beforebreak beforebreak;
    protected Afterbreak afterbreak;
    @XmlAttribute(name = "break")
    protected String _break;

    /**
     * Gets the value of the beforebreak property.
     * 
     * @return
     *     possible object is
     *     {@link Beforebreak }
     *     
     */
    public Beforebreak getBeforebreak() {
        return beforebreak;
    }

    /**
     * Sets the value of the beforebreak property.
     * 
     * @param value
     *     allowed object is
     *     {@link Beforebreak }
     *     
     */
    public void setBeforebreak(Beforebreak value) {
        this.beforebreak = value;
    }

    /**
     * Gets the value of the afterbreak property.
     * 
     * @return
     *     possible object is
     *     {@link Afterbreak }
     *     
     */
    public Afterbreak getAfterbreak() {
        return afterbreak;
    }

    /**
     * Sets the value of the afterbreak property.
     * 
     * @param value
     *     allowed object is
     *     {@link Afterbreak }
     *     
     */
    public void setAfterbreak(Afterbreak value) {
        this.afterbreak = value;
    }

    /**
     * Gets the value of the break property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBreak() {
        return _break;
    }

    /**
     * Sets the value of the break property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBreak(String value) {
        this._break = value;
    }

}
