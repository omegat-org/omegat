
package gen.taas;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the gen.taas package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: gen.taas
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link TaasCollection }
     * 
     */
    public TaasCollection createTaasCollection() {
        return new TaasCollection();
    }

    /**
     * Create an instance of {@link TaasCollections }
     * 
     */
    public TaasCollections createTaasCollections() {
        return new TaasCollections();
    }

    /**
     * Create an instance of {@link TaasCollection.Languages }
     * 
     */
    public TaasCollection.Languages createTaasCollectionLanguages() {
        return new TaasCollection.Languages();
    }

    /**
     * Create an instance of {@link TaasCollection.Domains }
     * 
     */
    public TaasCollection.Domains createTaasCollectionDomains() {
        return new TaasCollection.Domains();
    }

    /**
     * Create an instance of {@link TaasLanguage }
     * 
     */
    public TaasLanguage createTaasLanguage() {
        return new TaasLanguage();
    }

    /**
     * Create an instance of {@link TaasDomains }
     * 
     */
    public TaasDomains createTaasDomains() {
        return new TaasDomains();
    }

    /**
     * Create an instance of {@link TaasDomain }
     * 
     */
    public TaasDomain createTaasDomain() {
        return new TaasDomain();
    }

    /**
     * Create an instance of {@link TaasArrayOfTerm }
     * 
     */
    public TaasArrayOfTerm createTaasArrayOfTerm() {
        return new TaasArrayOfTerm();
    }

    /**
     * Create an instance of {@link TaasTerm }
     * 
     */
    public TaasTerm createTaasTerm() {
        return new TaasTerm();
    }

    /**
     * Create an instance of {@link TaasExtractionResult }
     * 
     */
    public TaasExtractionResult createTaasExtractionResult() {
        return new TaasExtractionResult();
    }

}
