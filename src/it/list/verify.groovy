log = new File(basedir, "build.log")
assert log.getText().contains("The following LESS sources have been resolved:");
assert log.getText().contains("less1.less");
assert log.getText().contains("|-- less1import1.less");
assert log.getText().contains("|   `-- less1import1a.less");
assert log.getText().contains("|-- less1import2.less");
assert log.getText().contains("|   |-- import2/less1import2a.less");
assert log.getText().contains("|   |   `-- import2a/less1import2a1.less");
assert log.getText().contains("|   `-- import2/less1import2b.less");
assert log.getText().contains("`-- less1import3.less");
assert log.getText().contains("less2.less");