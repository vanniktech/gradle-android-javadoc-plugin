package com.vanniktech.android.javadoc

import com.android.build.gradle.BasePlugin
import com.android.build.gradle.api.BaseVariant
import com.vanniktech.android.javadoc.extensions.AndroidJavadocExtension
import org.gradle.api.DomainObjectCollection
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.JavadocMemberLevel

import javax.annotation.Nullable

class Generation implements Plugin<Project> {

    private static final String JAVADOC_TASK = "generateJavadoc"
    private static final String JAVADOC_JAR_TASK = "generateJavadocJar"

    Logger logger

    @Override
    void apply(final Project project) {
        if (!project) {
            throw new UnsupportedOperationException("Project is null")
        }
        logger = project.getLogger()

        project.extensions.create("androidJavadoc", AndroidJavadocExtension)

        def hasSubProjects = project.subprojects.size() > 0

        if (hasSubProjects) {
            project.subprojects { subProject ->
                applyPlugin(subProject)
            }
        } else {
            applyPlugin(project)
        }
    }

    private void applyPlugin(Project project) {
        if (project.hasProperty('android')) {
            applyPluginToProject(project)
        } else {
            // Waiting for android plugin to be applied
            project.plugins.whenPluginAdded { Plugin plugin ->
                if (plugin instanceof BasePlugin) {
                    applyPluginToProject(project)
                }
            }
        }
    }

    private void applyPluginToProject(Project project) {
        if (project.android.hasProperty('applicationVariants')) {
            createRootTask(project)
            addJavaTaskToProjectWith(project, (DomainObjectCollection<BaseVariant>) project.android.applicationVariants)
        } else if (project.android.hasProperty('libraryVariants')) {
            createRootTask(project)
            addJavaTaskToProjectWith(project, (DomainObjectCollection<BaseVariant>) project.android.libraryVariants)
        } else {
            // Do not throw exception anymore to support big projects with a lot of modules that not all of them are android.
            logger.info "${project.name} has no application or library variant - plugin is not applied"
        }
    }

    private void createRootTask(Project project) {
        def javadoc
        try {
            javadoc = project.tasks.named(JAVADOC_TASK)
            logger.debug "task $JAVADOC_TASK already exists"
        } catch (Exception ignored) {
            javadoc = project.tasks.register(JAVADOC_TASK) {
                description = "Generates javadoc for ${project.name}"
                group = 'Documentation'
            }
        }

        def javadocJar
        try {
            javadocJar = project.tasks.named(JAVADOC_JAR_TASK)
            logger.debug "task $JAVADOC_JAR_TASK already exists"
        } catch (Exception ignored) {
            javadocJar = project.tasks.register(JAVADOC_JAR_TASK) {
                description = "Generates javadoc for ${project.name}"
                group = 'Documentation'
            }
        }

        javadocJar.configure {
            dependsOn javadoc
        }
    }

    private void addJavaTaskToProjectWith(final Project project, final DomainObjectCollection<BaseVariant> variants) {
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

    private void createTask(final Project project, variant) {
        // Get task's name according to android variant.
        String taskName = genJavadocTaskName(project, variant)

        logger.debug "Create task ${taskName} for project ${project.name}, variant ${variant.name}"
        // We are creating the task with the same name only once. This is a protection if task already exists
        // because the name of the task is configurable and can be the same for several variants.
        if (findTask(project, taskName)) {
            logger.debug "task $taskName already exists"
        } else {
            TaskProvider javadocTask = createJavadocTask(project, variant, taskName)
            TaskProvider javadocArchiveTask = createJavadocArchiveTask(project, variant, genJavadocJarTaskName(project, variant))
            javadocArchiveTask.configure {
                dependsOn javadocTask
            }
            findTask(project, JAVADOC_TASK).configure {
                dependsOn javadocTask
            }
            findTask(project, JAVADOC_JAR_TASK).configure {
                dependsOn javadocArchiveTask
            }
        }
    }

    private TaskProvider createJavadocTask(final Project project, variant, String taskName) {
        project.tasks.register(taskName, Javadoc) {
            title = "Documentation for ${project.name} at version ${project.android.defaultConfig.versionName}"
            description = "Generates javadoc for $variant.name variant."
            group = 'Documentation'

            destinationDir = getJavadocFolder(project, variant)
            source = variant.sourceSets.collect { it.java.sourceFiles }.inject { m, i -> m + i }

            // Fix issue : Error: Can not create variant 'android-lint' after configuration ': library: debugRuntimeElements' has been resolved
            doFirst {
                classpath = project.files(variant.javaCompileProvider.get().classpath.files,
                        project.android.getBootClasspath())
            }

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

    private TaskProvider createJavadocArchiveTask(Project project, variant, String taskName) {
        project.tasks.register(taskName, Jar) {
            description = "Compress javadoc for $variant.name variant."
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
        if (findTask(project, taskName)) {
            logger.debug "task $taskName already exists"
        } else {
            def del = project.tasks.register(taskName, Delete) {
                delete getJavadocFolder(project, variant)
            }
            project.tasks.named("clean").configure {
                dependsOn del
            }
        }
    }

    private static String getBaseTaskName(final Project project, variant) {
        return "${project.androidJavadoc.taskNameTransformer(variant).capitalize()}"
    }

    static String genJavadocTaskName(final Project project, variant) {
        return "generate${getBaseTaskName(project, variant)}Javadoc"
    }

    static String genDeleteTaskName(final Project project, variant) {
        return "delete${getBaseTaskName(project, variant)}Javadoc"
    }

    static String genJavadocJarTaskName(final Project project, variant) {
        return "${genJavadocTaskName(project, variant)}Jar"
    }

    private static File getJavadocFolder(final Project project, variant) {
        return new File("${project.androidJavadoc.outputDir(project)}", "${project.androidJavadoc.taskNameTransformer(variant)}")
    }

    @Nullable
    private TaskProvider findTask(final Project project, String taskName) {
        TaskProvider ret = null
        try {
            ret = project.tasks.named(taskName)
        } catch (Exception ignored) {
            logger.debug ignored.toString()
        }
        return ret
    }
}
