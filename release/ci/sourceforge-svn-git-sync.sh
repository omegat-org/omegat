# As of November 2015, CloudBees DEV@cloud linux slaves are
# Fedora 17 VMs[1] that do not have the Perl SVN::Core package
# needed for git-svn installed[2]. So we build this in a separate
# job and copy the binaries over and install if necessary.
#
# git-svn clones contain special metadata that is not preserved
# when pushing to remotes, and is not necessarily recreatable
# by re-cloning. Thus we need to treat the git-svn clone itself
# as an artifact to be preserved.
#
# In /private/omegat/ we have a cleaned-up copy of the original
# git-svn clone created and synced by Alex Buloichik. This serves
# as our base, onto which we fetch new revisions. The result of
# this fetching is then tarred and propagated to further builds of
# this job as an artifact, in prev/.
#
# [1] https://documentation.cloudbees.com/docs/dev-at-cloud/Sharing+Files+with+Build+Executors.html
# [2] https://examples.ci.cloudbees.com/job/General/job/fedora-packages/lastSuccessfulBuild/console

if [ ! -d $HOME/perl5 ]; then
	SVNCORE=$(pwd)/svncore.tar.gz
	cd $HOME
	tar -zxf $SVNCORE
	cd -
fi

PATH="$HOME/perl5/bin${PATH+:}${PATH}"; export PATH;
PERL5LIB="$HOME/perl5/lib/perl5${PERL5LIB+:}${PERL5LIB}"; export PERL5LIB;
PERL_LOCAL_LIB_ROOT="$HOME/perl5${PERL_LOCAL_LIB_ROOT+:}${PERL_LOCAL_LIB_ROOT}"; export PERL_LOCAL_LIB_ROOT;
PERL_MB_OPT="--install_base \"$HOME/perl5\""; export PERL_MB_OPT;
PERL_MM_OPT="INSTALL_BASE=$HOME/perl5"; export PERL_MM_OPT;

REPO=omegat-git-svn

# If we have copied the tarred repo from a previous build, use it.
if [ ! -d $REPO ] && [ -f prev/$REPO.tar ]; then
	tar -xf prev/$REPO.tar
fi

# If we still don't have a repo, use the archive.
if [ ! -d $REPO ]; then
	tar -xf /private/omegat/$REPO.tar
fi

cd $REPO
git svn info
# Overwrite the authors file with the latest version from SVN.
curl -L -o authors-new https://sourceforge.net/p/omegat/svn/HEAD/tree/trunk/release/ci/authors.txt?format=raw
mv authors-new authors
git svn fetch
#git push ssh://omegat-jenkins@git.code.sf.net/p/omegat/code
cd ..
tar -cf $REPO.tar $REPO
