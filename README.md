Official LESS CSS Maven Plugin
==============================

Usage
-----

Declare the plugin and its goals. The process-sources phase is bound to by default:

    <plugin>
        <groupId>org.lesscss</groupId>
        <artifactId>lesscss-maven-plugin</artifactId>
        <version>1.3.0</version>
        <executions>
            <execution>
                <goals>
                    <goal>compile</goal>
                </goals>
            </execution>
        </executions>
    </plugin>

To run the compiler manually just execute: mvn lesscss:compile


Example configuration for web project
-------------------------------------

    <plugin>
        <groupId>org.lesscss</groupId>
        <artifactId>lesscss-maven-plugin</artifactId>
        <version>1.3.0</version>
        <configuration>
            <sourceDirectory>${project.basedir}/src/main/webapp/less</sourceDirectory>
            <outputDirectory>${project.build.directory}/${project.build.finalName}/css</outputDirectory>
            <compress>true</compress>
            <includes>
                <include>main.less</include>
            </includes>
        </configuration>
        <executions>
            <execution>
                <goals>
                    <goal>compile</goal>
                </goals>
            </execution>
        </executions>
    </plugin>


All configuration options
-------------------------

+ outputDirectory (File) - The directory for compiled CSS stylesheets. Default value is: ${project.build.directory}.
+ sourceDirectory (File) - The source directory containing the LESS sources. Default value is: ${project.basedir}/src/main/less.
+ compress (boolean) - When true the LESS compiler will compress the CSS stylesheets. Default value is: false.
+ encoding (String) The character encoding the LESS compiler will use for writing the CSS stylesheets. Default value is: ${project.build.sourceEncoding}.
+ excludes (String[]) - List of files to exclude. Specified as fileset patterns which are relative to the source directory.
+ force (boolean) - When true forces the LESS compiler to always compile the LESS sources. By default LESS sources are only compiled when modified (including imports) or the CSS stylesheet does not exists. Default value is: false.
+ includes (String[]) - List of files to include. Specified as fileset patterns which are relative to the source directory. Default value is: { "**\/*.less" }
+ lessJs (String) - The location of the LESS JavasSript file.
+ concatenate (boolean) - When true all less-files will be concatenated into a single file before compiling to css


List sources
------------

To list the LESS sources in your project the lesscss:list goal can be used. It lists the LESS sources and it's imports based on sourceDirectory and optionally includes and excludes configuration options.  


Support
-------

Have a question, or found an issue? Just create a issue: https://github.com/marceloverdijk/lesscss-maven-plugin/issues


Authors
-------

**Marcel Overdijk**

+ marcel@overdijk.me
+ http://twitter.com/marceloverdijk
+ http://github.com/marceloverdijk


Copyright and License
---------------------

Copyright 2012 Marcel Overdijk

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
