
package gen.core.segmentation;

import java.util.ArrayList;
import java.util.List;
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
 *         <element ref="{http://www.lisa.org/srx20}languagemap" maxOccurs="unbounded"/>
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
    "languagemap"
})
@XmlRootElement(name = "maprules")
public class Maprules {

    /**
     * Maps one or more languages to a set of rules
     * 
     */
    @XmlElement(required = true)
    protected List<Languagemap> languagemap;

    /**
     * Maps one or more languages to a set of rules
     * 
     * Gets the value of the languagemap property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the languagemap property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getLanguagemap().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Languagemap }
     * </p>
     * 
     * 
     * @return
     *     The value of the languagemap property.
     */
    public List<Languagemap> getLanguagemap() {
        if (languagemap == null) {
            languagemap = new ArrayList<>();
        }
        return this.languagemap;
    }

}
