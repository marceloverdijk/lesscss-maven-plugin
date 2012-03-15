expected = """#header {
  color: #4d926f;
}
h2 {
  color: #4d926f;
}
"""

css = new File(basedir, "target/webapp-testing/css/test.css")
assert css.exists()
assert css.getText().equals(expected)
