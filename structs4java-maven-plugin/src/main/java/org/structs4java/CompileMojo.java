package org.structs4java;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.generator.GeneratorDelegate;
import org.eclipse.xtext.generator.JavaIoFileSystemAccess;
import org.eclipse.xtext.generator.JavaIoFileSystemAccess.IFileCallback;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.IResourceValidator;
import org.eclipse.xtext.validation.Issue;

import com.google.inject.Injector;

@Mojo(name = "compile", defaultPhase = LifecyclePhase.GENERATE_SOURCES, requiresDependencyResolution = ResolutionScope.COMPILE, requiresDependencyCollection = ResolutionScope.COMPILE)
public class CompileMojo extends AbstractMojo {

	@Parameter(defaultValue = "${project}")
	private MavenProject project;

	@Parameter(defaultValue = "${project.build.directory}/structs-gen")
	private File outputDirectory;

	@Parameter(defaultValue = "${basedir}/src/main/structs")
	private File structsDirectory;

	@Parameter
	private String[] includes;

	@Parameter
	private String[] excludes;

	public void execute() throws MojoExecutionException {
		if (includes == null || includes.length == 0) {
			includes = new String[1];
			includes[0] = "**/*.structs";
		}

		if (!structsDirectory.exists()) {
			getLog().info("No struct files at " + structsDirectory);
			return;
		}

		Injector injector = new Structs4JavaDslStandaloneSetupGenerated().createInjectorAndDoEMFRegistration();
		XtextResourceSet resourceSet = injector.getInstance(XtextResourceSet.class);

		loadModelsIntoResourceSet(resourceSet);
		if (resourceSet.getResources().size() == 0) {
			getLog().info("No struct files at " + structsDirectory);
			return;
		}
		validateModels(resourceSet);

		GeneratorDelegate generator = injector.getInstance(GeneratorDelegate.class);
		JavaIoFileSystemAccess fileSystemAccess = injector.getInstance(JavaIoFileSystemAccess.class);
		generateCode(generator, fileSystemAccess, resourceSet);

		project.addCompileSourceRoot(outputDirectory.getAbsolutePath());
	}

	private void loadModelsIntoResourceSet(XtextResourceSet resourceSet) {
		List<String> modelFiles = selectModelFiles();
		for (String modelFile : modelFiles) {
			getLog().info("Loading " + modelFile);
			resourceSet.getResource(URI.createFileURI(modelFile), true);
		}
	}

	private List<String> selectModelFiles() {
		DirectoryScanner scanner = new DirectoryScanner();
		scanner.setBasedir(structsDirectory);
		scanner.setCaseSensitive(true);
		scanner.setIncludes(includes);
		scanner.setExcludes(excludes);
		scanner.scan();
		List<String> files = new ArrayList<String>();
		for (String file : scanner.getIncludedFiles()) {
			files.add(new File(structsDirectory, file).getAbsolutePath());
		}
		return files;
	}

	private void validateModels(XtextResourceSet resourceSet) throws MojoExecutionException {
		Map<URI, List<Issue>> modelIssues = new HashMap<URI, List<Issue>>();
		for (Resource resource : resourceSet.getResources()) {
			if (resource instanceof XtextResource) {
				List<Issue> issues = validateModel((XtextResource) resource);
				if (issues != null && issues.size() > 0) {
					modelIssues.put(resource.getURI(), issues);
				}
			}
		}
		logModelIssues(modelIssues);
		if (modelIssues.size() > 0) {
			throw new MojoExecutionException("Compilation stopped due to model issues.");
		}
	}

	private List<Issue> validateModel(XtextResource resource) {
		IResourceValidator validator = resource.getResourceServiceProvider().getResourceValidator();
		return validator.validate(resource, CheckMode.ALL, CancelIndicator.NullImpl);
	}

	private void logModelIssues(Map<URI, List<Issue>> modelIssues) {
		for (URI modelURI : modelIssues.keySet()) {
			List<Issue> issues = modelIssues.get(modelURI);
			for (Issue issue : issues) {
				getLog().error(String.format("%s (Line %d, Col %d): %s", modelURI.toString(), issue.getLineNumber(),
						issue.getColumn(), issue.getMessage()));
			}
		}
	}

	private void generateCode(GeneratorDelegate generator, JavaIoFileSystemAccess fsa, XtextResourceSet resourceSet) {
		fsa.setOutputPath(outputDirectory.getAbsolutePath());
		fsa.setCallBack(new IFileCallback() {

			public void fileDeleted(File file) {
			}

			public void fileAdded(File file) {
				getLog().info("Generating " + file);
			}
		});

		for (Resource resource : resourceSet.getResources()) {
			generator.doGenerate(resource, fsa);
		}
	}
}
