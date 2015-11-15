# As of November 2015, CloudBees DEV@cloud linux slaves are
# Fedora 17 VMs[1] that do not have the Perl SVN::Core package
# needed for git-svn installed[2]. So we have to build this
# package ourselves and provide it for any jobs that use
# git-svn.
# [1] https://documentation.cloudbees.com/docs/dev-at-cloud/Jenkins+Build+Machine+Specifications.html
# [2] https://examples.ci.cloudbees.com/job/General/job/fedora-packages/lastSuccessfulBuild/console

# Install Adobe Portable Runtime
if [ ! -d ~/apr ]; then
	curl -O http://ftp.riken.jp/net/apache/apr/apr-1.5.2.tar.gz
	tar -zxf apr-1.5.2.tar.gz
	cd apr-1.5.2
	./configure --prefix=$HOME/apr
	make
	#make test
	make install
	cd ..
fi

# Install APR-Util
if [ ! -d ~/apr-util ]; then
	curl -O http://ftp.riken.jp/net/apache/apr/apr-util-1.5.4.tar.gz
	tar -zxf apr-util-1.5.4.tar.gz
	cd apr-util-1.5.4
	./configure --prefix=$HOME/apr-util --with-apr=$HOME/apr
	make
	#make test
	make install
	cd ..
fi

# Install SQLite amalgamation
if [ ! -d $HOME/sqlite ]; then
    curl -O https://www.sqlite.org/sqlite-amalgamation-3071501.zip
    unzip -o sqlite-amalgamation-3071501.zip
    mv sqlite-amalgamation-3071501 $HOME/sqlite
fi

# Install SWIG
if [ ! -d $HOME/swig ]; then
    curl -L -O https://downloads.sourceforge.net/project/swig/swig/swig-1.3.40/swig-1.3.40.tar.gz
    tar -zxf swig-1.3.40.tar.gz
	cd swig-1.3.40
    ./configure --with-perl5=/usr/bin/perl --prefix=$HOME/swig --without-java --without-python --without-php --without-ocaml --without-lua --without-r
    make
    make install
    cd ..
fi

if [ ! -d ~/.cpan ]; then
    # Init CPAN
    cpan <<EOF



EOF
fi

# Set environment
PATH="$HOME/perl5/bin${PATH+:}${PATH}"; export PATH;
PERL5LIB="$HOME/perl5/lib/perl5${PERL5LIB+:}${PERL5LIB}"; export PERL5LIB;
PERL_LOCAL_LIB_ROOT="$HOME/perl5${PERL_LOCAL_LIB_ROOT+:}${PERL_LOCAL_LIB_ROOT}"; export PERL_LOCAL_LIB_ROOT;
PERL_MB_OPT="--install_base \"$HOME/perl5\""; export PERL_MB_OPT;
PERL_MM_OPT="INSTALL_BASE=$HOME/perl5"; export PERL_MM_OPT;

# Force install old ExtUtils::Embed in order
# to resolve swig-pl-lib includes
cpan -f -i DOUGM/ExtUtils-Embed-1.14.tar.gz

CONFIG_ARGS="--libdir=$HOME/perl5/lib/perl5/x86_64-linux-thread-multi/Alien/SVN --prefix=$HOME/perl5 PERL=/usr/bin/perl --with-apr=$HOME/apr --with-apr-util=$HOME/apr-util --with-sqlite=$HOME/sqlite/sqlite3.c --with-swig=$HOME/swig"

cpan SVN::Core <<EOF


$CONFIG_ARGS
EOF

# Zip up SVN::Core installation for achiving
cd $HOME
tar -cvzhf svncore.tar.gz .cpan perl5 apr apr-util sqlite swig
cd -
mv $HOME/svncore.tar.gz ./
