
package gen.core.tmx14;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElementRef;
import jakarta.xml.bind.annotation.XmlElementRefs;
import jakarta.xml.bind.annotation.XmlMixed;
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
 *       <choice maxOccurs="unbounded" minOccurs="0">
 *         <element ref="{}bpt"/>
 *         <element ref="{}ept"/>
 *         <element ref="{}ph"/>
 *         <element ref="{}it"/>
 *         <element ref="{}hi"/>
 *         <element ref="{}ut"/>
 *       </choice>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "content"
})
@XmlRootElement(name = "seg")
public class Seg {

    @XmlElementRefs({
        @XmlElementRef(name = "bpt", type = Bpt.class, required = false),
        @XmlElementRef(name = "ept", type = Ept.class, required = false),
        @XmlElementRef(name = "ph", type = Ph.class, required = false),
        @XmlElementRef(name = "it", type = It.class, required = false),
        @XmlElementRef(name = "hi", type = Hi.class, required = false),
        @XmlElementRef(name = "ut", type = Ut.class, required = false)
    })
    @XmlMixed
    protected List<Object> content;

    /**
     * Gets the value of the content property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the content property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getContent().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Bpt }
     * {@link Ept }
     * {@link Hi }
     * {@link It }
     * {@link Ph }
     * {@link Ut }
     * {@link String }
     * </p>
     * 
     * 
     * @return
     *     The value of the content property.
     */
    public List<Object> getContent() {
        if (content == null) {
            content = new ArrayList<>();
        }
        return this.content;
    }

}
