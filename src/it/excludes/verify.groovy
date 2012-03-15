assert new File(basedir, "target/test.css").exists()
assert !new File(basedir, "target/exclude.css").exists()
