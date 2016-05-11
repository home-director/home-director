var express = require('express');
var path = require('path');
var favicon = require('serve-favicon');
var logger = require('morgan');
var cookieParser = require('cookie-parser');
var bodyParser = require('body-parser');
var nunjucks = require('nunjucks');
require('dotenv').config({silent: true});

var routes = require('./routes/index');

var app = express();

var DeviceDb = require('device-db');
var SmartThings = require('smartthings');

var deviceDb = DeviceDb();
var smartthings = SmartThings(process.env.SMARTTHINGS_URL);

smartthings.getDevices()
  .then((devices) => {
    devices.deviceList.forEach((device) => {
      deviceDb.setDevice(device);
    });
    console.log(deviceDb);
  }, (err) => {
    console.log(err);
  })
  .catch((err) => {
    console.trace(err);
  });

setImmediate(() => {
  app.locals.io.on('connection', function(socket){
    console.log('a user connected');
  });
});


// view engine setup
// app.set('views', path.join(__dirname, 'views'));

nunjucks.configure('src/server/views', {
    autoescape: true,
    express: app
});

// uncomment after placing your favicon in /public
//app.use(favicon(__dirname + '/public/favicon.ico'));
app.use(logger('dev'));
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: false }));
app.use(cookieParser());
app.use(express.static(path.join(__dirname, '../../public')));

app.use(function(req, res, next) {
  res.locals.deviceDb = deviceDb;
  next();
});

app.use('/', routes);

// catch 404 and forward to error handler
app.use(function(req, res, next) {
  var err = new Error('Not Found');
  err.status = 404;
  next(err);
});

// error handlers

// development error handler
// will print stacktrace
if (app.get('env') === 'development') {
  app.use(function(err, req, res, next) {
    res.status(err.status || 500);
    res.render('error.html', {
      message: err.message,
      error: err
    });
  });
}

// production error handler
// no stacktraces leaked to user
app.use(function(err, req, res, next) {
  res.status(err.status || 500);
  res.render('error.html', {
    message: err.message,
    error: {}
  });
});


module.exports = app;
