//
//  NetworkManagerModel.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2023/9/1.
//

import Foundation
import Alamofire

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
  
    
    public override func getHeaders() -> HTTPHeaders {
        var headers = super.getHeaders()
        headers.add(HTTPHeader(name: "Content-Type", value: "application/json"))
        headers.add(HTTPHeader(name: "X-LC-Id", value: "fkUjxadPMmvYF3F3BI4uvmjo-gzGzoHsz"))
        headers.add(HTTPHeader(name: "X-LC-Key", value: "QAvFS62IOR28GfSFQO5ze45s"))
        headers.add(HTTPHeader(name: "X-LC-Session", value: "qmdj8pdidnmyzp0c7yqil91oc"))
        headers.add(HTTPHeader(name: kAppProjectName, value: kAppProjectValue))
        headers.add(HTTPHeader(name: kAppOS, value: kAppOSValue))
        headers.add(HTTPHeader(name: kAppVersion, value: UIApplication.shared.appVersion ?? ""))
        headers.add(HTTPHeader(name: "Authorization", value: getToken()))
        return headers
    }
    
    public override func parse(data: Data?) throws -> Any {
        var dic: Any? = nil
        do {
            try dic = super.parse(data: data)
        } catch let err {
            throw err
        }
        guard let dic = dic as? [String: Any], let data = dic["data"] as? [String: Any] else {
            throw AUICommonError.networkParseFail.toNSError()
        }
        return data
    }
}


@objcMembers
open class NMGenerateTokennNetworkModel: NMCommonNetworkModel {
    
    private let appCertificate: String? = KeyCenter.Certificate
    private let appId: String? = KeyCenter.AppId
    private let src = "iOS"
    private let ts: String? = "".timeStamp
    
    public var channelName: String?
    public var expire: NSNumber?
    public var type: NSNumber?
    public var uid: String?
    
    public override init() {
        super.init()
    }
    
    public override func parse(data: Data?) throws -> Any {
        guard let dic = try? super.parse(data: data) as? [String : Any], let token = dic["token"] else {
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
    private var appKey: String? = KeyCenter.IMAppKey
    private var clientId: String? = KeyCenter.IMClientId
    private var clientSecret: String? = KeyCenter.IMClientSecret
}


@objcMembers
open class NMGenerateIMConfigNetworkModel: NMCommonNetworkModel {
    
    private let appId =  KeyCenter.AppId
    private let src = "iOS"
    private let traceId: String? = NSString.withUUID().md5()
    
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
   
    private let appId = KeyCenter.AppId
    private let src = "iOS"
    private let traceId = UUID().uuidString.md5Encrypt
    
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
    
    private let appId: String = KeyCenter.AppId
    private let appCert = KeyCenter.Certificate ?? ""
    private let traceId = NSString.withUUID().md5() ?? ""
    private let region = "cn"
    private let src = "iOS"
    
    private lazy var basicAuth: String = {
        createBasicAuth(key: KeyCenter.CloudPlayerKey ?? "", password: KeyCenter.CloudPlayerSecret ?? "")
    }()
    
    var channelName: String?
    var uid: String?
    var robotUid: NSNumber?
    var streamUrl: String?

    public override init() {
        super.init()
        interfaceName = "v1/cloud-player/start"
    }
   
}

@objcMembers
open class NMCloudPlayerHeartbeatNetworkModel: NMCommonNetworkModel {
    
    private let appId = KeyCenter.AppId
    private let src = "iOS"
    private let traceId = NSString.withUUID().md5() ?? ""
    
    var channelName: String?
    var uid: String?
    
    public override init() {
        super.init()
        interfaceName = "v1/heartbeat"
    }
}



@objcMembers
open class NMReportSceneClickNetworkModel: NMCommonNetworkModel {
    
    private let src: String = "agora_ent_demo"
    private let ts: Int64 = Int64(Date().timeIntervalSince1970 * 1000)
    var sign: String?
    var pts: [[String: Any]]?
         
    public override init() {
        super.init()
        host = "https://report-ad.agoralab.co/"
        interfaceName = "v1/report"
        sign = "src=\(src)&ts=\(ts)".md5Encrypt
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
    
    private let appVersion = UIApplication.shared.appVersion ?? ""
    private let model = UIDevice.current.machineModel ?? ""
    private let platform = "iOS"
    
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


