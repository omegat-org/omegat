# issue_provider_sample.groovy

## Feature Realization
This script demonstrates how to extend OmegaT's Quality Assurance (QA) capabilities by registering a custom `IIssueProvider` via script. Once registered, the script-based provider can flag specific translation issues (in this example, missing "bar" when "foo" is present) that will then appear in the standard "Issues" window alongside built-in checks.

## Key APIs
- `org.omegat.gui.issues.IssueProviders.addIssueProvider()`: The core method used to register the custom provider.
- `org.omegat.gui.issues.IIssueProvider`: The interface that the script implements (using a Groovy map-to-interface coercion).
- `org.omegat.gui.issues.SimpleIssue`: A base class for creating individual issue objects.

## Important Constraints or Limitations
- **Registration Lifecycle**: The script must be run once to register the provider. To have the provider always active, the script should be placed in the `application_startup` event folder.
- **Statelessness**: The provider should ideally be stateless and efficient, as it is called frequently during project editing.
- **Manual Removal**: There is no simple GUI way to unregister a script-based provider once it has been added; an application restart or a dedicated unregistering script would be required.
