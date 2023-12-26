//
//  NetworkManagerModel.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2023/9/1.
//

import Foundation
//import Alamofire

@objcMembers
open class NMCommonNetworkModel: AUINetworkModel {
    public var userId: String?
    public override init() {
        super.init()
        host = KeyCenter.baseServerUrl!
        method = .post
    }
    
    func getToken() -> String {
        if VLUserCenter.shared().isLogin() {
            return VLUserCenter.user.token
        }
        return ""
    }
    
    public override func getHeaders() -> [String : String] {
        var headers = super.getHeaders()
        headers["Content-Type"] = "application/json"
        headers["X-LC-Id"] = "fkUjxadPMmvYF3F3BI4uvmjo-gzGzoHsz"
        headers["X-LC-Key"] = "QAvFS62IOR28GfSFQO5ze45s"
        headers["X-LC-Session"] = "qmdj8pdidnmyzp0c7yqil91oc"
        headers[kAppProjectName] = kAppProjectValue
        headers[kAppOS] = kAppOSValue
        headers[kAppVersion] = UIApplication.shared.appVersion ?? ""
        headers["Authorization"] = getToken()
        return headers
    }
    
    public override func parse(data: Data?) throws -> Any? {
        var dic: Any? = nil
        do {
            try dic = super.parse(data: data)
        } catch let err {
            throw err
        }
        guard let dic = dic as? [String: Any] else {
            throw AUICommonError.networkParseFail.toNSError()
        }
        return dic["data"]
    }
    
}


@objcMembers
open class NMGenerateTokennNetworkModel: NMCommonNetworkModel {
    
    var appCertificate: String? = KeyCenter.Certificate
    var appId: String? = KeyCenter.AppId
    var src: String = "iOS"
    var ts: String? = "".timeStamp
    
    public var channelName: String?
    public var expire: NSNumber?
    public var type: NSNumber?
    public var uid: String?
    
    public override init() {
        super.init()
    }
    
    public override func parse(data: Data?) throws -> Any? {
        let data = try? super.parse(data: data) as? [String: Any]
        guard let token = data?["token"] as? String else {
            throw AUICommonError.networkParseFail.toNSError()
        }
        return token
    }
}

@objcMembers
open class NMGenerate006TokennNetworkModel: NMGenerateTokennNetworkModel {
    public override init() {
        super.init()
        interfaceName = "v2/token006/generate"
    }
}

@objcMembers
open class NMGenerate007TokennNetworkModel: NMGenerateTokennNetworkModel {
    public override init() {
        super.init()
        interfaceName = "v2/token/generate"
    }
}



@objcMembers
open class NMGenerateIMConfigNetworkModelChatParams: NSObject {
    var name: String?
    var desc: String?
    var owner: String?
    var chatId: String?
    
    static func modelCustomPropertyMapper()-> [String: Any]? {
        return [
            "desc": "description",
            "chatId": "id"
        ]
    }
}


@objcMembers
open class NMGenerateIMConfigNetworkModelUserParmas: NSObject {
    var username: String?
    var password: String?
    var nickname: String?
}

@objcMembers
open class NMGenerateIMConfigNetworkModelIMParmas: NSObject {
    var appKey: String? = KeyCenter.IMAppKey
    var clientId: String? = KeyCenter.IMClientId
    var clientSecret: String? = KeyCenter.IMClientSecret
}


@objcMembers
open class NMGenerateIMConfigNetworkModel: NMCommonNetworkModel {
    
    var appId: String? =  KeyCenter.AppId
    var src: String? = "iOS"
    var traceId: String? = NSString.withUUID().md5()
    
    var chat: NMGenerateIMConfigNetworkModelChatParams?
    var im: NMGenerateIMConfigNetworkModelIMParmas?
    var payload: String?
    var user: NMGenerateIMConfigNetworkModelUserParmas?
    var type: NSNumber?
    
    static func modelContainerPropertyGenericClass()-> [String: Any]? {
        return [
            "chat": NMGenerateIMConfigNetworkModelChatParams.self,
            "im": NMGenerateIMConfigNetworkModelIMParmas.self,
            "user": NMGenerateIMConfigNetworkModelUserParmas.self
        ]
    }

    public override init() {
        super.init()
        interfaceName = "v1/webdemo/im/chat/create"
    }
   
}



@objcMembers
open class NMVoiceIdentifyNetworkModel: NMCommonNetworkModel {
   
    var appId: String? = KeyCenter.AppId
    var src: String? = "iOS"
    var traceId: String? = UUID().uuidString.md5Encrypt
    
    var channelName: String?
    var channelType: NSNumber?
    var payload: String?
    
    public override init() {
        super.init()
        interfaceName = "v1/moderation/audio"
    }
   
}

@objcMembers
open class NMStartCloudPlayerNetworkModel: NMCommonNetworkModel {
    
    var appId: String? = KeyCenter.AppId
    var appCert: String? = KeyCenter.Certificate ?? ""
    var traceId: String? = NSString.withUUID().md5() ?? ""
    var region: String? = "cn"
    var src: String? = "iOS"
    
    lazy var basicAuth: String? = {
        createBasicAuth(key: KeyCenter.CloudPlayerKey ?? "", password: KeyCenter.CloudPlayerSecret ?? "")
    }()
    
    var channelName: String?
    var uid: String?
    var robotUid: NSNumber?
    var streamUrl: String?

    public override init() {
        super.init()
        interfaceName = "v1/rte-cloud-player/start"
    }
   
}

@objcMembers
open class NMCloudPlayerHeartbeatNetworkModel: NMCommonNetworkModel {
    
    var appId: String? = KeyCenter.AppId
    var src: String? = "iOS"
    var traceId: String? = NSString.withUUID().md5() ?? ""
    
    var channelName: String?
    var uid: String?
    
    public override init() {
        super.init()
        interfaceName = "v1/heartbeat"
    }
}



@objcMembers
open class NMReportSceneClickNetworkModel: NMCommonNetworkModel {
    
    var src: String? = "agora_ent_demo"
    var ts: Int64 = Int64(Date().timeIntervalSince1970 * 1000)
    var sign: String?
    var pts: [[String: Any]]?
         
    public override init() {
        super.init()
        host = "https://report-ad.shengwang.cn/"
        interfaceName = "v1/report"
        sign = "src=\(src ?? "agora_ent_demo")&ts=\(ts)".md5Encrypt
    }
    
    func setProject(_ project: String){
        pts = [["m": "event",
               "ls": [
                "name": "entryScene",
                "project": project,
                "version": UIApplication.shared.appVersion ?? "",
                "platform": "iOS",
                "model": UIDevice.current.machineModel ?? ""
               ],
               "vs": ["count": 1]]
        ]
    }
}

@objcMembers
open class NMReportDeviceInfoNetworkModel: NMCommonNetworkModel {
    
    var appVersion: String? = UIApplication.shared.appVersion ?? ""
    var model: String? = UIDevice.current.machineModel ?? ""
    var platform: String? = "iOS"
    
    public init(sceneId: String, userNo: String, appId: String) {
        super.init()
        host = KeyCenter.HostUrl
        interfaceName = "/api-login/report/device?userNo=\(userNo)&sceneId=\(sceneId)&appId=\(appId)&projectId=agora_ent_demo"
    }
    
}

@objcMembers
open class NMReportUserBehaviorNetworkModel: NMCommonNetworkModel {
    
    var action: String?
    
    public init(sceneId: String, userNo: String, appId: String) {
        super.init()
        host = KeyCenter.HostUrl
        interfaceName = "/api-login/report/action?userNo=\(userNo)&sceneId=\(sceneId)&appId=\(appId)&projectId=agora_ent_demo"
        action = sceneId
    }
    
}

