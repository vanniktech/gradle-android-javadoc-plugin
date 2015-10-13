# gradle-android-javadoc-plugin

[![Build Status](https://travis-ci.org/vanniktech/gradle-android-javadoc-plugin.svg)](https://travis-ci.org/vanniktech/gradle-android-javadoc-plugin)
[![License](http://img.shields.io/:license-apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

Gradle plugin that generates Java Documentation from an Android Gradle project.

# Set up

**app/build.gradle or library/build.gradle**

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

apply plugin: "com.vanniktech.android.javadoc"
```

## Get Javadoc

```groovy
./gradlew generateDebugJavadoc
./gradlew generateReleaseJavadoc
```

**HTML reports**

```
<subproject>/javaDoc/debug/index.html
<subproject>/javaDoc/release/index.html
```

# License

Copyright (C) 2014-2015 Vanniktech - Niklas Baudy

Licensed under the Apache License, Version 2.0