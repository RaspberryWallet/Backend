#!/bin/sh
javac -cp Manager/target/Manager-1.0-jar-with-dependencies.jar:Manager/src/main/java Manager/src/main/java/io/raspberrywallet/manager/modules/ExampleModule.java Manager/src/main/java/io/raspberrywallet/manager/modules/PinModule.java \
&& mv Manager/src/main/java/io/raspberrywallet/manager/modules/ExampleModule.class modules/ \
&& mv Manager/src/main/java/io/raspberrywallet/manager/modules/PinModule.class modules/
