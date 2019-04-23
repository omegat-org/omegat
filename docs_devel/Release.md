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


## 4. Tag release revision

```sh
svn copy ^/trunk ^/tags/vX.Y.Z -m "Published version X.Y.Z of OmegaT"
```

Replace `^/trunk` with the appropriate branch if releasing a Stable version.

- [Subversion:
  Tags](http://svnbook.red-bean.com/en/1.7/svn.branchmerge.tags.html)


## 5. Run build job on Azure Devops

1. Go to [OmegaT Builds](https://dev.azure.com/omegat-org/OmegaT/_build)
2. Queue a Release Build, specifying for Branch the tag created earlier,
   e.g. `tags/vX.Y.Z`.

After building, the distfiles will be deployed automatically to the [SourceForge
"Files" area](https://sourceforge.net/projects/omegat/files/).

This will publish all distfiles except for Signed Mac and WebStart.


## 6. Build signed Mac distfile locally, publish

```sh
./gradlew macSignedDistZip
```

Publish to SourceForge Files.


## 7. Set default downloads

Only if a Standard release:

1. Go to [SourceForge Files](https://sourceforge.net/projects/omegat/files/)
2. Navigate to the directory for this release
3. For each platform: click the ⓘ button on the representative download for the
   platform
4. Select the radio button for the platform under the Default Download For label
5. Click Save


## 8. Build WebStart dist locally, publish

Make sure `jwsCodebase` is set correctly e.g. in `local.properties` or on the
command line:

- Latest: `https://omegat.sourceforge.io/webstart-dev/`
- Standard: `https://omegat.sourceforge.io/webstart/`

If you have SourceForge Web credentials set up (see `local.properties.example`),
you can just do:

```sh
./gradlew publishWebstartDev -PjwsCodebase=https://omegat.sourceforge.io/webstart-dev/
```

or

```sh
./gradlew publishWebstart -PjwsCodebase=https://omegat.sourceforge.io/webstart/
```

Otherwise do

```sh
./gradlew installWebstartDist
```

and publish the contents to SourceForge Web manually.


## 9. Announce to News, user group

- [OmegaT News](https://sourceforge.net/p/omegat/news/)
  - [Example](https://sourceforge.net/p/omegat/news/2019/03/omegat-latest-version-415-update-4-released/)
- [User Group](https://groups.yahoo.com/neo/groups/OmegaT/info)
  - [Example](https://groups.yahoo.com/neo/groups/OmegaT/conversations/messages/43871)


## 10. Push new version for version check

```sh
./gradlew publishVersion
```


## 11. Cleanup

- Bump version in `Version.properties`, `changes.txt`
- Move old versions in SourceForge Files into `OldFiles/Releases`
  - Leave one previous version just in case
- If promoting from Latest to Standard, move the old Standard versions to Legacy
- Set fixed bug tickets and implemented RFEs to `closed-fixed`
- Update ticket milestones if necessary
