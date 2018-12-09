#!/usr/bin/env bash

scp ../../Manager/target/*with-dependencies.jar dietpi@10.7.7.2:/home/dietpi/Manager.jar
scp -r ../../modules dietpi@10.7.7.2:/home/dietpi/
ssh dietpi@10.7.7.2 installwallet