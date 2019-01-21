package com.vanniktech.android.javadoc

import com.vanniktech.android.javadoc.extensions.AndroidJavadocExtension
import org.gradle.api.Task
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

class GenerationTest {
    @Rule public ExpectedException expectedException = ExpectedException.none()

    def generation
    def project

    @Before
    void setUp() {
        generation = new Generation()
        project = ProjectBuilder.builder().withName('project').build()

        def manifestFile = new File(project.projectDir, "src/main/AndroidManifest.xml")
        manifestFile.parentFile.mkdirs()
        manifestFile.setText('<manifest package="com.foo.bar"/>')
    }

    @Test
    void testNullProject() throws Exception {
        expectedException.expect(UnsupportedOperationException.class)
        expectedException.expectMessage('Project is null')

        generation.apply(null)
    }

    @Test
    void testThatExtensionIsAdded() {
        project.plugins.apply('com.android.application')
        generation.apply(project)

        assert project.androidJavadoc instanceof AndroidJavadocExtension
        assert project.androidJavadoc.variantFilter instanceof Closure
        assert project.androidJavadoc.taskNameTransformer instanceof Closure
        assert project.androidJavadoc.outputDir instanceof Closure
    }

    @Test
    void testNotAndroidProject() {
        generation.apply(project)

        assert !project.hasProperty("generateReleaseJavadoc")
        assert !project.hasProperty("generateDebugJavadoc")
        assert !project.hasProperty("generateReleaseJavadocJar")
        assert !project.hasProperty("generateDebugJavadocJar")
        assert !project.hasProperty("generateJavadoc")
        assert !project.hasProperty("generateJavadocJar")
    }

    @Test
    void testJavaProject() {
        project.plugins.apply('java')
        generation.apply(project)

        assert !project.hasProperty("generateReleaseJavadoc")
        assert !project.hasProperty("generateDebugJavadoc")
        assert !project.hasProperty("generateReleaseJavadocJar")
        assert !project.hasProperty("generateDebugJavadocJar")
        assert !project.hasProperty("generateJavadoc")
        assert !project.hasProperty("generateJavadocJar")
    }

    @Test
    void testAndroidAppProject() {
        withAndroidAppProject()

        // These tasks are only added after project.afterEvaluated() is called.
        assert !project.hasProperty("generateReleaseJavadoc")
        assert !project.hasProperty("generateDebugJavadoc")
        assert !project.hasProperty("generateReleaseJavadocJar")
        assert !project.hasProperty("generateDebugJavadocJar")

        project.evaluate()

        assert project.generateReleaseJavadoc instanceof Javadoc
        assert project.generateDebugJavadoc instanceof Javadoc
        assert project.generateReleaseJavadocJar instanceof Jar
        assert project.generateDebugJavadocJar instanceof Jar
        assert project.generateJavadoc instanceof Task
        assert project.generateJavadocJar instanceof Task
    }

    @Test
    void testAndroidAppProjectInverseApply() {

        // Apply javadoc plugin first
        generation.apply(project)
        applyAndroidPlugin('com.android.application')

        // These tasks are only added after project.afterEvaluated() is called.
        assert !project.hasProperty("generateReleaseJavadoc")
        assert !project.hasProperty("generateDebugJavadoc")
        assert !project.hasProperty("generateReleaseJavadocJar")
        assert !project.hasProperty("generateDebugJavadocJar")

        project.evaluate()

        assert project.generateReleaseJavadoc instanceof Javadoc
        assert project.generateDebugJavadoc instanceof Javadoc
        assert project.generateReleaseJavadocJar instanceof Jar
        assert project.generateDebugJavadocJar instanceof Jar
        assert project.generateJavadoc instanceof Task
        assert project.generateJavadocJar instanceof Task
    }

    @Test
    void testAndroidLibraryProject() {
        withAndroidLibProject()

        // These tasks are only added after project.afterEvaluated() is called.
        assert !project.hasProperty("generateReleaseJavadoc")
        assert !project.hasProperty("generateDebugJavadoc")
        assert !project.hasProperty("generateReleaseJavadocJar")
        assert !project.hasProperty("generateDebugJavadocJar")

        project.evaluate()

        assert project.generateReleaseJavadoc instanceof Javadoc
        assert project.generateDebugJavadoc instanceof Javadoc
        assert project.generateReleaseJavadocJar instanceof Jar
        assert project.generateDebugJavadocJar instanceof Jar
        assert project.generateJavadoc instanceof Task
        assert project.generateJavadocJar instanceof Task
    }

    @Test
    void filterVariant() {
        withAndroidAppProject()

        project.androidJavadoc.variantFilter { variant ->
            variant.buildType.name == "release"
        }

        project.evaluate()

        assert !project.hasProperty("generateDebugJavadoc")
        assert !project.hasProperty("generateDebugJavadocJar")
        assert project.generateReleaseJavadoc instanceof Javadoc
        assert project.generateReleaseJavadocJar instanceof Jar
        assert project.generateJavadoc instanceof Task
        assert project.generateJavadocJar instanceof Task
    }

    @Test
    void transformTaskName() {
        withAndroidAppProject()

        project.androidJavadoc.taskNameTransformer { variant ->
            "yeah"
        }

        project.evaluate()

        assert !project.hasProperty("generateReleaseJavadoc")
        assert !project.hasProperty("generateDebugJavadoc")
        assert !project.hasProperty("generateReleaseJavadocJar")
        assert !project.hasProperty("generateDebugJavadocJar")
        assert project.generateYeahJavadoc instanceof Javadoc
        assert project.generateYeahJavadocJar instanceof Jar
        assert project.generateJavadoc instanceof Task
        assert project.generateJavadocJar instanceof Task
    }

    private void withAndroidAppProject() {
        applyAndroidPlugin('com.android.application')
        generation.apply(project)
    }

    private void withAndroidLibProject() {
        applyAndroidPlugin('com.android.library')
        generation.apply(project)
    }

    private void applyAndroidPlugin(String plugin) {
        project.plugins.apply(plugin)
        project.android.compileSdkVersion 28
        project.android.buildToolsVersion "28.0.3"
        project.android.defaultConfig {
            minSdkVersion 17
            targetSdkVersion 28
            versionCode 1
            versionName "dev"
        }
    }
}
