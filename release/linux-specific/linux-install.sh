#/bin/bash

# installation script 2012-04-08

# read version number from changes.txt

VERSION=$(grep -o -m1 "OmegaT\s*[\.0-9]*[0-9 a-z]*" ./changes.txt)

# substitute underlines for spaces in version number

VERSIONUL=${VERSION// /_}

# check whether /opt/omegat/<OmegaT version> exists
# exit if it does

if  [ -d /opt/omegat/$VERSIONUL ] ; then

   echo "$VERSION is already installed"

   exit

else

   # create /opt/omegat and
   # /opt/omegat/<OmegaT version>

   sudo mkdir -p /opt/omegat/$VERSIONUL

   # copy OmegaT files and folders 
   # to /opt/omegat/<OmegaT version>

   sudo cp -r ./* /opt/omegat/$VERSIONUL

   cd /opt/omegat/$VERSIONUL

fi


# handling plugins folder

if  [ -d /opt/omegat/plugins ] ; then

   # /opt/omegat/plugins exists,
   # delete /opt/omegat/<OmegaT version>/plugins

   sudo rm -d -f -r /opt/omegat/$VERSIONUL/plugins

else

   # /opt/omegat/plugins does not exist, 
   # move plugins folder from within application 

   sudo mv /opt/omegat/$VERSIONUL/plugins /opt/omegat

fi

# symlink from /opt/omegat/plugins to plugins folder within OmegaT

sudo ln -s /opt/omegat/plugins /opt/omegat/$VERSIONUL/plugins


# handling jre folder

if  [ -d /opt/omegat/$VERSIONUL/jre ] ; then

   # user is installing OmegaT with JRE
   # deletes old local JRE, if present
   # move jre folder from within application 
   # symlink from /opt/omegat/jre to jre folder within OmegaT

   sudo rm -d -f -r /opt/omegat/jre

   sudo mv /opt/omegat/$VERSIONUL/jre /opt/omegat

   sudo ln -s /opt/omegat/jre /opt/omegat/$VERSIONUL/jre

else

   # user is installing OmegaT without JRE
   # check whether /opt/omegat/jre exists

   if  [ -d /opt/omegat/jre ] ; then

      # /opt/omegat/jre exists, 
      # symlink from /opt/omegat/jre to jre folder within OmegaT

      sudo ln -s /opt/omegat/jre /opt/omegat/$VERSIONUL/jre

   else

      # /opt/omegat/jre does not exist,
      # do nothing
      echo

   fi

fi


# symlink bash OmegaT launch script 
# from <OmegaT version> to /usr/local/bin

sudo ln -s -b /opt/omegat/$VERSIONUL/OmegaT /usr/local/bin/omegat

# symlink Kaptain OmegaT launch script 
# from <OmegaT version> to /usr/local/bin

sudo ln -s /opt/omegat/$VERSIONUL/omegat.kaptn /usr/local/bin/omegat.kaptn


exit
