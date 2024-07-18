var exec = require('cordova/exec');
var PLUGIN_NAME = "Zoom";

function callNativeFunction(name, args, success, error) {
    args = args || [];
    success = success || function(){};
    error = error || function(){};
    exec(success, error, PLUGIN_NAME, name, args);

}

var zoom = {

    initialize: function(token, success, error) {
        callNativeFunction('initialize', [token], success, error);
    },

    joinMeeting: function(meetingNo, meetingPassword, displayName, noAudio, noVideo, success, error) {
         callNativeFunction('joinMeeting', [meetingNo, meetingPassword, displayName, noAudio, noVideo], success, error);
    },

    setLocale: function(languageTag, success, error) {
        callNativeFunction('setLocale', [languageTag], success, error);
    }

};

module.exports = zoom;
