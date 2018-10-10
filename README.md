# Backend

```
usage: java -jar Manager.jar [-ktor] [-modules <arg>] [-server <arg>]
       [-sync] [-vertx]
 -ktor            Use Ktor as HTTP Server impl
 -modules <arg>   Modules classes directory path
 -server <arg>    HTTP Server impl
 -sync            Sync bitcoin blockchain
 -vertx           Use VertX as HTTP Server impl
```


## Custom modules
Your `CustomModule.java`
- must implement `io.raspberrywallet.manager.modules.Module interface`
- must be in package `io.raspberrywallet.manager.modules`

Manager loads modules (in `bytecode` format) from `/modules` directory relative to your current directory.
You can change this direcotry with param `-modules /path/to/your/custom/modules/`

In order to compile your `CustomModule.java` source file to bytecode class, execute this command:

`javac -cp Manager/target/Manager-<version>-jar-with-dependencies.jar /path/to/your/CustomModule.java`

then place your generated `CustomModule.class` to `/modules` folder
It will be automatically loaded on startup
```
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
