# Summary of `TransformationTask`
The class is part of the package and extends the class. It is a Gradle task designed to handle transformations of input files, particularly XML files, using XSLT stylesheets. This class utilizes Saxon, a high-performance XSLT and XQuery processor, to perform transformations. `TransformationTask``org.omegat.documentation``AbstractDocumentTask`
## Key Features
### 1. **XSLT-Based Transformation**
- The primary purpose of the is to transform input XML files into other formats (e.g., XHTML) by applying XSLT stylesheets. `TransformationTask`
- It does this by leveraging Saxon APIs (, , and ) for highly efficient and flexible transformation handling. `Processor``XsltCompiler``XsltTransformer`

### 2. **Customizable Input and Output**
- **Input File**: The property defines the XML file to be transformed. `@InputFile``inputFile`
- **Stylesheet**: The property represents the XSLT file to be used for the transformation. `@InputFile``styleSheetFile`
- **Output File**: The property specifies the destination for the transformed output. `@OutputFile``outputFile`

By managing these files as Gradle properties, the task ensures compatibility with Gradle's up-to-date checking to optimize build performance.
### 3. **Transformation Workflow**
The method outlines the transformation process: `@TaskAction``transform()`
1. **Read Input**: The XML input file is parsed and prepared into a using a configured . `SAXSource``XMLReader`
2. **Compile XSLT**: The XSLT stylesheet is compiled with the help of Saxon's . `XsltCompiler`
3. **Transformation**: The XML source is processed with the XSLT transformer (), and the output is serialized into the specified file. `XsltTransformer`

### 4. **Pre- and Post-Transformation Hooks**
- **`preTransform()`**: A hook method that can be overridden to customize the transformation process, such as setting additional XSLT parameters. It is executed before the transformation.
- **`postTransform()`**: A hook method called after the transformation process completes. This can be used for post-processing the output file.

These hooks allow for extending and specializing the task’s behavior in subclasses.
### 5. **Advanced XML Parsing and Resolution**
- **XMLReader Initialization**:
    - The method creates a instance configured to:
        - Support namespaces and XInclude processing.
        - Allow `DOCTYPE` declarations, while also handling external entities safely.

`initializeXmlReader()``XMLReader`

- **Catalog Resolver**:
    - The method sets up a to handle URI resolution using a catalog file (). `initializeResourceResolver()``ResourceResolverWrappingURIResolver``classpath:/org/xmlresolver/catalog.xml`
    - This is especially useful for resolving external or relative URLs in the context of XML processing.

### 6. **Static Compilation**
- The class is annotated with for:
    - Improved performance by compiling Groovy code statically.
    - Enhanced type safety during build script execution.

`@CompileStatic`

## Use Case
The is a highly reusable Gradle task for developers looking to automate the transformation of XML documents into other formats, such as HTML, using XSLT. It serves as a foundation for more specialized transformation tasks in documentation or other XML-heavy projects, allowing for: `TransformationTask`
- Dynamic input/output handling.
- Extensibility via pre- and post-transformation hooks.
- Advanced URI resolution and XML parsing features.
