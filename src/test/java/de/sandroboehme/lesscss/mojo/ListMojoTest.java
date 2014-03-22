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

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.codehaus.plexus.util.Scanner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.sonatype.plexus.build.incremental.BuildContext;

import de.sandroboehme.lesscss.LessSource;

@PrepareForTest(ListMojo.class)
@RunWith(PowerMockRunner.class)
public class ListMojoTest extends AbstractMojoTestCase {

	private ListMojo mojo;

	private File sourceDirectory = new File("./source");

	private String[] includes = new String[] { "include" };

	private String[] excludes = new String[] { "exclude" };

	private String[] files = new String[] { "file" };

	@Mock
	private Log log;

	@Mock
	private LessSource lessSource1;

	@Mock
	private LessSource lessSource1import1;

	@Mock
	private LessSource lessSource1import1a;

	@Mock
	private LessSource lessSource1import2;

	@Mock
	private LessSource lessSource1import2a;

	@Mock
	private LessSource lessSource1import2b;

	@Mock
	private LessSource lessSource1import3;

	@Mock
	private LessSource lessSource2;

	@Mock
	private BuildContext buildContext;

	@Mock
	private Scanner scanner;

	@Before
	public void setUp() throws Exception {
		mojo = new ListMojo();
		mojo.setLog(log);

		setVariableValueToObject(mojo, "buildContext", buildContext);
		setVariableValueToObject(mojo, "sourceDirectory", sourceDirectory);
		setVariableValueToObject(mojo, "includes", includes);
		setVariableValueToObject(mojo, "excludes", excludes);
	}

	@SuppressWarnings("serial")
	@Test
	public void testExecution() throws Exception {
		files = new String[] { "less1.less", "less2.less" };

		when(buildContext.newScanner(sourceDirectory, true)).thenReturn(scanner);
		when(scanner.getIncludedFiles()).thenReturn(files);

		whenNew(LessSource.class).withArguments(new File(sourceDirectory, "less1.less")).thenReturn(lessSource1);
		when(lessSource1.getImports()).thenReturn(new LinkedHashMap<String, LessSource>() {
			{
				put("less1import1.less", lessSource1import1);
				put("less1import2.less", lessSource1import2);
				put("less1import3.less", lessSource1import3);
			}
		});
		when(lessSource1import1.getImports()).thenReturn(new LinkedHashMap<String, LessSource>() {
			{
				put("less1import1a.less", lessSource1import1a);
			}
		});
		when(lessSource1import2.getImports()).thenReturn(new LinkedHashMap<String, LessSource>() {
			{
				put("less1import2a.less", lessSource1import2a);
				put("less1import2b.less", lessSource1import2b);
			}
		});

		whenNew(LessSource.class).withArguments(new File(sourceDirectory, "less2.less")).thenReturn(lessSource2);

		mojo.execute();

		InOrder inOrder = inOrder(log);
		inOrder.verify(log).info("The following LESS sources have been resolved:");
		inOrder.verify(log).info("less1.less");
		inOrder.verify(log).info("|-- less1import1.less");
		inOrder.verify(log).info("|   `-- less1import1a.less");
		inOrder.verify(log).info("|-- less1import2.less");
		inOrder.verify(log).info("|   |-- less1import2a.less");
		inOrder.verify(log).info("|   `-- less1import2b.less");
		inOrder.verify(log).info("`-- less1import3.less");
		inOrder.verify(log).info("less2.less");
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void testExecutionIncludedFilesEmpty() throws Exception {
		files = new String[] {};

		when(buildContext.newScanner(sourceDirectory, true)).thenReturn(scanner);
		when(scanner.getIncludedFiles()).thenReturn(files);

		mojo.execute();

		verify(log).info("No LESS sources found");
	}

	@Test
	public void testExecutionIncludedFilesNull() throws Exception {
		files = null;

		when(buildContext.newScanner(sourceDirectory, true)).thenReturn(scanner);
		when(scanner.getIncludedFiles()).thenReturn(files);

		mojo.execute();

		verify(log).info("No LESS sources found");
	}

	@Test(expected = MojoExecutionException.class)
	public void testExecutionFileNotFoundException() throws Exception {
		files = new String[] { "less.less" };

		when(buildContext.newScanner(sourceDirectory, true)).thenReturn(scanner);
		when(scanner.getIncludedFiles()).thenReturn(files);

		whenNew(LessSource.class).withArguments(new File(sourceDirectory, "less.less")).thenThrow(new FileNotFoundException(""));

		mojo.execute();
	}

	@Test(expected = MojoExecutionException.class)
	public void testExecutionIOException() throws Exception {
		files = new String[] { "less.less" };

		when(buildContext.newScanner(sourceDirectory, true)).thenReturn(scanner);
		when(scanner.getIncludedFiles()).thenReturn(files);

		whenNew(LessSource.class).withArguments(new File(sourceDirectory, "less.less")).thenThrow(new IOException(""));

		mojo.execute();
	}

	@After
	public void tearDown() {
	}
}
