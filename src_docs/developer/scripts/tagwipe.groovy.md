# tagwipe.groovy

## Feature Realization
This script is a sophisticated utility designed to clean up redundant or unnecessary tags from `.docx` source documents. It works by manipulating the underlying XML structure of the Word document (stored within the `.docx` zip container). It provides a graphical user interface to select the level of cleaning and whether to process all documents or just the current one.

## Key APIs
- `java.util.zip.ZipFile` & `ZipOutputStream`: Used to read from and write to the `.docx` container.
- `project.projectFiles`: To list the files within the OmegaT project.
- `editor.@displayedFileIndex`: To identify the currently active file in the editor (accessing protected field via Groovy).
- `Files.move`: To handle file operations like backing up original files and replacing them with cleaned versions.
- `org.omegat.gui.main.ProjectUICommands.projectReload()`: Triggers a reload of the project so that OmegaT can recognize the changes made to the source files.

## Important Constraints or Limitations
- **Format Specific**: Only works with `.docx` files.
- **Project Reload**: Requires the project to be reloaded after processing, which is handled automatically by the script but takes a moment.
- **Source Modification**: It modifies the files in the project's source directory. While it offers a backup option, users should use it with caution.
- **External Dependency**: This is a Groovy port of a Perl script (`tagwipe.pl`) and implements complex heuristic cleaning rules.
