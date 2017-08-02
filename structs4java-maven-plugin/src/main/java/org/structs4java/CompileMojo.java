package org.structs4java;

import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.name;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayList;
import static org.eclipse.xtext.util.Strings.concat;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.toolchain.Toolchain;
import org.apache.maven.toolchain.ToolchainManager;
import org.apache.maven.toolchain.java.DefaultJavaToolChain;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.common.util.WrappedException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtend.core.compiler.batch.XtendBatchCompiler;
//import org.eclipse.xtext.common.types.access.impl.TypeResourceServices;
import org.eclipse.xtext.diagnostics.Severity;
//import org.eclipse.xtext.generator.GeneratorDelegate;
import org.eclipse.xtext.generator.JavaIoFileSystemAccess;
//import org.eclipse.xtext.generator.JavaIoFileSystemAccess.IFileCallback;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.IResourceValidator;
import org.eclipse.xtext.validation.Issue;
import org.structs4java.structs4JavaDsl.StructDeclaration;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;

@Mojo(name = "compile", defaultPhase = LifecyclePhase.GENERATE_SOURCES, requiresDependencyResolution = ResolutionScope.COMPILE, requiresDependencyCollection = ResolutionScope.COMPILE)
public class CompileMojo extends AbstractMojo {

	@Component
	private MavenProject project;
	
	@Component
	private MavenSession mavenSession;
	
	@Component
	private BuildPluginManager pluginManager;
	

	@Parameter(defaultValue = "${project.build.directory}/structs-gen")
	private File outputDirectory;

	@Parameter(defaultValue = "${basedir}/src/main/structs")
	private File structsDirectory;

	@Parameter
	private String[] includes;

	@Parameter
	private String[] excludes;
	
	@Inject
	protected MavenLog4JConfigurator log4jConfigurator;

	/**
	 * @parameter expression="${maven.compiler.source}" default-value="1.6"
	 */
	private String compilerSourceLevel;

	/**
	 * @parameter expression="${maven.compiler.target}" default-value="1.6"
	 */
	private String compilerTargetLevel;

	private Injector injector;
	
	public CompileMojo() {
		injector = new MavenStructs4JavaDslStandaloneSetupGenerated().createInjectorAndDoEMFRegistration();
		injector.injectMembers(this);
	}

	
//	private URLClassLoader createClassLoader() throws MojoExecutionException {
//		List<String> classpathElements = null;
//		try {
//			classpathElements = project.getCompileClasspathElements();
//			List<URL> projectClasspathList = new ArrayList<URL>();
//			for (String element : classpathElements) {
//				System.out.println("Classpath: " + element);
//				try {
//					projectClasspathList.add(new File(element).toURI().toURL());
//				} catch (MalformedURLException e) {
//					throw new MojoExecutionException(element + " is an invalid classpath element", e);
//				}
//			}
//
//			return new URLClassLoader(projectClasspathList.toArray(new URL[0]),
//					Thread.currentThread().getContextClassLoader());
//
//		} catch (DependencyResolutionRequiredException e) {
//			throw new MojoExecutionException("Dependency resolution failed", e);
//		}
//	}
//	
//	private void compileStubs() throws MojoExecutionException {
//		executeMojo(
//			    plugin(
//			        groupId("org.apache.maven.plugins"),
//			        artifactId("maven-compiler-plugin"),
//			        version("3.6.1")
//			    ),
//			    goal("compile"),
//			    configuration(
//			        element(name("failOnError"), "false")
//			    ),
//			    executionEnvironment(
//			        project,
//			        mavenSession,
//			        pluginManager
//			    )
//			);
//		
//		executeMojo(
//			    plugin(
//			        groupId("org.apache.maven.plugins"),
//			        artifactId("maven-compiler-plugin"),
//			        version("3.6.1")
//			    ),
//			    goal("testCompile"),
//			    configuration(
//			    		element(name("failOnError"), "false")
//			    ),
//			    executionEnvironment(
//			        project,
//			        mavenSession,
//			        pluginManager
//			    )
//			);
//	}
	
//	private void configureCompiler(IJavaCompiler compiler) {
//		CompilerConfiguration conf = compiler.getConfiguration();
//		conf.setSourceLevel(compilerSourceLevel);
//		conf.setTargetLevel(compilerTargetLevel);
//		conf.setVerbose(getLog().isDebugEnabled());
//	}

	public void execute() throws MojoExecutionException {
		if (includes == null || includes.length == 0) {
			includes = new String[1];
			includes[0] = "**/*.structs";
		}

		if (!structsDirectory.exists()) {
			getLog().info("No struct files at " + structsDirectory);
			return;
		}
		
		log4jConfigurator.configureLog4j(getLog());
		
		//compileStubs();

//		Injector injector = new MavenStructs4JavaDslStandaloneSetupGenerated().createInjectorAndDoEMFRegistration();
//		XtextResourceSet resourceSet = injector.getInstance(XtextResourceSet.class);

		//TypeResourceServices typeResourceServices = injector.getInstance(TypeResourceServices.class);
		
		
		//EclipseJavaCompiler compiler = new EclipseJavaCompiler();
		//System.out.println("Compiler: " + compiler);

//		ClassLoader classLoader = createClassLoader();
//
//		resourceSet.setClasspathURIContext(classLoader);
//		resourceSet.addLoadOption(XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE);
//
//		loadModelsIntoResourceSet(resourceSet);
//		if (resourceSet.getResources().size() == 0) {
//			getLog().info("No struct files at " + structsDirectory);
//			return;
//		}
//
//		// IJvmTypeProvider.Factory factory = new
//		// ClasspathTypeProviderFactory(classLoader, typeResourceServices);
//		// IJvmTypeProvider typeProvider =
//		// factory.createTypeProvider(resourceSet);
//
//		validateModels(resourceSet);
//
//		GeneratorDelegate generator = injector.getInstance(GeneratorDelegate.class);
//		JavaIoFileSystemAccess fileSystemAccess = injector.getInstance(JavaIoFileSystemAccess.class);
//		generateCode(generator, fileSystemAccess, resourceSet);
//
//		project.addCompileSourceRoot(outputDirectory.getAbsolutePath());
		
		
		// ****************
		
//		if(!outputDirectory.exists()) {
//			outputDirectory.mkdirs();
//		}
		
		//outputDirectory = resolveToBaseDir(outputDirectory);
		List<String> compileSourceRoots = Lists.newArrayList(project.getCompileSourceRoots());
		compileSourceRoots.remove(outputDirectory);
		String classPath = concat(File.pathSeparator, getClassPath());
		project.addCompileSourceRoot(outputDirectory.toString());
		compile(classPath, compileSourceRoots, structsDirectory.toString(), outputDirectory.toString());
	}

	@SuppressWarnings("deprecation")
	protected List<String> getClassPath() {
		Set<String> classPath = Sets.newLinkedHashSet();
		classPath.add(project.getBuild().getSourceDirectory());
		try {
			classPath.addAll(project.getCompileClasspathElements());
		} catch (DependencyResolutionRequiredException e) {
			throw new WrappedException(e);
		}
		addDependencies(classPath, project.getCompileArtifacts());
		classPath.remove(project.getBuild().getOutputDirectory());
		return newArrayList(filter(classPath, FILE_EXISTS));
	}
	
//	private void loadModelsIntoResourceSet(XtextResourceSet resourceSet) {
//		List<String> modelFiles = selectModelFiles();
//		for (String modelFile : modelFiles) {
//			getLog().info("Loading " + modelFile);
//			resourceSet.getResource(URI.createFileURI(modelFile), true);
//		}
//	}
//
//	private List<String> selectModelFiles() {
//		DirectoryScanner scanner = new DirectoryScanner();
//		scanner.setBasedir(structsDirectory);
//		scanner.setCaseSensitive(true);
//		scanner.setIncludes(includes);
//		scanner.setExcludes(excludes);
//		scanner.scan();
//		List<String> files = new ArrayList<String>();
//		for (String file : scanner.getIncludedFiles()) {
//			files.add(new File(structsDirectory, file).getAbsolutePath());
//		}
//		return files;
//	}

//	private void validateModels(XtextResourceSet resourceSet) throws MojoExecutionException {
//		Map<URI, List<Issue>> modelIssues = new HashMap<URI, List<Issue>>();
//		for (Resource resource : resourceSet.getResources()) {
//			if (resource instanceof XtextResource) {
//				List<Issue> issues = validateModel((XtextResource) resource);
//				if (issues != null && issues.size() > 0) {
//					modelIssues.put(resource.getURI(), issues);
//				}
//			}
//		}
//		logModelIssues(resourceSet, modelIssues);
//		if (modelIssues.values().stream().flatMap(List::stream).filter((issue) -> issue.getSeverity() == Severity.ERROR)
//				.count() > 0) {
//			// .filter((issue) -> !isLinkIssueWithInterface(resourceSet, issue))
//			throw new MojoExecutionException("Compilation stopped due to model issues.");
//		}
//	}

//	private static boolean isLinkIssueWithInterface(XtextResourceSet resourceSet, Issue issue) {
//		// dirty hack to suppress linker issues when referencing a JvmType that
//		// is build in the same project
//		// as the struct files. Since they are only visible to the
//		// ClassPath-based Type Resolver once
//		// the Maven compiler did it's job but the Struct Generator is executed
//		// in the Generate Source Phase of Maven
//		// there is a circular dependency.
//		// We need the compiled Java sources in order to generate sources out of
//		// the structs that need to get compiled.
//		//
//		// Suppressing these errors defers the actual issue to the Java
//		// Compiler.
//		if ("org.eclipse.xtext.diagnostics.Diagnostic.Linking".equals(issue.getCode())) {
//			EObject eObject = resourceSet.getEObject(issue.getUriToProblem(), true);
//			if (eObject instanceof StructDeclaration) {
//				return true;
//			}
//		}
//		return false;
//	}
//
//	private List<Issue> validateModel(XtextResource resource) {
//		IResourceValidator validator = resource.getResourceServiceProvider().getResourceValidator();
//		return validator.validate(resource, CheckMode.ALL, CancelIndicator.NullImpl);
//	}

//	private void logModelIssues(XtextResourceSet resourceSet, Map<URI, List<Issue>> modelIssues) {
//		for (URI modelURI : modelIssues.keySet()) {
//			List<Issue> issues = modelIssues.get(modelURI);
//			for (Issue issue : issues) {
//				String message = issue.getMessage();
//
//				Consumer<CharSequence> logMethod;
//				switch (issue.getSeverity()) {
//				case ERROR:
//					logMethod = getLog()::error;
//					if (isLinkIssueWithInterface(resourceSet, issue)) {
//						//logMethod = getLog()::warn;
//						message += " This might be because you referenced a JvmType that is build in the same project but is not yet compiled at the time the Struct Generator is executed. If the Java Compiler runs afterwards you can ignore this warning.";
//					}
//					break;
//				case WARNING:
//					logMethod = getLog()::warn;
//					break;
//				case INFO:
//				case IGNORE:
//				default:
//					logMethod = getLog()::info;
//				}
//				logMethod.accept(String.format("%s (Line %d, Col %d): %s", modelURI.toString(), issue.getLineNumber(),
//						issue.getColumn(), message));
//				if (issue.getData() != null) {
//					for (String data : issue.getData()) {
//						logMethod.accept(data);
//					}
//				}
//			}
//		}
//	}

//	private void generateCode(GeneratorDelegate generator, JavaIoFileSystemAccess fsa, XtextResourceSet resourceSet) {
//		fsa.setOutputPath(outputDirectory.getAbsolutePath());
//		fsa.setCallBack(new IFileCallback() {
//
//			public void fileDeleted(File file) {
//			}
//
//			public void fileAdded(File file) {
//				getLog().info("Generating " + file);
//			}
//		});
//
//		for (Resource resource : resourceSet.getResources()) {
//			generator.doGenerate(resource, fsa);
//		}
//	}
	
	// ************************
	
	protected String resolveToBaseDir(final String directory) throws MojoExecutionException {
		File outDir = new File(directory);
		if (!outDir.isAbsolute()) {
			outDir = new File(project.getBasedir(), directory);
		}
		return outDir.getAbsolutePath();
	}
	
	protected void addDependencies(Set<String> classPath, List<Artifact> dependencies) {
		for (Artifact artifact : dependencies) {
			classPath.add(artifact.getFile().getAbsolutePath());
		}
	}
	
	private String[] getValues(Xpp3Dom[] children) {
		String[] values = new String[children.length];
		for (int i = 0; i < values.length; i++) {
			values[i] = children[i].getValue();
		}
		return values;
	}
	
	private String scanBootclasspath(String javaHome, String[] includes, String[] excludes) {
		getLog().debug(
				"Scanning bootClassPath:\n" + "\tjavaHome = " + javaHome + "\n" + "\tincludes = "
						+ Arrays.toString(includes) + "\n" + "\texcludes = " + Arrays.toString(excludes));
		DirectoryScanner scanner = new DirectoryScanner();
		scanner.setBasedir(new File(javaHome));
		scanner.setIncludes(includes);
		scanner.setExcludes(excludes);
		scanner.scan();

		StringBuilder bootClassPath = new StringBuilder();
		String[] includedFiles = scanner.getIncludedFiles();
		for (int i = 0; i < includedFiles.length; i++) {
			if (i > 0) {
				bootClassPath.append(File.pathSeparator);
			}
			bootClassPath.append(new File(javaHome, includedFiles[i]).getAbsolutePath());
		}
		return bootClassPath.toString();
	}
	
	private String getBootClassPath() {
		Toolchain toolchain = toolchainManager.getToolchainFromBuildContext("jdk", session);
		if (toolchain instanceof DefaultJavaToolChain) {
			DefaultJavaToolChain javaToolChain = (DefaultJavaToolChain) toolchain;
			getLog().info("Using toolchain " + javaToolChain);

			String[] includes = { "jre/lib/*", "jre/lib/ext/*", "jre/lib/endorsed/*" };
			String[] excludes = new String[0];
			Xpp3Dom config = (Xpp3Dom) javaToolChain.getModel().getConfiguration();
			if (config != null) {
				Xpp3Dom bootClassPath = config.getChild("bootClassPath");
				if (bootClassPath != null) {
					Xpp3Dom includeParent = bootClassPath.getChild("includes");
					if (includeParent != null) {
						includes = getValues(includeParent.getChildren("include"));
					}
					Xpp3Dom excludeParent = bootClassPath.getChild("excludes");
					if (excludeParent != null) {
						excludes = getValues(excludeParent.getChildren("exclude"));
					}
				}
			}

			return scanBootclasspath(javaToolChain.getJavaHome(), includes, excludes);
		}
		return "";
	}
	protected void compile(String classPath, List<String> sourcePaths, String structSourceRoot, String outputPath) throws MojoExecutionException {
		StructsBatchCompiler compiler = getBatchCompiler();
		Log log = getLog();
		compiler.setResourceSet(injector.getInstance(XtextResourceSet.class));
		Iterable<String> filtered = filter(sourcePaths, FILE_EXISTS);
		if (Iterables.isEmpty(filtered)) {
			String dir = Iterables.toString(sourcePaths);
			log.info("skip compiling sources because the configured directory '" + dir + "' does not exists.");
			return;
		}
		//String baseDir = project.getBasedir().getAbsolutePath();
		//log.debug("Set Java Compliance Level: " + javaSourceVersion);
		//compiler.setJavaSourceVersion(javaSourceVersion);
		//log.debug("Set generateSyntheticSuppressWarnings: " + generateSyntheticSuppressWarnings);
		//compiler.setGenerateSyntheticSuppressWarnings(generateSyntheticSuppressWarnings);
		//log.debug("Set generateGeneratedAnnotation: " + generateGeneratedAnnotation);
		//compiler.setGenerateGeneratedAnnotation(generateGeneratedAnnotation);
		//log.debug("Set includeDateInGeneratedAnnotation: " + includeDateInGeneratedAnnotation);
		//compiler.setIncludeDateInGeneratedAnnotation(includeDateInGeneratedAnnotation);
		//log.debug("Set generatedAnnotationComment: " + generatedAnnotationComment);
		//compiler.setGeneratedAnnotationComment(generatedAnnotationComment);
		//log.debug("Set baseDir: " + baseDir);
		//compiler.setBasePath(baseDir);
		//log.debug("Set temp directory: " + getTempDirectory());
		//compiler.setTempDirectory(getTempDirectory());
		log.debug("Set DeleteTempDirectory: " + false);
		compiler.setDeleteTempDirectory(false);
		log.debug("Set classpath: " + classPath);
		compiler.setClassPath(classPath);
		String bootClassPath = getBootClassPath();
		log.debug("Set bootClasspath: " + bootClassPath);
		compiler.setBootClassPath(bootClassPath);
		log.debug("Set source path: " + concat(File.pathSeparator, newArrayList(filtered)));
		compiler.setSourcePath(concat(File.pathSeparator, newArrayList(filtered)));
		log.debug("Set output path: " + outputPath);
		compiler.setOutputPath(outputPath);
		log.debug("Set encoding: " + encoding);
		compiler.setFileEncoding(encoding);
		log.debug("Set structSourceRoot: " + structSourceRoot);
		compiler.setStructSourceRoot(structSourceRoot);
		log.debug("Set writeTraceFiles: " + writeTraceFiles);
		compiler.setWriteTraceFiles(writeTraceFiles);
		
		if (!compiler.compile()) {
			String dir = concat(File.pathSeparator, newArrayList(filtered));
			throw new MojoExecutionException("Error compiling xtend sources in '" + dir + "'.");
		}
	}
	
	@Inject
	private Provider<StructsBatchCompiler> structsBatchCompilerProvider;

	protected StructsBatchCompiler getBatchCompiler() {
		return structsBatchCompilerProvider.get();
	}
	protected static final Predicate<String> FILE_EXISTS = new Predicate<String>() {

		public boolean apply(String filePath) {
			return new File(filePath).exists();
		}
	};

	/**
	 * Create Java Source Code that is compatible to this Java version.
	 * 
	 * Supported values: 1.5, 1.6, 1.7, and 1.8
	 *
	 * @parameter expression="${maven.compiler.source}" default-value="1.6"
	 */
	private String javaSourceVersion;

	@Component
	private ToolchainManager toolchainManager;

	/**
	 * The current build session instance. This is used for toolchain manager API calls.
	 *
	 * @parameter expression="${session}"
	 * @required
	 * @readonly
	 */
	private MavenSession session;

	/**
	 * Xtend-File encoding argument for the compiler.
	 * 
	 * @parameter expression="${encoding}" default-value="${project.build.sourceEncoding}"
	 */
	private String encoding;

	/**
	 * Set this to false to suppress the creation of *._trace files.
	 * 
	 * @parameter default-value="true" expression="${writeTraceFiles}"
	 */
	private boolean writeTraceFiles;

	/**
	 * Location of the Xtend settings file.
	 * 
	 * @parameter default-value="${basedir}/.settings/org.eclipse.xtend.core.Xtend.prefs"
	 * @readonly
	 */
	//private String propertiesFileLocation;

	/**
	 * Whether <code>@SuppressWarnings</code> shall be generated for non-nested types.
	 * 
	 * @parameter default-value="true"
	 */
	//private boolean generateSyntheticSuppressWarnings;

	/**
	 * Whether <code>@Generated</code> shall be generated for non-nested types.
	 * 
	 * @parameter default-value="false"
	 */
	//private boolean generateGeneratedAnnotation;

	/**
	 * Whether the current time shall be added to <code>@Generated</code> annotations.
	 * 
	 * @parameter default-value="false"
	 */
	//private boolean includeDateInGeneratedAnnotation;
	
}
