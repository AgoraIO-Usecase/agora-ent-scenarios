//
//  AgoraSceneConfig.swift
//  AgoraScene_iOS
//
//  Created by CP on 2022/9/5.
//

import Foundation

public enum AgoraConfig {
    private static let VMBaseUrl = "https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/meta/demo/fulldemochat/aisound"
    public static let CreateCommonRoom = "\(AgoraConfig.VMBaseUrl)/01CreateRoomCommonChatroom"
    public static let CreateSpatialRoom = "\(AgoraConfig.VMBaseUrl)/02CeateRoomSpaticalChatroom"
    public static let SetAINSIntroduce = "\(AgoraConfig.VMBaseUrl)/07AINSIntroduce"

    public static let baseAlienMic: [String] = [
        "/CN/01-01-B-CN.wav",
        "/CN/01-02-R-CN.wav",
        "/CN/01-03-B&R-CN.wav",
        "/CN/01-04-B-CN.wav",
        "/CN/01-05-R-CN.wav",
        "/CN/01-06-B-CN.wav",
        "/CN/01-07-R-CN.wav",
        "/CN/01-08-B-CN.wav",
    ]

    public static let spatialAlienMic: [String] = [
        "/CN/02-01-B-CN.wav",
        "/CN/02-02-R-CN.wav",
        "/CN/02-03-B&R-CN.wav",
        "/CN/02-04-B-CN.wav",
        "/CN/02-05-R-CN.wav",
        "/CN/02-06-B-CN.wav",
        "/CN/02-07-R-CN.wav",
        "/CN/02-08-B-CN.wav",
        "/CN/02-09-R-CN.wav",
        "/CN/02-10-B-CN.wav",
        "/CN/02-11-R-CN.wav",
        "/CN/02-12-B-CN.wav",
        "/CN/02-13-R-CN.wav",
        "/CN/02-14-B-CN.wav",
        "/CN/02-15-R-CN.wav",
        "/CN/02-16-B-CN.wav",
        "/CN/02-17-R-CN.wav",
        "/CN/02-18-B-CN.wav",
    ]

    public static let HighAINSIntroduc: [String] = [
        "/CN/Share/07-01-R-CN.wav",
        "/CN/High/07-02-B-CN-High.wav",
        "/CN/High/07-03-R-CN-High.wav",
        "/CN/Share/07-04-B-CN.wav",
        "/CN/Share/07-05-R-CN.wav",
        "/CN/Share/07-06-B-CN.wav",
        "/CN/Share/07-07-R-CN.wav",
    ]

    public static let MediumAINSIntroduc: [String] = [
        "/CN/Share/07-01-R-CN.wav",
        "/CN/Medium/07-02-B-CN-Medium.wav",
        "/CN/Medium/07-03-R-CN-Medium.wav",
        "/CN/Share/07-04-B-CN.wav",
        "/CN/Share/07-05-R-CN.wav",
        "/CN/Share/07-06-B-CN.wav",
        "/CN/Share/07-07-R-CN.wav",
    ]

    public static let NoneAINSIntroduc: [String] = [
        "/CN/Share/07-01-R-CN.wav",
        "/CN/None/07-02-B-CN-None.wav",
        "/CN/None/07-03-R-CN-None.wav",
        "/CN/Share/07-04-B-CN.wav",
        "/CN/Share/07-05-R-CN.wav",
        "/CN/Share/07-06-B-CN.wav",
        "/CN/Share/07-07-R-CN.wav",
    ]

    public static let SoundSelectSocial: [String] = [
        "\(AgoraConfig.VMBaseUrl)/03SoundSelectionSocialChat/CN/03-01-B-CN.wav",
        "\(AgoraConfig.VMBaseUrl)/03SoundSelectionSocialChat/CN/03-02-R-CN.wav",
        "\(AgoraConfig.VMBaseUrl)/03SoundSelectionSocialChat/CN/03-03-B-CN.wav",
        "\(AgoraConfig.VMBaseUrl)/03SoundSelectionSocialChat/CN/03-04-R-CN.wav",
        "\(AgoraConfig.VMBaseUrl)/03SoundSelectionSocialChat/CN/03-05-B-CN.wav",
        "\(AgoraConfig.VMBaseUrl)/03SoundSelectionSocialChat/CN/03-06-R-CN.wav",
        "\(AgoraConfig.VMBaseUrl)/03SoundSelectionSocialChat/CN/03-07-B-CN.wav",
        "\(AgoraConfig.VMBaseUrl)/03SoundSelectionSocialChat/CN/03-08-R-CN.wav",
    ]

    public static let SoundSelectKTV: [String] = [
        "\(AgoraConfig.VMBaseUrl)/04SoundSelectionKaraoke/CN/04-01-B-CN.wav",
        "\(AgoraConfig.VMBaseUrl)/04SoundSelectionKaraoke/CN/04-02-B-CN.wav",
    ]

    public static let SoundSelectGame: [String] = [
        "\(AgoraConfig.VMBaseUrl)/05SoundSelectionGamingBuddy/CN/05-01-B-CN.wav",
        "\(AgoraConfig.VMBaseUrl)/05SoundSelectionGamingBuddy/CN/05-02-R-CN.wav",
        "\(AgoraConfig.VMBaseUrl)/05SoundSelectionGamingBuddy/CN/05-03-B-CN.wav",
        "\(AgoraConfig.VMBaseUrl)/05SoundSelectionGamingBuddy/CN/05-04&05-R-CN.wav",
        "\(AgoraConfig.VMBaseUrl)/05SoundSelectionGamingBuddy/CN/05-06-B-CN.wav",
        "\(AgoraConfig.VMBaseUrl)/05SoundSelectionGamingBuddy/CN/05-07-R-CN.wav",
        "\(AgoraConfig.VMBaseUrl)/05SoundSelectionGamingBuddy/CN/05-08-B-CN.wav",
        "\(AgoraConfig.VMBaseUrl)/05SoundSelectionGamingBuddy/CN/05-09-R-CN.wav",
        "\(AgoraConfig.VMBaseUrl)/05SoundSelectionGamingBuddy/CN/05-10-B-CN.wav",
        "\(AgoraConfig.VMBaseUrl)/05SoundSelectionGamingBuddy/CN/05-11-R-CN.wav",
    ]

    public static let SoundSelectAnchor: [String] = [
        "\(AgoraConfig.VMBaseUrl)/06SoundProfessionalBroadcaster/CN/06-01-R-CN.wav",
        "\(AgoraConfig.VMBaseUrl)/06SoundProfessionalBroadcaster/CN/06-02-R-CN.wav",
        "\(AgoraConfig.VMBaseUrl)/06SoundProfessionalBroadcaster/CN/06-03-R-CN.wav",
    ]

    public static let HighSound: [String] = [
        "\(AgoraConfig.VMBaseUrl)/08AINSTVSound/CN/High/08-01-B-CN-High.wav",
        "\(AgoraConfig.VMBaseUrl)/09AINSKitchenSound/CN/High/09-01-B-CN-High.wav",
        "\(AgoraConfig.VMBaseUrl)/10AINStreetSound/CN/High/10-01-B-CN-High.wav",
        "\(AgoraConfig.VMBaseUrl)/11AINSRobotSound/CN/High/11-01-B-CN-High.wav",
        "\(AgoraConfig.VMBaseUrl)/12AINSOfficeSound/CN/High/12-01-B-CN-High.wav",
        "\(AgoraConfig.VMBaseUrl)/13AINSHomeSound/CN/High/13-01-B-CN-High.wav",
        "\(AgoraConfig.VMBaseUrl)/14AINSConstructionSound/CN/High/14-01-B-CN-High.wav",
        "\(AgoraConfig.VMBaseUrl)/15AINSAlertSound/CN/High/15-01-B-CN-High.wav",
        "\(AgoraConfig.VMBaseUrl)/16AINSApplause/CN/High/16-01-B-CN-High.wav",
        "\(AgoraConfig.VMBaseUrl)/17AINSWindSound/CN/High/17-01-B-CN-High.wav",
        "\(AgoraConfig.VMBaseUrl)/18AINSMicPopFilter/CN/High/18-01-B-CN-High.wav",
        "\(AgoraConfig.VMBaseUrl)/19AINSAudioFeedback/CN/High/19-01-B-CN-High.wav",
        "\(AgoraConfig.VMBaseUrl)/20ANISMicrophoneFingerRubSound/CN/High/20-01-B-CN-High.wav",
        "\(AgoraConfig.VMBaseUrl)/21ANISScreenTapSound/CN/High/21-01-B-CN-High.wav",
    ]

    public static let NoneSound: [String] = [
        "\(AgoraConfig.VMBaseUrl)/08AINSTVSound/CN/None/08-01-B-CN-None.wav",
        "\(AgoraConfig.VMBaseUrl)/09AINSKitchenSound/CN/None/09-01-B-CN-None.wav",
        "\(AgoraConfig.VMBaseUrl)/10AINStreetSound/CN/None/10-01-B-CN-None.wav",
        "\(AgoraConfig.VMBaseUrl)/11AINSRobotSound/CN/None/11-01-B-CN-None.wav",
        "\(AgoraConfig.VMBaseUrl)/12AINSOfficeSound/CN/None/12-01-B-CN-None.wav",
        "\(AgoraConfig.VMBaseUrl)/13AINSHomeSound/CN/None/13-01-B-CN-None.wav",
        "\(AgoraConfig.VMBaseUrl)/14AINSConstructionSound/CN/None/14-01-B-CN-None.wav",
        "\(AgoraConfig.VMBaseUrl)/15AINSAlertSound/CN/None/15-01-B-CN-None.wav",
        "\(AgoraConfig.VMBaseUrl)/16AINSApplause/CN/None/16-01-B-CN-None.wav",
        "\(AgoraConfig.VMBaseUrl)/17AINSWindSound/CN/None/17-01-B-CN-None.wav",
        "\(AgoraConfig.VMBaseUrl)/18AINSMicPopFilter/CN/None/18-01-B-CN-None.wav",
        "\(AgoraConfig.VMBaseUrl)/19AINSAudioFeedback/CN/None/19-01-B-CN-None.wav",
        "\(AgoraConfig.VMBaseUrl)/20ANISMicrophoneFingerRubSound/CN/None/20-01-B-CN-None.wav",
        "\(AgoraConfig.VMBaseUrl)/21ANISScreenTapSound/CN/None/21-01-B-CN-None.wav",
    ]
    
    public static let parmKeys: [String] = [
        "che.audio.sf.nsEnable", // 0
        "che.audio.sf.ainsToLoadFlag",// 1
        "che.audio.sf.nsngAlgRoute",// 2
        "che.audio.sf.nsngPredefAgg",// 3
        "che.audio.sf.nsngMapInMaskMin",// 4
        "che.audio.sf.nsngMapOutMaskMin",// 5
        "che.audio.sf.statNsLowerBound",// 6
        "che.audio.sf.nsngFinalMaskLowerBound",// 7
        "che.audio.sf.statNsEnhFactor",// 8
        "che.audio.sf.statNsFastNsSpeechTrigThreshold",// 9
        "che.audio.aed.enable",// 10
        "che.audio.sf.nsngMusicProbThr",// 11
        "che.audio.sf.statNsMusicModeBackoffDB",// 12
        "che.audio.sf.ainsMusicModeBackoffDB",// 13
        "che.audio.sf.ainsSpeechProtectThreshold",// 14
    ]
    
    public static var parmVals: [Double] {
        UserDefaults.standard.synchronize()
        
        return [
            UserDefaults.standard.object(forKey: parKeys[0]) as? Double ?? 0,
            UserDefaults.standard.object(forKey: parKeys[1]) as? Double ?? 0,
            UserDefaults.standard.object(forKey: parKeys[2]) as? Double ?? 10,
            UserDefaults.standard.object(forKey: parKeys[3]) as? Double ?? 11,
            UserDefaults.standard.object(forKey: parKeys[4]) as? Double ?? 80,
            UserDefaults.standard.object(forKey: parKeys[5]) as? Double ?? 50,
            UserDefaults.standard.object(forKey: parKeys[6]) as? Double ?? 5,
            UserDefaults.standard.object(forKey: parKeys[7]) as? Double ?? 30,
            UserDefaults.standard.object(forKey: parKeys[8]) as? Double ?? 200,
            UserDefaults.standard.object(forKey: parKeys[9]) as? Double ?? 0,
            UserDefaults.standard.object(forKey: parKeys[10]) as? Double ?? 1,
            UserDefaults.standard.object(forKey: parKeys[11]) as? Double ?? 85,
            UserDefaults.standard.object(forKey: parKeys[12]) as? Double ?? 200,
            UserDefaults.standard.object(forKey: parKeys[13]) as? Double ?? 270,
            UserDefaults.standard.object(forKey: parKeys[14]) as? Double ?? 100
        ]
    }
}
