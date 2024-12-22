package org.structs4java;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.file.DirectoryProperty;

import java.io.File;

public class CompileStructsTask extends DefaultTask {

    ConfigurableFileTree structFiles;
    File outputDirectory;

    @InputFiles
    public ConfigurableFileTree getStructFiles() {
        return structFiles;
    }

    @OutputDirectory
    public File getOutputDirectory() {
        return outputDirectory;
    }

    @TaskAction
    public void compileStructs() {
        for(File f : getStructFiles()) {
            getLogger().info("Found struct file: " + f);
        }
    }
}
