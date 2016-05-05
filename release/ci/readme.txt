OmegaT makes use of Jenkins continuous integration services provided by
CloudBees: https://omegat.ci.cloudbees.com/

There are two main functions:

1. Building and testing OmegaT trunk and active branches (nightly builds)

2. Syncing SourceForge Subversion and git repositories

Scripts and data used for these tasks are stored in this directory to serve as
canonical sources. However with the exception of authors.txt they are not used
directly; the scripts are pasted into the job configuration pages in Jenkins.

The git-svn sync grabs the latest version of authors.txt before fetching the
latest changes, so new authors should be added to the authors.txt here as
necessary.

Instead of polling the repositories, builds are triggered by post-commit
hooks. This is because Jenkins's Cross-Site Request Forgery (CSRF) protection
requires an authentication "crumb" on POST requests; SourceForge's webhooks make
POST requests even though a mere GET is sufficient (and this is not
configurable).

As of May 2016, CI functions are maintained by Aaron Madlon-Kay
<aaron@madlon-kay.com>.
