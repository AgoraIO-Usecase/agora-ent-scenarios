# 语聊房-空间音频-iOS

> 本文档主要介绍如何快速跑通 示例工程
> 
> Demo 效果:
> 
> <img src="https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/ent-full/spatialAudioVoiceRoom_1.png" width="300" height="640"><img src="https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/ent-full/spatialAudioVoiceRoom_2.png" width="300" height="640">
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
  
  空间音频语聊房不需要配置IM Key
    static var IMAppKey: String? = 环信AppKey
    static var IMClientId: String? = 环信ClientId
    static var IMClientSecret: String? = 环信ClientSecret
  
  ```



### 3.项目介绍

##### 3.1 概述

项目名称：空间音频语聊房  

空间音频语聊房项目是声网空间音频场景的开源代码，开发者可以获取并添加到您的APP工程里，本源码会伴随声动语聊Demo同步更新，为了获取更多新的功能和更佳的音效，强烈推荐您下载最新代码集成。

##### 3.2 功能介绍

相关类restApi网络请求交互
- 房间管理以及对语聊房内的基本交互请求和响应，例如麦位的变化、消息的变化、成员变化等，通过SpatialAudioServiceProtocol来定义协议，通过SpatialAudioSyncSerciceImp来实现，您可以通过自己实现的其他ServiceImp来一键替换，无需改动业务代码。

空间音频语聊目前已涵盖以下功能，您可以参考注释按需从代码中调用：

- 房间管理：房间列表管理，创建房间：
  - 协议文件[SpatialAudioSyncSerciceImp](AgoraEntScenarios/Scenes/SpatialAudio/Service/SpatialAudioSyncSerciceImp.swift)
- 席位管理：踢人，麦位静音，麦位锁定：
  - 麦位管理相关功能主要依托于组件[SA3DRtcView](AgoraEntScenarios/Scenes/SpatialAudio/Views/VoiceChat/SA3DRtcView.swift)
  - 管理房间头部以及麦位置数据变化代理，支持麦位的上下麦，换麦，静音/解除静音，锁麦/解锁，麦位的单个刷新，音量更新等麦位功能参考：[SARoomViewController+Mic](AgoraEntScenarios/Scenes/SpatialAudio/Controllers/VoiceChat/SARoomViewController+Mic.swift)
- 空间音频：空间音频参数设置，空间位置更新，空间位置计算
  - 空间音频启动与参数设置的API封装在[SARTCManager](AgoraEntScenarios/Scenes/SpatialAudio/Compoment/AgoraRtcKit/SARTCManager.swift)
  - 空间位置设置前，将视图坐标的转化为直角坐标系中的坐标[SA3DRtcView](AgoraEntScenarios/Scenes/SpatialAudio/Views/VoiceChat/SA3DRtcView.swift)

### 4.使用场景

空间音频语聊源码，最终目的是方便开发者快速按需集成，减少开发者搭建语聊房的工作量。在现有源码的基础上，您可以按需自由定制，包括UI/UE，前端逻辑，权限体系等。


### 5 重要类介绍

AgoraRtc管理类：[SARTCManager](AgoraEntScenarios/Scenes/SpatialAudio/Compoment/AgoraRtcKit/SARTCManager.swift)  

空间位置坐标计算类：[SA3DRtcView](AgoraEntScenarios/Scenes/SpatialAudio/Views/VoiceChat/SA3DRtcView.swift)  

### 6.FAQ
- 如何获取声网和环信APPID：
  - 声网APPID申请：https://www.agora.io/cn/
- 集成遇到困难，该如何联系声网获取协助
  - 方案1：如果您已经在使用声网服务或者在对接中，可以直接联系对接的销售或服务；
  - 方案2：发送邮件给support@agora.io咨询。



