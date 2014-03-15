log = new File(basedir, "build.log")
assert log.getText().contains("Skipping plugin execution per configuration");