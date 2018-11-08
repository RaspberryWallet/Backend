#!/usr/local/bin/python3

import os
import shutil
import subprocess
from pathlib import Path

modules = ['PinModule', 'ExampleModule', 'AuthorizationServerModule']

# Abs path normalization
projectRoot = Path().absolute()
if '/modules' in str(projectRoot):
    projectRoot = Path(projectRoot.parents[1])

if '/Scripts' in str(projectRoot):
    projectRoot = Path(projectRoot.parents[1])

systemModulesDir = '/opt/wallet/modules'
keyStorePath = str(projectRoot / 'RaspberryWallet.keystore')

if not Path(systemModulesDir).exists():
    password = input('Root Password:')
    subprocess.call('echo %s | sudo -S mkdir -p %s' % (password, systemModulesDir), shell=True)
    subprocess.call('echo %s | sudo -S chown %s %s' % (password, os.environ['USER'], systemModulesDir), shell=True)

# Copy .class files
for moduleName in modules:
    shutil.copy(
        str(projectRoot / 'Manager/target/classes/io/raspberrywallet/manager/modules'
            / moduleName.replace("Module", "").lower() / str(moduleName + '.class'))
        , systemModulesDir)

# Convert .class to .jar files
for fileName in Path(systemModulesDir).iterdir():
    if '.class' in str(fileName):
        subprocess.call(['jar', 'cvf', str(fileName.absolute()).replace(".class", ".jar"), str(fileName.name)],
                        cwd=Path(systemModulesDir))
        os.remove(str(fileName))

# Sign .jar
keystorePassword = input('Keystore Password:')
projectModulesPath = Path(projectRoot / 'modules')
if not projectModulesPath.exists():
    projectModulesPath.mkdir()

for fileName in Path(systemModulesDir).iterdir():
    if '.jar' in str(fileName):
        cmd = 'jarsigner -storepass %s -keystore %s -signedjar %s %s signModules' % (
            keystorePassword, keyStorePath, str(fileName), str(fileName))
        print(cmd)
        subprocess.call(cmd, shell=True)
        shutil.copy(str(fileName), str(projectModulesPath))
