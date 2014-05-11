expected = """#header {
  color: #4d926f;
}
h2 {
  color: #4d926f;
}

"""

css = new File(basedir, "target/min-test-1.33.7.css")
assert css.exists()
assert css.getText().equals(expected)
