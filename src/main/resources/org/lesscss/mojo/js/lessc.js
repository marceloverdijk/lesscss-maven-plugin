var less = require('./less/index');
var fs = require('fs');

var inputFile = process.argv[2];
var outputFile = process.argv[3];
var compress = (process.argv[4] !== 'false');

var inputText = fs.readFileSync(inputFile, 'utf8');
var parser = new less.Parser({});
parser.parse(inputText, function(e, tree) {
  if (e instanceof Object) {
	throw e;
  };
  try {
	  var result = tree.toCSS({compress: compress});
      fs.writeFileSync(outputFile, result);
  } catch (e) {
	  fs.writeFileSync(outputFile, e.message);
	  process.exit(1);
  }
});
