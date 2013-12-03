/* Copyright 2013 the original author or authors.
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.logging.Log;
import org.lesscss.LessException;
import org.lesscss.LessSource;

public class NodeJsLessCompiler {

  private static final List<String> resources = Arrays.asList(
      "lessc.js",
      "less/visitor.js",
      "less/tree.js",
      "less/to-css-visitor.js",
      "less/source-map-output.js",
      "less/rhino.js",
      "less/parser.js",
      "less/lessc_helper.js",
      "less/join-selector-visitor.js",
      "less/index.js",
      "less/import-visitor.js",
      "less/functions.js",
      "less/extend-visitor.js",
      "less/env.js",
      "less/colors.js",
      "less/browser.js",
      "less/tree/variable.js",
      "less/tree/value.js",
      "less/tree/url.js",
      "less/tree/unicode-descriptor.js",
      "less/tree/selector.js",
      "less/tree/ruleset.js",
      "less/tree/rule.js",
      "less/tree/ratio.js",
      "less/tree/quoted.js",
      "less/tree/paren.js",
      "less/tree/operation.js",
      "less/tree/negative.js",
      "less/tree/mixin.js",
      "less/tree/media.js",
      "less/tree/keyword.js",
      "less/tree/javascript.js",
      "less/tree/import.js",
      "less/tree/extend.js",
      "less/tree/expression.js",
      "less/tree/element.js",
      "less/tree/directive.js",
      "less/tree/dimension.js",
      "less/tree/condition.js",
      "less/tree/comment.js",
      "less/tree/color.js",
      "less/tree/call.js",
      "less/tree/assignment.js",
      "less/tree/anonymous.js",
      "less/tree/alpha.js");

  private final Log log;

  private final boolean compress;

  private final String encoding;

  private final File tempDir;

  private final String nodeExecutablePath;

  public NodeJsLessCompiler(boolean compress, String encoding, Log log) throws IOException {
    this("node", compress, encoding, log);
  }

  public NodeJsLessCompiler(String nodeExecutablePath, boolean compress,
      String encoding, Log log) throws IOException {
    this.compress = compress;
    this.encoding = encoding;
    this.log = log;
    this.nodeExecutablePath = nodeExecutablePath;

    tempDir = createTempDir("lessc");
    new File(tempDir, "less/tree").mkdirs();
    for (String resource : resources) {
      InputStream in = NodeJsLessCompiler.class.getClassLoader()
          .getResourceAsStream("org/lesscss/mojo/js/" + resource);
      FileOutputStream out = new FileOutputStream(new File(tempDir, resource));
      IOUtils.copy(in, out);
      in.close();
      out.close();
    }
  }

  public void close() {
    for (String resource : resources) {
      File tempFile = new File(tempDir, resource);
      if (!tempFile.delete()) {
        log.warn("Could not delete temp file: " + tempFile.getAbsolutePath());
      }
    }
    File lessSubdir = new File(tempDir, "less");
    File treeSubdir = new File(lessSubdir, "tree");
    if (!treeSubdir.delete()) {
      log.warn("Could not delete temp dir: " + treeSubdir.getAbsolutePath());
    }
    if (!lessSubdir.delete()) {
      log.warn("Could not delete temp dir: " + lessSubdir.getAbsolutePath());
    }
    if (!tempDir.delete()) {
      log.warn("Could not delete temp dir: " + tempDir.getAbsolutePath());
    }
  }

  public void compile(LessSource input, File output, boolean force)
      throws IOException, LessException, InterruptedException {
    if (force || !output.exists() || output.lastModified() < input.getLastModifiedIncludingImports()) {
      String data = compile(input.getNormalizedContent());
      FileUtils.writeStringToFile(output, data, encoding);
    }
  }

  private String compile(String input) throws LessException, IOException, InterruptedException {
    long start = System.currentTimeMillis();

    File inputFile = File.createTempFile("lessc-input-", ".less");
    FileOutputStream out = new FileOutputStream(inputFile);
    IOUtils.write(input, out);
    out.close();
    File outputFile = File.createTempFile("lessc-output-", ".css");
    File lesscJsFile = new File(tempDir, "lessc.js");

    ProcessBuilder pb = new ProcessBuilder(nodeExecutablePath, lesscJsFile.getAbsolutePath(),
        inputFile.getAbsolutePath(), outputFile.getAbsolutePath(), String.valueOf(compress));
    pb.redirectErrorStream(true);
    Process process = pb.start();
    IOUtils.copy(process.getInputStream(), System.out);

    int exitStatus = process.waitFor();

    FileInputStream in = new FileInputStream(outputFile);
    String result = IOUtils.toString(in);
    in.close();
    if (!inputFile.delete()) {
      log.warn("Could not delete temp file: " + inputFile.getAbsolutePath());
    }
    if (!outputFile.delete()) {
      log.warn("Could not delete temp file: " + outputFile.getAbsolutePath());
    }
    if (exitStatus != 0) {
      throw new LessException(result, null);
    }

    log.debug("Finished compilation of LESS source in " + (System.currentTimeMillis() - start) + " ms.");

    return result;
  }

  // copied from guava's Files.createTempDir, with added prefix
  private static File createTempDir(String prefix) {
    final int tempDirAttempts = 10000;
    File baseDir = new File(System.getProperty("java.io.tmpdir"));
    String baseName = prefix + "-" + System.currentTimeMillis() + "-";
    for (int counter = 0; counter < tempDirAttempts; counter++) {
      File tempDir = new File(baseDir, baseName + counter);
      if (tempDir.mkdir()) {
        return tempDir;
      }
    }
    throw new IllegalStateException("Failed to create directory within " + tempDirAttempts
        + " attempts (tried " + baseName + "0 to " + baseName + (tempDirAttempts - 1) + ')');
  }
}
