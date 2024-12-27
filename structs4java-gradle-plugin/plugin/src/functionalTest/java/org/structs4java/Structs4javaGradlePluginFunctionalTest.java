/*
 * This source file was generated by the Gradle 'init' task
 */
package org.structs4java;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.io.FileWriter;
import java.nio.file.Files;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.BuildResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

/**
 * A simple functional test for the 'com.github.marc-christian-schulze.structs4java' plugin.
 */
class Structs4javaGradlePluginFunctionalTest {
    @TempDir(cleanup = org.junit.jupiter.api.io.CleanupMode.NEVER)
    File projectDir;

    private File getBuildFile() {
        return new File(projectDir, "build.gradle");
    }

    private File getSettingsFile() {
        return new File(projectDir, "settings.gradle");
    }

    private File getStructSrcDirectory() {
        File dir = new File(projectDir, "src/main/structs");
        dir.mkdirs();
        return dir;
    }

    private File getJavaSrcDirectory() {
        File dir = new File(projectDir, "src/main/java");
        dir.mkdirs();
        return dir;
    }

    private File getStructsFile() {
        return new File(getStructSrcDirectory(), "example.structs");
    }

    private File getInterfaceFile() {
        new File(getJavaSrcDirectory(), "something").mkdirs();
        return new File(getJavaSrcDirectory(), "something/SomeInterface.java");
    }

    @Test void canRunTask() throws IOException {
        writeString(getSettingsFile(), "");
        writeString(getBuildFile(), """
            plugins {
                id('java')
                id('io.github.marc-christian-schulze.structs4java.structs4java-gradle-plugin')
            }
            """);
        writeString(getInterfaceFile(), """
            package something;
            
            interface SomeInterface {
            }  
            """);
        writeString(getStructsFile(), """
            package something;
            
            struct Example implements SomeInterface {
                uint32_t anInt;
            }    
            """);

        // Run the build
        GradleRunner runner = GradleRunner.create();
        runner.forwardOutput();
        runner.withPluginClasspath();
        runner.withArguments("compileJava", "--info");
        runner.withProjectDir(projectDir);
        BuildResult result = runner.build();

        // Verify the result
        assertTrue(result.getOutput().contains("loading structs file"));
        assertTrue(result.getOutput().contains("/example.structs"));
        assertTrue(new File(projectDir + "/build/generated/main/java/something/Example.java").isFile());
        assertTrue(new File(projectDir + "/build/classes/java/main/something/Example.class").isFile());
    }

    private void writeString(File file, String string) throws IOException {
        try (Writer writer = new FileWriter(file)) {
            writer.write(string);
        }
    }
}
