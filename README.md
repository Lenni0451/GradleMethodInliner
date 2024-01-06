# GradleMethodInliner
A gradle plugin to mark methods for inlining.

## Installation
To use this plugin, you need to add my maven server to your plugin repositories:
```groovy
pluginManagement {
    repositories {
        maven {
            name = "lenni0451 releases"
            url = "https://maven.lenni0451.net/releases"
        }
    }
}
```

After adding the repository, you can add the plugin to your project:
```groovy
plugins {
    id "net.lenni0451.method-inliner" version "x.x.x"
}
```

The plugin will automatically load the required `compileOnly` dependencies.

## Usage
### Inlining methods
To mark a method for inlining, you need to add the `InlineMethod` annotation to it:
```java
@InlineMethod
private static void inlined() {
```

<b>Limitations:</b>
 - The method must be `private`
 - Recursive methods are not supported
 - The inlined method gets removed. Make sure that it is not accessed using reflection or similar
 - Only `INVOKEVIRTUAL` and `INVOKESTATIC` calls are inlined. `INVOKESPECIAL` and `INVOKEINTERFACE` calls are not supported

### Inlining public methods
Inlining public methods is also possible but a bit more limited.\
To inline a public method you need to set `keep` to `true` in the `InlineMethod` annotation:
```java
@InlineMethod(keep = true)
public static void inlined() {
```

<b>Limitations:</b>
 - The method must be `static`
 - Only `INVOKEVIRTUAL` and `INVOKESTATIC` calls are inlined. `INVOKESPECIAL` and `INVOKEINTERFACE` calls are not supported
 - Accessing private fields/methods of the class will result in exceptions at runtime

## Example
Let's say we have the following class:
```java
public class Test {

    public static void main(String[] args) {
        inlined();
        notInlined();
    }

    @InlineMethod
    private static void inlined() {
        System.out.println("Inlined method");
    }

    private static void notInlined() {
        System.out.println("Not inlined method");
    }

}
```
In this case, the `inlined` method will be inlined, but the `notInlined` method will not be inlined.

The compiled bytecode of the class will look like this:
```java
public class Test {

    public static void main(String[] args) {
        System.out.println("Inlined method");
        notInlined();
    }

    private static void notInlined() {
        System.out.println("Not inlined method");
    }

}
```
