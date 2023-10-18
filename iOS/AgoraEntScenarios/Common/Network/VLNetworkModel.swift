//
//  TestModel.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2023/8/30.
//

import Foundation
import KakaJSON
import Alamofire

@objcMembers
open class VLResponseData: NSObject, Convertible {
    public var message: String?
    public var code: NSNumber?
    public var requestId: String?
    public var data: Any?
    
    override public required init() {}

    public func kj_modelKey(from property: Property) -> ModelPropertyKey {
        property.name
    }
}

@objcMembers
open class VLCommonNetworkModel: AUINetworkModel {
    public var userId: String?
    public override init() {
        super.init()
        host = KeyCenter.HostUrl
    }
    
    func getToken() -> String {
        if VLUserCenter.shared().isLogin() {
            return VLUserCenter.user.token
        }
        return ""
    }
  
    
    public override func getHeaders() -> [String: String] {
        var headers = super.getHeaders()
        headers["Authorization"] = getToken()
        headers["Content-Type"] = "application/json"
        headers["appProject"] = "agora_ent_demo"
        headers["appOs"] = "iOS"
        headers["versionName"] = UIApplication.shared.appVersion ?? ""
        return headers
    }
    
    public override func parse(data: Data?) throws -> Any {
        var dic: Any? = nil
        do {
            try dic = super.parse(data: data)
        } catch let err {
            throw err
        }
        guard let dic = dic as? [String: Any] else {
            throw AUICommonError.networkParseFail.toNSError()
        }
        let rooms = dic.kj.model(VLResponseData.self)
        return rooms
    }
}


@objcMembers
open class VLUploadUserInfoNetworkModel: VLCommonNetworkModel {
    
    public var userNo: String?
    public var headUrl: String?
    public var name: String?
    
    public override init() {
        super.init()
        interfaceName = "/api-login/users/update"
    }
}


@objcMembers
open class VLGetUserInfoNetworkModel: VLCommonNetworkModel {
    
    public var userNo: String?
    
    public override init() {
        super.init()
        interfaceName = "/api-login/users/getUserInfo"
        method = .get
    }
}

@objcMembers
open class VLDetoryUserInfoNetworkModel: VLCommonNetworkModel {
    
    public var userNo: String?
    
    public override init() {
        super.init()
        interfaceName = "/api-login/users/cancellation"
        method = .get
    }
}

@objcMembers
open class VLUploadImageNetworkModel: AUIUploadNetworkModel {
    
    public var url: String?
    
    public var image: UIImage! {
        didSet{
            fileData = image.jpegData(compressionQuality: 1.0)
        }
    }
    
    public override init() {
        super.init()
        interfaceName = "/api-login/upload"
        
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyyMMddHHmmss"
        let time = formatter.string(from: Date())
        name = "file"
        fileName = "\(time)\(arc4random() % 1000).jpg"
        mimeType = "image/jpg"
    }
    
    public override func getHeaders() -> [String: String] {
        var headers = super.getHeaders()
        headers["Authorization"] =  getToken()
        return headers
    }
    
    public override func parse(data: Data?) throws -> Any {
        var dic: Any? = nil
        do {
            try dic = super.parse(data: data)
        } catch let err {
            throw err
        }
        guard let dic = dic as? [String: Any] else {
            throw AUICommonError.networkParseFail.toNSError()
        }
        let response = dic.kj.model(VLResponseData.self)
        return response
    }
    
    func getToken() -> String {
        if VLUserCenter.shared().isLogin() {
            return VLUserCenter.user.token
        }
        return ""
    }
}

@objcMembers
open class VLLoginNetworkModel: VLCommonNetworkModel {
    
    public var phone: String?
    public var code: String?
    
    public override init() {
        super.init()
        interfaceName = "/api-login/users/login"
        method = .get
    }
}


@objcMembers
open class VLVerifyCodeNetworkModel: VLCommonNetworkModel {
    
    public var phone: String?
   
    public override init() {
        super.init()
        interfaceName = "/api-login/users/verificationCode"
        method = .get
    }
}

