//
//  KeyCenter.swift
//  OpenLive
//
//  Created by GongYuhua on 6/25/16.
//  Copyright © 2016 Agora. All rights reserved.
//

@objcMembers
class KeyCenter: NSObject {
    
    /**
     Agora APP ID.
     Agora assigns App IDs to app developers to identify projects and organizations.
     If you have multiple completely separate apps in your organization, for example built by different teams,
     you should use different App IDs.
     If applications need to communicate with each other, they should use the same App ID.
     In order to get the APP ID, you can open the agora console (https://console.shengwang.cn/) to create a project,
     then the APP ID can be found in the project detail page.
     声网APP ID
     Agora 给应用程序开发人员分配 App ID，以识别项目和组织。如果组织中有多个完全分开的应用程序，例如由不同的团队构建，
     则应使用不同的 App ID。如果应用程序需要相互通信，则应使用同一个App ID。
     进入声网控制台(https://console.shengwang.cn/)，创建一个项目，进入项目配置页，即可看到APP ID。
     */

    static let AppId: String = <#Your AppId#>
  
    /**
     Certificate.
     Agora provides App certificate to generate Token. You can deploy and generate a token on your server,
     or use the console to generate a temporary token.
     In order to get the APP ID, you can open the agora console (https://console.shengwang.cn/) to create a project with the App Certificate enabled,
     then the APP Certificate can be found in the project detail page.
     PS: If the project does not have certificates enabled, leave this field blank.
     声网APP证书
     Agora 提供 App certificate 用以生成 Token。您可以在您的服务器部署并生成，或者使用控制台生成临时的 Token。
     进入声网控制台(https://console.shengwang.cn/)，创建一个带证书鉴权的项目，进入项目配置页，即可看到APP证书。
     注意：如果项目没有开启证书鉴权，这个字段留空。
     */

    static let Certificate: String? = <#Your Certificate#>
    
    /**
     EaseMob APPKEY.
     The application name filled in when creating an application on the EaseMob  console.
     If you need to use Chat Room, you must to set this parameter.
     Please refer to the information on obtaining instant messaging IM from EaseMob for more information(http://docs-im-beta.easemob.com/product/enable_and_configure_IM.html#%E8%8E%B7%E5%8F%96%E7%8E%AF%E4%BF%A1%E5%8D%B3%E6%97%B6%E9%80%9A%E8%AE%AF-im-%E7%9A%84%E4%BF%A1%E6%81%AF).
     环信APPKEY
     在环信即时通讯云控制台创建应用时填入的应用名称。
     如需使用语聊房场景，需要设置该参数。
     详见获取环信即时通讯IM的信息(http://docs-im-beta.easemob.com/product/enable_and_configure_IM.html#%E8%8E%B7%E5%8F%96%E7%8E%AF%E4%BF%A1%E5%8D%B3%E6%97%B6%E9%80%9A%E8%AE%AF-im-%E7%9A%84%E4%BF%A1%E6%81%AF)。
     */
    
    static let IMAppKey: String? = nil
    
    /**
     EaseMob Client ID.
     Client id of EaseMob, used to generate app tokens to call REST API.
     If you need to use Chat Room, you must to set this parameter.
     Please refer to the information on obtaining instant messaging IM from EaseMob for more information( https://console.easemob.com/user/login/ ).
     环信Client ID
     环信的 client id，用于生成 app token 调用 REST API。
     如需使用语聊房场景，需要设置该参数。
     详见 环信即时通讯云控制台(https://console.easemob.com/user/login/)的应用详情页面。
     */
    
    static let IMClientId: String? = nil
    
    /**
     EaseMob Client Secret.
     Client Secret of EaseMob, used to generate app tokens to call REST API.
     If you need to use Chat Room, you must to set this parameter.
     Please refer to the information on obtaining instant messaging IM from EaseMob for more information( https://console.easemob.com/user/login/ ).
     环信Client Secret
     App 的 client_secret，用于生成 app token 调用 REST API。
     如需使用语聊房场景，需要设置该参数。
     详见 环信即时通讯云控制台( https://console.easemob.com/user/login/ )的应用详情页面。
     */
    
    static let IMClientSecret: String? = nil
    // cantata cloud server key
    static let RestfulApiKey: String? = nil
    //cantata cloud server secret
    static let RestfulApiSecret: String? = nil

    //dynamic resource manifest download url
    static let DynamicResourceUrl: String? = nil

    static let SUDMGP_APP_ID: String? = nil
    static let SUDMGP_APP_KEY: String? = nil
    
    //hy key
    static let HyAppId: String? = nil
    static let HyAPISecret: String? = nil
    static let HyAPIKey: String? = nil
    
    //ai chat host url
    static let AIChatAgentServerDevUrl = "https://ai-chat-service-staging.sh3t.agoralab.co"
    static let AIChatAgentServerUrl = "https://ai-chat-service.apprtc.cn"
    
    static let HostUrl: String = "https://gateway-fulldemo.apprtc.cn/"
    static let HostUrlDev: String = "https://gateway-fulldemo-staging.agoralab.co/"

    static let baseServerUrlDev: String? = "https://service-staging.agora.io/"
    static let baseServerUrl: String? = "https://service.apprtc.cn/"
}
