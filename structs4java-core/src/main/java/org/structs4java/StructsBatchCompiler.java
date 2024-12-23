package org.structs4java;

import static com.google.common.collect.Iterables.addAll;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.toArray;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;
import static java.util.Arrays.asList;
import static org.eclipse.xtext.util.Strings.concat;
import static org.eclipse.xtext.util.Strings.emptyIfNull;
import static org.eclipse.xtext.util.Strings.isEmpty;
import static org.eclipse.xtext.util.Strings.split;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import com.google.inject.Injector;
import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jdt.core.compiler.batch.BatchCompiler;
import org.eclipse.xtend.core.compiler.batch.BootClassLoader;
import org.eclipse.xtend.core.macro.ProcessorInstanceForJvmTypeProvider.ProcessorClassloaderAdapter;
import org.eclipse.xtext.common.types.access.impl.ClasspathTypeProvider;
import org.eclipse.xtext.common.types.access.impl.IndexedJvmTypeAccess;
import org.eclipse.xtext.common.types.descriptions.IStubGenerator;
import org.eclipse.xtext.diagnostics.Severity;
import org.eclipse.xtext.generator.GeneratorDelegate;
import org.eclipse.xtext.generator.JavaIoFileSystemAccess;
import org.eclipse.xtext.mwe.NameBasedFilter;
import org.eclipse.xtext.mwe.PathTraverser;
import org.eclipse.xtext.parser.IEncodingProvider;
import org.eclipse.xtext.parser.IParseResult;
import org.eclipse.xtext.resource.CompilerPhases;
import org.eclipse.xtext.resource.FileExtensionProvider;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.resource.impl.ResourceSetBasedResourceDescriptions;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.util.Strings;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.IResourceValidator;
import org.eclipse.xtext.validation.Issue;
import org.structs4java.structs4JavaDsl.StructsFile;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.eclipse.xtext.util.Strings;
import org.eclipse.xtext.validation.Issue;

public class StructsBatchCompiler {

	private static final Logger log = Logger.getLogger(StructsBatchCompiler.class);

	private final static class SeverityFilter implements Predicate<Issue> {
		private static final SeverityFilter WARNING = new SeverityFilter(Severity.WARNING);
		private static final SeverityFilter ERROR = new SeverityFilter(Severity.ERROR);
		private Severity severity;

		private SeverityFilter(Severity severity) {
			this.severity = severity;
		}

		public boolean apply(Issue issue) {
			return this.severity == issue.getSeverity();
		}
	}

	private static final FileFilter ACCEPT_ALL_FILTER = new FileFilter() {
		public boolean accept(File pathname) {
			return true;
		}
	};

	private XtextResourceSet resourceSet;
	@Inject
	private Provider<JavaIoFileSystemAccess> javaIoFileSystemAccessProvider;
	@Inject
	private FileExtensionProvider fileExtensionProvider;
	@Inject
	private Provider<ResourceSetBasedResourceDescriptions> resourceSetDescriptionsProvider;
	@Inject
	private GeneratorDelegate generator;
	@Inject
	private IndexedJvmTypeAccess indexedJvmTypeAccess;
	@Inject
	private IEncodingProvider.Runtime encodingProvider;
	@Inject
	private IResourceDescription.Manager resourceDescriptionManager;
	@Inject
	private CompilerPhases compilerPhases;
	@Inject
	private IStubGenerator stubGenerator;

	private Writer debugWriter;
	private String sourcePath;
	private String classPath;
	private String structSourceRoot;
	/**
	 * @since 2.7
	 */
	private String bootClassPath;
	private boolean useCurrentClassLoaderAsParent;
	private String outputPath;
	private String fileEncoding;
	private String sourceVersion = "11";
	private String targetVersion = "11";
	private boolean verbose = false;
	private String tempDirectory = System.getProperty("java.io.tmpdir");
	private boolean deleteTempDirectory = true;
	private List<File> tempFolders = Lists.newArrayList();
	private boolean writeTraceFiles = true;
	private ClassLoader currentClassLoader = getClass().getClassLoader();

	public StructsBatchCompiler() {
		Injector injector = new MavenStructs4JavaDslStandaloneSetupGenerated().createInjectorAndDoEMFRegistration();
		injector.injectMembers(this);
		setResourceSet(injector.getInstance(XtextResourceSet.class));
	}

	public void setCurrentClassLoader(ClassLoader currentClassLoader) {
		this.currentClassLoader = currentClassLoader;
	}

	public void setUseCurrentClassLoaderAsParent(boolean useCurrentClassLoaderAsParent) {
		this.useCurrentClassLoaderAsParent = useCurrentClassLoaderAsParent;
	}

	public String getTempDirectory() {
		return tempDirectory;
	}

	public void setTempDirectory(String tempDirectory) {
		this.tempDirectory = tempDirectory;
	}

	public boolean isWriteTraceFiles() {
		return writeTraceFiles;
	}

	public void setWriteTraceFiles(boolean writeTraceFiles) {
		this.writeTraceFiles = writeTraceFiles;
	}

	public void setResourceSet(XtextResourceSet resourceSet) {
		this.resourceSet = resourceSet;
	}

	public boolean isDeleteTempDirectory() {
		return deleteTempDirectory;
	}

	public void setDeleteTempDirectory(boolean deletetempDirectory) {
		this.deleteTempDirectory = deletetempDirectory;
	}

	public Writer getDebugWriter() {
		if (debugWriter == null) {
			debugWriter = new Writer() {
				@Override
				public void write(char[] data, int offset, int count) throws IOException {
					String message = String.copyValueOf(data, offset, count);
					if (!Strings.isEmpty(message.trim())) {
						log.debug(message);
					}
				}

				@Override
				public void flush() throws IOException {
				}

				@Override
				public void close() throws IOException {
				}
			};
		}
		return debugWriter;
	}

	public void setClassPath(String classPath) {
		this.classPath = classPath;
	}

	/**
	 * @since 2.7
	 */
	public void setBootClassPath(String bootClassPath) {
		this.bootClassPath = bootClassPath;
	}

	public void setOutputPath(String outputPath) {
		this.outputPath = outputPath;
	}

	public void setSourcePath(String sourcePath) {
		this.sourcePath = sourcePath;
	}

	public void setStructSourceRoot(String structSourceRoot) {
		this.structSourceRoot = structSourceRoot;
	}

	protected String getSourceVersion() {
		return sourceVersion;
	}

	protected String getTargetVersion() {
		return targetVersion;
	}

	public void setSourceVersion(String version) {
		sourceVersion = version;
	}

	public void setTargetVersion(String version) {
		targetVersion = version;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	protected boolean isVerbose() {
		return verbose;
	}

	public String getFileEncoding() {
		return fileEncoding;
	}

	public void setFileEncoding(String encoding) {
		this.fileEncoding = encoding;
	}

	public boolean compile() {
		try {
			resourceSet.addLoadOption(XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE);

			File classDirectory = createTempDir("classes");
			try {
				compilerPhases.setIndexing(resourceSet, true);
				// install a type provider without index lookup for the first
				// phase
				installJvmTypeProvider(resourceSet, classDirectory, true);
				loadStructsFiles(resourceSet);
				File sourceDirectory = createStubs(resourceSet);
				preCompileStubs(sourceDirectory, classDirectory);
					
			} finally {
				compilerPhases.setIndexing(resourceSet, false);
			}
			// install a fresh type provider for the second phase, so we clear
			// all previously cached classes and misses.
			installJvmTypeProvider(resourceSet, classDirectory, false);
			EcoreUtil.resolveAll(resourceSet);
			List<Issue> issues = validate(resourceSet);
			Iterable<Issue> errors = Iterables.filter(issues, SeverityFilter.ERROR);
			Iterable<Issue> warnings = Iterables.filter(issues, SeverityFilter.WARNING);
			reportIssues(Iterables.concat(errors, warnings));
			if (!Iterables.isEmpty(errors)) {
				return false;
			}
			generateJavaFiles(resourceSet);
		} finally {
			if (isDeleteTempDirectory()) {
				deleteTmpFolders();
			}
		}
		return true;
	}

	protected ResourceSet loadStructsFiles(final ResourceSet resourceSet) {
		encodingProvider.setDefaultEncoding(getFileEncoding());
		final NameBasedFilter nameBasedFilter = new NameBasedFilter();
		nameBasedFilter.setExtension(fileExtensionProvider.getPrimaryFileExtension());
		PathTraverser pathTraverser = new PathTraverser();
		List<String> sourcePathDirectories = getStructsSourcePathDirectories();
		Multimap<String, URI> pathes = pathTraverser.resolvePathes(sourcePathDirectories, new Predicate<URI>() {
			public boolean apply(URI input) {
				boolean matches = nameBasedFilter.matches(input);
				return matches;
			}
		});
		for (String src : pathes.keySet()) {
			URI baseDir = URI.createFileURI(src + "/");

			String identifier = Joiner.on("_").join(baseDir.segments());
			URI platformResourceURI = URI.createPlatformResourceURI(identifier + "/", true);

			resourceSet.getURIConverter().getURIMap().put(platformResourceURI, baseDir);

			for (URI uri : pathes.get(src)) {
				URI uriToUse = uri.replacePrefix(baseDir, platformResourceURI);
				log.info("loading structs file '" + uri.toFileString() + "'");
				resourceSet.getResource(uri, true);
			}
		}
		return resourceSet;
	}

	protected File createStubs(ResourceSet resourceSet) {
		File outputDirectory = createTempDir("stubs");
		JavaIoFileSystemAccess fileSystemAccess = javaIoFileSystemAccessProvider.get();
		fileSystemAccess.setOutputPath(outputDirectory.toString());
		List<Resource> resources = Lists.newArrayList(resourceSet.getResources());
		for (Resource resource : resources) {
			IResourceDescription description = resourceDescriptionManager.getResourceDescription(resource);
			stubGenerator.doGenerateStubs(fileSystemAccess, description);
		}
		return outputDirectory;
	}

	protected boolean preCompileStubs(File tmpSourceDirectory, File classDirectory) {
		List<String> commandLine = Lists.newArrayList();
		// todo args
		if (isVerbose()) {
			commandLine.add("-verbose");
		}
		if (!isEmpty(bootClassPath)) {
			commandLine.add("-bootclasspath \"" + concat(File.pathSeparator, getBootClassPathEntries()) + "\"");
		}
		if (!isEmpty(classPath)) {
			commandLine.add("-cp \"" + concat(File.pathSeparator, getClassPathEntries()) + "\"");
		}
		commandLine.add("-d \"" + classDirectory.toString() + "\"");
		commandLine.add("-source " + getSourceVersion());
		commandLine.add("-target " + getTargetVersion());
		commandLine.add("-proceedOnError");
		List<String> sourceDirectories = newArrayList(getSourcePathDirectories());
		sourceDirectories.add(tmpSourceDirectory.toString());
		commandLine.add(concat(" ", transform(sourceDirectories, new Function<String, String>() {

			public String apply(String path) {
				return "\"" + path + "\"";
			}
		})));
		log.debug("pre-compiling Java stubs: '" + concat(" ", commandLine) + "'");
		return BatchCompiler.compile(concat(" ", commandLine), new PrintWriter(getDebugWriter()),
				new PrintWriter(getDebugWriter()), null);
	}

	protected List<Issue> validate(ResourceSet resourceSet) {
		List<Issue> issues = Lists.newArrayList();
		List<Resource> resources = Lists.newArrayList(resourceSet.getResources());
		for (Resource resource : resources) {
			IResourceServiceProvider resourceServiceProvider = IResourceServiceProvider.Registry.INSTANCE
					.getResourceServiceProvider(resource.getURI());
			if (resourceServiceProvider != null) {
				IResourceValidator resourceValidator = resourceServiceProvider.getResourceValidator();
				List<Issue> result = resourceValidator.validate(resource, CheckMode.ALL, CancelIndicator.NullImpl);
				addAll(issues, result);
			}
		}
		return issues;
	}

	/**
	 * Installs the complete JvmTypeProvider including index access into the
	 * {@link ResourceSet}. The lookup classpath is enhanced with the given tmp
	 * directory.
	 * 
	 */
	@Deprecated
	protected void installJvmTypeProvider(XtextResourceSet resourceSet, File tmpClassDirectory) {
		internalInstallJvmTypeProvider(resourceSet, tmpClassDirectory, false);
	}

	/**
	 * Installs the JvmTypeProvider optionally including index access into the
	 * {@link ResourceSet}. The lookup classpath is enhanced with the given tmp
	 * directory.
	 */
	protected void installJvmTypeProvider(XtextResourceSet resourceSet, File tmpClassDirectory,
			boolean skipIndexLookup) {
		if (skipIndexLookup) {
			internalInstallJvmTypeProvider(resourceSet, tmpClassDirectory, skipIndexLookup);
		} else {
			// delegate to the deprecated signature in case it was overridden by
			// clients
			installJvmTypeProvider(resourceSet, tmpClassDirectory);
		}
	}

	/**
	 * Performs the actual installation of the JvmTypeProvider.
	 */
	private void internalInstallJvmTypeProvider(XtextResourceSet resourceSet, File tmpClassDirectory,
			boolean skipIndexLookup) {
		Iterable<String> classPathEntries = concat(getClassPathEntries(), getSourcePathDirectories(),
				asList(tmpClassDirectory.toString()));
		classPathEntries = filter(classPathEntries, new Predicate<String>() {
			public boolean apply(String input) {
				return !Strings.isEmpty(input.trim());
			}
		});
		Function<String, URL> toUrl = new Function<String, URL>() {
			public URL apply(String from) {
				try {
					return new File(from).toURI().toURL();
				} catch (MalformedURLException e) {
					throw new RuntimeException(e);
				}
			}
		};
		Iterable<URL> classPathUrls = Iterables.transform(classPathEntries, toUrl);
		log.debug("classpath used for Struct compilation : " + classPathUrls);
		ClassLoader parentClassLoader;
		if (useCurrentClassLoaderAsParent) {
			parentClassLoader = currentClassLoader;
		} else {
			if (isEmpty(bootClassPath)) {
				parentClassLoader = ClassLoader.getSystemClassLoader().getParent();
			} else {
				Iterable<URL> bootClassPathUrls = Iterables.transform(getBootClassPathEntries(), toUrl);
				parentClassLoader = new BootClassLoader(toArray(bootClassPathUrls, URL.class));
			}
		}
		ClassLoader urlClassLoader = new URLClassLoader(toArray(classPathUrls, URL.class), parentClassLoader);
		new ClasspathTypeProvider(urlClassLoader, resourceSet, skipIndexLookup ? null : indexedJvmTypeAccess);
		resourceSet.setClasspathURIContext(urlClassLoader);

		// for annotation processing we need to have the compiler's classpath as
		// a parent.
		URLClassLoader urlClassLoaderForAnnotationProcessing = new URLClassLoader(toArray(classPathUrls, URL.class),
				currentClassLoader);
		resourceSet.eAdapters().add(new ProcessorClassloaderAdapter(urlClassLoaderForAnnotationProcessing));
	}

	protected void reportIssues(Iterable<Issue> issues) {
		for (Issue issue : issues) {
			StringBuilder issueBuilder = createIssueMessage(issue);
			if (Severity.ERROR == issue.getSeverity()) {
				log.error(issueBuilder.toString());
			} else if (Severity.WARNING == issue.getSeverity()) {
				log.warn(issueBuilder.toString());
			}
		}
	}

	private String getFilePath(URI uri) {
		if(uri.isFile()) {
			return uri.toFileString();
		}

		if(uri.trimFragment().isPlatform()) {
			return uri.trimFragment().toPlatformString(true);
		}

		return uri.trimFragment().toString();
	}

	private StringBuilder createIssueMessage(Issue issue) {
		StringBuilder issueBuilder = new StringBuilder();

		String filePath = getFilePath(issue.getUriToProblem());

		issueBuilder.append(filePath).append(":").append(issue.getLineNumber()).append(" ").append(issue.getMessage()).append("\n");

		String sourceLineWithIssue = readLineFromResource(filePath, issue.getLineNumber());
		int numTabsBeforeColStart = countTabsTillColumnStart(sourceLineWithIssue, issue.getColumn());
		// 1 \t replaced by 4 spaces = 3 additional spaces / tab
		int spacesIntroducedOnTopByReplacingTabsWithSpaces = 3 * numTabsBeforeColStart;

		issueBuilder.append(replaceTabsWith4Spaces(sourceLineWithIssue)).append("\n");
		issueBuilder.append(com.google.common.base.Strings.repeat(" ", issue.getColumn() - 1 + spacesIntroducedOnTopByReplacingTabsWithSpaces)).append("^");
		int colLength = issue.getColumnEnd() - issue.getColumn();
		if(colLength > 1) {
			issueBuilder.append(com.google.common.base.Strings.repeat("-", colLength - 1));
		}
		return issueBuilder;
	}

	private int countTabsTillColumnStart(String str, int colStart) {
		int count = 0;
		for(int i = 0; i < colStart; ++i) {
			if(str.charAt(i) == '\t') {
				count++;
			}
		}
		return count;
	}

	private String replaceTabsWith4Spaces(String str) {
		return str.replaceAll("\t", "    ");
	}

	private String readLineFromResource(String filePath, int line) {
		JavaIoFileSystemAccess fileSystemAccess = javaIoFileSystemAccessProvider.get();
		// if you don't set the output path before using you end up with below exception
		// compile failed: A slot with name 'DEFAULT_OUTPUT' has not been configured.
		fileSystemAccess.setOutputPath("");
		String fileContent = fileSystemAccess.readTextFile(filePath).toString();
		List<String> lines = Strings.split(fileContent, "\n");
		return lines.get(line - 1);
	}

	protected void generateJavaFiles(ResourceSet resourceSet) {
		JavaIoFileSystemAccess javaIoFileSystemAccess = javaIoFileSystemAccessProvider.get();
		javaIoFileSystemAccess.setOutputPath(outputPath);
		javaIoFileSystemAccess.setWriteTrace(writeTraceFiles);

		for (Resource resource : newArrayList(resourceSet.getResources())) {
			if (getRootEObject(resource) instanceof StructsFile) {
				log.debug("Generating source for: " + resource);
				generator.doGenerate(resource, javaIoFileSystemAccess);
			}
		}
	}

	private EObject getRootEObject(Resource resource) {
		if (resource == null) {
			return null;
		}
		if (resource.getContents() == null || resource.getContents().size() == 0) {
			return null;
		}
		return resource.getContents().get(0);
	}

	protected List<String> getClassPathEntries() {
		return getDirectories(classPath);
	}

	/**
	 * @since 2.7
	 */
	protected List<String> getBootClassPathEntries() {
		return getDirectories(bootClassPath);
	}

	protected List<String> getSourcePathDirectories() {
		return getDirectories(sourcePath);
	}

	protected List<String> getStructsSourcePathDirectories() {
		return getDirectories(structSourceRoot);
	}

	protected List<String> getDirectories(String path) {
		if (Strings.isEmpty(path)) {
			return Lists.newArrayList();
		}
		final List<String> split = split(emptyIfNull(path), File.pathSeparator);
		return transform(split, new Function<String, String>() {
			public String apply(String from) {
				return new File(new File(from).getAbsoluteFile().toURI().normalize()).getAbsolutePath();
			}
		});
	}

	protected File createTempDir(String prefix) {
		File tempDir = new File(getTempDirectory(), prefix + System.nanoTime());
		cleanFolder(tempDir, ACCEPT_ALL_FILTER, true, true);
		if (!tempDir.mkdirs()) {
			throw new RuntimeException("Error creating temp directory '" + tempDir.getAbsolutePath() + "'");
		}
		tempFolders.add(tempDir);
		return tempDir;
	}

	protected void deleteTmpFolders() {
		for (File file : tempFolders) {
			cleanFolder(file, ACCEPT_ALL_FILTER, true, true);
		}
	}

	// FIXME: use Files#cleanFolder after the maven distro availability of
	// version 2.2.x
	protected static boolean cleanFolder(File parentFolder, FileFilter filter, boolean continueOnError,
			boolean deleteParentFolder) {
		if (!parentFolder.exists()) {
			return true;
		}
		if (filter == null)
			filter = ACCEPT_ALL_FILTER;
		log.debug("Cleaning folder " + parentFolder.toString());
		final File[] contents = parentFolder.listFiles(filter);
		for (int j = 0; j < contents.length; j++) {
			final File file = contents[j];
			if (file.isDirectory()) {
				if (!cleanFolder(file, filter, continueOnError, true) && !continueOnError)
					return false;
			} else {
				if (!file.delete()) {
					log.warn("Couldn't delete " + file.getAbsolutePath());
					if (!continueOnError)
						return false;
				}
			}
		}
		if (deleteParentFolder) {
			if (parentFolder.list().length == 0 && !parentFolder.delete()) {
				log.warn("Couldn't delete " + parentFolder.getAbsolutePath());
				return false;
			}
		}
		return true;
	}

}