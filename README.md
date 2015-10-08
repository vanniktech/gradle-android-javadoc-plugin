# gradle-android-javadoc-plugin

Gradle plugin that generates Java Documentation from Android Gradle project.

# Set up

## Root build.gradle

```groovy
buildscript {
    repositories {
        maven {
            url "https://plugins.gradle.org/m2/"
        }
    }
    dependencies {
        classpath "gradle.plugin.com.vanniktech:gradle-android-javadoc-plugin:0.2.1"
    }
}
```

## Android or Library build.gradle

```groovy
apply plugin: "com.vanniktech.android.javadoc"
```

## Javadoc

```groovy
./gradlew generateDebugJavadoc
./gradlew generateReleaseJavadoc
```

# License

Copyright (C) 2014-2015 Vanniktech - Niklas Baudy

Licensed under the Apache License, Version 2.0