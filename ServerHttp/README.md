# Raspberry HTTP Server

## Usage

start server by invoking
```java
new Server(this).start();
```
You will need to implement `io.raspberrywallet.Manager`
 
## Dependency
### Gradle
**Step 1.** Add it in your root build.gradle at the end of repositories:

```gradle
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

**Step 2.** Add the dependency
```gradle
	dependencies {
	        implementation 'com.github.raspberrywallet:server:0.0.10-alpha'
	}

```
## Maven
**Step 1.** Add the JitPack repository to your build file
```xml
	<repositories>
		<repository>
		    <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
	</repositories>
```

**Step 2.** Add the dependency
```xml
	<dependency>
	    <groupId>com.github.RaspberryWallet</groupId>
	    <artifactId>Server</artifactId>
	    <version>0.0.10-alpha</version>
	</dependency>

```

