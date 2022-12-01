# Integration test

We have an integration test that does a time-based stress test of the team feature: it spins up multiple threads
that check out a single team project and concurrently commit changes to it.

See /test-integration/src/org/omegat/core/data/TestTeamIntegration.java for details.

An integration test is test for team project concurrent modification. It doesn't simple
junit test, but looks like 'integration' test.

This test prepare scenario, execute separate JVMs for concurrent updates, then check remote repository data.

Each child process updates own segments with source1..5/0/1/2/3 by values from 1 and more.
Segment source/0 updated each time, but source/1/2/3 updated once per cycle.
After process will be finished, values in tmx should be in right order, i.e. only by increasing order.
That means user will not commit previous translation for other user's segments.

Segment with 'concurrent' source will be modified by all users by values from 1 and more with user's prefix.
Conflicts should be resolved by choose higher value.
After process will be finished, values in 'concurrent' segment should be also increased only.

Each child saves {@code Integer.MAXVALUE} as last translation, but current OmegaT implementation doesn't require
to commit it, see "GIT_CONFLICT=Push failed. Will be synchronized next time."

Note that when using a git repository accessed by the `file://` protocol, gc can cause concurrency issues that
result in an error saving the project and missed sync cycles.
When this happens near the end of the test will fail (seen with JGit 4.8).

This has not been seen when using `git+ssh` protocol. This is unlikely to be a problem in real-world
scenarios, but a workaround for this test is to disable gc on the repo with

```
git config gc.auto 0
git config gc.autodetach false
git config receive.autogc false
```

## Test with Docker and Docker Compose

Docker and docker compose is popular to prepare a pragmatic test environment.
You can find docker configurations `Dockerfile` in `test-integration/docker/`

A test setup requires two containers.
1. server: ssh+git, git https, http content, http-dav and ssh+svn server
   container to provide team repository and mapping
2. client: omegat test instance container to run integration test.

OmegaT project provide `.docker-compose.yml` compose configuration that orchestrate
test setup and execution in one line command.

Server has FOUR individual contents.
1. Git team: ssh+git and git https protocol provide omegat team repository
2. SVN team 1: http-dav provide omegat team repository without authentication
   on http://server/svn/omegat-test.svn
3. SVN team 2: ssh+svn omegat team repository on /home/git/omegat-test.svn
4. Plain HTTP server: http://server/README provide content for mapping

Repositories are independent among each other.

### How to run

WARNING: You should operate all command from project root directory.

Before you start test, you need to build test containers,

```console
docker-compose -f .docker-compose.yml build
```

then you can run with default settings

```console
docker-compose -f .docker-compose.yml up
```

You can stop integration test processes by press Ctrl-C key.

Default duration of execution is 4200 second (= 40 min.)
When you want to specify duration, you can set environment variable
and give docker-compose option to stop servers after test finished.

```console
env DURATION=600 \
  docker-compose -f .docker-compose.yml up \
    --abort-on-container-exit
```

This case specifies duration as 10 min. and will exit all three container when 
integration test finished.

When you want to run the test in CI environment,
and want to get exit code from the test, we can get exit code
by adding an option like;

```console
env DURATION=600 \
  docker-compose -f .docker-compose.yml up \
    --abort-on-container-exit --exit-code-from client
```

### check logs

You can check execution log with docker-compose command.

```console
docker-compose -f .docker-compose.yml logs client
```

When you want to monitor log, you can add `-f` option.

### Stop test setup

You can also stop test setup and remove image from another shell.

```console
docker-compose -f ./docker-compose.yml stop
```

This stop container execution and you can see logs afterword.
This is as same as enter Ctrl-C key in executed shell.

### Clean up resources

After end of tests, images of containers are on disk.
You can clean up resources with a command

```console
docker-compose -f .docker-compose.yml down
```

This clean up container resources.

### repository type

When "TYPE" environment variable is set to "GIT", it tests ssh+git configuration
with a mapping configuration with http repository for source data. 

When set it to "SVN", it tests with "http://svn-server/svn/trunk/repo.svn" setup
with a mapping configuration with http repository for source data.

When set it to "SSH", it tests with "svn+ssh://svn-server/svn/repo.svn" setup
with a mapping configuration with http repository for source data.

When "TYPE" is not set or other than above, it tests with "GIT" configuration as default. 

```console
env DURATION=2400 TYPE=GIT \
  docker-compose -f .docker-compose.yml up \
    --abort-on-container-exit --exit-code-from client
```

```console
env DURATION=2400 TYPE=SVN \
  docker-compose -f .docker-compose.yml up \
    --abort-on-container-exit --exit-code-from client
```

```console
env DURATION=2400 TYPE=SSH \
  docker-compose -f .docker-compose.yml up \
    --abort-on-container-exit --exit-code-from client
```

## Manual execution

The way you do the integration test is:

1. Create a team project.
2. This can be a remote project (GitHub, etc.) or it can be local (file://...).
3. For the latter case, you can use the OmegaT CLI team tool to easily init the project.

### How to run

#### Prepare repository server

You need to prepare repository server where test against.
It can not be empty, but omegat team project will be overwritten by integration test.

#### Simple integration test

To run simple integration test to check update integrity in concurrent edits,
you can run it simply

```console
./gradlew testIntegration -Domegat.test.repo=<URL of repo>.
```

1. Specify the test duration with `-Domegat.test.duration=X` where X is a number of seconds
   (default is 14,400s = 4 hours)
2. Wait
3. Check the result

#### Mapping test

To run with mapping configuration, you can give mapping property to the test.
An integration test prepare a team project with a specified mapping.

```console
./gradlew testIntegration -Domegat.test.repo=<URL of repo> \
 -Domegat.test.map.repo=<URL of mapping target directory> \
 -Domegat.test.map.file=<Filename of mapping target>
```

For example, when you have a repository in GitHub user example repository omegat-test.git
and mapping target `https://example.com/index.html` as `source/index.html`

```console
./gradlew testIntegration -Domegat.test.repo=git@github.com:example/omegat-test.git \
 -Domegat.test.map.repo=https://example.com/ \
 -Domegat.test.map.file=index.html
````

#### Multiple scheme test

Some popular public git repository services such as GitHub and Gitlab support
both ssh+git and git smart http(s) access for the repositories.
You can test a situation that team project members access both protocols.

```console
./gradlew testIntegration -Domegat.test.repo=<URL of repo> -Domegat.test.repo.alt=<another URL of repo>
```

It is worth testing with configuration of both mapping and multiple protocol scheme,
Because OmegaT embeds a repository URL in remote `omegat.project` file when mapping feature is used,
OmegaT v5.7 or before override local access URL scheme by remote URL.
When you clone team project with git+ssh to local work folder, then omegat access team repository,
that is `https` URL in remote `omegat.project`, you are forced to use `https` instead of `git+ssh`.

An integration with a configuration of mapping and multiple protocol scheme will detect a behavior.

### Team repository schema

OmegaT supports git and subversion repository URL.

#### Git URLs

* git@example.com:user/trans.git
* ssh+git://user@example.com/user/trans.git
* https://user:pass@example.net/user/trans.git

#### Git Branches

Automatically detect default branch such as `main` or `master`
It also accept arbitrary branch name.
Integration test does not accept branch name.

#### Subversion URLs

* "svn+ssh://user@svn.example.net/test/"
* "https://example.com/user/trans.svn/trunk/"
* "http://example.com/user/trans.svn/trunk/"
