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
     In order to get the APP ID, you can open the agora console (https://console.agora.io/) to create a project,
     then the APP ID can be found in the project detail page.
     声网APP ID
     Agora 给应用程序开发人员分配 App ID，以识别项目和组织。如果组织中有多个完全分开的应用程序，例如由不同的团队构建，
     则应使用不同的 App ID。如果应用程序需要相互通信，则应使用同一个App ID。
     进入声网控制台(https://console.agora.io/)，创建一个项目，进入项目配置页，即可看到APP ID。
     */

    static let AppId: String = <#Your AppId#>

    /**
     Certificate.
     Agora provides App certificate to generate Token. You can deploy and generate a token on your server,
     or use the console to generate a temporary token.
     In order to get the APP ID, you can open the agora console (https://console.agora.io/) to create a project with the App Certificate enabled,
     then the APP Certificate can be found in the project detail page.
     PS: If the project does not have certificates enabled, leave this field blank.
     声网APP证书
     Agora 提供 App certificate 用以生成 Token。您可以在您的服务器部署并生成，或者使用控制台生成临时的 Token。
     进入声网控制台(https://console.agora.io/)，创建一个带证书鉴权的项目，进入项目配置页，即可看到APP证书。
     注意：如果项目没有开启证书鉴权，这个字段留空。
     */
    
    static let Certificate: String? = <#YOUR Certificate#>

    /**
     Token.
     Agora provides Temporary Access Token to join the spatial channel with APP ID which enable App Certificate.
     You can use it to test your project.
     You can generate the temporary access token in the project console with the App Certificate enabled.
     PS：If agora_app_certificate is configured, this field will be invalid.
     音视频临时Token
     Agora 提供 音视频临时Token 用以加入带证书鉴权的频道。您可以使用这个Token来做测试。
     进入控制台开启证书鉴权的项目配置页，在APP证书下方有"生成临时音视频token"的按钮，输入频道名即可生成一个临时token。
     注意：如果配置了agora_app_certificate，则这个字段会失效。
     */

    static var Token: String? = nil
    
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
    
    static var IMAppKey: String? = ""
    
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
    
    static var IMClientId: String? = ""
    
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
    
    static var IMClientSecret: String? = ""
  
    
    static let CloudPlayerKey: String? = nil
    static let CloudPlayerSecret: String? = nil
    
    static var HostUrl: String = "https://gateway-fulldemo-staging.agoralab.co"
    static var baseServerUrl: String? = "https://toolbox.bj2.agoralab.co/v1/"
    static var onlineBaseServerUrl: String? = baseServerUrl

}
