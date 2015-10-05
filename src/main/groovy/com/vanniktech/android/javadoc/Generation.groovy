package com.vanniktech.android.javadoc

import com.vanniktech.android.javadoc.app.GenerationApp
import com.vanniktech.android.javadoc.library.GenerationLibrary
import org.gradle.api.Plugin
import org.gradle.api.Project;

class Generation implements Plugin<Project> {
    @Override
    void apply(final Project project) {
        if (project == null || project.android == null) {
            throw new UnsupportedOperationException("Project is not an Android project")
        }

        if (project.android.applicationVariants != null) {
            new GenerationApp().apply(project)
        } else if (project.android.libraryVariants != null) {
            new GenerationLibrary().apply(project)
        } else {
            throw new UnsupportedOperationException("Project is not an Android app nor an Android library module")
        }
    }

    static String getJavadocFolder(Project project) {
        return "${project.getProjectDir()}/javaDoc/";
    }
}
