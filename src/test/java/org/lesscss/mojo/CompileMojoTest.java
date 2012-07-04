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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.doThrow;
import static org.powermock.api.mockito.PowerMockito.verifyNew;
import static org.powermock.api.mockito.PowerMockito.verifyNoMoreInteractions;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.codehaus.plexus.util.Scanner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lesscss.LessCompiler;
import org.lesscss.LessException;
import org.lesscss.LessSource;
import org.lesscss.mojo.CompileMojo;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.sonatype.plexus.build.incremental.BuildContext;

@PrepareForTest(CompileMojo.class)
@RunWith(PowerMockRunner.class)
public class CompileMojoTest extends AbstractMojoTestCase {

	private CompileMojo mojo;

	private File sourceDirectory = new File("./source");

	private File outputDirectory = new File("./output");

	private String[] includes = new String[] { "include" };

	private String[] excludes = new String[] { "exclude" };

	private String[] files = new String[] { "file" };

	@Mock
	private Log log;

	@Mock
	private BuildContext buildContext;

	@Mock
	private Scanner scanner;

	@Mock
	private LessCompiler lessCompiler;

	@Mock
	private File lessJs;

	@Mock
	private URI lessJsURI;

	@Mock
	private URL lessJsURL;

	@Mock
	private File input;

	@Mock
	private File output;

	@Mock
	private File parent;

	@Mock
	private LessSource lessSource;

	@Before
	public void setUp() throws URISyntaxException, IllegalAccessException, IOException {
		mojo = new CompileMojo();
		mojo.setLog(log);

		setVariableValueToObject(mojo, "buildContext", buildContext);
		setVariableValueToObject(mojo, "sourceDirectory", sourceDirectory);
		setVariableValueToObject(mojo, "outputDirectory", outputDirectory);
		setVariableValueToObject(mojo, "includes", includes);
		setVariableValueToObject(mojo, "excludes", excludes);
	}

	@Test
	public void testExecution() throws Exception {
		files = new String[] { "less.less" };

		when(buildContext.newScanner(sourceDirectory, true)).thenReturn(scanner);
		when(scanner.getIncludedFiles()).thenReturn(files);

		whenNew(LessCompiler.class).withNoArguments().thenReturn(lessCompiler);

		whenNew(File.class).withArguments(sourceDirectory, "less.less").thenReturn(input);
		whenNew(File.class).withArguments(outputDirectory, "less.css").thenReturn(output);

        when(output.exists()).thenReturn(true);
		when(output.getParentFile()).thenReturn(parent);
		when(parent.exists()).thenReturn(true);

		whenNew(LessSource.class).withArguments(input).thenReturn(lessSource);

		when(output.lastModified()).thenReturn(1l);
		when(lessSource.getLastModifiedIncludingImports()).thenReturn(2l);

		mojo.execute();

		verify(buildContext).newScanner(same(sourceDirectory), eq(true));
		verify(scanner).setIncludes(same(includes));
		verify(scanner).setExcludes(same(excludes));
		verify(scanner).scan();

		verifyNew(LessCompiler.class).withNoArguments();
		verify(lessCompiler).setCompress(false);
		verify(lessCompiler).setEncoding(null);

		verifyNew(File.class).withArguments(sourceDirectory, "less.less");
		verifyNew(File.class).withArguments(outputDirectory, "less.css");

		verify(output).getParentFile();
		verify(parent).exists();

		verifyNew(LessSource.class).withArguments(input);

		verify(output).lastModified();
		verify(lessSource).getLastModifiedIncludingImports();

		verify(log).info("Compiling LESS source: less.less...");
		verify(lessCompiler).compile(lessSource, output, false);
	}

	@Test
	public void testExecutionNotModified() throws Exception {
		files = new String[] { "less.less" };

		when(buildContext.newScanner(sourceDirectory, true)).thenReturn(scanner);
		when(scanner.getIncludedFiles()).thenReturn(files);

		whenNew(LessCompiler.class).withNoArguments().thenReturn(lessCompiler);

		whenNew(File.class).withArguments(sourceDirectory, "less.less").thenReturn(input);
		whenNew(File.class).withArguments(outputDirectory, "less.css").thenReturn(output);

        when(output.exists()).thenReturn(true);
		when(output.getParentFile()).thenReturn(parent);
		when(parent.exists()).thenReturn(true);

		whenNew(LessSource.class).withArguments(input).thenReturn(lessSource);

		when(output.lastModified()).thenReturn(2l);
		when(lessSource.getLastModifiedIncludingImports()).thenReturn(1l);

		mojo.execute();

		verify(buildContext).newScanner(same(sourceDirectory), eq(true));
		verify(scanner).setIncludes(same(includes));
		verify(scanner).setExcludes(same(excludes));
		verify(scanner).scan();

		verifyNew(LessCompiler.class).withNoArguments();
		verify(lessCompiler).setCompress(false);
		verify(lessCompiler).setEncoding(null);

		verifyNew(File.class).withArguments(sourceDirectory, "less.less");
		verifyNew(File.class).withArguments(outputDirectory, "less.css");

		verify(output).getParentFile();
		verify(parent).exists();

		verifyNew(LessSource.class).withArguments(input);
		verify(lessCompiler).setCompress(false);
		verify(lessCompiler).setEncoding(null);

		verify(output).lastModified();
		verify(lessSource).getLastModifiedIncludingImports();

		verify(log).info("Bypassing LESS source: less.less (not modified)");
		verifyNoMoreInteractions(lessCompiler);
	}

	@Test
	public void testExecutionIncludedFilesEmpty() throws Exception {
		files = new String[] {};

		when(buildContext.newScanner(sourceDirectory, true)).thenReturn(scanner);
		when(scanner.getIncludedFiles()).thenReturn(files);

		mojo.execute();

		verify(log).info("Nothing to compile - no LESS sources found");
	}

	@Test
	public void testExecutionIncludedFilesNull() throws Exception {
		files = null;

		when(buildContext.newScanner(sourceDirectory, true)).thenReturn(scanner);
		when(scanner.getIncludedFiles()).thenReturn(files);

		mojo.execute();

		verify(log).info("Nothing to compile - no LESS sources found");
	}

	@Test(expected = MojoExecutionException.class)
	public void testExecutionIOExceptionWhenCreatingLessSource() throws Exception {
		files = new String[] { "less.less" };

		when(buildContext.newScanner(sourceDirectory, true)).thenReturn(scanner);
		when(scanner.getIncludedFiles()).thenReturn(files);

		whenNew(LessCompiler.class).withNoArguments().thenReturn(lessCompiler);

		whenNew(File.class).withArguments(sourceDirectory, "less.less").thenReturn(input);
		whenNew(File.class).withArguments(outputDirectory, "less.css").thenReturn(output);

		when(output.getParentFile()).thenReturn(parent);
		when(parent.exists()).thenReturn(true);

		whenNew(LessSource.class).withArguments(input).thenThrow(new IOException());

		mojo.execute();

		verify(buildContext).newScanner(same(sourceDirectory), eq(true));
		verify(scanner).setIncludes(same(includes));
		verify(scanner).setExcludes(same(excludes));
		verify(scanner).scan();

		verifyNew(LessCompiler.class).withNoArguments();
		verify(lessCompiler).setCompress(false);
		verify(lessCompiler).setEncoding(null);

		verifyNew(File.class).withArguments(sourceDirectory, "less.less");
		verifyNew(File.class).withArguments(outputDirectory, "less.css");

		verify(output).getParentFile();
		verify(parent).exists();

		verifyNew(LessSource.class).withArguments(input);
	}

	@Test(expected = MojoExecutionException.class)
	public void testExecutionLessExceptionWhenCompilingLessSource() throws Exception {
		files = new String[] { "less.less" };

		when(buildContext.newScanner(sourceDirectory, true)).thenReturn(scanner);
		when(scanner.getIncludedFiles()).thenReturn(files);

		whenNew(LessCompiler.class).withNoArguments().thenReturn(lessCompiler);

		whenNew(File.class).withArguments(sourceDirectory, "less.less").thenReturn(input);
		whenNew(File.class).withArguments(outputDirectory, "less.css").thenReturn(output);

		when(output.getParentFile()).thenReturn(parent);
		when(parent.exists()).thenReturn(true);

		whenNew(LessSource.class).withArguments(input).thenReturn(lessSource);

		when(output.lastModified()).thenReturn(1l);
		when(lessSource.getLastModifiedIncludingImports()).thenReturn(2l);

		doThrow(new LessException(new Throwable())).when(lessCompiler).compile(lessSource, output, false);

		mojo.execute();

		verify(buildContext).newScanner(same(sourceDirectory), eq(true));
		verify(scanner).setIncludes(same(includes));
		verify(scanner).setExcludes(same(excludes));
		verify(scanner).scan();

		verifyNew(LessCompiler.class).withNoArguments();
		verify(lessCompiler).setCompress(false);
		verify(lessCompiler).setEncoding(null);

		verifyNew(File.class).withArguments(sourceDirectory, "less.less");
		verifyNew(File.class).withArguments(outputDirectory, "less.css");

		verify(output).getParentFile();
		verify(parent).exists();

		verifyNew(LessSource.class).withArguments(input);

		verify(output).lastModified();
		verify(lessSource).getLastModifiedIncludingImports();

		verify(log).info("Compiling LESS source: less.less...");
		verify(lessCompiler).compile(lessSource, output, false);
	}

	@Test
	public void testExecutionWithCustomLessJs() throws Exception {
		setVariableValueToObject(mojo, "lessJs", lessJs);

		files = new String[] { "less.less" };

		when(buildContext.newScanner(sourceDirectory, true)).thenReturn(scanner);
		when(scanner.getIncludedFiles()).thenReturn(files);

		whenNew(LessCompiler.class).withNoArguments().thenReturn(lessCompiler);

		when(lessJs.toURI()).thenReturn(lessJsURI);
		when(lessJsURI.toURL()).thenReturn(lessJsURL);

		whenNew(File.class).withArguments(sourceDirectory, "less.less").thenReturn(input);
		whenNew(File.class).withArguments(outputDirectory, "less.css").thenReturn(output);

        when(output.exists()).thenReturn(true);
		when(output.getParentFile()).thenReturn(parent);
		when(parent.exists()).thenReturn(true);

		whenNew(LessSource.class).withArguments(input).thenReturn(lessSource);

		when(output.lastModified()).thenReturn(1l);
		when(lessSource.getLastModifiedIncludingImports()).thenReturn(2l);

		mojo.execute();

		verify(buildContext).newScanner(same(sourceDirectory), eq(true));
		verify(scanner).setIncludes(same(includes));
		verify(scanner).setExcludes(same(excludes));
		verify(scanner).scan();

		verifyNew(LessCompiler.class).withNoArguments();
		verify(lessCompiler).setCompress(false);
		verify(lessCompiler).setEncoding(null);
		verify(lessCompiler).setLessJs(lessJsURL);

		verifyNew(File.class).withArguments(sourceDirectory, "less.less");
		verifyNew(File.class).withArguments(outputDirectory, "less.css");

		verify(output).getParentFile();
		verify(parent).exists();

		verifyNew(LessSource.class).withArguments(input);

		verify(output).lastModified();
		verify(lessSource).getLastModifiedIncludingImports();

		verify(log).info("Compiling LESS source: less.less...");
		verify(lessCompiler).compile(lessSource, output, false);
	}

	@Test(expected = MojoExecutionException.class)
	public void testExecutionMalformedURLExceptionWhenCustomLessJs() throws Exception {
		setVariableValueToObject(mojo, "lessJs", lessJs);

		files = new String[] { "less.less" };

		when(buildContext.newScanner(sourceDirectory, true)).thenReturn(scanner);
		when(scanner.getIncludedFiles()).thenReturn(files);

		whenNew(LessCompiler.class).withNoArguments().thenReturn(lessCompiler);

		when(lessJs.toURI()).thenReturn(lessJsURI);
		when(lessJsURI.toURL()).thenThrow(new MalformedURLException());

		mojo.execute();

		verify(buildContext).newScanner(same(sourceDirectory), eq(true));
		verify(scanner).setIncludes(same(includes));
		verify(scanner).setExcludes(same(excludes));
		verify(scanner).scan();

		verifyNew(LessCompiler.class).withNoArguments();
		verify(lessCompiler).setCompress(false);
		verify(lessCompiler).setEncoding(null);
	}

	@Test
	public void testExecutionMakeDirsWhenOutputDirectoryDoesNotExists() throws Exception {
		files = new String[] { "less.less" };

		when(buildContext.newScanner(sourceDirectory, true)).thenReturn(scanner);
		when(scanner.getIncludedFiles()).thenReturn(files);

		whenNew(LessCompiler.class).withNoArguments().thenReturn(lessCompiler);

		whenNew(File.class).withArguments(sourceDirectory, "less.less").thenReturn(input);
		whenNew(File.class).withArguments(outputDirectory, "less.css").thenReturn(output);

        when(output.exists()).thenReturn(true);
		when(output.getParentFile()).thenReturn(parent);
		when(parent.exists()).thenReturn(false);
		when(parent.mkdirs()).thenReturn(true);

		whenNew(LessSource.class).withArguments(input).thenReturn(lessSource);

		when(output.lastModified()).thenReturn(1l);
		when(lessSource.getLastModifiedIncludingImports()).thenReturn(2l);

		mojo.execute();

		verify(buildContext).newScanner(same(sourceDirectory), eq(true));
		verify(scanner).setIncludes(same(includes));
		verify(scanner).setExcludes(same(excludes));
		verify(scanner).scan();

		verifyNew(LessCompiler.class).withNoArguments();
		verify(lessCompiler).setCompress(false);
		verify(lessCompiler).setEncoding(null);

		verifyNew(File.class).withArguments(sourceDirectory, "less.less");
		verifyNew(File.class).withArguments(outputDirectory, "less.css");

		verify(output, times(2)).getParentFile();
		verify(parent).exists();
		verify(parent).mkdirs();

		verifyNew(LessSource.class).withArguments(input);

		verify(output).lastModified();
		verify(lessSource).getLastModifiedIncludingImports();

		verify(log).info("Compiling LESS source: less.less...");
		verify(lessCompiler).compile(lessSource, output, false);
	}

	@Test(expected = MojoExecutionException.class)
	public void testExecutionMakeDirsFailsWhenOutputDirectoryDoesNotExists() throws Exception {
		files = new String[] { "less.less" };

		when(buildContext.newScanner(sourceDirectory, true)).thenReturn(scanner);
		when(scanner.getIncludedFiles()).thenReturn(files);

		whenNew(LessCompiler.class).withNoArguments().thenReturn(lessCompiler);

		whenNew(File.class).withArguments(sourceDirectory, "less.less").thenReturn(input);
		whenNew(File.class).withArguments(outputDirectory, "less.css").thenReturn(output);

		when(output.getParentFile()).thenReturn(parent);
		when(parent.exists()).thenReturn(false);
		when(parent.mkdirs()).thenReturn(false);

		mojo.execute();

		verify(buildContext).newScanner(same(sourceDirectory), eq(true));
		verify(scanner).setIncludes(same(includes));
		verify(scanner).setExcludes(same(excludes));
		verify(scanner).scan();

		verifyNew(LessCompiler.class).withNoArguments();
		verify(lessCompiler).setCompress(false);
		verify(lessCompiler).setEncoding(null);

		verifyNew(File.class).withArguments(sourceDirectory, "less.less");
		verifyNew(File.class).withArguments(outputDirectory, "less.css");

		verify(output, times(2)).getParentFile();
		verify(parent).exists();
		verify(parent).mkdirs();
	}

	@After
	public void tearDown() {
	}
}
