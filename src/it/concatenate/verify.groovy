assert new File(basedir, "target/less.css").exists()
assert !new File(basedir, "target/test.css").exists()
assert !new File(basedir, "target/variables.css").exists()
