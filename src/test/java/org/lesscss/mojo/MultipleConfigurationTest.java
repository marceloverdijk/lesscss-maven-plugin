package org.lesscss.mojo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.junit.Test;

public class MultipleConfigurationTest extends AbstractMojoTestCase {

	private CompileMojo mojo;

	private File sourceDirectory = new File("./source");
	private File outputDirectory = new File("./output");
	private String[] includes = new String[] { "include" };
	private String[] excludes = new String[] { "exclude" };
	private boolean compress = true;
	private boolean watch = true;
	private int watchInterval = 3600;
	private String encoding = "UTF-8";
	private boolean force = true;
	private File lessJs = new File("./lessJs");
	private String concatenateTo = "test.css";

	private String[] files = new String[] { "file" };

	@Test
	public void testNeverReturnNullConfigurations() {
		mojo = new CompileMojo();
		List<ConfigurationItem> configurationItems = mojo.getConfiguration();
		assertNotNull(configurationItems);
		assertEquals(0, configurationItems.size());
	}

	@Test
	public void testGetSimpleSingleConfiguration() throws IllegalAccessException {
		mojo = new CompileMojo();

		setVariableValueToObject(mojo, "sourceDirectory", sourceDirectory);
		setVariableValueToObject(mojo, "outputDirectory", outputDirectory);
		setVariableValueToObject(mojo, "includes", includes);
		setVariableValueToObject(mojo, "excludes", excludes);
		setVariableValueToObject(mojo, "compress", compress);
		setVariableValueToObject(mojo, "watch", watch);
		setVariableValueToObject(mojo, "watchInterval", watchInterval);
		setVariableValueToObject(mojo, "force", force);
		setVariableValueToObject(mojo, "lessJs", lessJs);
		setVariableValueToObject(mojo, "concatenateTo", concatenateTo);

		List<ConfigurationItem> configurationItems = mojo.getConfiguration();
		assertEquals(1, configurationItems.size());

		ConfigurationItem item = configurationItems.get(0);
		assertEquals(sourceDirectory.getAbsolutePath(), item.getSourceDirectory()
				.getAbsolutePath());
		assertEquals(outputDirectory.getAbsolutePath(), item.getOutputDirectory()
				.getAbsolutePath());
		assertEquals(includes, item.getIncludes());
		assertEquals(excludes, item.getExcludes());
		assertEquals(compress, item.isCompress());
		assertEquals(watch, item.isWatch());
		assertEquals(watchInterval, item.getWatchInterval());
		assertEquals(force, item.isForce());
		assertEquals(lessJs, item.getLessJs());
		assertEquals(concatenateTo, item.getConcateanteTo());
	}

	@Test
	public void testSingleConfigurationItem() throws IllegalAccessException {
		ConfigurationItem configurationItem = new ConfigurationItem();
		configurationItem.setExcludes(excludes);
		configurationItem.setIncludes(includes);
		configurationItem.setOutputDirectory(outputDirectory);
		configurationItem.setSourceDirectory(sourceDirectory);
		List<ConfigurationItem> original = new ArrayList<ConfigurationItem>();
		original.add(configurationItem);

		mojo = new CompileMojo();
		setVariableValueToObject(mojo, "configurationItems", original);

		List<ConfigurationItem> actual = mojo.getConfiguration();
		assertEquals(1, actual.size());

		ConfigurationItem item = actual.get(0);
		assertEquals(sourceDirectory.getAbsolutePath(), item.getSourceDirectory()
				.getAbsolutePath());
		assertEquals(outputDirectory.getAbsolutePath(), item.getOutputDirectory()
				.getAbsolutePath());
		assertEquals(includes, item.getIncludes());
		assertEquals(excludes, item.getExcludes());
	}

	@Test
	public void testTwoConfigurationItems() throws IllegalAccessException {
		ConfigurationItem configurationItem = new ConfigurationItem();
		configurationItem.setExcludes(excludes);
		configurationItem.setIncludes(includes);
		configurationItem.setOutputDirectory(outputDirectory);
		configurationItem.setSourceDirectory(sourceDirectory);
		List<ConfigurationItem> original = new ArrayList<ConfigurationItem>();
		original.add(configurationItem);
		original.add(configurationItem); // add twice

		mojo = new CompileMojo();
		setVariableValueToObject(mojo, "configurationItems", original);

		List<ConfigurationItem> actual = mojo.getConfiguration();
		assertEquals(2, actual.size());
	}

	@Test
	public void testReadConfigurationFromPom() throws Exception {
		File pom = getTestFile("src/test/resources/multiple-pom.xml");
		assertNotNull(pom);
		assertTrue(pom.exists());
		final CompileMojo compileMojo = (CompileMojo) lookupMojo("compile", pom);
		assertNotNull(compileMojo);
		List<ConfigurationItem> config = compileMojo.getConfiguration();
		assertEquals(2, config.size());
		assertTrue(config.get(0).getOutputDirectory().getAbsolutePath().endsWith("css1"));
		assertTrue(config.get(1).getOutputDirectory().getAbsolutePath().endsWith("css2"));
	}
}
