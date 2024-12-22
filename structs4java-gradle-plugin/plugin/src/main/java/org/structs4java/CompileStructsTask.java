package org.structs4java;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.file.ConfigurableFileTree;

import java.io.File;

public class CompileStructsTask extends DefaultTask {

    private ConfigurableFileTree structFiles;

    @InputFiles
    public ConfigurableFileTree getStructFiles() {
        return structFiles;
    }

    public void setStructFiles(ConfigurableFileTree structFiles) {
        this.structFiles = structFiles;
    }

    @TaskAction
    public void compileStructs() {
        for(File f : getStructFiles()) {
            getLogger().info("Found struct file: " + f);
        }
    }
}
