#!/usr/bin/env bash


# OmegaT installation script for Linux
# Marc Prior
# Last modified: 2022-08-21


OMTVERSION="OmegaT_x.x.x"

# check whether /opt/omegat/<OmegaT version> exists
# exit if it does

if  [ -d /opt/omegat/$OMTVERSION ] ; then

   echo "$omtversion is already installed"

   exit

else

   # create /opt/omegat and
   # /opt/omegat/<OmegaT version>

   sudo mkdir -p /opt/omegat/$OMTVERSION

   # copy OmegaT files and folders
   # to /opt/omegat/<OmegaT version>

   sudo cp -r ./* /opt/omegat/$OMTVERSION

   cd /opt/omegat/$OMTVERSION

fi


# handling plugins folder

if  [ -d /opt/omegat/plugins ] ; then

   # /opt/omegat/plugins already exists

   echo "/opt/omegat/plugins already exists"

else

   # /opt/omegat/plugins does not exist; create

   sudo mkdir -p /opt/omegat/plugins

fi

   # move all files from /opt/omegat/OMTVERSION 
   # to /opt/omegat/plugins (overwriting identical file names)

   sudo cp -r -f /opt/omegat/$OMTVERSION/plugins /opt/omegat

   sudo rm -r /opt/omegat/$OMTVERSION/plugins

   # create symlink from /opt/omegat/plugins to /opt/omegat/$OMTVERSION/plugins 
   # (overwriting symlinks with same name)

   sudo ln -s -f /opt/omegat/plugins /opt/omegat/$OMTVERSION/plugins


# handling scripts folder

if  [ -d /opt/omegat/scripts ] ; then

   # /opt/omegat/scripts already exists

   echo "/opt/omegat/scripts already exists"

else

   # /opt/omegat/scripts does not exist; create

   sudo mkdir -p /opt/omegat/scripts

fi

   # move all files from /opt/omegat/OMTVERSION 
   # to /opt/omegat/scripts (overwriting identical file names)

   sudo cp -r -f /opt/omegat/$OMTVERSION/scripts /opt/omegat

   sudo rm -r /opt/omegat/$OMTVERSION/scripts

   # create symlink from /opt/omegat/scripts tp /opt/omegat/$OMTVERSION/scripts
   # (overwriting symlinks with same name)

   sudo ln -s -f /opt/omegat/scripts /opt/omegat/$OMTVERSION/scripts


# handling jre folder

if  [ -d /opt/omegat/$OMTVERSION/jre ] ; then

   # user is installing OmegaT with JRE

   # delete old local JRE, if present
   
   sudo rm -d -f -r /opt/omegat/jre

   # move???? jre folder from within application

   sudo cp -r /opt/omegat/$OMTVERSION/jre /opt/omegat

   # create symlink from /opt/omegat/jre to /opt/omegat/$OMTVERSION/jre

   sudo ln -s /opt/omegat/jre /opt/omegat/$OMTVERSION/jre

else

   # user is installing OmegaT without JRE
   # check whether /opt/omegat/jre exists

   if  [ -d /opt/omegat/jre ] ; then

      # /opt/omegat/jre exists,
      # create symlink from /opt/omegat/jre to jre folder within /opt/omegat/$OMTVERSION/jre

      sudo ln -s /opt/omegat/jre /opt/omegat/$OMTVERSION/jre

   else

      # /opt/omegat/jre does not exist,
      # do nothing
      echo

   fi

fi

## symlink /opt/omegat/$OMTVERSION (version just installed) to /opt/omegat/OmegaT-default (default directory)

sudo ln -s -b -T /opt/omegat/$OMTVERSION /opt/omegat/OmegaT-default

# symlink bash OmegaT launch script
# from <OmegaT version> to /usr/local/bin
# copy omegat.desktop configuration file 
# to /usr/share/applications/

sudo ln -s -b /opt/omegat/OmegaT-default/OmegaT /usr/local/bin/omegat

sudo cp omegat.desktop /usr/share/applications/

exit
