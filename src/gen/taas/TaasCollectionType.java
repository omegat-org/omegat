
package gen.taas;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CollectionType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <pre>
 * &lt;simpleType name="CollectionType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="public"/&gt;
 *     &lt;enumeration value="private"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "CollectionType")
@XmlEnum
public enum TaasCollectionType {

    @XmlEnumValue("public")
    PUBLIC("public"),
    @XmlEnumValue("private")
    PRIVATE("private");
    private final String value;

    TaasCollectionType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static TaasCollectionType fromValue(String v) {
        for (TaasCollectionType c: TaasCollectionType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
