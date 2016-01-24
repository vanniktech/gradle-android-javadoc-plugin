package com.vanniktech.android.javadoc

import com.android.build.gradle.api.BaseVariant
import org.gradle.api.DomainObjectCollection
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.JavadocMemberLevel

class Generation implements Plugin<Project> {
    @Override
    void apply(final Project project) {
        if (project == null || !project.hasProperty("android")) {
            throw new UnsupportedOperationException("Project is not an Android project")
        }

        if (project.android.hasProperty('applicationVariants')) {
            addJavaTaskToProjectWith(project, (DomainObjectCollection<BaseVariant>) project.android.applicationVariants)
        } else if (project.android.hasProperty('libraryVariants')) {
            addJavaTaskToProjectWith(project, (DomainObjectCollection<BaseVariant>) project.android.libraryVariants)
        } else {
            throw new UnsupportedOperationException("Project is not an Android app nor an Android library module")
        }

        addDeleteJavadocTaskToCleanTaskIn(project)
    }

    private static Task addJavaTaskToProjectWith(final Project project, final DomainObjectCollection<BaseVariant> variants) {
        variants.all { variant ->
            project.task("generate${variant.name.capitalize()}Javadoc", type: Javadoc) {
                title = "Documentation for Android $project.android.defaultConfig.versionName v$project.android.defaultConfig.versionCode"
                description = "Generates Javadoc for $variant.name."
                group = 'Documentation'

                destinationDir = new File(getJavadocFolder(project), variant.baseName)
                source = variant.javaCompile.source

                ext.androidJar = "${project.android.sdkDirectory}/platforms/${project.android.compileSdkVersion}/android.jar"
                classpath = project.files(variant.javaCompile.classpath.files) + project.files(ext.androidJar)


                options.memberLevel = JavadocMemberLevel.PROTECTED
                options.links("http://docs.oracle.com/javase/7/docs/api/")
                options.links("http://developer.android.com/reference/")
                exclude '**/BuildConfig.java'
                exclude '**/R.java'
            }
        }
    }

    private static void addDeleteJavadocTaskToCleanTaskIn(final Project project) {
        project.clean.dependsOn project.task("deleteJavadoc", type: Delete) {
            delete getJavadocFolder(project)
        }
    }

    private static String getJavadocFolder(final Project project) {
        return "${project.getProjectDir()}/javaDoc/"
    }
}
