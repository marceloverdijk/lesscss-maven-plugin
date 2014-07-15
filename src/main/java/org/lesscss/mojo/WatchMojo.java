package org.lesscss.mojo;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * Goal which will watch for changes in LESS files and compile if it detects one.
 *
 * @author Marcel Overdijk
 * @goal watch
 */
public class WatchMojo extends CompileMojo {

    @Override
    public void execute() throws MojoExecutionException {
        watch = true;
        super.execute();
    }
}
