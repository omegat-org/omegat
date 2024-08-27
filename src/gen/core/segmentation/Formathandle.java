
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
 *       <attribute name="include" use="required">
 *         <simpleType>
 *           <restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             <enumeration value="yes"/>
 *             <enumeration value="no"/>
 *           </restriction>
 *         </simpleType>
 *       </attribute>
 *       <attribute name="type" use="required">
 *         <simpleType>
 *           <restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             <enumeration value="start"/>
 *             <enumeration value="end"/>
 *             <enumeration value="isolated"/>
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
@XmlType(name = "")
@XmlRootElement(name = "formathandle")
public class Formathandle {

    /**
     * A value of "no" indicates that the format code does not belong
     *                         to the segment being created. A value of "yes" indicates that the format code
     *                         belongs to the segment being created.
     * 
     */
    @XmlAttribute(name = "include", required = true)
    protected String include;
    /**
     * The type of format for which behaviour is being defined. Can be
     *                         "start", "end" or "isolated".
     * 
     */
    @XmlAttribute(name = "type", required = true)
    protected String type;

    /**
     * A value of "no" indicates that the format code does not belong
     *                         to the segment being created. A value of "yes" indicates that the format code
     *                         belongs to the segment being created.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInclude() {
        return include;
    }

    /**
     * Sets the value of the include property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getInclude()
     */
    public void setInclude(String value) {
        this.include = value;
    }

    /**
     * The type of format for which behaviour is being defined. Can be
     *                         "start", "end" or "isolated".
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getType()
     */
    public void setType(String value) {
        this.type = value;
    }

}
