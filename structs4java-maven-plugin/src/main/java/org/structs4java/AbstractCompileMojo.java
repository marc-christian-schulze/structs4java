package org.structs4java;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayList;
import static org.eclipse.xtext.util.Strings.concat;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.toolchain.Toolchain;
import org.apache.maven.toolchain.ToolchainManager;
import org.apache.maven.toolchain.java.DefaultJavaToolChain;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.xtext.resource.XtextResourceSet;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;

public abstract class AbstractCompileMojo extends AbstractMojo {

	@Component
	private MavenProject project;

	@Component
	private MavenSession mavenSession;

	@Component
	private BuildPluginManager pluginManager;

	@Parameter
	private String[] includes;

	@Parameter
	private String[] excludes;

	@Inject
	protected MavenLog4JConfigurator log4jConfigurator;

	private Injector injector;

	protected MavenProject getProject() {
		return project;
	}

	protected abstract File getStructsDirectory();

	public AbstractCompileMojo() {
		injector = new MavenStructs4JavaDslStandaloneSetupGenerated().createInjectorAndDoEMFRegistration();
		injector.injectMembers(this);
	}

	public void execute() throws MojoExecutionException {
		if (includes == null || includes.length == 0) {
			includes = new String[1];
			includes[0] = "**/*.structs";
		}

		if (!getStructsDirectory().exists()) {
			getLog().info("No struct files at " + getStructsDirectory());
			return;
		}

		log4jConfigurator.configureLog4j(getLog());

		List<String> compileSourceRoots = getCompileSourceRoots();
		compileSourceRoots.remove(getOutputDirectory());
		String classPath = concat(File.pathSeparator, getClassPath());
		project.addCompileSourceRoot(getOutputDirectory().toString());
		compile(classPath, compileSourceRoots, getStructsDirectory().toString(), getOutputDirectory().toString());
	}

	protected abstract List<String> getCompileSourceRoots();

	protected abstract List<String> getClassPath();

	protected abstract File getOutputDirectory();

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
		getLog().debug("Scanning bootClassPath:\n" + "\tjavaHome = " + javaHome + "\n" + "\tincludes = "
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

	protected void compile(String classPath, List<String> sourcePaths, String structSourceRoot, String outputPath)
			throws MojoExecutionException {
		StructsBatchCompiler compiler = getBatchCompiler();
		Log log = getLog();
		compiler.setResourceSet(injector.getInstance(XtextResourceSet.class));
		Iterable<String> filtered = filter(sourcePaths, FILE_EXISTS);
		if (Iterables.isEmpty(filtered)) {
			String dir = Iterables.toString(sourcePaths);
			log.info("skip compiling sources because the configured directory '" + dir + "' does not exists.");
			return;
		}
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
			throw new MojoExecutionException("Error compiling struct sources in '" + dir + "'.");
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

	@Component
	private ToolchainManager toolchainManager;

	/**
	 * The current build session instance. This is used for toolchain manager
	 * API calls.
	 *
	 * @parameter expression="${session}"
	 * @required
	 * @readonly
	 */
	private MavenSession session;

	/**
	 * Xtend-File encoding argument for the compiler.
	 * 
	 * @parameter expression="${encoding}"
	 *            default-value="${project.build.sourceEncoding}"
	 */
	private String encoding;

	/**
	 * Set this to false to suppress the creation of *._trace files.
	 * 
	 * @parameter default-value="true" expression="${writeTraceFiles}"
	 */
	private boolean writeTraceFiles;

}
