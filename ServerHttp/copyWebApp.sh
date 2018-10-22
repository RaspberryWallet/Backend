#!/bin/sh
/bin/rm -r ServerHttp/src/main/resources/assets \
&& rsync -a ../../JSProjects/raspberry-wallet-frontend/build/ ServerHttp/src/main/resources/assets/
