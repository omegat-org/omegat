package org.omegat.module

import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.testing.Test

class OmegatModulePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.plugins.apply('java-library')
        project.plugins.apply('maven-publish')

        project.repositories {
            mavenCentral()
            mavenLocal()
        }

        project.configurations.configureEach { conf ->
            if (conf.name == "runtimeClasspath") {
                conf.canBeResolved = true
            }
        }

        Map<String, Object> manifestAttrs = buildManifestAttributes(project)

        project.tasks.named("jar", Jar).configure { Jar jarTask ->
            from({
                project.configurations.getByName("runtimeClasspath")
                        .resolve()
                        .collect { it.directory ? it : project.zipTree(it) }
            })
            duplicatesStrategy = DuplicatesStrategy.EXCLUDE
            exclude "META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA"
            destinationDirectory.set(project.rootProject.layout.buildDirectory.dir("modules"))
            archiveBaseName.set(getPropertyOrDefault(project, 'org.omegat.module.packageName', project.name))
            manifest {
                if (!manifestAttrs.isEmpty()) {
                    attributes(manifestAttrs)
                }
            }
        }

        project.tasks.named("assemble").configure {
            dependsOn(project.tasks.named("jar"))
        }

        // Add sources and javadoc jars
        project.java {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
            withSourcesJar()
            withJavadocJar()
        }

        configureTestEnvironment(project)

        // Configure Maven publishing
        configureMavenPublishing(project)
    }

    private Map<String, String> buildManifestAttributes(Project project) {
        def attributes = [:]

        // Standard OmegaT module attributes
        attributes['Implementation-Title'] = getPropertyOrDefault(project, 'org.omegat.module.name', project.name)
        attributes['Plugin-Name'] = getPropertyOrDefault(project, 'org.omegat.module.name', project.name)

        def moduleVersion = getPropertyOrDefault(project, 'org.omegat.module.version', project.version.toString())
        attributes['Implementation-Version'] = moduleVersion
        attributes['Plugin-Version'] = moduleVersion
        attributes['Implementation-Vendor'] = getPropertyOrDefault(project, 'org.omegat.vendor', 'OmegaT')
        attributes['Built-By'] = System.getProperty('user.name')
        attributes['Built-Date'] = new Date().toString()
        attributes['Built-JDK'] = System.getProperty('java.version')
        attributes['Created-By'] = "Gradle ${project.gradle.gradleVersion}"

        attributes['OmegaT-Plugins'] = project.property('org.omegat.module.class').toString()
        attributes['Plugin-Version'] =  project.version.toString()
        attributes['Plugin-Category'] = getPropertyOrDefault(project, 'org.omegat.module.category', 'miscellaneous')
        attributes['Plugin-License'] = getPropertyOrDefault(project, 'org.omegat.module.license', 'GNU Public License version 3 or later')

        def moduleAuthor = getPropertyOrDefault(project, 'org.omegat.module.author', 'OmegaT team')
        if (moduleAuthor) {
            attributes['Plugin-Author'] = moduleAuthor
        }
        def moduleDescription = getPropertyOrDefault(project, 'org.omegat.module.description', null)
        if (moduleDescription) {
            attributes['Plugin-Description'] = moduleDescription
        }

        // Custom OmegaT module entries from gradle.properties
        project.properties.each { key, value ->
            if (key.startsWith('org.omegat.module.custom.')) {
                def manifestKey = key.substring('org.omegat.module.custom.'.length())
                attributes[manifestKey] = value.toString()
            }
        }

        return attributes
    }

    private void configureTestEnvironment(Project project) {
        project.tasks.named("test", Test).configure {
            useJUnit()
            workingDir project.rootProject.projectDir
            systemProperty 'java.util.logging.config.file', project.rootProject.layout.settingsDirectory.file("config/test/logger.properties").asFile
        }
    }

    private void configureMavenPublishing(Project project) {
        project.extensions.configure(PublishingExtension) { publishing ->
            publishing.publications {
                maven(MavenPublication) { publication ->
                    from project.components.java
                    groupId = 'org.omegat'
                    artifactId = getPropertyOrDefault(project, 'org.omegat.module.packageName', project.name)
                    version = getPropertyOrDefault(project, 'org.omegat.module.version', null)

                    // Use the custom jar from modules directory
                    artifact project.tasks.jar

                    pom {
                        name = getPropertyOrDefault(project, 'org.omegat.module.name', project.name)
                        description = getPropertyOrDefault(project, 'org.omegat.module.description', 'OmegaT Module')
                        url = 'https://omegat.org'
                        scm {
                            connection = "scm:git:https://github.com/omegat-org/omegat"
                            developerConnection = "scm:git:https://github.com/omegat-org/omegat"
                            url = "https://sourceforge.net/p/omegat/"
                        }
                        licenses {
                            license {
                                name = 'The GNU General Public License, Version 3.0'
                                url = 'https://www.gnu.org/licenses/licenses/gpl-3.0.html'
                            }
                        }
                        developers {
                            developer {
                                id = 'omegat'
                                name = 'OmegaT Developers'
                                email = 'info@omegat.org'
                            }
                        }
                    }
                }
            }
        }

        // Add a task to print publication info
        project.tasks.register('printPublicationInfo') {
            group = 'publishing'
            description = 'Prints information about the Maven publication'

            doLast {
                project.publishing.publications.maven { publication ->
                    println "Publication Info:"
                    println "  Group ID: ${publication.groupId}"
                    println "  Artifact ID: ${publication.artifactId}"
                    println "  Version: ${publication.version}"
                    println "  POM Name: ${publication.pom.name.get()}"
                    println "  POM Description: ${publication.pom.description.get()}"
                }
            }
        }
    }

    private static String getPropertyOrDefault(Project project, String propertyName, String defaultValue) {
        return project.hasProperty(propertyName) ?
                project.property(propertyName).toString() : defaultValue
    }
}
