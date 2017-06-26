package com.vanniktech.android.javadoc.extensions

import org.gradle.api.Project

/**
 * <p>Created on 23/06/17
 *
 * @author Bastien Paul
 */
public class AndroidJavadocExtension {
    /**
     * Closure used for filter some variant out
     */
    Closure variantFilter = { variant ->
        if (variant) {
            return true
        } else {
            return false
        }
    }

    /**
     * Closure used for customizing the task name generation.
     * <p> It is always following the pattern : generate{{ClosureOutput}.capitalize()}Javadoc
     */
    Closure taskNameTransformer = { variant ->
        return variant.name
    }

    /**
     * Closure used for changing the output folder of the documentation
     */
    Closure outputDir = { Project project ->
        "${project.buildDir}/docs/javadoc/"
    }

}
