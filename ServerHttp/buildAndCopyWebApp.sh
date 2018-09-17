#!/bin/sh
yarn --cwd "../../JSProjects/raspberry-wallet-frontend/" run build \
& rsync -a ../../JSProjects/raspberry-wallet-frontend/build/ src/main/resources/assets/
