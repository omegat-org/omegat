# Summary of `AbstractDocumentTask`

The class is in the package, extending Gradle's job.
It provides a foundation for document-related tasks in a Gradle build script. `AbstractDocumentTask`**abstract class**`org.omegat.documentation``DefaultTask`

## Key Features
### 1. **Input File Handling**
- The class defines a task input property , represented by a . `inputFile``Provider<RegularFile>`
- This property allows the task to dynamically manage and validate file input during the build process.
- It is annotated with , which enables Gradle to track the task’s inputs and ensure proper up-to-date checking. `@InputFile`

### 2. **Logging Configuration**
- Includes the method to control logging behavior during task execution. `configureLogging()`
- By default:
    - Standard output and error logs are captured and redirected to the level. `INFO`
    - Explicitly configured logging overrides this behavior. `DEBUG`

- This ensures that logs are clean and concise unless explicitly configured for detailed debugging.

### 3. **Static Compilation**
- The class is annotated with , which is a Groovy feature that:
    - Improves performance.
    - Enforces type safety at compile time.

`@CompileStatic`

## Use Case
The is designed as a **base class** for custom Gradle tasks related to document processing. By extending this class, other tasks can inherit common functionality such as: `AbstractDocumentTask`
- Input file management.
- Predefined logging behavior.

It encourages reusability and adheres to best practices for building custom tasks with the Gradle API.
