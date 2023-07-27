#!/usr/bin/env bash

OMTVERSION="OmegaT_@VERSION_NUMBER_SUBST@"

# check whether /opt/omegat/<OmegaT version> exists
# exit if it does

if  [ -d /opt/omegat/$OMTVERSION ] ; then

   echo "$omtversion is already installed"

   exit

else

   SOURCE=$(dirname -- "${BASH_SOURCE[0]}")

   # create /opt/omegat and
   # /opt/omegat/<OmegaT version>

   sudo mkdir -p /opt/omegat/$OMTVERSION

   # copy OmegaT files and folders
   # to /opt/omegat/<OmegaT version>

   (cd "${SOURCE}" && sudo cp -r ./* /opt/omegat/$OMTVERSION)

   cd /opt/omegat/$OMTVERSION

fi


# handling plugins folder

if  [ -d /opt/omegat/plugins ] ; then

   # /opt/omegat/plugins exists,
   # delete /opt/omegat/<OmegaT version>/plugins

   sudo rm -d -f -r /opt/omegat/$OMTVERSION/plugins

else

   # /opt/omegat/plugins does not exist,
   # move plugins folder from within application

   sudo mv /opt/omegat/$OMTVERSION/plugins /opt/omegat

fi

# symlink from /opt/omegat/plugins to plugins folder within OmegaT

sudo ln -s /opt/omegat/plugins /opt/omegat/$OMTVERSION/plugins

# handling scripts folder

if  [ -d /opt/omegat/scripts ] ; then

   # /opt/omegat/scripts exists,
   # delete /opt/omegat/<OmegaT version>/scripts

   sudo rm -d -f -r /opt/omegat/$OMTVERSION/scripts

else

   # /opt/omegat/scripts does not exist,
   # move scripts folder from within application

   sudo mv /opt/omegat/$OMTVERSION/scripts /opt/omegat

fi

# symlink from /opt/omegat/scripts to scripts folder within OmegaT

sudo ln -s /opt/omegat/scripts /opt/omegat/$OMTVERSION/scripts

# handling jre folder

if  [ -d /opt/omegat/$OMTVERSION/jre ] ; then

   # user is installing OmegaT with JRE
   # deletes old local JRE, if present
   # move jre folder from within application
   # symlink from /opt/omegat/jre to jre folder within OmegaT

   sudo rm -d -f -r /opt/omegat/jre

   sudo mv /opt/omegat/$OMTVERSION/jre /opt/omegat

   sudo ln -s /opt/omegat/jre /opt/omegat/$OMTVERSION/jre

else

   # user is installing OmegaT without JRE
   # check whether /opt/omegat/jre exists

   if  [ -d /opt/omegat/jre ] ; then

      # /opt/omegat/jre exists,
      # symlink from /opt/omegat/jre to jre folder within OmegaT

      sudo ln -s /opt/omegat/jre /opt/omegat/$OMTVERSION/jre

   else

      # /opt/omegat/jre does not exist,
      # do nothing
      echo

   fi

fi

## symlink just installed version to /opt/omegat/OmegaT-default

sudo ln -s -b -T /opt/omegat/$OMTVERSION /opt/omegat/OmegaT-default

# symlink bash OmegaT launch script
# from <OmegaT version> to /usr/local/bin

sudo ln -s -b /opt/omegat/OmegaT-default/OmegaT /usr/local/bin/omegat

# symlink Kaptain OmegaT launch script
# from <OmegaT version> to /usr/local/bin

sudo ln -s -b /opt/omegat/OmegaT-default/omegat.kaptn /usr/local/bin/omegat.kaptn

sudo chmod +x /usr/local/bin/omegat /usr/local/bin/omegat.kaptn

# install icons
icon_sizes=( 32 128 256 512 )
for size in "${icon_sizes[@]}"
do
	sudo xdg-icon-resource install --novendor --noupdate --mode system --size $size /opt/omegat/OmegaT-default/images/OmegaT.iconset/icon_$size\x$size.png omegat
done
sudo xdg-icon-resource forceupdate --mode system

# install desktop file
sudo xdg-desktop-menu install --novendor --mode system /opt/omegat/OmegaT-default/omegat.desktop

exit
