var moment = require('moment');

function setPrimaryAttributes() {
  var updatedTime;

  var allThings = Array.prototype.slice.call(document.querySelectorAll('.thing'));

  allThings.forEach(function(thing) {
    var primaryAttribute = thing.dataset.primaryAttribute;
    thing._primaryValue = thing.querySelector('.thing__attribute--' + primaryAttribute + ' .thing__attribute-value').innerText;
    thing._hasColor = !!thing.querySelector('.thing__attribute--color');
    var timeEl = thing.querySelector('.thing__attribute-time');

    timeEl._updatedTime = moment(parseInt(timeEl.dataset.timestamp));

    setColor(thing);
  });

  updateTimes();
}

function updateTimes() {
  var allTimestamps = Array.prototype.slice.call(document.querySelectorAll('.thing__attribute-time'));

  allTimestamps.forEach(function(timeEl) {
    timeEl.innerText = timeEl._updatedTime.toNow(true) + ' ago ' + (timeEl._updatedTime.calendar().toLowerCase());
  });
}

setInterval(updateTimes, 30000);

function setColor(thing) {
  var color, level;

  if (thing.dataset.primaryAttribute === 'switch' && thing._hasColor && thing._primaryValue === 'on') {
    color = hexToRgb(thing.querySelector('.thing__attribute--color .thing__attribute-value').innerText);
    level = parseInt(thing.querySelector('.thing__attribute--level .thing__attribute-value').innerText) || 0;
    thing.style.backgroundColor = 'rgba(' + color.r + ',' + color.g + ',' + color.b + ',' + ( level / 100 ) + ')';
  }
  else if (thing.dataset.primaryAttribute === 'switch' && thing._primaryValue === 'on') {
    thing.style.backgroundColor = '#aa8';
  }
  else if (thing.dataset.primaryAttribute === 'presence' && thing._primaryValue === 'not present') {
    thing.style.backgroundColor = '#C5950C';
  }
  else if (thing.dataset.primaryAttribute === 'motion' && thing._primaryValue === 'active') {
    thing.style.backgroundColor = '#C5950C';
  }
  else if (thing.dataset.primaryAttribute === 'status' && thing._primaryValue === 'open') {
    thing.style.backgroundColor = '#C5950C';
  }
  else {
    thing.style.backgroundColor = '';
  }
}

function hexToRgb(hex) {
    var result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
    return result ? {
        r: parseInt(result[1], 16),
        g: parseInt(result[2], 16),
        b: parseInt(result[3], 16)
    } : null;
}

setPrimaryAttributes();

var socket = io();



 socket.on('update', function(data) {
   var thingEl = document.querySelector('.thing[data-id="' + data.device + '"]');
   var attribEl = thingEl.querySelector('.thing__attribute--' + data.attribute + ' .thing__attribute-value');
   var timeEl = thingEl.querySelector('.thing__attribute-time');
   var updatedTime = moment(data.date.time);

   if (attribEl) {
     if (attribEl.innerText == data.value) return; // no change
     attribEl.innerText = data.value;
   }

   if (data.attribute === thingEl.dataset.primaryAttribute) {
     thingEl._primaryValue = data.value;
     if (timeEl) {
       timeEl._updatedTime = updatedTime;
       timeEl.innerText = updatedTime.toNow(true) + ' ago';
     }
   }

   clearTimeout(thingEl._animTimer);
   thingEl.classList.add('thing--updated');
   attribEl.classList.add('thing__attribute--updated');

   setColor(thingEl);

   thingEl._animTimer = setTimeout(function() {
     thingEl.classList.remove('thing--updated');

     var updatedAttrs = Array.prototype.slice.call(thingEl.querySelectorAll('.thing__attribute--updated'));
     updatedAttrs.forEach(function(attrEl) {
       attrEl.classList.remove('thing__attribute--updated');
     });

   }, 10000);

 });
