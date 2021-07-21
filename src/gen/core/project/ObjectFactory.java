
package gen.core.project;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the gen.core.project package. 
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
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: gen.core.project
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Project }
     * 
     */
    public Project createProject() {
        return new Project();
    }

    /**
     * Create an instance of {@link Omegat }
     * 
     */
    public Omegat createOmegat() {
        return new Omegat();
    }

    /**
     * Create an instance of {@link Masks }
     * 
     */
    public Masks createMasks() {
        return new Masks();
    }

    /**
     * Create an instance of {@link Project.Repositories }
     * 
     */
    public Project.Repositories createProjectRepositories() {
        return new Project.Repositories();
    }

    /**
     * Create an instance of {@link RepositoryDefinition }
     * 
     */
    public RepositoryDefinition createRepositoryDefinition() {
        return new RepositoryDefinition();
    }

    /**
     * Create an instance of {@link RepositoryMapping }
     * 
     */
    public RepositoryMapping createRepositoryMapping() {
        return new RepositoryMapping();
    }

}
