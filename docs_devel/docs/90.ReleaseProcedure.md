# Release Procedure

## 0. Pre-announce on omegat-dev-tech mailing list

Especially if there is new translatable material, give the L10N team some lead
time to update translations.


## 1. Test

Ensure that `./gradlew check` completes successfully.


## 2. Decide release version

See `/docs_devel/README.txt` for details about versioning scheme.


## 3. Update files with release version

- `/src/org/omegat/Version.properties`
- `/release/changes.txt`
  - Version header (including date!)
  - "[current] vs [previous]" line


## 4. Update manuals

Run the following, then commit any changes:

```sh
./gradlew updateManuals
```


## 5. Tag release revision

```sh
git tag vX.Y.Z
git push origin vX.Y.Z
```

## 6. Update bundled JREs

See if an update is available for the bundled JREs, and update
[jre-prep-ci](https://github.com/omegat-org/jre-prep-ci) if necessary.

Make sure the JRE Prep build on Azure is caught up before proceeding.


## 7. Run build job on Azure DevOps

1. Go to [OmegaT Builds](https://dev.azure.com/omegat-org/OmegaT/_build)
2. Queue a Release Build, specifying for Branch the tag created earlier,
   e.g. `refs/tags/vX.Y.Z`.

After building, the distfiles will be deployed automatically to the [SourceForge
"Files" area](https://sourceforge.net/projects/omegat/files/).

This will publish all distfiles except for Signed Mac and WebStart.


## 8. Build notarized Mac distfile locally, publish

First make sure the local JRE is up to date.

Sign and submit binary to Apple:

```sh
./gradlew macNotarize
```

When the confirmation email arrives, do:

```sh
./gradlew macStapledNotarizedDistZip
```

Publish to SourceForge Files.


## 9. Set default downloads

Only if a Standard release:

1. Go to [SourceForge Files](https://sourceforge.net/projects/omegat/files/)
2. Navigate to the directory for this release
3. For each platform: click the ⓘ button on the representative download for the
   platform
4. Select the radio button for the platform under the Default Download For label
5. Click Save


## 10. Publish the manual and Javadoc

```sh
./gradlew publishManual publishJavadoc
```


## 11. Publish to Maven Central

```sh
./gradlew publish
```

Then log onto [Sonatype Nexus](https://s01.oss.sonatype.org/) and publish the
release.


## 12. Announce to News, user group

- [OmegaT News](https://sourceforge.net/p/omegat/news/)
  - [Example](https://sourceforge.net/p/omegat/news/2019/11/omegat-latest-version-510-released/)
- [User Group](https://omegat.org/support)
  - [Example](https://sourceforge.net/p/omegat/mailman/omegat-users/thread/CAHvKJZsm4ZSOmvCOpfbtss0z9uo0z7q--bDowRkyAQ5e2zNJJg%40.../#msg36855627)


## 13. Cleanup

- Bump version in `Version.properties`, `changes.txt`
- Set fixed bug tickets and implemented RFEs to `closed-fixed`
- Update ticket milestones if necessary

Note: Don't "clean up" old releases by moving them out of the way. It's
important that distfile URLs remain stable.

## 14. Push new version for version check

If no catastrophic problems are reported with the new version, once the
[website](https://github.com/omegat-org/omegat-website/) has been updated, bump
the version check master file:

```sh
./gradlew publishVersion
```

Consider opening a ticket on the website to coordinate timing.
