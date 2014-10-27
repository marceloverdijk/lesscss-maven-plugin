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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.lesscss.LessSource;

/**
 * Goal which list the LESS sources and its imports.
 * 
 * @author Marcel Overdijk
 */
@Mojo( name = "list" )
public class ListMojo extends AbstractLessCssMojo {

	/**
	 * Execute the MOJO.
	 * 
	 * @throws MojoExecutionException
	 *             if something unexpected occurs.
	 */
	public void execute() throws MojoExecutionException {
		if (getLog().isDebugEnabled()) {
			getLog().debug("sourceDirectory = " + sourceDirectory);
			getLog().debug("includes = " + Arrays.toString(includes));
			getLog().debug("excludes = " + Arrays.toString(excludes));
		}

		String[] files = getIncludedFiles();

		if (files == null || files.length < 1) {
			getLog().info("No LESS sources found");
		} else {
			getLog().info("The following LESS sources have been resolved:");

			for (String file : files) {
				File lessFile = new File(sourceDirectory, file);
				try {
					LessSource lessSource = new LessSource(lessFile);
					listLessSource(lessSource, file, 0, false);
				} catch (FileNotFoundException e) {
					throw new MojoExecutionException("Error while loading LESS source: " + lessFile.getAbsolutePath(), e);
				} catch (IOException e) {
					throw new MojoExecutionException("Error while loading LESS source: " + lessFile.getAbsolutePath(), e);
				}
			}
		}
	}

	private void listLessSource(LessSource lessSource, String path, int level, boolean last) {
		String prefix = "";
		if (level > 0) {
			for (int i = 1; i <= level; i++) {
				if (i == level && last) {
					prefix = prefix + "`-- ";
				} else if (i == level) {
					prefix = prefix + "|-- ";
				} else {
					prefix = prefix + "|   ";
				}
			}
		}

		getLog().info(prefix + path);

		Iterator<Entry<String, LessSource>> it = lessSource.getImports().entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, LessSource> entry = it.next();
			listLessSource(entry.getValue(), entry.getKey(), level + 1, !it.hasNext());
		}
	}
}
