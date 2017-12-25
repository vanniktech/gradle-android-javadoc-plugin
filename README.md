# gradle-android-javadoc-plugin

[![Build Status](https://travis-ci.org/vanniktech/gradle-android-javadoc-plugin.svg?branch=master)](https://travis-ci.org/vanniktech/gradle-android-javadoc-plugin?branch=master)
[![Codecov](https://codecov.io/github/vanniktech/gradle-android-javadoc-plugin/coverage.svg?branch=master)](https://codecov.io/github/vanniktech/gradle-android-javadoc-plugin?branch=master)
[![License](http://img.shields.io/:license-apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

Gradle plugin that generates Java Documentation from an Android Gradle project.

Works with the latest Gradle Android Tools version 3.0.1.

# Set up

**app/build.gradle** or **library/build.gradle**

```groovy
buildscript {
  repositories {
    mavenCentral()
  }
  dependencies {
    classpath 'com.vanniktech:gradle-android-javadoc-plugin:0.2.1'
  }
}

apply plugin: 'com.vanniktech.android.javadoc'
```

Information: [This plugin is also available on Gradle plugins](https://plugins.gradle.org/plugin/com.vanniktech.android.javadoc)

### Snapshots

Can be found [here](https://oss.sonatype.org/#nexus-search;quick~gradle-android-javadoc-plugin). Current one is:

```groovy
classpath 'com.vanniktech:gradle-android-javadoc-plugin:0.2.2-SNAPSHOT'
```

## Get Javadoc

```groovy
./gradlew generateDebugJavadoc
./gradlew generateReleaseJavadoc
```

**HTML reports**

```
<subproject>/build/docs/javadoc/debug/index.html
<subproject>/build/docs/javadoc/release/index.html
```

## Get Javadoc archive

```
./gradlew generateDebugJavadocJar
./gradlew generateReleaseJavadocJar
```

## Customize Plugin

```groovy
androidJavadoc {
  // variantFilter takes a closure that received an Android variant as parameter.
  // Return true to generate javadoc task for this variant, false to do nothing
  // This is the default closure :
  variantFilter { variant ->
    if (variant) {
      return true
    } else {
      return false
    }
  }

  // taskNameTransformer takes a closure to customise the task name.
  // Task name pattern is "generate${taskNameTransformer(variant).capitalize()}Javadoc"
  // This is the default implementation :
  taskNameTransformer { variant ->
    variant.name
  }

  // outputDir return the documentation output dir
  // Default implementation :
  outputDir = { Project project ->
    "${project.buildDir}/docs/javadoc/"
  }
}
```

# License

Copyright (C) 2015 Vanniktech - Niklas Baudy

Licensed under the Apache License, Version 2.0