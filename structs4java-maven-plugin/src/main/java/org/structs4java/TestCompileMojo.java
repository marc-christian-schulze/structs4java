package org.structs4java;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayList;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@Mojo(name = "testCompile", defaultPhase = LifecyclePhase.GENERATE_TEST_SOURCES, requiresDependencyResolution = ResolutionScope.COMPILE, requiresDependencyCollection = ResolutionScope.COMPILE)
public class TestCompileMojo extends AbstractCompileMojo {

	@Parameter(defaultValue = "${basedir}/src/test/structs")
	private File structsDirectory;

	@Parameter(defaultValue = "${project.build.testOutputDirectory}/structs-gen")
	private File outputDirectory;

	protected File getStructsDirectory() {
		return structsDirectory;
	}

	protected File getOutputDirectory() {
		return outputDirectory;
	}

	protected List<String> getCompileSourceRoots() {
		return Lists.newArrayList(getProject().getTestCompileSourceRoots());
	}

	protected List<String> getClassPath() {
		Set<String> classPath = Sets.newLinkedHashSet();
		classPath.add(getProject().getBuild().getTestSourceDirectory());
		try {
			classPath.addAll(getProject().getTestClasspathElements());
		} catch (DependencyResolutionRequiredException e) {
			throw new RuntimeException(e);
		}
		addDependencies(classPath, getProject().getTestArtifacts());
		classPath.remove(getProject().getBuild().getTestOutputDirectory());
		return newArrayList(filter(classPath, FILE_EXISTS));
	}
}
