package com.vanniktech.android.javadoc

import com.android.build.gradle.api.BaseVariant
import com.vanniktech.android.javadoc.extensions.AndroidJavadocExtension
import org.gradle.api.DomainObjectCollection
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.JavadocMemberLevel

class Generation implements Plugin<Project> {
    Logger logger;

    @Override
    void apply(final Project project) {
        if (!project) {
            throw new UnsupportedOperationException("Project is null")
        }
        logger = project.getLogger()

        project.extensions.create("androidJavadoc", AndroidJavadocExtension)

        project.allprojects {
            afterEvaluate { Project p ->
                // Do not throw exception anymore. We want to support big projects with a lot of modules that not all of them are android.
                if (p.hasProperty('android')) {
                    applyPluginToProject(p);
                } else {
                    logger.info "${p.name} is not an android project - plugin is not applied"
                }
            }
        }
    }

    private void applyPluginToProject(Project project) {
        if (project.android.hasProperty('applicationVariants')) {
            addJavaTaskToProjectWith(project, (DomainObjectCollection<BaseVariant>) project.android.applicationVariants)
        } else if (project.android.hasProperty('libraryVariants')) {
            addJavaTaskToProjectWith(project, (DomainObjectCollection<BaseVariant>) project.android.libraryVariants)
        } else {
            // Do not throw exception anymore to support big projects with a lot of modules that not all of them are android.
            logger.info "${project.name} has no application or library variant - plugin is not applied"
        }
    }

    private Task addJavaTaskToProjectWith(final Project project, final DomainObjectCollection<BaseVariant> variants) {
        variants.all { variant ->
            // Apply a filter because javadoc could be configured for only some particular variants.
            if (project.androidJavadoc.variantFilter(variant)) {
                createTask(project, variant)
                addDeleteJavadocTaskToCleanTaskIn(project, variant)
            } else {
                logger.debug "Do not create task for ${variant.name} because it has been filtered out"
            }
        }
    }

    private Task createTask(final Project project, variant) {
        // Get task's name according to android variant.
        String taskName = genJavadocTaskName(project, variant)

        logger.debug "Create task ${taskName} for project ${project.name}, variant ${variant.name}"
        // protection if a task already exists because the name of the task is configurable
        if (project.tasks.findByPath(taskName)) {
            logger.debug "task $taskName already exists"
            return project.tasks.findByPath(taskName)
        } else {
            Task t1 = createJavadocTask(project, variant, taskName)
            Task t2 = createJavadocArchiveTask(project, variant, genJavadocJarTaskName(project, variant))
            t2.dependsOn(t1)
        }
    }

    private Task createJavadocTask(final Project project, variant, String taskName) {
        project.task(taskName, type: Javadoc) {
            title = "Documentation for ${project.name} at version ${project.android.defaultConfig.versionName}"
            description = "Generates Javadoc for $variant.name."
            group = 'Documentation'

            destinationDir = getJavadocFolder(project, variant)
            source = variant.javaCompiler.source

            ext.androidJar = "${project.android.sdkDirectory}/platforms/${project.android.compileSdkVersion}/android.jar"
            classpath = project.files(variant.javaCompiler.classpath.files) + project.files(ext.androidJar)

            if (JavaVersion.current().isJava8Compatible()) {
                options.addStringOption('Xdoclint:none', '-quiet')
            }
            options.memberLevel = JavadocMemberLevel.PROTECTED
            options.links("http://docs.oracle.com/javase/7/docs/api/")
            options.links("http://developer.android.com/reference/")
            exclude '**/BuildConfig.java'
            exclude '**/R.java'
        }
    }

    private Task createJavadocArchiveTask(Project project, variant, String taskName) {
        project.task("${taskName}", type: Jar) {
            description = "Compress Javadoc for $variant.name."
            group = "Documentation"
            classifier = 'javadoc'
            from getJavadocFolder(project, variant)
            into ""
            baseName = "${project.name}-${project.android.defaultConfig.versionName}-${getBaseTaskName(project, variant)}"
            extension = "jar"
            manifest = null
        }
    }

    private void addDeleteJavadocTaskToCleanTaskIn(final Project project, variant) {
        String taskName = genDeleteTaskName(project, variant)
        if (project.tasks.findByPath(taskName)) {
            logger.debug "task $taskName already exists"
        } else {
            project.clean.dependsOn project.task(taskName, type: Delete) {
                delete getJavadocFolder(project, variant)
            }
        }
    }

    private static String getBaseTaskName(final Project project, variant) {
        return "${project.androidJavadoc.taskNameTransformer(variant).capitalize()}"
    }

    private static String genJavadocTaskName(final Project project, variant) {
        return "generate${getBaseTaskName(project, variant)}Javadoc"
    }

    private static String genDeleteTaskName(final Project project, variant) {
        return "delete${getBaseTaskName(project, variant)}Javadoc"
    }

    private static String genJavadocJarTaskName(final Project project, variant) {
        return "${genJavadocTaskName(project, variant)}Jar"
    }

    private static File getJavadocFolder(final Project project, variant) {
        return new File(project.androidJavadoc.outputDir(project), getBaseTaskName(project, variant))
    }
}
