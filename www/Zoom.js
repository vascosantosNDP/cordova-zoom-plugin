var exec = require('cordova/exec');
var PLUGIN_NAME = "Zoom";

function callNativeFunction(name, args, success, error) {
    args = args || [];
    success = success || function(){};
    error = error || function(){};
    exec(success, error, PLUGIN_NAME, name, args);

}

var zoom = {

    initialize: function(token, language, success, error) {
        callNativeFunction('initialize', [token, language], success, error);
    },

    joinMeeting: function(meetingNo, meetingPassword, displayName, noAudio, noVideo, success, error) {
         callNativeFunction('joinMeeting', [meetingNo, meetingPassword, displayName, noAudio, noVideo], success, error);
    },

    setMeetingCallback: function(success, error) {
        callNativeFunction('setMeetingCallback', success, error);
    },

    closeMeetingCallback: function(success, error) {
        callNativeFunction('closeMeetingCallback', success, error);
    }

};

module.exports = zoom;
