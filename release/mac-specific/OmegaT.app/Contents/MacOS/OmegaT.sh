#!/bin/bash

cd `dirname $0`/Java

#LANGUAGE="-Duser.language=en"
#COUNTRY="-Duser.country=GB"
# Settings to access the Internet behind a proxy
#PROXY_HOST="-Dhttp.proxyHost=192.168.1.1"
#PROXY_PORT="-Dhttp.proxyPort=3128"
# Google Translate v2 API key
#GOOGLE_API_KEY="-Dgoogle.api.key=0123456789A0123456789B0123456789C0123456789D"
# Microsoft Translator credentials
#MS_CLIENT_ID="-Dmicrosoft.api.client_id=xxxxx"
#MS_CLIENT_SECRET="-Dmicrosoft.api.client_secret=xxxxx'

../jre/bin/java -Xdock:name="OmegaT" -Xdock:icon="../../Resources/OmegaT.icns" -Xmx1024m ${LANGUAGE} ${COUNTRY} ${PROXY_HOST} ${PROXY_PORT} ${GOOGLE_API_KEY} ${MS_CLIENT_ID} ${MS_CLIENT_SECRET} -jar OmegaT.jar
