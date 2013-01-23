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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
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

	private static final int WATCH_INTERVAL_DEFAULT = 1000;

	/**
	 * The directory for compiled CSS stylesheets.
	 * 
	 * @parameter expression="${lesscss.outputDirectory}"
	 *            default-value="${project.build.directory}"
	 * @required
	 */
	protected File outputDirectory;

	/**
	 * When <code>true</code> the LESS compiler will compress the CSS
	 * stylesheets.
	 * 
	 * @parameter expression="${lesscss.compress}" default-value="false"
	 */
	private boolean compress;

	/**
	* When <code>true</code> the plugin will watch for changes in LESS files and compile if it detects one.
	*
	* @parameter expression="${lesscss.watch}" default-value="false"
	*/
	protected boolean watch = false;

	/**
	* When <code>true</code> the plugin will watch for changes in LESS files and compile if it detects one.
	*
	* @parameter expression="${lesscss.watchInterval}" default-value="1000"
	*/
	private int watchInterval = WATCH_INTERVAL_DEFAULT;

	/**
	 * The character encoding the LESS compiler will use for writing the CSS
	 * stylesheets.
	 * 
	 * @parameter expression="${lesscss.encoding}"
	 *            default-value="${project.build.sourceEncoding}"
	 */
	private String encoding;

	/**
	 * When <code>true</code> forces the LESS compiler to always compile the
	 * LESS sources. By default LESS sources are only compiled when modified
	 * (including imports) or the CSS stylesheet does not exists.
	 * 
	 * @parameter expression="${lesscss.force}" default-value="false"
	 */
	private boolean force;

	/**
	 * If set concatenates the less files and outputs to the filename given
	 * 
	 * @parameter expression="${lesscss.concatenateTo}" 
	 */
	private String concatenateTo;

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

		List<ConfigurationItem> configurationItems = getConfiguration();

		if (configurationItems.size() == 0) {
			return;
		}

		for (ConfigurationItem item : configurationItems) {
			if (getLog().isDebugEnabled()) {
				getLog().debug("sourceDirectory = " + item.getSourceDirectory());
				getLog().debug("outputDirectory = " + item.getOutputDirectory());
				getLog().debug("includes = " + Arrays.toString(item.getIncludes()));
				getLog().debug("excludes = " + Arrays.toString(item.getExcludes()));
				getLog().debug("force = " + item.isForce());
				getLog().debug("lessJs = " + item.getLessJs());
				getLog().debug("concatenate = " + item.getConcateanteTo());
				getLog().debug("watch = " + item.isWatch());
				getLog().debug("watchInterval = " + item.getWatchInterval());
				getLog().debug("compress = " + item.isCompress());
			}

			String[] files = getIncludedFiles(item);

			if (item.getConcateanteTo() != null) {
				try {

					String tmpPath = item.getConcateanteTo();
					File tmpFile = new File(item.getSourceDirectory(), tmpPath);
					
					boolean updated = isFilesUpdated(item, files, tmpFile);
					if(!updated){
						getLog().info("No files updated since last build");
						return;
					}
					
					deleteTemporaryFile(tmpFile);
					buildConcatenatedFile(item, files, tmpFile);
					files = setTemporaryAsFileToCompile(tmpPath);
				} catch (IOException ioe) {
					getLog().error("Error concatenating files", ioe);
				}
			}

			if (files == null || files.length < 1) {
				getLog().info("Nothing to compile - no LESS sources found");
			} else {
				if (getLog().isDebugEnabled()) {
					getLog().debug("included files = " + Arrays.toString(files));
				}

				LessCompiler lessCompiler = new LessCompiler();
				lessCompiler.setCompress(item.isCompress());
				lessCompiler.setEncoding(item.getEncoding());

				if (item.getLessJs() != null) {
					try {
						lessCompiler.setLessJs(item.getLessJs().toURI().toURL());
					} catch (MalformedURLException e) {
						throw new MojoExecutionException("Error while loading LESS JavaScript: "
								+ item.getLessJs().getAbsolutePath(), e);
					}
				}
				if (item.isWatch()) {
					getLog().info("Watching " + item.getOutputDirectory());
					if (item.isForce()) {
						force = false;
						getLog().info("Disabled the 'force' flag in watch mode.");
					}
					Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
					while (item.isWatch() && !Thread.currentThread().isInterrupted()) {
						compileIfChanged(files, lessCompiler, item);
						try {
							Thread.sleep(item.getWatchInterval());
						} catch (InterruptedException e) {
							System.out.println("interrupted");
						}
					}
				} else {
					compileIfChanged(files, lessCompiler, item);
				}

				getLog().info(
						"Complete Less compile job finished in "
								+ (System.currentTimeMillis() - start) + " ms");
			}
		}
	}

	private String[] setTemporaryAsFileToCompile(String tmpPath) {
		String[] files;
		files = new String[] { tmpPath };
		return files;
	}

	private void buildConcatenatedFile(ConfigurationItem item, String[] files, File tmpFile)
			throws IOException {
		for (String path : files) {
			File original = new File(item.getSourceDirectory(), path);
			System.out.println(original.getAbsolutePath());
			String content = FileUtils.readFileToString(original);
			FileUtils.write(tmpFile, content, true);
		}
	}

	private void deleteTemporaryFile(File tmpFile) {
		tmpFile.delete();
	}

	private boolean isFilesUpdated(ConfigurationItem item, String[] files, File tmpFile) {
		boolean updated = false;
		for(String path: files){
			File original = new File(item.getSourceDirectory(), path);
			if(original.lastModified() > tmpFile.lastModified()){
				updated = true;
			}
		}
		return updated;
	}

	private void compileIfChanged(String[] files, LessCompiler lessCompiler,
			ConfigurationItem item) throws MojoExecutionException {
		for (String file : files) {
			File input = new File(item.getSourceDirectory(), file);

			buildContext.removeMessages(input);

			File output = new File(item.getOutputDirectory(), file.replace(".less", ".css"));

			if (!output.getParentFile().exists() && !output.getParentFile().mkdirs()) {
				throw new MojoExecutionException("Cannot create output directory "
						+ output.getParentFile());
			}

			try {
				LessSource lessSource = new LessSource(input);
				if (output.lastModified() < lessSource.getLastModifiedIncludingImports()) {
					long compilationStarted = System.currentTimeMillis();
					getLog().info("Compiling LESS source: " + file + "...");
					lessCompiler.compile(lessSource, output, force);
					buildContext.refresh(output);
					getLog().info(
							"Finished compilation to " + outputDirectory + " in "
									+ (System.currentTimeMillis() - compilationStarted) + " ms");
				} else if (!watch) {
					getLog().info("Bypassing LESS source: " + file + " (not modified)");
				}
			} catch (IOException e) {
				buildContext.addMessage(input, 0, 0, "Error compiling LESS source",
						BuildContext.SEVERITY_ERROR, e);
				throw new MojoExecutionException("Error while compiling LESS source: " + file, e);
			} catch (LessException e) {
				String message = e.getMessage();
				if (StringUtils.isEmpty(message)) {
					message = "Error compiling LESS source";
				}
				buildContext.addMessage(input, 0, 0, "Error compiling LESS source",
						BuildContext.SEVERITY_ERROR, e);
				throw new MojoExecutionException("Error while compiling LESS source: " + file, e);
			}
		}
	}

	
	public List<ConfigurationItem> getConfiguration() {
		if (this.configurationItems == null) {
			configurationItems = new ArrayList<ConfigurationItem>();
		}else{
			//TODO: Should not need to do this but somehow an extra configuration 
			//TODO: is included... 
			return configurationItems; 
		}
		

		ConfigurationItem configurationItem = new ConfigurationItem();
		boolean configured = false;
		if (sourceDirectory != null) {
			configurationItem.setSourceDirectory(sourceDirectory);
			configured = true;
		}
		if (outputDirectory != null) {
			configurationItem.setOutputDirectory(outputDirectory);
			configured = true;
		}
		if (excludes.length > 0) {
			configurationItem.setExcludes(excludes);
			configured = true;
		}
		if (includes[0] != INCLUDES_DEFAULT_VALUE) {
			configurationItem.setIncludes(includes);
			configured = true;
		}
		if (compress) {
			configurationItem.setCompress(compress);
			configured = true;
		}
		if (watch) {
			configurationItem.setWatch(watch);
			if (watchInterval != WATCH_INTERVAL_DEFAULT) {
				configurationItem.setWatchInterval(watchInterval);
			}else{
				configurationItem.setWatchInterval(WATCH_INTERVAL_DEFAULT);
			}
			configured = true;
		}
		if (encoding != null) {
			configurationItem.setEncoding(encoding);
			configured = true;
		}
		if (force) {
			configurationItem.setForce(force);
			configured = true;
		}
		if (lessJs != null) {
			configurationItem.setLessJs(lessJs);
			configured = true;
		}
		if (concatenateTo != null) {
			configurationItem.setConcatenateTo(concatenateTo);
			configured = true;
		}

		if (configured) {
			configurationItems.add(configurationItem);
		}

		return configurationItems;
	}
}
