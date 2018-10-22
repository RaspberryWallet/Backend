#!/bin/sh
/bin/rm -r ServerHttp/src/main/resources/assets \
&& yarn --cwd "../../JSProjects/raspberry-wallet-frontend/" run build \
&& rsync -a ../../JSProjects/raspberry-wallet-frontend/build/ ServerHttp/src/main/resources/assets/
