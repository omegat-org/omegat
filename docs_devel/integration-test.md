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
When this happens near the end of the test the test will fail (seen with JGit 4.8).

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

A test setup requires three containers.
1. keygen: a docker container to create ssh key pair
2. server: ssh+git server container to provide team repository
3. client: omegat test instance container to run integration test.

OmegaT project provide `docker-compose.yml` compose configuration that orchestrate
test setup and execution in one line command, `docker-compose up`

### How to run

```bash
docker-compose up
```

You can stop integration test processes by press Ctrl-C key.

Default duration of execution is 4200 second (= 40 min.)
When you want to specify duration you can set environment variable

```bash
env DURATION=600 docker-compose up
```

This case specifies duration as 10 min.

### check logs

You can check execution log with docker-compose command.
```bash
docker-compose logs client
```

### Stop test setup

You can also stop test setup and remove image from another shell.

```bash
docker-compose stop
```

This stop container execution and you can see logs afterword.
This is as same as enter Ctrl-C key in executed shell.

```bash
docker-compose down
```

This clean up container resources.

## Manual execution

The way you do the integration test is:

1. Create a team project.
2. This can be a remote project (GitHub, etc.) or it can be local (file://...).
3. For the latter case, you can use the OmegaT CLI team tool to easily init the project.

### How to run

```bash
./gradlew testIntegration -Domegat.test.repo=<URL of repo>.
```

1. Specify the test duration with `-Domegat.test.duration=X` where X is a number of seconds
   (default is 14,400s = 4 hours)
2. Wait
3. Check the result
