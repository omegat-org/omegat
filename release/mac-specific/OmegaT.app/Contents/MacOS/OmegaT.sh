#!/bin/bash

cd `dirname $0`/Java

#GOOGLE_API_KEY="-Dgoogle.api.key=0123456789A0123456789B0123456789C0123456789D"
../jre/bin/java -Xmx1024m ${GOOGLE_API_KEY} -jar OmegaT.jar
