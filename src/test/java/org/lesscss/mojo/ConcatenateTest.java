package org.lesscss.mojo;

import java.io.File;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.junit.Test;

public class ConcatenateTest extends AbstractMojoTestCase {

	@Test
	public void testDontUpdateIfNotChanged() throws Exception {
		File pom = getTestFile("src/test/resources/concatenate-pom.xml");
		final CompileMojo compileMojo = (CompileMojo) lookupMojo("compile", pom);
		
		File expected = new File(compileMojo.sourceDirectory, "concatenated.less");
		if(expected.exists()){
			expected.delete();
		}
		
		compileMojo.execute();
		expected = new File(compileMojo.sourceDirectory, "concatenated.less");
		
		compileMojo.execute();
		File actual = new File(compileMojo.sourceDirectory, "concatenated.less");
		
		assertEquals(expected.lastModified(), actual.lastModified());
	}
	
	@Test
	public void testProducesCssFile() throws Exception{
		File pom = getTestFile("src/test/resources/concatenate-pom.xml");
		final CompileMojo compileMojo = (CompileMojo) lookupMojo("compile", pom);
		compileMojo.execute();
		
		File expected = new File(compileMojo.outputDirectory, "concatenated.css");
		assertTrue(expected.exists());
	}
}
