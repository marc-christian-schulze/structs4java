/*
 * This source file was generated by the Gradle 'init' task
 */
package org.structs4java;

import org.gradle.api.Project;
import org.gradle.api.Plugin;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.plugins.JavaPluginExtension;

import com.android.build.gradle.AppPlugin;
import com.android.build.api.variant.ApplicationAndroidComponentsExtension;


/**
 * A simple 'hello world' plugin.
 */
public class Structs4javaGradlePlugin implements Plugin<Project> {
    public void apply(Project project) {

        TaskProvider<CompileStructsTask> taskProvider = project.getTasks().register("compileStructs", CompileStructsTask.class);

        taskProvider.configure(compileStructs -> {
            compileStructs.outputDirectory.value(project.getLayout().getBuildDirectory().dir("generated/main/java").get());
            compileStructs.structFiles.setDir(project.getLayout().getProjectDirectory().dir("src/main/structs").getAsFile().toString()).include("**/*.structs");
            compileStructs.fileEncoding.set("UTF-8");
            compileStructs.source.set("17");
            compileStructs.target.set("17");
            compileStructs.deleteTempDirectory.set(false);
            compileStructs.writeTraceFiles.set(false);
        });

        if(isAndroidProject(project)) {
            project.getPlugins().withType(AppPlugin.class).configureEach(plugin -> {
                ApplicationAndroidComponentsExtension androidComponents = project.getExtensions().getByType(ApplicationAndroidComponentsExtension.class);
                androidComponents.onVariants(androidComponents.selector().all(), variant -> {
                    variant.getSources().getJava().addGeneratedSourceDirectory(taskProvider, CompileStructsTask::getOutputDirectory);
                });
            });

        } else {
            project.getPlugins().withType(JavaPlugin.class, javaPlugin -> {
                JavaPluginExtension javaExtension = project.getExtensions().getByType(JavaPluginExtension.class);
                SourceSet mainSourceSet = javaExtension.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
                mainSourceSet.getJava().srcDir(taskProvider.get().getOutputDirectory());
            });

            project.afterEvaluate(p -> {
                project.getTasks().withType(JavaCompile.class).configureEach(javaCompile -> {
                    javaCompile.dependsOn("compileStructs");
                    taskProvider.get().classPath.set(javaCompile.getClasspath().getAsPath());
                    //javaCompile.getSource().
                });
            });
        }
    }

    private static boolean isAndroidProject(Project project) {
        return project.getPlugins().withType(AppPlugin.class).size() > 0;
    }

}
