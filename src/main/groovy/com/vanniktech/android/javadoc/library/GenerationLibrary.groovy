package com.vanniktech.android.javadoc.library

import com.vanniktech.android.javadoc.Generation
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.javadoc.Javadoc

class GenerationLibrary implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.android.libraryVariants.all { variant ->
            project.task("generate${variant.name.capitalize()}Javadoc", type: Javadoc) {
                title = "Documentation for Android Library module $project.android.defaultConfig.versionName v$project.android.defaultConfig.versionCode"
                description "Generates Javadoc for $variant.name."

                destinationDir = new File(Generation.getJavadocFolder(project), variant.baseName)
                source = variant.javaCompile.source

                ext.androidJar = "${project.android.sdkDirectory}/platforms/${project.android.compileSdkVersion}/android.jar"
                classpath = project.files(variant.javaCompile.classpath.files) + project.files(ext.androidJar)


                options.memberLevel = org.gradle.external.javadoc.JavadocMemberLevel.PROTECTED
                options.links("http://docs.oracle.com/javase/7/docs/api/")
                options.links("http://developer.android.com/reference/")
                exclude '**/BuildConfig.java'
                exclude '**/R.java'
            }
        }

        project.clean.dependsOn project.task("deleteJavadoc", type: Delete) {
            delete Generation.getJavadocFolder(project)
        }
    }
}