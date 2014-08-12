/*
Copyright 2014 Divya Anna Marcus

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

*/

/*Reference: This application is developed based on the google sample available in github
Link: https://github.com/googlecast/Cast-Player-Sample*/

// Create the namespace
 window.sampleplayer = window.sampleplayer || {};

/**
 * The amount of time in a given state before the player goes idle.
 */
sampleplayer.IDLE_TIMEOUT = {
  LAUNCHING: 30 * 1000,    // 30 seconds
  LOADING: 3000 * 1000,   
  PAUSED: 30 * 1000,       
  DONE: 30 * 1000,         
  IDLE: 30 * 1000          
};

/**
 * Describes the type of media being played
 *
 *image/audio/video
 */
sampleplayer.Type = {
  IMAGE: 'image',
  VIDEO: 'video',
  AUDIO: 'audio'
};

/**
 * Describes the state of the player
 *
 * 
 */
sampleplayer.State = {
  LAUNCHING: 'launching',
  LOADING: 'loading',
  PLAYING: 'playing',
  PAUSED: 'paused',
  IDLE: 'idle'
};

/**
 *This is used to load the 
 */
window.onload = function() {
  
      var playerDiv = document.getElementById('player');
      window.castreceiver = cast.receiver.CastReceiverManager.getInstance();
	  window.player = new sampleplayer.CastOffline(playerDiv);
	  window.castreceiver.start(window.castreceiver);
  
}

/**
 * <p>
 * Cast player constructor - This does the following:
 * </p>
 * <ol>
 * <li>Bind a listener to visibilitychange</li>
 * <li>Set the default state</li>
 * <li>Bind event listeners for img ,audio &video tags<br />
 *  error, playing, pause, ended, timeupdate, seeking, &
 *  seeked</li>
 * <li>Find and remember the various elements</li>
 * <li>Create the MediaManager and bind to onLoad & onStop</li>
 * </ol>
 *
 */
sampleplayer.CastOffline = function(element) {

  /**
   * The DOM element the player is attached.
   * @private {Element}
   */
  this.player_element = element;

  
  /**
   * The current state of the player
   * @private {sampleplayer.State}
   */
  this.currentstate;
  this.setState_(sampleplayer.State.LAUNCHING);

  /**
   * The image element.
   * @private {HTMLImageElement}
   */
  this.playerimage = /** @type {HTMLImageElement} */
      (this.player_element.querySelector('img'));
  this.playerimage.addEventListener('error', this.onError_.bind(this), false);

  /**
   * The media element
   * @private {HTMLMediaElement}
   */
  this.playerelement = /** @type {HTMLMediaElement} */(this.player_element.querySelector('video'));
  this.playerelement.addEventListener('error', this.onError_.bind(this), false);
  this.playerelement.addEventListener('playing', this.onPlaying_.bind(this),false);
  this.playerelement.addEventListener('pause', this.onPause_.bind(this), false);
  this.playerelement.addEventListener('ended', this.onEnded_.bind(this), false);
  this.playerelement.addEventListener('timeupdate', this.onProgress_.bind(this),false);
  
  /* Currently seek function is not working in this project */
  this.playerelement.addEventListener('seeking', this.onSeekStart_.bind(this),false);
  this.playerelement.addEventListener('seeked', this.onSeekEnd_.bind(this),false);
  this.progressBarInnerElement_ = this.player_element.querySelector('.controls-progress-inner');
  this.progressBarThumbElement_ = this.player_element.querySelector('.controls-progress-thumb');
  this.curTimeElement_ = this.player_element.querySelector('.controls-cur-time');
  this.totalTimeElement_ = this.player_element.querySelector('.controls-total-time');

  /**
   * The remote media object
   * @private {cast.receiver.MediaManager}
   */
  this.mediaManager_ = new cast.receiver.MediaManager(this.playerelement);
  this.mediaManager_.onLoad = this.onLoad_.bind(this);
  this.mediaManager_.onStop = this.onStop_.bind(this);

};

/**
 * Sets the amount of time before the player is considered idle.
 *
 * @param {number} t the time in milliseconds before the player goes idle
 * @private
 */
sampleplayer.CastOffline.prototype.setIdleTimeout_ = function(t) {
  clearTimeout(this.idle_);
  if (t) {
    this.idle_ = setTimeout(this.onIdle_.bind(this), t);
  }
};


/**
 * Sets the type of player
 *
 * @param {string} mimeType the mime type of the content
 * @private
 */
sampleplayer.CastOffline.prototype.setContentType_ = function(mimeType) {
  if (mimeType.indexOf('image/') == 0) {
    this.type_ = sampleplayer.Type.IMAGE;
  } else if (mimeType.indexOf('video/') == 0) {
    this.type_ = sampleplayer.Type.VIDEO;
  }
  else if (mimeType.indexOf('audio/') == 0) {
    this.type_ = sampleplayer.Type.AUDIO;
  }
};


/**
 * Sets the state of the player
 *
 * @param {sampleplayer.State} state the new state of the player
 * @param {boolean=} crossfade true if should cross fade between states
 * @param {number=} delay the amount of time (in ms) to wait
 */
sampleplayer.CastOffline.prototype.setState_ = function(state, crossfade, delay){
  var self = this;
  clearTimeout(self.delay_);
  if (delay) {
    var func = function() { self.setState_(state, crossfade); };
    self.delay_ = setTimeout(func, delay);
  } else {
    if (!crossfade) {
      self.currentstate = state;
      self.player_element.className = 'player ' + (self.type_ || '') + ' ' + state;
      self.setIdleTimeout_(sampleplayer.IDLE_TIMEOUT[state.toUpperCase()]);
      console.log('setState(%o)', state);
    } else {
      sampleplayer.fadeOut_(self.player_element, 0.75, function() {
        self.setState_(state, false);
        sampleplayer.fadeIn_(self.player_element, 0.75);
      });
    }
  }
};


/**
 * Callback called when media has started playing
 *
 */
sampleplayer.CastOffline.prototype.onPlaying_ = function() {
  console.log('onPlaying');
  var isLoading = this.currentstate == sampleplayer.State.LOADING;
  var xfade = isLoading;
  var delay = !isLoading ? 0 : 3000;      // 3 seconds
  this.setState_(sampleplayer.State.PLAYING, xfade, delay);
};

/**
 * Callback called when media has been paused
 *
 */
sampleplayer.CastOffline.prototype.onPause_ = function() {
  console.log('onPause');
  if (this.currentstate != sampleplayer.State.DONE) {
    this.setState_(sampleplayer.State.PAUSED, false);
  }
};


/**
 * Callback called when media has been stopped
 *
 */
sampleplayer.CastOffline.prototype.onStop_ = function() {
  console.log('onStop');
  var self = this;
  sampleplayer.fadeOut_(self.player_element, 0.75, function() {
    self.playerelement.pause();
    self.playerelement.removeAttribute('src');
    self.playerimage.removeAttribute('src');
    self.setState_(sampleplayer.State.DONE, false);
    sampleplayer.fadeIn_(self.player_element, 0.75);
  });
};


/**
 * Callback called when media has ended
 *
 */
sampleplayer.CastOffline.prototype.onEnded_ = function() {
  console.log('onEnded');
  this.setState_(sampleplayer.State.DONE, true);
};

/**
 * Callback called when media position has changed
 *
 */
sampleplayer.CastOffline.prototype.onProgress_ = function() {
  var curTime = this.playerelement.currentTime;
  var totalTime = this.playerelement.duration;
  if (!isNaN(curTime) && !isNaN(totalTime)) {
    var pct = 100 * (curTime / totalTime);
    this.curTimeElement_.innerText = sampleplayer.formatDuration_(curTime);
    this.totalTimeElement_.innerText = sampleplayer.formatDuration_(totalTime);
    this.progressBarInnerElement_.style.width = pct + '%';
    this.progressBarThumbElement_.style.left = pct + '%';
  }
};

/**
 * Callback called when user starts seeking
 *
 */
sampleplayer.CastOffline.prototype.onSeekStart_ = function() {
  console.log('onSeekStart');
  clearTimeout(this.seekingTimeout_);
  this.player_element.classList.add('seeking');
};

/**
 * Callback called when user stops seeking
 *
 */
sampleplayer.CastOffline.prototype.onSeekEnd_ = function() {
  console.log('onSeekEnd');
  clearTimeout(this.seekingTimeout_);
  this.seekingTimeout_ = sampleplayer.addClassWithTimeout_(this.player_element,
      'seeking', 3000);
};

/**
 * Callback called when media volume has changed - we rely on the pause timer
 * to get us to the right state.  If we are paused for too long, things will
 * close. Otherwise, we can come back, and we start again.
 *
 */
sampleplayer.CastOffline.prototype.onVisibilityChange_ = function() {
  console.log('onVisibilityChange');
  if (document.webkitHidden) {
    this.playerelement.pause();
  } else {
    this.playerelement.play();
  }
};

/**
 * Callback called when player enters idle state 
 *
 */
sampleplayer.CastOffline.prototype.onIdle_ = function() {
  console.log('onIdle');
  if (this.currentstate != sampleplayer.State.IDLE) {
    this.setState_(sampleplayer.State.IDLE, true);
  } else {
    window.close();
  }
};

/**
 * Called to handle an error when the media could not be loaded.
 * cast.MediaManager in the Receiver also listens for this event, and it will
 * notify any senders. We choose to just enter the done state, bring up the
 * finished image and let the user either choose to do something else.  We are
 * trying not to put up errors on the second screen.
 *
 */
sampleplayer.CastOffline.prototype.onError_ = function() {
  console.log('onError');
  this.setState_(sampleplayer.State.DONE, true);
};

/**
 * Called to handle a load request
 * TODO() handle errors better here (i.e. missing contentId, contentType, etc)
 *
 * @param {cast.receiver.MediaManager.Event} event the load event
 */
sampleplayer.CastOffline.prototype.onLoad_ = function(event) {
  var self = this;

  var title = sampleplayer.getValue_(event.data, ['media', 'metadata', 'title']);
  var titleElement = self.player_element.querySelector('.media-title');
  sampleplayer.setInnerText_(titleElement, title);
  var artist = sampleplayer.getValue_(event.data, ['media', 'metadata', 'artist']);
  var artistElement = self.player_element.querySelector('.media-artist');
  sampleplayer.setInnerText_(artistElement, artist);
  var artwork = sampleplayer.getValue_(event.data, ['media', 'metadata','images', 0, 'url']);
  var artworkElement = self.player_element.querySelector('.media-artwork');
  sampleplayer.setBackgroundImage_(artworkElement, artwork);
  var autoplay = sampleplayer.getValue_(event.data, ['autoplay']);
  var contentId = sampleplayer.getValue_(event.data, ['media', 'contentId']);
  var contentType = sampleplayer.getValue_(event.data, ['media', 'contentType']);
  self.setContentType_(contentType);
  self.setState_(sampleplayer.State.LOADING, false);
  switch (self.type_) {
    case sampleplayer.Type.IMAGE:
      self.playerimage.onload = function() {
        self.setState_(sampleplayer.State.PAUSED, false);
      };
      self.playerimage.src = contentId || '';
      self.playerelement.removeAttribute('src');
      break;
    case sampleplayer.Type.VIDEO:
      self.playerimage.onload = null;
      self.playerimage.removeAttribute('src');
      self.playerelement.autoplay = autoplay || false; // autoplay is set to false
      self.playerelement.src = contentId || '';
      break;
     case sampleplayer.Type.AUDIO:
      self.playerimage.onload = null;
      self.playerimage.removeAttribute('src');
      self.playerelement.autoplay = autoplay || false; // audio autoplay is set to false
      self.playerelement.src = contentId || '';
      break;
  }
};

sampleplayer.getValue_ = function(obj, keys) {
  for (var i = 0; i < keys.length; i++) {
    if (obj === null || obj === undefined) {
      return '';                    // default to an empty string
    } else {
      obj = obj[keys[i]];
    }
  }
  return obj;
};

/**
 * Sets the inner text for the given element.
 *
 * @param {Element} element The element.
 * @param {string} text The text.
 */
sampleplayer.setInnerText_ = function(element, text) {
  element.innerText = text || '';
};

/**
 * Sets the background image for the given element.
 *
 * @param {Element} element The element.
 * @param {string} url The image url.
 */
sampleplayer.setBackgroundImage_ = function(element, url) {
  element.style.backgroundImage = (url ? 'url("' + url + '")' : 'none');
  element.style.display = (url ? '' : 'none');
};

/**
 * Formats the given duration
 *
 * @param {number} dur the duration (in seconds)
 * @return {string} the time (in HH:MM:SS)
 */
sampleplayer.formatDuration_ = function(dur) {
  function digit(n) { return ('00' + Math.floor(n)).slice(-2); }
  var hr = Math.floor(dur / 3600);
  var min = Math.floor(dur / 60) % 60;
  var sec = dur % 60;
  if (!hr) {
    return digit(min) + ':' + digit(sec);
  } else {
    return digit(hr) + ':' + digit(min) + ':' + digit(sec);
  }
};

/**
 * Adds the given className to the given element for the specified amount of
 * time
 *
 * @param {Element} element the element to add the given class
 * @param {string} className the class name to add to the given element
 * @param {number} timeout the amount of time (in ms) the class should be
 *                 added to the given element
 * @return {number} returns a numerical id, which can be used later with
 *                  window.clearTimeout()
 */
sampleplayer.addClassWithTimeout_ = function(element, className, timeout) {
  element.classList.add(className);
  return setTimeout(function() {
    element.classList.remove(className);
  }, timeout);
};

/**
 * Causes the given element to fade in
 *
 * @param {Element} element the element to fade in
 * @param {number} time the amount of time (in seconds) to transition
 * @param {function()=} doneFunc the function to call when complete
 */
sampleplayer.fadeIn_ = function(element, time, doneFunc) {
  sampleplayer.fadeTo_(element, '', time, doneFunc);
};

/**
 * Causes the given element to fade out
 *
 * @param {Element} element the element to fade out
 * @param {number} time the amount of time (in seconds) to transition
 * @param {function()=} doneFunc the function to call when complete
 */
sampleplayer.fadeOut_ = function(element, time, doneFunc) {
  sampleplayer.fadeTo_(element, 0, time, doneFunc);
};

/**
 * Causes the given element to fade to the given opacity
 *
 * @param {Element} element the element to fade in/out
 * @param {string|number} opacity the opacity to transition to
 * @param {number} time the amount of time (in seconds) to transition
 * @param {function()=} doneFunc the function to call when complete
 */
sampleplayer.fadeTo_ = function(element, opacity, time, doneFunc) {
  var listener = null;
  listener = function() {
    element.style.webkitTransition = '';
    element.removeEventListener('webkitTransitionEnd', listener, false);
    if (doneFunc) {
      doneFunc();
    }
  };
  element.addEventListener('webkitTransitionEnd', listener, false);
  element.style.webkitTransition = 'opacity ' + time + 's';
  element.style.opacity = opacity;
};
