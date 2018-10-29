# Backend

```
usage: java -jar Manager.jar[-modules <arg>]
 -modules <arg>   Modules classes directory path
```


## Custom modules
Your `CustomModule.java`
- must implement `io.raspberrywallet.manager.modules.Module interface`
- must be in package `io.raspberrywallet.manager.modules.<your_package_name>`

Manager loads modules (in `jar` format) from `/opt/wallet/modules` or specified by `-modules path/to/modules/` relative to your current directory.

In order to compile your `CustomModule.java` source file to bytecode class, execute this command:
`javac -cp Manager/target/Manager-<version>-jar-with-dependencies.jar /path/to/your/CustomModule.java -d modules/`
Then create jars
`jar cvf CustomModule.jar CustomModule.class`
Sign jar
`jarsigner -keystore RaspberryWallet.keystore -signedjar CustomModule.jar CustomModule.jar signModules`
And now it can be loaded on startup

There is also helper script in `Scripts/modules/compileJarSignAndCopyModules.py` that automate everything

```
It will be automatically loaded and verified on startup
```
ℹ[INFO][23:45:55][] Successfully verified module io.raspberrywallet.manager.modules.pin.PinModule
ℹ[INFO][23:45:55][] Successfully verified module io.raspberrywallet.manager.modules.example.ExampleModule
ℹ[INFO][18:12:39][ModuleClassLoader] Loaded 2 modules
ℹ[INFO][18:12:39][] Module {
        name: ExampleModule
        id: io.raspberrywallet.manager.modules.ExampleModule
        description: An example waiting and xoring module to show how things work.
}
ℹ[INFO][18:12:39][] Module {
        name: PinModule
        id: io.raspberrywallet.manager.modules.PinModule
        description: Module that require enter 4 digits code
}
```

Docs: https://docs.google.com/document/d/1wW5mRy51MvwghFcwk7K07LozbIV1sD53q4ejCQhjzFw/edit?usp=sharing

mnemonic code for tests purposes `farm hospital shadow common raw neither pond access suggest army prefer expire`
