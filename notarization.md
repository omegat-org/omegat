# Notes regarding the notarization of the macOS package.

## Current status

We can produce a notarized `OmegaT.app` within a signed disk image, but we need to update the build process.

## Notes
### Signed application tasks
#### `installMacX64SignedDist`
This task produces a signed `OmegaT.app` binary that can be launched without problems.

It also produces an unsigned `OmegaT.app` that requests access to the folder where it is upon launch.

#### `macX64Signed`
This task produces a signed zipped `OmegaT.app` that needs unzipping.

**Warning** Upon unzipping macOS does not recognize `OmegaT.app` as a legit package. It requires a right-click to force its launch.

### Packages for the signed application
#### zipping installMacX64SignedDist
Using `ditto` to zip the package for distribution creates the same outcome as the `macX64Signed` task.

The process zips a signed `OmegaT.app`.

**Warning** Upon unzipping, macOS does not recognize `OmegaT.app` as a legit package. It requires a right-click to force its launch.

#### packaging installMacX64SignedDist as a `dmg` 
Since the `macX64Signed` task cannot produce a valid signed application, we must use the `installMacX64SignedDist` task.

Packaging the signed `OmegaT.app` in a `dmg` results in a signed `OmegaT` that can be launched without problems.

https://developer.apple.com/documentation/xcode/packaging-mac-software-for-distribution

The `dmg` itself can be signed, and the contained `OmegaT.app` can be notarized.

I have a prototype of a working signed `dmg` that contains a notarized `OmegaT.app`. See the end of the document.

### Notarization issues

Some libraries need manual signing. We need to find a way to automatize that.


## Current notarization gradlew process
Our gradlew process for macOS notarization is here:

[omegat.org/build.gradle 952-970](https://github.com/omegat-org/omegat/blob/99d67fba25ffc33477d1e67368619f66c167e5cc/build.gradle#L952)

    tasks.register(notarizeTaskName, Exec) {
        onlyIf {
            conditions([project.hasProperty('macNotarizationUsername'), 'Username for notarization not set'],
                    [exePresent('xcrun'), 'XCode is not present in system.'])
        }
        inputs.files tasks.getByName(signedZipTaskName).outputs.files
        doLast {
            exec {
                // Assuming setup per instructions at
                // https://developer.apple.com/documentation/security/notarizing_your_app_before_distribution/customizing_the_notarization_workflow#3087734
                commandLine 'xcrun', 'altool', '--notarize-app',
                        '--primary-bundle-id', "org.omegat.$version",
                        '--username', project.property('macNotarizationUsername'),
                        '--password', '@keychain:AC_PASSWORD',
                        '--file', inputs.files.singleFile
            }
        }
        dependsOn signedZipTaskName
    }

**Warning** `'xcrun atool` is not used anymore, instead `xcrun notarytool` is used:

https://developer.apple.com/documentation/Security/notarizing-macos-software-before-distribution

[xcrun notarytool use](https://developer.apple.com/documentation/technotes/tn3147-migrating-to-the-latest-notarization-tool)

We will need to update our build process to adapt it to this new requirement.

## Manual process
### Credential information
#### Developer account
An Apple developer account is required.

https://developer.apple.com/account

#### Developer ID certificate
The developer account is used to create a developer ID certificate.

To create a developer ID certificate, either use Xcode or the developer portal:

https://developer.apple.com/help/account/create-certificates/create-developer-id-certificates

Here is a good summary on how to prepare a certificate and create an app specific ID:

https://github.com/JetBrains/compose-multiplatform/blob/master/tutorials/Signing_and_notarization_on_macOS/README.md

**Warning** Summary of how the developer ID certificate can impact users:

https://developer.apple.com/support/developer-id/

#### Team ID
The Team ID is a string that is used in the signing and notarization process.

In Keychain, Team ID is found in the certificate name:
`Developer ID Application: [name of the applicant] (Team ID)`

#### App-specific password
An app-specific password associated to the apple-id is required.

https://support.apple.com/en-us/102654

#### Storing IDs in `local.properties`
The build process uses ID information found in the `local.properties` file.

##### Mac code signing
macCodesignIdentity=[Team ID]

##### Mac notarization
macNotarizationUsername=[Developer ID]

### Code signing process
The `codesign` utility is used to sign the application.

[omegat.org/build.gradle 931-936](https://github.com/omegat-org/omegat/blob/99d67fba25ffc33477d1e67368619f66c167e5cc/build.gradle#L931)

[man codesign](https://ss64.com/mac/codesign.html)

To create the signed version of OmegaT:

`./gradlew installMacX64SignedDist`

That creates an `OmegaT.app` package in `build/install/OmegaT-Mac_x64_Signed`.

### Package notarization process
- Once the application is signed, it can be sent to Apple for notarization.
- Apple checks the validity of the signature process and either refuses the submission or accepts it, in which case the application is notarized.
- When the submission is refused, Apple issues an error message that indicates which file needs extra care.

#### Zipping `OmegaT.app`
The application must be zipped for submission to the notarization process:

    EXPORT_PATH="/path/to/build/"
    APP_PATH="/path/to/build/signedPackage"
    ZIP_PATH="/path/to/build/signedPackage.zip"

    /usr/bin/ditto -c -k --keepParent "$APP_PATH" "$ZIP_PATH"

#### Remark
The signed `OmegaT.app` that is created with `./gradlew installMacX64SignedDist` must be zipped to send it to the Apple notarization process. Once Apple validates `OmegaT.app`, it cannot be modified and cannot be zipped for distribution because once unzipped for use, macOS will not recognize it as a legitimately signed package.

#### Updating our build process
See ["Current notarization gradlew process" above](## Current notarization gradlew process).

Our build process needs to be updated (remplace `altool` above with the appropriate `notarytool` options).

Until it is updated, we will have to run it manually.

### notarytool subcommands
The `notarytool` utility has the following subcommands:
- store-credentials	 Save Notary service credentials to the Keychain. Leave credential options not specified for interactive prompts.
- submit					 Submit an archive to the Notary service
- info						 Get status information for a submission
- wait						 Wait for completion of a previous submission
- history					 Get a list of previous submissions for your team
- log						 	 Retrieve notarization log for a single completed submission

#### Storing credentials in the keychain
Execute `xcrun notarytool store-credentials`  to store the credentials into Keychain so as to not have to enter them in interactive prompts.

`xcrun notarytool store-credentials --apple-id [apple account name] --team-id [cf. above] --password [app specific password for the apple ID]`

The command will ask for a "Profile name", the value that you will use in place of your credentials for tasks that require them:

    Credentials saved to Keychain.
    To use them, specify `--keychain-profile [Profile name]`

#### Manually submit the notarization request

`xcrun notarytool submit --keychain-profile [Profile name] /path/to/build/zippedPackage`

The process issues a submission id.

#### Check the submission validity

The submission id is used with `notarytool` to check the validation status.

`xcrun notarytool info --keychain-profile [Profile name] [submission id]`

#### Identify the issues

if the submission is invalid, check the log to see the errors:

`xcrun notarytool log --keychain-profile [Profile name] [submission id]`

Current errors come in 4 sorts, each has with a documentation link and a platform (x86_64 or arm64) indication.

- *The binary is not signed*

https://developer.apple.com/documentation/security/notarizing_macos_software_before_distribution/resolving_common_notarization_issues#3087721

- *The signature does not include a secure timestamp*

https://developer.apple.com/documentation/security/notarizing_macos_software_before_distribution/resolving_common_notarization_issues#3087733

- *The binary is not signed with a valid Developer ID certificate*

https://developer.apple.com/documentation/security/notarizing_macos_software_before_distribution/resolving_common_notarization_issues#3087721

- *The signature of the binary is invalid*

This one comes with the second pass, since the contents of the signed package has been modified.

https://developer.apple.com/documentation/security/notarizing_macos_software_before_distribution/resolving_common_notarization_issues#3087735

#### Current errors
At the time of writing (4040897eb8c879fa8cd7f2c3ca28c2e99dacb329), the following files show errors from files inside `OmegaT.app/Contents`:

- /Java/lib/grpc-netty-shaded-1.50.2.jar/META-INF/native/libio_grpc_netty_shaded_netty_tcnative_osx_x86_64.jnilib

*The binary is not signed / The signature does not include a secure timestamp* / x86_64

- /Java/lib/grpc-netty-shaded-1.50.2.jar/META-INF/native/libio_grpc_netty_shaded_netty_tcnative_osx_aarch_64.jnilib

*The binary is not signed with a valid Developer ID certificate / The signature does not include a secure timestamp* / arm64

- /Java/lib/hunspell-2.1.2.jar/darwin-aarch64/libhunspell.dylib

*The binary is not signed with a valid Developer ID certificate / The signature does not include a secure timestamp* / arm64

- /Java/lib/hunspell-2.1.2.jar/darwin-x86-64/libhunspell.dylib

*The binary is not signed / The signature does not include a secure timestamp* / x86_64

- /Java/lib/jna-5.14.0.jar/com/sun/jna/darwin-aarch64/libjnidispatch.jnilib

*The binary is not signed with a valid Developer ID certificate / The signature does not include a secure timestamp* / arm64

- /Java/lib/jna-5.14.0.jar/com/sun/jna/darwin-x86-64/libjnidispatch.jnilib

*The binary is not signed / The signature does not include a secure timestamp* / x86_64

- /Java/lib/lz4-java-1.4.1.jar/net/jpountz/util/darwin/x86_64/liblz4-java.dylib

*The binary is not signed / The signature does not include a secure timestamp* / x86_64

- /Java/modules/theme-omegat.jar/com/formdev/flatlaf/natives/libflatlaf-macos-x86_64.dylib

*The binary is not signed with a valid Developer ID certificate* / x86_64

- /Java/modules/theme-omegat.jar/com/formdev/flatlaf/natives/libflatlaf-macos-arm64.dylib

*The binary is not signed with a valid Developer ID certificate* / arm64

- /OmegaT_6.1.0_Beta_Mac_x64_Signed/OmegaT.app/Contents/MacOS/OmegaT",

*The signature of the binary is invalid* / x86_64 / arm64

**This error is generated on a second validation pass**. This is  expected, since the package has been modified.

#### Fixing the issues

All the files within `.jar` packages must be unzipped, appropriately signed, and zipped again.

`codesign --force --verbose --strict --timestamp --sign [Team ID] /path/to/file`

#### Repeat the process until the notarization is validated

Since the package has been modified by the fixes, the original signing is not valid anymore and validation produces an error on `OmegaT.app/Contents/MacOS/OmegaT`.

Sign `OmegaT.app` again.

`codesign --deep --force --sign [Team id] --timestamp --options runtime --entitlements release/mac-specific/java.entitlements /path/to/OmegaT.app`

Except for the `OmegaT` launcher, once the other files are signed, and as long as we do not upgrade the library versions, we can make local copies of the libraries and not bother with the "unzip/sign/rezip" manual process.

### Stapling the notarization

Once the package has been validated, it is notarized by Apple. The associated Gatekeeper ticket must now be stapled to the local package so that it is clearly identified on user machines as having been notarized.

`xcrun stapler staple /path/to/OmegaT.app`

## Packaging `OmegaT.app`

### Package creator

A zipped package cannot be signed. We need to use a different packaging system, otherwise signing and notarizing `OmegaT.app` will be useless.

We can create a signed `dmg` package.

https://developer.apple.com/documentation/xcode/packaging-mac-software-for-distribution#Build-a-disk-image-file

#### Create the disk image file
`hdiutil create -srcFolder [Folder that contains OmegaT.app] -o OmegaT.dmg`

#### Sign the disk image file
`codesign -s [Team id] --timestamp -i [Apple id] OmegaT.dmg`

#### Notarized `OmegaT.app` prototype

The current prototype for a signed `dmg` package of a notarized 6.0.1 `OmegaT.app` is here:

https://traductaire-libre.org/omegat/OmegaT_6.0.1_Notarized.dmg

I used 6.0.1 because the documentation is included. There is a small library upgrade (see https://github.com/omegat-org/omegat/tree/topic/jc/notarization/6.0) but the rest is the same as the released OmegaT 6.0.1.
