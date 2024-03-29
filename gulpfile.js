var gulp = require("gulp"),
    order = require('gulp-order'),
	concat = require('gulp-concat'),
	uglify = require('gulp-uglify'),
	rename =require('gulp-rename'),
	notify =require('gulp-notify');
var paths = {
    scripts: [
        'web/js/**/**/*.js',
        'web/js/**/*.js',        
        'web/js/*.js'
    ]
};
var orders = {
    scripts: [
        'libs/jquery/*.js',
        'libs/jquery-ui/*.js',
        'libs/jquery-migrate/*.js',
        'libs/*.js',
        'common/commonData.js',
        'plugins/highstock/highstock.js',
        'plugins/highstock/highstock_nodata.js',
        'plugins/exporting/*.js',
        'plugins/**/*.js',
        'crypto/*.js',
        'crypto/rsa/*.js',
        'src/*.js'
    ]
};
gulp.task('build-production', function() {
	gulp.src(paths.scripts)
    .pipe(order(orders.scripts))
    .pipe(concat('main.js'))
    .pipe(uglify())
    .pipe(rename({ suffix: '.min' }))
    .pipe(gulp.dest('web/js/min'))
    .pipe(notify({ message: 'build-production task complete' }));
});
gulp.task('build-dev', function() {
	gulp.src(paths.scripts)
    .pipe(order(orders.scripts))
    .pipe(concat('main.js'))
    .pipe(rename({ suffix: '.min' }))
    .pipe(gulp.dest('web/js/min'))
    .pipe(notify({ message: 'build-dev task complete' }));
});
gulp.task('watch', function() {
    gulp.watch(paths.scripts, ['build-dev']);
});
