
package gen.core.segmentation;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
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
 *         &lt;element ref="{http://www.lisa.org/srx20}languagerules"/&gt;
 *         &lt;element ref="{http://www.lisa.org/srx20}maprules"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "languagerules",
    "maprules"
})
@XmlRootElement(name = "body")
public class Body {

    @XmlElement(required = true)
    protected Languagerules languagerules;
    @XmlElement(required = true)
    protected Maprules maprules;

    /**
     * Gets the value of the languagerules property.
     * 
     * @return
     *     possible object is
     *     {@link Languagerules }
     *     
     */
    public Languagerules getLanguagerules() {
        return languagerules;
    }

    /**
     * Sets the value of the languagerules property.
     * 
     * @param value
     *     allowed object is
     *     {@link Languagerules }
     *     
     */
    public void setLanguagerules(Languagerules value) {
        this.languagerules = value;
    }

    /**
     * Gets the value of the maprules property.
     * 
     * @return
     *     possible object is
     *     {@link Maprules }
     *     
     */
    public Maprules getMaprules() {
        return maprules;
    }

    /**
     * Sets the value of the maprules property.
     * 
     * @param value
     *     allowed object is
     *     {@link Maprules }
     *     
     */
    public void setMaprules(Maprules value) {
        this.maprules = value;
    }

}
