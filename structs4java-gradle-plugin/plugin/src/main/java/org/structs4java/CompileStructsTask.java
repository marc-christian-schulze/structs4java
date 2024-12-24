package org.structs4java;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Input;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.GradleException;
import org.structs4java.StructsBatchCompiler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CompileStructsTask extends DefaultTask {

    String structFiles;
    DirectoryProperty outputDirectory = getProject().getObjects().directoryProperty();
    Property<String> fileEncoding = getProject().getObjects().property(String.class);
    Property<String> source = getProject().getObjects().property(String.class);
    Property<String> target = getProject().getObjects().property(String.class);
    Property<Boolean> writeTraceFiles = getProject().getObjects().property(Boolean.class);
    Property<Boolean> deleteTempDirectory = getProject().getObjects().property(Boolean.class);

    @Input
    public Property<String> getFileEncoding() { return fileEncoding; }

    @Input
    public Property<String> getSource() { return source; }

    @Input
    public Property<String> getTarget() { return target; }

    @Input
    public Property<Boolean> getWriteTraceFiles() { return writeTraceFiles; }

    @Input
    public Property<Boolean> getDeleteTempDirectory() { return deleteTempDirectory; }

    @InputFiles
    public String getStructFiles() { // ConfigurableFileTree
        return structFiles;
    }

    @OutputDirectory
    public DirectoryProperty getOutputDirectory() {
        return outputDirectory;
    }

    @TaskAction
    public void compileStructs() {
        StructsBatchCompiler compiler = new StructsBatchCompiler();

        getLogger().debug("deleteTempDirectory = " + getDeleteTempDirectory().get());
        compiler.setDeleteTempDirectory(getDeleteTempDirectory().get());
        compiler.setClassPath("");
        compiler.setBootClassPath("");
        compiler.setSourcePath("");
        getLogger().debug("outputDirectory = " + getOutputDirectory().getAsFile().get().toString());
        compiler.setOutputPath(getOutputDirectory().getAsFile().get().toString());
        getLogger().debug("fileEncoding = " + fileEncoding.get());
        compiler.setFileEncoding(fileEncoding.get());
        getLogger().debug("structFiles = " + structFiles);
        compiler.setStructSourceRoot(structFiles);
        getLogger().debug("writeTraceFiles = " + getWriteTraceFiles().get());
        compiler.setWriteTraceFiles(getWriteTraceFiles().get());
        getLogger().debug("source = " + getSource().get());
        compiler.setSourceVersion(getSource().get());
        getLogger().debug("target = " + getTarget().get());
        compiler.setTargetVersion(getTarget().get());

        if (!compiler.compile()) {
            throw new GradleException("Failed to compile struct files!");
        }

    }
}
