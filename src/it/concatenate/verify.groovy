assert new File(basedir, "target/concatenated.css").exists()
assert !new File(basedir, "target/test.css").exists()
assert !new File(basedir, "target/variables.css").exists()
