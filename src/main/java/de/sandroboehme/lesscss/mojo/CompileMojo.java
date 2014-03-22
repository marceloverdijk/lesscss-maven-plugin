/* Copyright 2011-2012 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.sandroboehme.lesscss.mojo;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.plexus.build.incremental.BuildContext;

import de.sandroboehme.lesscss.LessCompiler;
import de.sandroboehme.lesscss.LessException;
import de.sandroboehme.lesscss.LessSource;

/**
 * Goal which compiles the LESS sources to CSS stylesheets.
 * 
 * @author Marcel Overdijk
 * @goal compile
 * @phase process-sources
 */
public class CompileMojo extends AbstractLessCssMojo {

	/**
	 * The directory for compiled CSS stylesheets.
	 * 
	 * @parameter expression="${lesscss.outputDirectory}" default-value="${project.build.directory}"
	 * @required
	 */
	protected File outputDirectory;

	/**
	 * An optional suffix to append to each output filename, before the .less extension
	 *
	 * @parameter expression="${lesscss.outputFileSuffix}"
	 */
	private String outputFileSuffix = "";

	/**
	 * When <code>true</code> the LESS compiler will compress the CSS stylesheets.
	 * 
	 * @parameter expression="${lesscss.compress}" default-value="false"
	 */
	private boolean compress;

	/**
	 * When <code>true</code> the plugin will watch for changes in LESS files and compile if it detects one.
	 * 
	 * @parameter expression="${lesscss.watch}" default-value="false"
	 */
	protected boolean watch=false;

	/**
	 * When <code>true</code> the plugin will watch for changes in LESS files and compile if it detects one.
	 * 
	 * @parameter expression="${lesscss.watchInterval}" default-value="1000"
	 */
	private int watchInterval=1000;

	/**
	 * The character encoding the LESS compiler will use for writing the CSS stylesheets.
	 * 
	 * @parameter expression="${lesscss.encoding}" default-value="${project.build.sourceEncoding}"
	 */
	private String encoding;

	/**
	 * When <code>true</code> forces the LESS compiler to always compile the LESS sources. By default LESS sources are only compiled when modified (including imports) or the CSS stylesheet does not exists.
	 * 
	 * @parameter expression="${lesscss.force}" default-value="false"
	 */
	private boolean force;

	/**
	 * The location of the LESS JavasSript file.
	 * 
	 * @parameter
	 */
	private File lessJs;

	/**
	 * The location of the NodeJS executable.
	 *
	 * @parameter
	 */
	private String nodeExecutable;

	/**
	 * Execute the MOJO.
	 * 
	 * @throws MojoExecutionException
	 *             if something unexpected occurs.
	 */
	public void execute() throws MojoExecutionException {
		if (getLog().isDebugEnabled()) {
			getLog().debug("sourceDirectory = " + sourceDirectory);
			getLog().debug("outputDirectory = " + outputDirectory);
			getLog().debug("outputFileSuffix = " + outputFileSuffix);
			getLog().debug("includes = " + Arrays.toString(includes));
			getLog().debug("excludes = " + Arrays.toString(excludes));
			getLog().debug("force = " + force);
			getLog().debug("lessJs = " + lessJs);
			getLog().debug("skip = " + skip);
		}

		if(!skip){
			executeInternal();
		} else {
			getLog().info("Skipping plugin execution per configuration");
		}
	}

	private void executeInternal() throws MojoExecutionException {
		long start = System.currentTimeMillis();
		
		String[] files = getIncludedFiles();

		if (files == null || files.length < 1) {
			getLog().info("Nothing to compile - no LESS sources found");
		} else {
			if (getLog().isDebugEnabled()) {
				getLog().debug("included files = " + Arrays.toString(files));
			}

			Object lessCompiler = initLessCompiler();
			if (watch){
				getLog().info("Watching "+sourceDirectory);
				if (force){
					force=false;
					getLog().info("Disabled the 'force' flag in watch mode.");
				}
				Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
				while (watch && !Thread.currentThread().isInterrupted()){
					compileIfChanged(files, lessCompiler);
					try {
						Thread.sleep(watchInterval);
					} catch (InterruptedException e) {
						System.out.println("interrupted");
					}
				}
			} else {
				compileIfChanged(files, lessCompiler);
			}

			getLog().info("Complete Less compile job finished in " + (System.currentTimeMillis() - start) + " ms");
		}
	}

	private void compileIfChanged(String[] files, Object lessCompiler) throws MojoExecutionException {
		try {
			for (String file : files) {
				File input = new File(sourceDirectory, file);

				buildContext.removeMessages(input);

				String filename = file.replace(".less", "%s.css");
				filename = String.format(filename, outputFileSuffix);

				File output = new File(outputDirectory, filename);

				if (!output.getParentFile().exists() && !output.getParentFile().mkdirs()) {
					throw new MojoExecutionException("Cannot create output directory " + output.getParentFile());
				}

				try {
					LessSource lessSource = new LessSource(input);
					if (force || !output.exists() || output.lastModified() < lessSource.getLastModifiedIncludingImports()) {
						long compilationStarted = System.currentTimeMillis();
						getLog().info("Compiling LESS source: " + file + "...");
						if (lessCompiler instanceof LessCompiler) {
							((LessCompiler) lessCompiler).compile(lessSource, output, force);
						} else {
							((NodeJsLessCompiler) lessCompiler).compile(lessSource, output, force);
						}
						buildContext.refresh(output);
						getLog().info("Finished compilation to "+outputDirectory+" in " + (System.currentTimeMillis() - compilationStarted) + " ms");
					}
					else if (!watch) {
						getLog().info("Bypassing LESS source: " + file + " (not modified)");
					}
				} catch (IOException e) {
					buildContext.addMessage(input, 0, 0, "Error compiling LESS source", BuildContext.SEVERITY_ERROR, e);
					throw new MojoExecutionException("Error while compiling LESS source: " + file, e);
				} catch (LessException e) {
					String message = e.getMessage();
					if (StringUtils.isEmpty(message)) {
						message = "Error compiling LESS source";
					}
					buildContext.addMessage(input, 0, 0, "Error compiling LESS source", BuildContext.SEVERITY_ERROR, e);
					throw new MojoExecutionException("Error while compiling LESS source: " + file, e);
				} catch (InterruptedException e) {
					buildContext.addMessage(input, 0, 0, "Error compiling LESS source", BuildContext.SEVERITY_ERROR, e);
					throw new MojoExecutionException("Error while compiling LESS source: " + file, e);
				}
			}
		} finally {
			if (lessCompiler instanceof NodeJsLessCompiler) {
				((NodeJsLessCompiler) lessCompiler).close();
			}
		}
	}

	private Object initLessCompiler() throws MojoExecutionException {
		if (nodeExecutable != null) {
			NodeJsLessCompiler lessCompiler;
			try {
				lessCompiler = new NodeJsLessCompiler(nodeExecutable, compress, encoding, getLog());
			} catch (IOException e) {
				throw new MojoExecutionException(e.getMessage(), e);
			}
			if (lessJs != null) {
				throw new MojoExecutionException(
						"Custom LESS JavaScript is not currently supported when using nodeExecutable");
			}
			return lessCompiler;
		} else {
			LessCompiler lessCompiler = new LessCompiler();
			lessCompiler.setCompress(compress);
			lessCompiler.setEncoding(encoding);
			if (lessJs != null) {
				try {
					lessCompiler.setLessJs(lessJs.toURI().toURL());
				} catch (MalformedURLException e) {
					throw new MojoExecutionException(
							"Error while loading LESS JavaScript: " + lessJs.getAbsolutePath(), e);
				}
			}
			return lessCompiler;
		}
	}
}
