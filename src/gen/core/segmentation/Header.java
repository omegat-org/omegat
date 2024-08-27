
package gen.core.segmentation;

import java.util.ArrayList;
import java.util.List;
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
 *         <element ref="{http://www.lisa.org/srx20}formathandle" maxOccurs="3" minOccurs="0"/>
 *       </sequence>
 *       <attribute name="segmentsubflows" use="required">
 *         <simpleType>
 *           <restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             <enumeration value="yes"/>
 *             <enumeration value="no"/>
 *           </restriction>
 *         </simpleType>
 *       </attribute>
 *       <attribute name="cascade" use="required">
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
    "formathandle"
})
@XmlRootElement(name = "header")
public class Header {

    /**
     * Determines which side of the segment break that formatting
     *                 information goes
     * 
     */
    protected List<Formathandle> formathandle;
    /**
     * Determines whether text subflows should be
     *                         segmented
     * 
     */
    @XmlAttribute(name = "segmentsubflows", required = true)
    protected String segmentsubflows;
    /**
     * Determines whether a matching <languagemap> element
     *                         should terminate the search
     * 
     */
    @XmlAttribute(name = "cascade", required = true)
    protected String cascade;

    /**
     * Determines which side of the segment break that formatting
     *                 information goes
     * 
     * Gets the value of the formathandle property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the formathandle property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getFormathandle().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Formathandle }
     * </p>
     * 
     * 
     * @return
     *     The value of the formathandle property.
     */
    public List<Formathandle> getFormathandle() {
        if (formathandle == null) {
            formathandle = new ArrayList<>();
        }
        return this.formathandle;
    }

    /**
     * Determines whether text subflows should be
     *                         segmented
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSegmentsubflows() {
        return segmentsubflows;
    }

    /**
     * Sets the value of the segmentsubflows property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getSegmentsubflows()
     */
    public void setSegmentsubflows(String value) {
        this.segmentsubflows = value;
    }

    /**
     * Determines whether a matching <languagemap> element
     *                         should terminate the search
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCascade() {
        return cascade;
    }

    /**
     * Sets the value of the cascade property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getCascade()
     */
    public void setCascade(String value) {
        this.cascade = value;
    }

}
