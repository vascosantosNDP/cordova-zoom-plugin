package cordova.plugin.zoom;

import android.util.Log;
import android.widget.Toast;

import org.apache.cordova.BuildConfig;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import us.zoom.sdk.JoinMeetingOptions;
import us.zoom.sdk.JoinMeetingParams;
import us.zoom.sdk.MeetingError;
import us.zoom.sdk.MeetingParameter;
import us.zoom.sdk.MeetingService;
import us.zoom.sdk.MeetingServiceListener;
import us.zoom.sdk.MeetingStatus;
import us.zoom.sdk.MeetingViewsOptions;
import us.zoom.sdk.ZoomError;
import us.zoom.sdk.ZoomSDK;
import us.zoom.sdk.ZoomSDKInitParams;
import us.zoom.sdk.ZoomSDKInitializeListener;
import us.zoom.sdk.ZoomSDKRawDataMemoryMode;

public class Zoom extends CordovaPlugin implements ZoomSDKInitializeListener, MeetingServiceListener {

    private static final String TAG = "^^^ZoomCordovaPlugin^^^";
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String WEB_DOMAIN = "https://zoom.us";
    private ZoomSDK mZoomSDK;
    private CallbackContext callbackContext;
    private CallbackContext callStatusCallback;

    private void ensureZoomSDKInitialized(Runnable action) {
        if (mZoomSDK != null) {
            action.run();
        } else {
            cordova.getActivity().runOnUiThread(() -> {
                mZoomSDK = ZoomSDK.getInstance();
                action.run();
            });
        }
    }
    /**
     * execute
     *
     * The bridging method to get parameters from JavaScript to execute the relevant Java methods.
     *
     * @param action            action name.
     * @param args              arguments.
     * @param callbackContext   callback context.
     * @return                  true if everything runs smooth / false if something is wrong.
     * @throws JSONException    might throw exceptions when parsing JSON arrays and objects.
     */
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext)
            throws JSONException {
        if (DEBUG) {
            Log.v(TAG, "----- [execute , action =" + action + "]");
            Log.v(TAG, "----- [execute , args =" + args + "]");
        }

        this.callbackContext = callbackContext;

        switch(action) {
            case "initialize":
                String jwtToken = args.getString(0);
                String languageTag = "en_US";
                if(args.getString(1) != null) {
                    languageTag = args.getString(1);
                }
                String finalLanguageTag = languageTag;
                ensureZoomSDKInitialized(() -> this.initializeZoomSDK(jwtToken, finalLanguageTag, callbackContext));
                break;
            case "joinMeeting":
                String meetingNo = args.getString(0);
                String meetingPassword = args.getString(1);
                String displayName = args.getString(2);
                JSONObject options = null;
                try {
                    if (args.getJSONObject(3) != null) {
                        options = args.getJSONObject(3);
                    }
                }catch (Exception e) {
                    Log.v(TAG, "********** Zoom's Options null: "+e.getMessage());
                }

                JSONObject finalOptions = options;
                ensureZoomSDKInitialized(() -> this.joinMeeting(meetingNo, meetingPassword, displayName, finalOptions, callbackContext));
                break;
            case "setMeetingCallback":
                setMeetingCallback(callbackContext);
                break;
            case "closeMeetingCallback":
                closeMeetingCallback();
                break;
            default:
                return false;
        }
        return true;
    }

    private void setMeetingCallback(CallbackContext callbackContext) {
        this.callStatusCallback = callbackContext;
    }

    private void closeMeetingCallback() {
        this.callStatusCallback = null;
    }

    private void sendMeetingCallback(MeetingStatus meetingStatus) {
        if(this.callStatusCallback != null) {
            PluginResult pluginResult =  new PluginResult(PluginResult.Status.OK, "meetingStatus:  "+meetingStatus);
            pluginResult.setKeepCallback(true);
            this.callStatusCallback.sendPluginResult(pluginResult);
        }
    }

    /**
     * initialize
     *
     * Initialize Zoom SDK.
     *
     * @param jwtToken        Zoom SDK jwtToken.
     * @param callbackContext Cordova callback context.
     */
    private void initializeZoomSDK(String jwtToken, String languageLocale, CallbackContext callbackContext) {
        cordova.getActivity().runOnUiThread(() -> {
            if (DEBUG) {
                Log.v(TAG, "********** Zoom's initialize called **********");
            }

            // If the SDK has been successfully initialized, simply return.
            if (mZoomSDK.isInitialized()) {
                callbackContext.error("Zoom already initialized!");
                return;
            }

            if (jwtToken == null) {
                callbackContext.error("Token JWT cannot be empty");
                return;
            }

            try {
                if (!mZoomSDK.isInitialized()) {
                    ZoomSDKInitParams initParams = new ZoomSDKInitParams();
                    initParams.jwtToken = jwtToken;
                    initParams.enableLog = true;
                    initParams.logSize = 5;
                    initParams.enableGenerateDump = true;
                    initParams.domain = WEB_DOMAIN;
                    initParams.videoRawDataMemoryMode = ZoomSDKRawDataMemoryMode.ZoomSDKRawDataMemoryModeStack;

                    try {
                        Locale language = new Locale.Builder().setLanguageTag(languageLocale.replaceAll("_","-")).build();
                        mZoomSDK.setSdkLocale(cordova.getActivity().getApplicationContext(), language);
                    } catch (Exception ex) {
                        mZoomSDK.setSdkLocale(cordova.getActivity().getApplicationContext(), Locale.US);
                    }

                    mZoomSDK.initialize(cordova.getContext(), this, initParams);
                }
            } catch (Exception e) {
                callbackContext.error(e.getMessage());
            }
        });
    }


    /**
     * joinMeeting
     *
     * Join a meeting
     *
     * @param meetingNo         meeting number.
     * @param meetingPassword   meeting password
     * @param displayName       display name shown in meeting.
     * @param option            meeting options.
     * @param callbackContext   cordova callback context.
     */
    private void joinMeeting(String meetingNo, String meetingPassword, String displayName, JSONObject option, CallbackContext callbackContext) {
        cordova.getActivity().runOnUiThread(() -> {
            if (DEBUG) { Log.v(TAG, "********** Zoom's join meeting called ,meetingNo=" + meetingNo + " **********"); }

            if (meetingNo == null || meetingNo.trim().isEmpty() || meetingNo.equals("null")) {
                callbackContext.error("Meeting number cannot be empty");
                return;
            }

            if (meetingPassword == null || meetingPassword.trim().isEmpty() || meetingPassword.equals("null")) {
                callbackContext.error("Meeting password cannot be empty");
                return;
            }

            String meetingNumber = meetingNo.trim().replaceAll("[^0-9]", "");

            if (meetingNumber.length() < 9 || meetingNumber.length() > 11 || !meetingNumber.matches("\\d{8,11}")) {
                callbackContext.error("Please enter a valid meeting number.");
                return;
            }


            if (DEBUG) {
                Log.v(TAG, "[Going to Join Meeting]");
                Log.v(TAG, "[meetingNo=]" + meetingNumber);
            }

            PluginResult pluginResult;
            // If the Zoom SDK instance is not initialized, throw error.
            if(!mZoomSDK.isInitialized()) {
                pluginResult =  new PluginResult(PluginResult.Status.ERROR, "ZoomSDK has not been initialized successfully");
                pluginResult.setKeepCallback(true);
                callbackContext.sendPluginResult(pluginResult);
                return;
            }

            // Get meeting service instance.
            MeetingService meetingService = mZoomSDK.getMeetingService();
            meetingService.addListener(this);

            JoinMeetingParams params = new JoinMeetingParams();
            params.displayName = displayName;
            params.meetingNo = meetingNumber;
            params.password = meetingPassword;

            JoinMeetingOptions opts = new JoinMeetingOptions();
            if (option != null) {
                try {
                    opts.no_audio = !option.isNull("no_audio") && option.getBoolean("no_audio");
                    opts.no_video = !option.isNull("no_video") && option.getBoolean("no_video");
                    opts.meeting_views_options = 0;

                    if (!option.isNull("no_button_video") && option.getBoolean("no_button_video")) {
                        opts.meeting_views_options += MeetingViewsOptions.NO_BUTTON_VIDEO;
                    }

                    if (!option.isNull("no_button_audio") && option.getBoolean("no_button_audio")) {
                        opts.meeting_views_options += MeetingViewsOptions.NO_BUTTON_AUDIO;
                    }

                } catch (JSONException ex) {
                    if (DEBUG) { Log.i(TAG, ex.getMessage()); }
                }

                int response = meetingService.joinMeetingWithParams(cordova.getActivity().getApplicationContext(),params, opts);
                if (DEBUG) { Log.i(TAG, "In JoinMeeting, response=" + getMeetingErrorMessage(response)); }
                PluginResult pluginResult12;
                if (response != MeetingError.MEETING_ERROR_SUCCESS) {
                    pluginResult12 =  new PluginResult(PluginResult.Status.ERROR, getMeetingErrorMessage(response));
                } else {
                    pluginResult12 =  new PluginResult(PluginResult.Status.OK, getMeetingErrorMessage(response));
                }
                pluginResult12.setKeepCallback(true);
                callbackContext.sendPluginResult(pluginResult12);

            } else {
                int response = meetingService.joinMeetingWithParams(cordova.getActivity().getApplicationContext(), params, opts);
                if (DEBUG) { Log.i(TAG, "In JoinMeeting, response=" + getMeetingErrorMessage(response)); }
                PluginResult pluginResult1;
                if (response != MeetingError.MEETING_ERROR_SUCCESS) {
                    pluginResult1 =  new PluginResult(PluginResult.Status.ERROR, getMeetingErrorMessage(response));
                } else {
                    pluginResult1 =  new PluginResult(PluginResult.Status.OK, getMeetingErrorMessage(response));
                }
                pluginResult1.setKeepCallback(true);
                callbackContext.sendPluginResult(pluginResult1);
            }
        });
    }

    @Override
    public void onMeetingStatusChanged(MeetingStatus meetingStatus, int errorCode, int internalErrorCode) {
        Log.i(TAG, "onMeetingStatusChanged, meetingStatus=" + meetingStatus + ", errorCode=" + errorCode + ", internalErrorCode=" + internalErrorCode);

        // Trigger the meeting callback
        sendMeetingCallback(meetingStatus);

        if (DEBUG) {
            Log.i(TAG, "onMeetingStatusChanged, meetingStatus=" + meetingStatus + ", errorCode=" + errorCode + ", internalErrorCode=" + internalErrorCode);
        }
        if(meetingStatus == MeetingStatus.MEETING_STATUS_FAILED && errorCode == MeetingError.MEETING_ERROR_CLIENT_INCOMPATIBLE) {
            final android.widget.Toast toast = android.widget.Toast.makeText(
                    cordova.getActivity().getApplicationContext(),
                    "Version of ZoomSDK is too low!",
                    android.widget.Toast.LENGTH_LONG
            );
            toast.show();
        }
    }

    @Override
    public void onMeetingParameterNotification(MeetingParameter meetingParameter) {
        Log.i(TAG, "onMeetingParameterNotification");
    }

    @Override
    public void onZoomSDKInitializeResult(int errorCode, int internalErrorCode) {
        Log.i(TAG, "onZoomSDKInitializeResult, errorCode=" + errorCode + ", internalErrorCode=" + internalErrorCode);

        if(errorCode != ZoomError.ZOOM_ERROR_SUCCESS) {
            callbackContext.error("Failed to initialize Zoom SDK. Error: " + errorCode + ", internalErrorCode=" + internalErrorCode);
            Toast.makeText(this.cordova.getContext(), "Failed to initialize Zoom SDK. Error: " + errorCode + ", internalErrorCode=" + internalErrorCode, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this.cordova.getContext(), "Initialize Zoom SDK successfully.", Toast.LENGTH_LONG).show();
            callbackContext.success("Initialize Zoom SDK successfully.");
        }
    }

    /**
     * getMeetingErrorMessage
     *
     * Get meeting error message.
     *
     * @param errorCode error code.
     * @return A string message.
     */
    private String getMeetingErrorMessage(int errorCode) {

        StringBuilder message = new StringBuilder();

        switch(errorCode) {
            case MeetingError.MEETING_ERROR_CLIENT_INCOMPATIBLE:
                message.append("Zoom SDK version is too low to connect to the meeting");
                break;
            case MeetingError.MEETING_ERROR_DISALLOW_HOST_RESGISTER_WEBINAR:
                message.append("Cannot register a webinar using the host email");
                break;
            case MeetingError.MEETING_ERROR_DISALLOW_PANELIST_REGISTER_WEBINAR:
                message.append("Cannot register a webinar using a panelist's email");
                break;
            case MeetingError.MEETING_ERROR_EXIT_WHEN_WAITING_HOST_START:
                message.append("User leave meeting when waiting host to start");
                break;
            case MeetingError.MEETING_ERROR_HOST_DENY_EMAIL_REGISTER_WEBINAR:
                message.append("The register to this webinar is denied by the host");
                break;
            case MeetingError.MEETING_ERROR_INCORRECT_MEETING_NUMBER:
                message.append("Incorrect meeting number");
                break;
            case MeetingError.MEETING_ERROR_INVALID_ARGUMENTS:
                message.append("Failed due to one or more invalid arguments.");
                break;
            case MeetingError.MEETING_ERROR_INVALID_STATUS:
                message.append("Meeting api can not be called now.");
                break;
            case MeetingError.MEETING_ERROR_LOCKED:
                message.append("Meeting is locked");
                break;
            case MeetingError.MEETING_ERROR_MEETING_NOT_EXIST:
                message.append("Meeting dose not exist");
                break;
            case MeetingError.MEETING_ERROR_MEETING_OVER:
                message.append("Meeting ended");
                break;
            case MeetingError.MEETING_ERROR_MMR_ERROR:
                message.append("Server error");
                break;
            case MeetingError.MEETING_ERROR_NETWORK_ERROR:
                message.append("Network error");
                break;
            case MeetingError.MEETING_ERROR_NETWORK_UNAVAILABLE:
                message.append("Network unavailable");
                break;
            case MeetingError.MEETING_ERROR_NO_MMR:
                message.append("No server is available for this meeting");
                break;
            case MeetingError.MEETING_ERROR_REGISTER_WEBINAR_FULL:
                message.append("Arrive maximum registers to this webinar");
                break;
            case MeetingError.MEETING_ERROR_RESTRICTED:
                message.append("Meeting is restricted");
                break;
            case MeetingError.MEETING_ERROR_RESTRICTED_JBH:
                message.append("Join this meeting before host is restricted");
                break;
            case MeetingError.MEETING_ERROR_SESSION_ERROR:
                message.append("Session error");
                break;
            case MeetingError.MEETING_ERROR_SUCCESS:
                message.append("Success");
                break;
            case MeetingError.MEETING_ERROR_TIMEOUT:
                message.append("Timeout");
                break;
            case MeetingError.MEETING_ERROR_UNKNOWN:
                message.append("Unknown error");
                break;
            case MeetingError.MEETING_ERROR_USER_FULL:
                message.append("Number of participants is full.");
                break;
            case MeetingError.MEETING_ERROR_WEB_SERVICE_FAILED:
                message.append("Request to web service failed.");
                break;
            case MeetingError.MEETING_ERROR_WEBINAR_ENFORCE_LOGIN:
                message.append("This webinar requires participants to login.");
                break;
            default:
                break;
        }

        if (DEBUG) {
            Log.v(TAG, "******getMeetingErrorMessage*********" + message);
        }
        return message.toString();
    }

    @Override
    public void onZoomAuthIdentityExpired() {
        Log.v(TAG, "onZoomAuthIdentityExpired is triggered");
    }
}
