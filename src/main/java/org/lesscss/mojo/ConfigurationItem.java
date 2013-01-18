package 
org.lesscss.mojo;

import java.io.File;

/**
 * A configurationItem is a complete set of configuration-parameters 
 * needed for the LessCss to compile a set of .less-files to .css.
 * 
 * @author Roland Heimdahl <roland(at)javalia.se>
 *
 */
public class ConfigurationItem {
	private File sourceDirectory;
	private File outputDirectory;
	private String[] includes;
	private String[] excludes;
	private boolean compress;
	private boolean watch;
	private int watchInterval;
	private String encoding;
	private boolean force;
	private File lessJs;
	private boolean concatenate;
	
	public File getSourceDirectory() {
		return sourceDirectory;
	}

	public File getOutputDirectory() {
		return outputDirectory;
	}

	public String[] getIncludes() {
		return includes;
	}

	public String[] getExcludes() {
		return excludes;
	}

	public void setSourceDirectory(final File sourceDirectory) {
		this.sourceDirectory = sourceDirectory;
	}
	
	public void setOutputDirectory(final File outputDirectory){
		this.outputDirectory = outputDirectory;
	}
	
	public void setIncludes(final String[] includes){
		this.includes = includes;
	}
	
	public void setExcludes(final String[] excludes){
		this.excludes = excludes;
	}

	public boolean isCompress() {
		return compress;
	}

	public void setCompress(boolean compress) {
		this.compress = compress;
	}

	public boolean isWatch() {
		return watch;
	}

	public void setWatch(boolean watch) {
		this.watch = watch;
	}

	public int getWatchInterval() {
		return watchInterval;
	}

	public void setWatchInterval(int watchInterval) {
		this.watchInterval = watchInterval;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public boolean isForce() {
		return force;
	}

	public void setForce(boolean force) {
		this.force = force;
	}

	public File getLessJs() {
		return lessJs;
	}

	public void setLessJs(File lessJs) {
		this.lessJs = lessJs;
	}

	public boolean isConcatenate() {
		return concatenate;
	}
	public void setConcatenate(final boolean concatenate){
		this.concatenate = concatenate;
	}
}
