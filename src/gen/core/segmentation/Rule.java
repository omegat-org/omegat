
package gen.core.segmentation;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
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
 *         <element ref="{http://www.lisa.org/srx20}beforebreak" minOccurs="0"/>
 *         <element ref="{http://www.lisa.org/srx20}afterbreak" minOccurs="0"/>
 *       </sequence>
 *       <attribute name="break">
 *         <simpleType>
 *           <restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             <enumeration value="yes"/>
 *             <enumeration value="no"/>
 *           </restriction>
 *         </simpleType>
 *       </attribute>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
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

    /**
     * Contains the regular expression to match after the segment
     *                 break
     * 
     */
    protected Beforebreak beforebreak;
    /**
     * Contains the regular expression to match before the segment
     *                 break
     * 
     */
    protected Afterbreak afterbreak;
    /**
     * Determines whether this is a segment break or an exception
     *                         rule
     * 
     */
    @XmlAttribute(name = "break")
    protected String _break;

    /**
     * Contains the regular expression to match after the segment
     *                 break
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
     * @see #getBeforebreak()
     */
    public void setBeforebreak(Beforebreak value) {
        this.beforebreak = value;
    }

    /**
     * Contains the regular expression to match before the segment
     *                 break
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
     * @see #getAfterbreak()
     */
    public void setAfterbreak(Afterbreak value) {
        this.afterbreak = value;
    }

    /**
     * Determines whether this is a segment break or an exception
     *                         rule
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
     * @see #getBreak()
     */
    public void setBreak(String value) {
        this._break = value;
    }

}
