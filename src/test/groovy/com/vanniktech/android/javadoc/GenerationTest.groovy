package com.vanniktech.android.javadoc

import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

class GenerationTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testNullProject() throws Exception {
        expectedException.expect(UnsupportedOperationException.class)
        expectedException.expectMessage("Project is not an Android project")

        new Generation().apply(null)
    }
}
