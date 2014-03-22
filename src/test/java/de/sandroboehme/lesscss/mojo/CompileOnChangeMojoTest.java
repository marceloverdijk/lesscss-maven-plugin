package de.sandroboehme.lesscss.mojo;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.sandroboehme.lesscss.mojo.CompileMojo;

/**
 * Tests the watch mode of the CompileMojo.
 */
public class CompileOnChangeMojoTest extends AbstractMojoTestCase {
	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	/** {@inheritDoc} */
	protected void setUp() throws Exception {
		// required
		super.setUp();
	}

	/** {@inheritDoc} */
	protected void tearDown() throws Exception {
		// required
		super.tearDown();
	}

	/**
	 * @throws Exception
	 *             This test touches two files and checks if the bootstrap.css
	 *             file has been recompiled after every touch.
	 */
	@Test
	public void testIfFileChangeCausesRecompilation() throws Exception {
		File pom = getTestFile("src/test/resources/pom.xml");
		assertNotNull(pom);
		assertTrue(pom.exists());
		final CompileMojo compileMojo = (CompileMojo) lookupMojo("compile", pom);
		assertNotNull(compileMojo);

		File generatedFile = new File(compileMojo.outputDirectory, "bootstrap.css");
		long oldLastModified = generatedFile.lastModified();

		compileMojo.outputDirectory = tempFolder.newFolder();

		/*
		 * Run the watching compileMojo in a new thread. This way we can test
		 * recompilation and stop the watching mode asynchronous in this thread.
		 */
		Thread watchThread = new Thread() {
			public void run() {
				try {
					compileMojo.execute();
				} catch (MojoExecutionException e) {
					assertTrue(e.getLongMessage(), true);
				}
			}
		};
		watchThread.start();

		long newLastModified = checkIfFileHasBeenChanged(compileMojo, oldLastModified);

		touchFile(compileMojo, "1/reset.less");
		newLastModified = checkIfFileHasBeenChanged(compileMojo, newLastModified);

		touchFile(compileMojo, "2/21/variables.less");
		checkIfFileHasBeenChanged(compileMojo, newLastModified);
		watchThread.interrupt();
	}

	private long checkIfFileHasBeenChanged(final CompileMojo compileMojo, long lastModified) throws InterruptedException {
		long newLastModified = lastModified;
		long oldLastModified = lastModified;

		File generatedFile = null;
		long startMillies = System.currentTimeMillis();
		long currentMillies = startMillies;
		// check for a maximum of ten seconds if the file has been changed
		while (oldLastModified >= newLastModified && ((currentMillies - startMillies) < 10000)) {
			Thread.sleep(200); // wait for 0.2 seconds
			generatedFile = new File(compileMojo.outputDirectory, "bootstrap.css");
			newLastModified = generatedFile.lastModified();
			currentMillies = System.currentTimeMillis();
		}
		assertTrue("No recompilation has been done within " + (currentMillies - startMillies) + " ms.",
				newLastModified > oldLastModified);
		return newLastModified;
	}

	private long touchFile(final CompileMojo compileMojo, String file2Touch) throws InterruptedException {
		File lessFile = new File(compileMojo.sourceDirectory, file2Touch);

		/*
		 * The last modified value might be rounded down on a second by the
		 * platform. This way the last compilation and the touch may end up with
		 * the same millis avoiding the compilation. To prevent that the thread
		 * has to sleep a second.
		 */
		Thread.sleep(1000);
		long currentMillies = System.currentTimeMillis();
		// touch the less file
		lessFile.setLastModified(currentMillies);
		return currentMillies;
	}
}
