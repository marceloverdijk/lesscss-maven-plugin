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
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.codehaus.plexus.util.Scanner;
import org.sonatype.plexus.build.incremental.BuildContext;

/**
 * Abstract class which provides common configuration properties and methods.
 * 
 * @author Marcel Overdijk
 */
public abstract class AbstractLessCssMojo extends AbstractMojo {

	/** @component */
	protected BuildContext buildContext;

	/**
	 * Using configurationItems allows you to have several different compilations on a single project
	 * 
	 * @parameter
	 */
	protected List<ConfigurationItem> configurationItems;

	/**
	 * The source directory containing the LESS sources.
	 * 
	 * @parameter expression="${lesscss.sourceDirectory}" default-value="${project.basedir}/src/main/less"
	 * @required
	 */
	protected File sourceDirectory;

	/**
	 * List of files to include. Specified as fileset patterns which are relative to the source directory. Default value is: { "**\/*.less" }
	 * 
	 * @parameter
	 */
	protected String[] includes = new String[] { INCLUDES_DEFAULT_VALUE };

	/**
	 * List of files to exclude. Specified as fileset patterns which are relative to the source directory.
	 * 
	 * @parameter
	 */
	protected String[] excludes = new String[] {};

	static final String INCLUDES_DEFAULT_VALUE = "**/*.less";

	/**
	 * Scans for the LESS sources that should be compiled.
	 * 
	 * @return The list of LESS sources.
	 */
	protected String[] getIncludedFiles(ConfigurationItem configurationItem) {
		Scanner scanner = buildContext.newScanner(configurationItem.getSourceDirectory(), true);
		scanner.setIncludes(configurationItem.getIncludes());
		scanner.setExcludes(configurationItem.getExcludes());
		scanner.scan();
		return scanner.getIncludedFiles();
	}

	/**
	 * Get configuration
	 * 
	 * @return a list of ConfigurationItems read from the plugin-configuration
	 */
	protected abstract List<ConfigurationItem> getConfiguration();
}
