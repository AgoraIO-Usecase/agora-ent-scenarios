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
  
    
    public override func getHeaders() -> HTTPHeaders {
        var headers = super.getHeaders()
        headers.add(HTTPHeader(name: "Authorization", value: getToken()))
        headers.add(HTTPHeader(name: "Content-Type", value: "application/json"))
        headers.add(HTTPHeader(name: "appProject", value: "agora_ent_demo"))
        headers.add(HTTPHeader(name: "appOs", value: "iOS"))
        headers.add(HTTPHeader(name: "versionName", value: UIApplication.shared.appVersion ?? ""))
        
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
    
    public var userNo: String? {
        didSet{
            if let userNo = userNo{
                interfaceName = "/api-login/users/getUserInfo?userNo=\(userNo)"
            }
        }
    }
    
    public override init() {
        super.init()
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
