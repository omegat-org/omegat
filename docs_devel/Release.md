# Release Procedure

## 0. Test

Ensure that `./gradlew check` completes successfully.


## 1. Decide release version

See `/docs_devel/README.txt` for details about versioning scheme.


## 2. Update files with release version

- `/src/org/omegat/Version.properties`
- `/release/changes.txt`
  - Version header (including date!)
  - "[current] vs [previous]" line


## 3. Tag release revision

```sh
svn copy ^/trunk ^/tags/vX.Y.Z -m "Published version X.Y.Z of OmegaT"
```

Replace `^/trunk` with the appropriate branch if releasing a Stable version.

- [Subversion:
  Tags](http://svnbook.red-bean.com/en/1.7/svn.branchmerge.tags.html)


## 4. Run build job on Azure Devops

1. Go to [OmegaT Builds](https://dev.azure.com/omegat-org/OmegaT/_build)
2. Queue a Release Build, specifying for Branch the tag created earlier,
   e.g. `tags/vX.Y.Z`.

After building, the distfiles will be deployed automatically to the [SourceForge
"Files" area](https://sourceforge.net/projects/omegat/files/).

This will publish all distfiles except for Signed Mac and WebStart.


## 5. Build signed Mac distfile locally, publish

```sh
./gradlew macSignedDistZip
```

Publish to SourceForge Files.


## 6. Set default downloads

Only if a Standard release:

1. Go to [SourceForge Files](https://sourceforge.net/projects/omegat/files/)
2. Navigate to the directory for this release
3. For each platform: click the ⓘ button on the representative download for the
   platform
4. Select the radio button for the platform under the Default Download For label
5. Click Save


## 7. Build WebStart dist locally, publish

Make sure `jwsCodebase` is set correctly e.g. in `local.properties`:

- Latest: `https://omegat.sourceforge.io/webstart-dev/`
- Standard: `https://omegat.sourceforge.io/webstart/`

```sh
./gradlew webstartDistZip
```

Publish contents of zip to SourceForge Web.


## 8. Announce to News, user group

- [OmegaT News](https://sourceforge.net/p/omegat/news/)
- [User Group](https://groups.yahoo.com/neo/groups/OmegaT/info)


## 9. Push new version for version check

```sh
./gradlew publishVersion
```


## 10. Cleanup

- Bump version in `Version.properties`, `changes.txt`
- Move old versions in SourceForge Files into `OldFiles/Releases`
