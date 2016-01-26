package com.vanniktech.android.javadoc

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
    public void setUp() {
        generation = new Generation()
        project = ProjectBuilder.builder().withName('project').build()
    }

    @Test
    public void testNullProject() throws Exception {
        expectedException.expect(UnsupportedOperationException.class)
        expectedException.expectMessage('Project is not an Android project')

        generation.apply(null)
    }

    @Test
    public void testNotAndroidProject() {
        expectedException.expect(UnsupportedOperationException.class)
        expectedException.expectMessage('Project is not an Android project')

        generation.apply(project)
    }

    @Test
    public void testJavaProject() {
        project.plugins.apply('java')

        expectedException.expect(UnsupportedOperationException.class)
        expectedException.expectMessage('Project is not an Android project')

        generation.apply(project)
    }

    @Test
    public void testAndroidAppProject() {
        project.plugins.apply('com.android.application')

        generation.apply(project)
    }


    @Test
    public void testAndroidLibraryProject() {
        project.plugins.apply('com.android.library')

        generation.apply(project)
    }
}
