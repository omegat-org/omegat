// :name=hello-script :description=Prints a greeting and project info

import org.omegat.core.events.IProjectEventListener

switch (eventType) {
case IProjectEventListener.PROJECT_CHANGE_TYPE.LOAD:
    console.println("Hello from script! Running acceptance test…")
    console.println("Project name: " + project.projectProperties.projectName)
    console.println("Project source language: " + project.projectProperties.sourceLanguage)
    console.println("Project target language: " + project.projectProperties.targetLanguage)
    break
case IProjectEventListener.PROJECT_CHANGE_TYPE.COMPILE:
    console.println("Compile project")
    break
case IProjectEventListener.PROJECT_CHANGE_TYPE.CLOSE:
    console.println("Bye from script: closing project")
    break
}