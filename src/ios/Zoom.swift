//
//  Zoom.swift
//  MyAPP
//
//  Created by Andre Grillo on 15/07/2024.
//

import Foundation
import MobileRTC

@objc(Zoom)
class Zoom: CDVPlugin {
    var command: CDVInvokedUrlCommand?
    var callStatusCallback: String?
    
    @objc(initialize:)
    func initialize(command: CDVInvokedUrlCommand) {
        self.command = command
        let JWT = command.arguments[0] as? String ?? ""
        let locale = command.arguments[0] as? String ?? "en_US"
        let initContext = MobileRTCSDKInitContext()
        initContext.domain = "https://zoom.us"
        
        if !JWT.isEmpty || !locale.isEmpty {
            MobileRTC.shared().setLanguage(locale)
            initContext.enableLog = true
            
            if MobileRTC.shared().initialize(initContext) {
                if let authService = MobileRTC.shared().getAuthService() {
                    authService.jwtToken = JWT
                    authService.delegate = self
                    authService.sdkAuth()
                }
            }
        } else {
            let pluginResult: CDVPluginResult
            pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Error: Invalid input parameters")
            self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
            return
        }
    }
    
    @objc(joinMeeting:)
    func joinMeeting(command: CDVInvokedUrlCommand) {
        self.command = command
        let pluginResult: CDVPluginResult
        
        if command.arguments.count >= 5{
            let meetingNo = command.arguments[0] as? String ?? ""
            let pwd = command.arguments[1] as? String ?? ""
            let userId = command.arguments[2] as? String ?? "User"
            let noAudio = command.arguments[3] as? Bool ?? false
            let noVideo = command.arguments[4] as? Bool ?? false
            
            if meetingNo.isEmpty || pwd.isEmpty {
                pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Error: Meeting number and password are required")
                self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
                return
            }
            
            let joinMeetingParameters = MobileRTCMeetingJoinParam()
            joinMeetingParameters.meetingNumber = meetingNo
            joinMeetingParameters.password = pwd
            joinMeetingParameters.userName = userId
            joinMeetingParameters.noAudio = noAudio
            joinMeetingParameters.noVideo = noVideo
            
            if let ms = MobileRTC.shared().getMeetingService() {
                ms.delegate = self
                let response = ms.joinMeeting(with: joinMeetingParameters)
                if response == .success {
                    pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "Joining meeting...")
                } else {
                    pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Failed to join meeting")
                }
            } else {
                pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Meeting service is not available")
            }
            
            self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
        } else {
            pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Error: Missing input parameters")
            self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
        }
    }
    
    @objc(setMeetingCallback:)
    func setMeetingCallback(command: CDVInvokedUrlCommand){
        self.callStatusCallback = command.callbackId
    }
    
    @objc(closeMeetingCallback:)
    func closeMeetingCallback(command: CDVInvokedUrlCommand){
        self.callStatusCallback = nil
    }
    
    private func sendMeetingCallback(message: String){
        if let callStatusCallback = callStatusCallback {
            let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: message)
            pluginResult?.setKeepCallbackAs(true)
            self.commandDelegate.send(pluginResult, callbackId: callStatusCallback)
        }
    }
}

extension Zoom: MobileRTCAuthDelegate {
    func onMobileRTCAuthReturn(_ returnValue: MobileRTCAuthError) {
        switch returnValue {
            case .success:
                print("✅ SDK auth successful.")
            default:
            print("🚨 SDK auth failed: \(returnValue)")
            }
    }
}

extension Zoom: MobileRTCMeetingServiceDelegate {
    func onMeetingStateChange(_ state: MobileRTCMeetingState) {
        print("ℹ️ onMeetingStateChange: \(state.description)")
        sendMeetingCallback(message: state.description)
    }
}

extension MobileRTCMeetingState: CustomStringConvertible {
    public var description: String {
        switch self {
        case .idle:
            return "Idle"
        case .connecting:
            return "Connecting"
        case .waitingForHost:
            return "WaitingForHost"
        case .inMeeting:
            return "InMeeting"
        case .disconnecting:
            return "Disconnecting"
        case .reconnecting:
            return "Reconnecting"
        case .failed:
            return "Failed"
        case .ended:
            return "Ended"
        case .locked:
            return "Locked"
        case .unlocked:
            return "Unlocked"
        case .inWaitingRoom:
            return "InWaitingRoom"
        case .webinarPromote:
            return "WebinarPromote"
        case .webinarDePromote:
            return "WebinarDePromote"
        case .joinBO:
            return "JoinBO"
        case .leaveBO:
            return "LeaveBO"
        @unknown default:
            return "Unknown"
        }
    }
}
