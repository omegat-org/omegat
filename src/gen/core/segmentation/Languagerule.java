
package gen.core.segmentation;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
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
 *         <element ref="{http://www.lisa.org/srx20}rule" maxOccurs="unbounded"/>
 *       </sequence>
 *       <attribute name="languagerulename" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "rule"
})
@XmlRootElement(name = "languagerule")
public class Languagerule {

    /**
     * A break/no break rule
     * 
     */
    @XmlElement(required = true)
    protected List<Rule> rule;
    /**
     * The name of the language rule
     * 
     */
    @XmlAttribute(name = "languagerulename", required = true)
    protected String languagerulename;

    /**
     * A break/no break rule
     * 
     * Gets the value of the rule property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the rule property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getRule().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Rule }
     * </p>
     * 
     * 
     * @return
     *     The value of the rule property.
     */
    public List<Rule> getRule() {
        if (rule == null) {
            rule = new ArrayList<>();
        }
        return this.rule;
    }

    /**
     * The name of the language rule
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLanguagerulename() {
        return languagerulename;
    }

    /**
     * Sets the value of the languagerulename property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getLanguagerulename()
     */
    public void setLanguagerulename(String value) {
        this.languagerulename = value;
    }

}
