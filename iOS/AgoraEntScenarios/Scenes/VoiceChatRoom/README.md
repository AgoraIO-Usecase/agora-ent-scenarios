# 语聊房

> 本文档主要介绍如何快速跑通 语聊房 示例工程
> 
> Demo 效果:
> 
> <img src="https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/ent-full/voiceRoom_1.png" width="300" height="640"><img src="https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/ent-full/voiceRoom_2.png" width="300" height="640">
---

## 1. 环境准备

- <mark>最低兼容 iOS 13.0及以上的手机设备。</mark>
- Xcode 13.0及以上版本。

---

## 2. 运行示例
-  安装依赖库

	切换到 **iOS** 目录，运行以下命令使用CocoaPods安装依赖，AgoraSDK会在安装后自动完成集成。
	
	使用cocoapods
	
	[安装cocoapods](http://t.zoukankan.com/lijiejoy-p-9680485.html)
	
	```
	pod install
	```

	打开 `AgoraEntScenarios.xcworkspace`


- 获取声网App ID 和 App 证书  
  [声网Agora - 文档中心 - 如何获取 App ID](https://docs.agora.io/cn/Agora%20Platform/get_appid_token?platform=All%20Platforms#%E8%8E%B7%E5%8F%96-app-id)  
  [声网Agora - 文档中心 - 获取 App 证书](https://docs.agora.io/cn/Agora%20Platform/get_appid_token?platform=All%20Platforms#%E8%8E%B7%E5%8F%96-app-%E8%AF%81%E4%B9%A6)

   - 点击创建应用
  
     ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/create_app_1.jpg)
  
   - 选择你要创建的应用类型
  
     ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/create_app_2.jpg)
  
   - 得到App ID与App 证书
      
     ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/get_app_id.jpg)
   
- 获取环信的App Key
  [环信](https://www.easemob.com/)

  - 创建应用

    ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/im_create_app.jpg)
  
  - 查看应用的App Key  
  
    ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/im_get_app_id.jpg)
    
 - 在项目的[KeyCenter.swift](../../KeyCenter.swift)里填写需要的声网 App ID 和 App证书
  
  ![xxx](https://download.agora.io/demo/test/KeyCenter.png)
  
  ```texag-0-1gpap96h0ag-1-1gpap96h0ag-0-1gpap96h0ag-1-1gpap96h0ag-0-1gpap96h0ag-1-1gpap96h0ag-0-1gpap96h0ag-1-1gpap96h0ag-0-1gpap96h0ag-1-1gpap96h0
  static let AppId: String = 声网AppID
  static let Certificate: String? = 声网App证书
  
  普通语聊房需要配置IM Key
    static var IMAppKey: String? = 环信AppKey
    static var IMClientId: String? = 环信ClientId
    static var IMClientSecret: String? = 环信ClientSecret
  
  ```

# 3.项目介绍
## 3.1 概述
**声动语聊**项目是声网语聊房场景的开源代码，开发者可以获取并添加到您的APP工程里，本源码会伴随声动语聊Demo同步更新，为了获取更多新的功能和更佳的音效，强烈推荐您下载最新代码集成。
## 3.2 功能介绍

### 场景功能代码根目录

**iOS/AgoraEntScenarios/Scenes/VoiceChatRoom**

### 相关类restApi网络请求交互
- 房间管理及语聊房内的交互请求和响应，如麦位变化、消息变化、礼物收发、定向消息转发、成员变化等，通过[**ChatRoomServiceProtocol**](https://github.com/AgoraIO-Usecase/agora-ent-scenarios/blob/feat/scene/voicechat_ios_merge/iOS/AgoraEntScenarios/Scenes/VoiceChatRoom/Service/ChatRoomServiceProtocol.swift)来定义协议，通过[**ChatRoomServiceImp**](https://github.com/AgoraIO-Usecase/agora-ent-scenarios/blob/feat/scene/voicechat_ios_merge/iOS/AgoraEntScenarios/Scenes/VoiceChatRoom/Service/ChatRoomServiceImp.swift)来实现，您可以通过自己实现的ServiceImp来一键替换，无需改动业务代码。

### 语聊房场景目前已涵盖以下功能，您可以参考注释按需从代码中调用：
### 房间管理：房间列表管理，创建房间

- 功能代码路径：[RoomService](https://github.com/AgoraIO-Usecase/agora-ent-scenarios/tree/feat/scene/voicechat_ios_merge/iOS/AgoraEntScenarios/Scenes/VoiceChatRoom/Controllers/RoomManager)

### 席位管理：踢人，麦位静音，麦位锁定
    
- 麦位管理相关功能主要依托于组件MicPosView，拖拽到项目中即可使用
- [AgoraChatRoomNormalRtcView](https://github.com/AgoraIO-Usecase/agora-ent-scenarios/blob/feat/scene/voicechat_ios_merge/iOS/AgoraEntScenarios/Scenes/VoiceChatRoom/Views/VoiceChat/AgoraChatRoomNormalRtcView.swift)：基础麦位组件，支持麦位的上下麦，换麦，静音/解除静音，锁麦/解锁，麦位的单个刷新，音量更新等麦位功能。
- 麦位相关操作逻辑请参考[VoiceRoomViewController+Mic.swift](https://github.com/AgoraIO-Usecase/agora-ent-scenarios/blob/feat/scene/voicechat_ios_merge/iOS/AgoraEntScenarios/Scenes/VoiceChatRoom/Controllers/VoiceChat/VoiceRoomViewController%2BMic.swift)文件
### 互动：弹幕，打赏
- IM相关的弹幕以及回调[VoiceRoomViewController+IM.swift](https://github.com/AgoraIO-Usecase/agora-ent-scenarios/blob/feat/scene/voicechat_ios_merge/iOS/AgoraEntScenarios/Scenes/VoiceChatRoom/Controllers/VoiceChat/VoiceRoomViewController%2BIM.swift)文件
- IM相关的打赏 [VoiceRoomViewController+ChatBar.swift](https://github.com/AgoraIO-Usecase/agora-ent-scenarios/blob/feat/scene/voicechat_ios_merge/iOS/AgoraEntScenarios/Scenes/VoiceChatRoom/Controllers/VoiceChat/VoiceRoomViewController%2BChatBar.swift)
- 当前版本使用的环信IM，用户可以自主选择集成的IM

### 音效：声网最佳音效，AI降噪
- 音效方法参考：[VoiceRoomViewController+ChatBar.swift](https://github.com/AgoraIO-Usecase/agora-ent-scenarios/blob/feat/scene/voicechat_ios_merge/iOS/AgoraEntScenarios/Scenes/VoiceChatRoom/Controllers/VoiceChat/VoiceRoomViewController%2BChatBar.swift)文件的 func showEQView()
- 该方法支持对音效功能的统一处理

# 4.使用场景

声网声动语聊源码，最终目的是方便开发者快速按需集成，减少开发者搭建语聊房的工作量。在现有源码的基础上，您可以按需自由定制，包括UI/UE，前端逻辑，权限体系等


# 5.FAQ

## 5.1 如何获取声网和环信APPID：
- 声网APPID申请：[https://www.agora.io/cn/](https://www.agora.io/cn/)
- 环信APPKey申请：[https://www.easemob.com/](https://www.easemob.com/)
## 5.2 语聊房中的弹幕组件使用的是哪家？是否可以自己选择供应商？
声动语聊源码使用的是环信AgoraChat的IM和信令服务，您也可以使用自己的服务
## 5.3 想体验更多场景
详情请查看 [声动互娱](../../../../README.md)
## 5.4 集成遇到困难，该如何联系声网获取协助
- 方案1：如果您已经在使用声网服务或者在对接中，可以直接联系对接的销售或服务
- 方案2：发送邮件给 [support@agora.io](mailto:support@agora.io) 咨询
- 方案3：扫码加入我们的微信交流群提问

  <img src="https://download.agora.io/demo/release/SDHY_QA.jpg" width="360" height="360">
