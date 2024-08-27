
package gen.core.segmentation;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;


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
 *         <element ref="{http://www.lisa.org/srx20}languagerules"/>
 *         <element ref="{http://www.lisa.org/srx20}maprules"/>
 *       </sequence>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
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

    /**
     * Contains all the logical sets of rules
     * 
     */
    @XmlElement(required = true)
    protected Languagerules languagerules;
    /**
     * A set of language maps
     * 
     */
    @XmlElement(required = true)
    protected Maprules maprules;

    /**
     * Contains all the logical sets of rules
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
     * @see #getLanguagerules()
     */
    public void setLanguagerules(Languagerules value) {
        this.languagerules = value;
    }

    /**
     * A set of language maps
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
     * @see #getMaprules()
     */
    public void setMaprules(Maprules value) {
        this.maprules = value;
    }

}
