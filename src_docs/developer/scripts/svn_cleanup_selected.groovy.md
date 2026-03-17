# svn_cleanup_selected.groovy

## Feature Realization
This script provides a utility to perform an SVN "cleanup" operation. This is particularly useful for OmegaT Team Projects when the local working copy becomes locked due to an interrupted sync or other SVN errors. The script can target the current project directory or allow the user to browse for a different folder.

## Key APIs
- `org.tmatesoft.svn.core.wc.SVNClientManager`: Part of the SVNKit library used by OmegaT to manage SVN operations.
- `project.isProjectLoaded()` & `project.projectProperties.getProjectRoot()`: To automatically identify the current project's path.
- `org.omegat.util.gui.UIThreadsUtil.executeInSwingThread`: Ensures that the cleanup process and UI updates are handled correctly across different threads.

## Important Constraints or Limitations
- **SVNKit Dependency**: Relies on the SVNKit library being available in OmegaT's classpath. This is standard for OmegaT versions that support Team Projects.
- **Scope**: It recursively looks for `.svn` directories and performs the cleanup on each one found.
- **Permissions**: Requires appropriate file system permissions to modify the `.svn` metadata.
