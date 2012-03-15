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

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.codehaus.plexus.util.Scanner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lesscss.mojo.AbstractLessCssMojo;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.sonatype.plexus.build.incremental.BuildContext;

@PrepareForTest(AbstractLessCssMojo.class)
@RunWith(PowerMockRunner.class)
public class AbstractLessCssMojoTest extends AbstractMojoTestCase {

	private AbstractLessCssMojo mojo;

	private File sourceDirectory = new File("./source");

	private String[] includes = new String[] { "include" };

	private String[] excludes = new String[] { "exclude" };

	private String[] files = new String[] { "file" };

	@Mock
	private BuildContext buildContext;

	@Mock
	private Scanner scanner;

	@Before
	public void setUp() throws Exception {
		mojo = new AbstractLessCssMojo() {
			public void execute() throws MojoExecutionException {
			}
		};

		setVariableValueToObject(mojo, "buildContext", buildContext);
		setVariableValueToObject(mojo, "sourceDirectory", sourceDirectory);
		setVariableValueToObject(mojo, "includes", includes);
		setVariableValueToObject(mojo, "excludes", excludes);
	}

	@Test
	public void testGetFiles() throws Exception {
		when(buildContext.newScanner(sourceDirectory, true)).thenReturn(scanner);
		when(scanner.getIncludedFiles()).thenReturn(files);

		assertSame(files, mojo.getIncludedFiles());

		verify(buildContext).newScanner(same(sourceDirectory), eq(true));
		verify(scanner).setIncludes(same(includes));
		verify(scanner).setExcludes(same(excludes));
		verify(scanner).scan();
	}

	@After
	public void tearDown() {
	}
}
