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
package org.lesscss.mojo;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.StringUtils;
import org.lesscss.LessCompiler;
import org.lesscss.LessException;
import org.lesscss.LessSource;
import org.sonatype.plexus.build.incremental.BuildContext;

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
	private File outputDirectory;

	/**
	 * When <code>true</code> the LESS compiler will compress the CSS stylesheets.
	 * 
	 * @parameter expression="${lesscss.compress}" default-value="false"
	 */
	private boolean compress;

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
	 * Execute the MOJO.
	 * 
	 * @throws MojoExecutionException
	 *             if something unexpected occurs.
	 */
	public void execute() throws MojoExecutionException {

		long start = System.currentTimeMillis();

		if (getLog().isDebugEnabled()) {
			getLog().debug("sourceDirectory = " + sourceDirectory);
			getLog().debug("outputDirectory = " + outputDirectory);
			getLog().debug("includes = " + Arrays.toString(includes));
			getLog().debug("excludes = " + Arrays.toString(excludes));
			getLog().debug("force = " + force);
			getLog().debug("lessJs = " + lessJs);
		}

		String[] files = getIncludedFiles();

		if (files == null || files.length < 1) {
			getLog().info("Nothing to compile - no LESS sources found");
		} else {
			if (getLog().isDebugEnabled()) {
				getLog().debug("included files = " + Arrays.toString(files));
			}

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

			for (String file : files) {
				File input = new File(sourceDirectory, file);

				buildContext.removeMessages(input);

				File output = new File(outputDirectory, file.replace(".less", ".css"));

				if (!output.getParentFile().exists() && !output.getParentFile().mkdirs()) {
					throw new MojoExecutionException("Cannot create output directory " + output.getParentFile());
				}

				try {
					LessSource lessSource = new LessSource(input);

					if (output.lastModified() < lessSource.getLastModifiedIncludingImports()) {
						getLog().info("Compiling LESS source: " + file + "...");
						lessCompiler.compile(lessSource, output, force);
						buildContext.refresh(output);
					}
					else {
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
				}
			}

			getLog().info("Compilation finished in " + (System.currentTimeMillis() - start) + " ms");
		}
	}
}
