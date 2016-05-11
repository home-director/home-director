var express = require('express');
var router = express.Router();

/* GET home page. */
router.get('/', function(req, res, next) {
  console.log(JSON.stringify(req.headers));
  res.render('index.html', { title: 'Express', verified: req.header('X-verified'), dn: req.header('X-dn') });
});

router.post('/api/0/update', function(req, res, next) {
  var updateData = req.body;

  updateData.forEach((deviceUpdate) => {
    console.log(deviceUpdate);
    res.locals.deviceDb.updateDeviceAttribute(deviceUpdate);
    req.app.locals.io.emit('update', deviceUpdate);
  });
  res.send('ok');
});

module.exports = router;
