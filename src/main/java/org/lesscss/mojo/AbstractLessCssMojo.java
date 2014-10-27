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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.Scanner;
import org.sonatype.plexus.build.incremental.BuildContext;

/**
 * Abstract class which provides common configuration properties and methods.
 * 
 * @author Marcel Overdijk
 */
public abstract class AbstractLessCssMojo extends AbstractMojo {

    @Component
	protected BuildContext buildContext;

	/**
	 * The source directory containing the LESS sources.
	 * 
	 */
    @Parameter( defaultValue = "${project.basedir}/src/main/less", property = "lesscss.sourceDirectory", required = true)
	protected File sourceDirectory;

	/**
	 * List of files to include. Specified as fileset patterns which are relative to the source directory. Default value is: { "**\/*.less" }
	 * 
	 */
    @Parameter
	protected String[] includes = new String[] { "**/*.less" };

	/**
	 * List of files to exclude. Specified as fileset patterns which are relative to the source directory.
	 * 
	 */
    @Parameter
	protected String[] excludes = new String[] {};

	/**
	 * Scans for the LESS sources that should be compiled.
	 * 
	 * @return The list of LESS sources.
	 */
	protected String[] getIncludedFiles() {
		Scanner scanner = buildContext.newScanner(sourceDirectory, true);
		scanner.setIncludes(includes);
		scanner.setExcludes(excludes);
		scanner.scan();
		return scanner.getIncludedFiles();
	}
	
	/**
	 * Whether to skip plugin execution. 
	 * This makes the build more controllable from profiles.
	 * 
	 */
    @Parameter( defaultValue = "false", property = "lesscss.skip")
	protected boolean skip;
}
