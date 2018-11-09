#!/usr/local/bin/python3

import os
import shutil
import subprocess

while not os.getcwd().lower().endswith("backend"):
    os.chdir("..")

shutil.rmtree("ServerHttp/src/main/resources/assets")
subprocess.call(['yarn', '--cwd', '../../JSProjects/raspberry-wallet-frontend/', 'run', 'build'])
shutil.copytree('../../JSProjects/raspberry-wallet-frontend/build/', 'ServerHttp/src/main/resources/assets/')
