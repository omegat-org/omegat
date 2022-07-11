# How to create a file filter plugin for OmegaT

# Intro
This manual describes how to write a file filter plugin for OmegaT. Assumed is knowledge of programming in Java.

# What is a file filter plugin
OmegaT can be extended with plugins. 
A plugin is just a `.jar` file, which is stored in `OMEGAT_INSTALLATION/plugins/` dir or `OMEGAT_USERPEFERENCES/plugins/` (see `StaticUtils.getConfigDir()` for details)
If a plugin needs to use additional jars, they can be placed in the same directory.

One type of plugin is the file filter plugin.
File filters can read files of a specific format and extract the text that needs to be translated and pass it to 
OmegaT so the user can translate the text in OmegaT.
The filter can get the translation back from OmegaT, and produce the translated file in the same format.

# requirements
To write a file filter plugin, you need to
* implement the `org/omegat/filters2/IFilter.java` interface
* create a .jar file that contains the implementation and a manifest file that indicates that the jar file is an 
OmegaT file filter plugin.

## manifest
There must be a manifest file that indicates that it is an OmegaT plugin. There are two flavors, see below. 
Omegat 5.3.0 also supports to provide additional information (valid for both flavors) that can be displayed in the UI. 
You can **optionally** provide name, version, author and description. 

OmegaT 5.5.0 can show the plugin name and author in Preferences. You are recommended to set these parameters.
You can **optionally** provide an URL of your plugin home page, license and category.
For each there are different manifest entry alternatives, and OmegaT will pick the first one present in the order from 
left to right as described in the table below:

| Attribute   | Manifest entry (pick one)                              | 
|-------------|--------------------------------------------------------| 
| Name        | Plugin-Name, Bundle-Name, Implementation-Title         |
| Version     | Plugin-Version, Bundle-Version, Implementation-Version |
| Author      | Plugin-Author, Implementation-Vendor, Built-By         |
| Description | Plugin-Description                                     |
| Link        | Plugin-Link                                            |
| License     | Plugin-License                                         |
| Category    | Plugin-Category                                        |

### plugins for OmegaT 2.1.3 and up
A plugin should be declared in `META-INF/MANIFEST.MF`:

    OmegaT-Plugin: true
    [Plugin-Name: …]
    [Plugin-Version: x.y.z]
    [Plugin-Author: …]
    [Plugin-Description: …]
    
    Name: my.Class
    OmegaT-Plugin: filter
    
    [Name: my.optional.other.Class
    OmegaT-Plugin: filter]

### plugins for OmegaT 3.0.1 and up
A plugin should be declared in `META-INF/MANIFEST.MF`:

    [Plugin-Name: …]
    [Plugin-Version: x.y.z]
    [Plugin-Author: …]
    [Plugin-Description: …]
    [Plugin-Link: https://..]
    [Plugin-Category: filter]
    OmegaT-Plugins: <classname>

where classname is the fully qualified classname of the plugin's initialization class. Multiple classnames can be defined, 
like in “Class-Path” attribute, i.e., space separated.
This class should contain the following methods:

    public static void loadPlugins() {}
    public static void unloadPlugins() {}

The `loadPlugins()` method is executed on application startup before any GUI initialization. 
The plugin initialization class should analyze OmegaT version and register classes for filters:

    Core.registerFilterClass(MyFilter.class);

Also, the initialization class can register its own event handlers, for example, for GUI initialization on application startup:

    CoreEvents.registerApplicationEventListener(...);

The loadPlugins() method should check OmegaT version, or existing interface, or other things required for plugin execution. 
If the plugin cannot be loaded, it can send some error message which will be displayed to the user after GUI initialization:

    Core.pluginLoadingError(“Some message”);
Since the plugin is likely to use some OmegaT classes, which can be changed in a future OmegaT version, 
we recommend separating plugin initialization class and plugin implementation class. 
Also, it will be better to do not use any other classes (except `Core` and `CoreEvents`) in import declarations of the 
plugin initialization class, so that it can catch loading errors and send a clear error message.

You can check if required classes and methods exist, or check the OmegaT version number.

Below, you see an example that checks for existence of VersionChecker class (doesn't exist in OmegaT3)
and next checks the version number.

    public static void loadPlugins() {
        try {
            //analyze OmegaT version:
            String requiredVersion = "5.4.0";
            String requiredUpdate = "0";
            try {
                Class<?> clazz = Class.forName("org.omegat.util.VersionChecker");
                Method compareVersions = clazz.getMethod("compareVersions", String.class, String.class, String.class, String.class);
                if ((int)compareVersions.invoke(clazz, OStrings.VERSION, OStrings.UPDATE, requiredVersion, requiredUpdate) < 0) {
                    Core.pluginLoadingError("Plugin … cannot be loaded because OmegaT Version "+OStrings.VERSION+" is lower than required version "+requiredVersion);
                    return;
                }
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                Core.pluginLoadingError("Plugin … cannot be loaded because this OmegaT version is not supported");
                return;
            }
            … register classes and events …
        } catch(Throwable ex) {
            Core.pluginLoadingError(“Plugin … cannot be loaded because this version of OmegaT is not supported”);
        }
    }
The `loadPlugins()` method shouldn't execute any long operations.
The `unloadPlugins()` method executes on application shutdown. Usually, it should be just an empty method, but it can 
be used to free some resources.

## Plugin categories

Plugin manifest has a mandatory entry `Plugin-Category`:

    [Plugin-Category: filter]

You can choose a value from following list. A value is not affected the plugin behavior
but it is used when showing a plugin list on preference dialog.

1. filter
2. tokenizer
3. marker
4. machinetranslator
5. base
6. glossary
7. dictionary
8. miscellaneous

When plugin has a value other than above, OmegaT will show it as 'unknown' category.
The list of values above is a superset of possible values for 'Omegat-Plugin:' field in
OmegaT 2.1.3 above definition.

# Set up your development project
When you develop your plugin, you will extend classes from the OmegaT project, or call methods. To be able to compile 
your project, you need the OmegaT dependencies. You can either stub them, copy them individually, or just include the 
entire OmegaT project `.jar` file in your project. But you have to make sure these files are not part of the .jar 
file that you produce.

## maven example
An example of how to include OmegaT code to your project in a Maven project, without adding it to the compiled .jar file:

    <dependency>
        <groupId>org.omegat</groupId>
        <artifactId>omegat</artifactId>
        <version>5.4.4</version>
        <scope>provided</scope>
    </dependency>
The above example goes in your pom.xml file under `<dependencies>`. The `scope` is the magic here.
But if you want to run OmegaT with your plugin under development, then you'd have to temporary remove `<scope>provided</scope>`.

To produce a `.jar` file, you can use the `maven-jar-plugin` as seen below.
If you used stubs or copied OmegaT `.java` files to your project, you can exclude them from the `.jar` file using the 
`<excludes>` section.
 
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-jar-plugin</artifactId>
                <version>3.2.0</version>
                <configuration>
                    <archive>
                        <index>true</index>
                        <manifest>
                            <addClasspath>true</addClasspath>
                            <addDefaultImplementationEntries>true</addDefaultImplementationEntries>
                            <!-- gives Implementation-Title: $project.name
                                       Implementation-Version: $project.version
                                      [Implementation-Vendor: $project.organization.name]
                            -->
                        </manifest>
                        <manifestEntries>
                            <OmegaT-Plugin>true</OmegaT-Plugin>
                            <Plugin-Description>I describe my plugin here. This plugin does amazing things™</Plugin-Description>
                        </manifestEntries>
                        <manifestSection>
                            <name>org.myorganization.MyFilter</name>
                            <manifestEntries>
                                <OmegaT-Plugin>filter</OmegaT-Plugin>
                            </manifestEntries>
                        </manifestSection>
                    </archive>
                </configuration>
                <executions>
                    <execution>
                        <id>default-jar</id>
                        <phase>package</phase>
                        <goals>
                            <goal>jar</goal>
                        </goals>
                        <configuration>
                            <excludes>
                                <exclude>org.omegat/**</exclude>
                            </excludes>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>

# Implementation
Now you've set up your project, it's time to implement the filter. As said before, you need to extend the `IFilter` 
interface.

Most functions are self-explanatory. There are three functions that are used to actually parse a file:
* `parseFile`: used when OmegaT reads a file to show the texts that need to be translated to the user.
* `translateFile`: used when OmegaT writes translated documents
* `alignFile`: used only for the console mode align function. NB: it has nothing to do with the align function that you 
can see in the OmegaT UI; it is only for **automatic** alignment in console mode. 

All three functions work with a callback. When you implement it, your function must call the callback for each 
text fragment (segment) from a file that need to be translated. NB: this is before OmegaT applies segmentation rules.

## translation
On translation, three properties are involved, as you 
can see in the `ITranslateCallback.getTranslation` function: 
id, source and path.

- ID: some file formats like properties files or key=value files have keys/ids to uniquely identify a segment. 
You see this often in localizing software. The ID is not shown on screen, only the value, the text itself, is.
The ID field is optional. Only use it if your file format has IDs. The translator can see the ID in OmegaT in the 
'segment properties' window.
- source: the actual text that needs translation. This is what the translator sees in the normal translation window in 
OmegaT. NB: if an ID is specified, the source can be empty. 
- path: something that additionally makes a segment unique. The path filed is optional. The only usage so far is in the PO filter. 
  A text can occur multiple times, but with a different 'context', or with a different number for plural alternatives.
  
OmegaT allows to give some source text different alternative translations, based on the ID, source and path, and 
additionally on filename, and optionally on previous/next segments. 
- Filename is known by OmegaT, the filter doesn't need to provide it. 
- previous/next segments are useful to determine the context of a segment, in 'normal' text files where one sentence 
follows another. Other formats, like key=value, do not have relationships between segments, and thus this should not be 
used. 
The previous/next segments are linked by calling `linkPrevNextSegments()` function of the IParseCallback 
interface at the end of processing a file. When translating a file, the previous/next segments are not known yet till 
at the end the segments are linked by calling `linkPrevNextSegments()` function of the ITranslateCallback interface. 
Therefore, another pass is needed. On the second pass you can fetch the correct translations. To indicate the pass, 
call `setPass()`. See for an example the `AbstractFilter`.

## parsing files
For loading files into OmegaT, the `IParseCallback` interface is used, and the functions to call have more arguments than 
you see on `ITranslateCallback.getTranslation`. 
Some are related to bilingual files, to give the existing translation to OmegaT: `translation`, `isFuzzy`, and one of 
the possible 'properties'.
And there is place for comments and for protected parts (a.k.a. tags).

### parsing bilingual files
On parsing files, you can set the translation as it was found in the source, if your file format has it. This translation 
will show in the comments pane and can be automatically filled in as translation (if it is not fuzzy and 
if translation != source or 'Allow translation to be equal to source' is true)

If you mark the translation to be fuzzy, it will only show in the Fuzzy Matches pane and in the comments, but not as 
translation.

Additionally, if you implement the function `isBilingual` returning true, then the filter can be used to read files in 
the `/tm/` folder of the project as external TMs.  

### properties
for each segment, you can add extra properties, which are key=value pairs. 

One of the possible keys is 
`SegmentProperties.COMMENT`. Comments will show on the comment pane. 
There is also a callback function that has the 
comment as separate argument, which is easier if you don't have other properties.

Another property is `SegmentProperties.REFERENCE`, useful for bi-lingual files.
When set to "true", it means that the segment (source+translation) will be used as reference TM (and not added to the
project as a segment to be translated). The PO filter uses this. 

All properties will show on the segment properties pane and can be searched for via the search function.
(and the values also on the comment pane, but layout is not as good as on segment properties pane)

### protected parts
When the file format contains formatting tags, placeholders or anything else you don't want to be altered in any 
way in the translation, you have two options
- your filter replaces the text parts with 'OmegaT tags' before it sends the text to the callback function (and on 
translation: does the reverse after the translation is fetched from the callback function). An OmegaT tag looks like 
`<x#>` (see `PatternConsts.OMEGAT_TAG` for the regex pattern). The HTML and XML filters use this technique. See for 
example `org.omegat.filters2.html2.FilterVisitor.shortcut()` 
- you use the protected parts argument of the callback function. This function exists since OmegaT 3.0.6 and is used by
 Java properties files and PO files for example.
The differences:
- OmegaT tags are more complex to implement. They hide the meaning / original text for the translator. They are  
possibly paired
(i.e. open and close tags, and it can be checked if tag pairs do not partially overlap with other sets 
(e.g. `<a1><b1></a1></b1>` instead of `<a1><b1></b1></a1>`.).
- protected parts are easier to implement. They can show the exact text to the translator, or whatever you want.
In both cases, the tags show greyed to the translator, and depending on the OmegaT config, they can(not) be modified or 
order changed and errors show on 'tools->check issues' command.

The easiest way to specify protected parts is using 
`List<ProtectedPart> protectedParts = 
TagUtil.applyCustomProtectedParts(source, java.util.regex.Pattern.compile("myregularexpression"), null);`    
which will find tags according a regular expression, and the text shown to the translator is the tag text itself 
without modification.

## align file
The `alignFile` function is used when starting OmegaT from the command line using argument `--mode=console-align`.
In this mode, OmegaT will create a TMX file with the source and translation as found by the filter.
The resulting TMX is stored in the `/omegat/` folder under the name `align.tmx`.

The arguments for the callback function are identical to the parse function. `isFuzzy` results in the fuzzy mark to be 
added to the translation.

# Plugin options
OmegaT by default lets the user specify the filename pattern and the encoding for the files 
used by a filter, if the filter does not auto-detect it.
Other options can be programmed in the filter, by implementing `changeOptions()`. You can show dialogs etc 
(using the parent Dialog as parent for your dialogs). Saving the options is handled by OmegaT. You only need to return 
the set op options (key/values).

# Head start
The AbstractFilter class gives you a head start in dealing with many tasks like linking segments. So you better extend 
the AbstractFilter instead of implementing IFilter from scratch.

And if your file format is close to a format of an other filter, or if you need some inspiration, 
then you might want to copy or look at the code of one of the 
other filters and adapt it. You can find the filters under `org.omegat.filters2` and the XML filters under 
`org.omegat.filters3`. 

# Testing
Every good piece of code comes with unit tests. It can be hard to create a test for every function, especially where 
code is relying on other classes, like an instantiated OmegaT project (RealProject), FilterBase, config etc. 
OmegaT source code is not very DependencyInjection ready. The class `org/omegat/filters/TestFilterBase.java` will help 
you set up a suitable test environment, and provides some handy functions to test if the filter extracts the correct 
segments, and if the translation file is what it should look like.  

## debugging and running in OmegaT
To run your plugin, you need to compile a `.jar` file, copy it to the right OmegaT folder (see begin of this document) 
and start OmegaT. 
For debugging and testing, you best write unit tests, and debug by running them.

If you really need to debug in the context of a running OmegaT instance (for some other plugin types this might be 
more relevant), you can 'run' `org.omegat.Main`. Make sure all dependent 3rd party libraries are in the classpath.
Since you did not compile a .jar file, you have to make sure there is a correct META-INF/MANIFEST.MF file
(which is missing if you rely on e.g. maven-jar plugin to generate it for you) 
