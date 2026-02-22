# Summary of `DocbookHtmlTask`

The class is part of the package and extends the class.
It is specifically designed to process DocBook files and generate HTML output using XSLT transformations. `DocbookHtmlTask``org.omegat.documentation``TransformationTask`

## Key Features
### 1. **DocBook to HTML Transformation**
- The primary purpose of the is to transform DocBook XML documents into chunked HTML files using a predefined XSL stylesheet (). `DocbookHtmlTask``xhtml.xsl`
- It sets up a variety of parameters for the XSLT transformation to control the structure and style of the generated HTML output.

### 2. **Custom Pre-Transformation Logic**
- The `preTransform()` method is overridden to define parameters that customize the XSLT transformation process, such as:
    - Setting the base URI for chunked output.
    - Enabling persistent Table of Contents (TOC) functionality.
    - Controlling HTML extension for output files.
    - Enabling or disabling features like lists of tables and figures.
    - Linking custom CSS for styling the generated HTML files.

### 3. **Parameterization**
The task accepts and configures several XSLT parameters, including:
- `chunk`: Specifies the base HTML file name for chunked output (default: ). `index.html`
- : Sets the base directory for writing chunked HTML files. `chunk-output-base-uri`
- and : Enable generation of lists of figures and tables in the output. `list-of-figures``list-of-tables`
- : Sets the file extension for HTML output files (default: ). `html-extension``.html`
- : Allows linking custom CSS, such as , for styling output files. `user-css-links``css/omegat.css`
- Other parameters to control TOC behavior, page styles, and media-specific output.

### 4. **Enhanced Configurability**
- The `extractRootName()` helper method is available to determine the root name of a file by removing its extension.
- These capabilities make it possible to adapt the task to different contexts and output requirements by simply modifying the input or XSL stylesheet.

## Use Case
The is intended to be used in Gradle build scripts to automate the generation of chunked HTML documentation from DocBook XML sources. It integrates seamlessly with related tasks to build and prepare comprehensive documentation, including: `DocbookHtmlTask`
- Converting XML files into navigable HTML.
- Styling the output with custom CSS.
- Organizing output into separate chunks for enhanced readability and navigation.

This task is especially useful for projects requiring structured and styled documentation output from XML-based source files, leveraging the power of XSLT for transformation and customization.
