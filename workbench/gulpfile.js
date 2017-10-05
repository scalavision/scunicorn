var postcss = require('gulp-postcss');
var print = require('gulp-print');
var gulp = require('gulp');
var autoprefixer = require('autoprefixer');
var gutil = require('gulp-util');

gulp.task('css-dev', function() {

  var plugins = [
    autoprefixer({browsers: ['last 1 version']})
  ];

  gulp.src('styles.css')
    .pipe(postcss(plugins)).pipe(gutil.buffer(function(err, files){
      process.stdout.isTTY;
      process.stdout.pipe(files);

//      console.log(files);
    }));
//    .pipe(process.stdout);
//    .pipe(process.stdout.write(JSON.stringify));
//    .pipe(print());

});

var plugins = [
  autoprefixer({browsers: ['last 1 version']})
];

var output = gulp.src('styles.css')
  .pipe(postcss(plugins));

function css() {
  return gulp.src('styles.css')
    .pipe(postcss(plugins))
    .pipe(gulp.dest(gutil.buffer));
};

css();

//console.log(css().on('end', function(buf){ console.log(buf); }));

/*
.pipe(gutil.buffer(function(err, files){
    process.stdout.isTTY;
    process.stdout.pipe(files);

    //      console.log(files);
//      */
//}));
