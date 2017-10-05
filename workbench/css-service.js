var server = require('net').createServer();
//var postcss = require('gulp-postcss');
var print = require('gulp-print');
var gulp = require('gulp');
var autoprefixer = require('autoprefixer');
var gutil = require('gulp-util');
var map = require('map-stream');
var vfs = require('vinyl-fs');
var streamify = require('streamify-string');
var postcss = require('postcss');


server.listen(5000, function() {
  console.log('Telnet server is running on port', server.address().port); 
});

server.on('connection', function(socket) {
/*
  socket.on('end', function(){
    unhookStdout();
  });
  */

  socket.on('data', function(data) {
    socket.setEncoding('utf8');

    // should only be 1 line at a time
    data = data.toString().replace(/(\r\n|\n|\r)/gm,"");

    postcss( [ autoprefixer ({ browsers: ['last 4 version'] }) ] )
      .process(data).then(function(result){
          result.warnings().forEach(function (warn) {
            console.warn(warn.toString());
          }); 
          socket.write(result.css);
    });

  });

});

/*
    var plugins = [
      autoprefixer({ browsers: ['last 4 version'] })
    ];

    var processCss = function read(file, cb) {
      console.log("processing css ..");
      console.log(JSON.stringify(file));
      console.log("data attribute ..");
      console.log(JSON.stringify(file.data));
      console.log(JSON.stringify(file.type));
      console.log('cb' + JSON.stringify(cb));
      socket.write(file);
//      socket.write(file._contents);
    }

    console.log('data in: ' + data);
    console.log(JSON.stringify(data));
    streamify(data)
      .pipe(postcss(plugins))
      .pipe(map(processCss))
    gulp.src('styles.css').pipe(
    
    );
*/

