package org.lesscss.mojo;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * Goal which compiles the LESS sources to CSS stylesheets.
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
