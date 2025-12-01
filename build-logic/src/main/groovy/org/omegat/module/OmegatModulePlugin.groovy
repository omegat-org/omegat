package org.omegat.module

import com.github.spotbugs.snom.SpotBugsExtension
import net.ltgt.gradle.nullaway.NullAwayExtension
import org.gradle.api.GradleException
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.plugins.quality.PmdExtension
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test

class OmegatModulePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.plugins.apply('java-library')
        project.plugins.apply('jacoco')
        project.plugins.apply('checkstyle')
        project.plugins.apply('pmd')
        project.plugins.apply('com.github.spotbugs')
        project.plugins.apply('net.ltgt.errorprone')
        project.plugins.apply('net.ltgt.nullaway')

        project.repositories {
            mavenCentral()
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
        }

        project.tasks.withType(JavaCompile).configureEach { javaCompile ->
            javaCompile.options.encoding = "UTF-8"
            javaCompile.options.compilerArgs.addAll '-Xlint'
        }

        project.dependencies {
            // Error Prone dependencies
            errorprone("com.google.errorprone:error_prone_core:2.40.0")
            errorprone("com.uber.nullaway:nullaway:0.12.7")
        }

        project.extensions.configure(SpotBugsExtension) { spotbugs ->
            spotbugs.extraArgs = ['-longBugCodes']
            spotbugs.jvmArgs = ['-Duser.language=en']
            spotbugs.toolVersion = '4.9.3'
            def excludeFile = project.rootProject.file('config/spotbugs/exclude.xml')
            if (excludeFile.exists()) {
                spotbugs.excludeFilter = excludeFile
            }
        }

        project.extensions.configure(PmdExtension) { pmd ->
            pmd.toolVersion = '6.38.0'
            def ruleset = project.rootProject.file('config/pmd/ruleset.xml')
            if (ruleset.exists()) {
                pmd.ruleSetFiles = project.files(ruleset)
            }
            pmd.consoleOutput = true
        }

        project.extensions.configure(NullAwayExtension) { nullaway ->
            nullaway.annotatedPackages.add("org.omegat")
        }

        // Register a metadata validation task and wire it into the verification lifecycle
        def validateTask = project.tasks.register("validateModuleMetadata") {
            group = "verification"
            description = "Validate OmegaT module metadata (e.g., org.omegat.module.category)."
            doLast {
                def category = getPropertyOrDefault(project, 'org.omegat.module.category', 'miscellaneous')
                def allowed = resolveAllowedCategoriesFromSource(project)
                def normalized = category?.toString()?.trim()?.toLowerCase(Locale.ENGLISH)
                if (!allowed.contains(normalized)) {
                    throw new GradleException("Invalid org.omegat.module.category '${category}' for project ${project.path}. Allowed values discovered from PluginType: ${allowed.join(', ')}")
                }
            }
        }
        project.tasks.named("check").configure { dependsOn(validateTask) }

        configureTestEnvironment(project)
    }

    private static Map<String, String> buildManifestAttributes(Project project) {
        def attributes = new HashMap<String, String>()

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
        attributes['Created-By'] = "Gradle ${project.gradle.gradleVersion}".toString()

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

    private static String getPropertyOrDefault(Project project, String propertyName, String defaultValue) {
        return project.hasProperty(propertyName) ?
                project.property(propertyName).toString() : defaultValue
    }

    /**
     * Derive allowed module categories from Java source enum PluginUtils.PluginType to avoid duplication.
     * Falls back to a conservative set if the source cannot be parsed.
     */
    private static Set<String> resolveAllowedCategoriesFromSource(Project project) {
        Set<String> fallback = ['language', 'spellcheck', 'machinetranslator', 'theme', 'miscellaneous'] as Set
        try {
            def srcPath = 'src/org/omegat/filters2/master/PluginUtils.java'
            def file = project.rootProject.file(srcPath)
            if (!file.exists()) {
                project.logger.info("[OmegatModulePlugin] PluginType source not found at ${file}. Using fallback categories: ${fallback}")
                return fallback
            }
            String text = file.getText('UTF-8')
            // Regex to capture enum constants with a single string constructor argument
            def matcher = (text =~ /(?m)^\s*([A-Z_]+)\s*\(\s*"([^"]+)"\s*\)\s*[,;]?\s*$/)
            Set<String> values = new LinkedHashSet<>()
            matcher.each { m ->
                String value = m[2]
                if (value) {
                    String v = value.trim().toLowerCase(Locale.ENGLISH)
                    if (!v.equalsIgnoreCase('undefined')) {
                        values.add(v)
                    }
                }
            }
            if (values.isEmpty()) {
                project.logger.info("[OmegatModulePlugin] No values parsed from PluginType. Using fallback: ${fallback}")
                return fallback
            }
            return values
        } catch (Throwable t) {
            project.logger.info("[OmegatModulePlugin] Failed to parse PluginType: ${t.class.simpleName}: ${t.message}. Using fallback: ${fallback}")
            return fallback
        }
    }
}
