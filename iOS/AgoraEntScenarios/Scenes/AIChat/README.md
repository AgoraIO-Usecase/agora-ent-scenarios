# AI陪聊-iOS

> 本文档主要介绍如何快速跑通 <mark>AI陪聊</mark> 示例工程
> 
> Demo 效果:
> 
> <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/ent-scenarios/readme/image/aichat/ios/screenshot/screenshot1.PNG" width="300" height="640"><img src="https://fullapp.oss-cn-beijing.aliyuncs.com/ent-scenarios/readme/image/aichat/ios/screenshot/screenshot2.PNG" width="300" height="640"><img src="https://fullapp.oss-cn-beijing.aliyuncs.com/ent-scenarios/readme/image/aichat/ios/screenshot/screenshot3.PNG" width="300" height="640"><img src="https://fullapp.oss-cn-beijing.aliyuncs.com/ent-scenarios/readme/image/aichat/ios/screenshot/screenshot4.PNG" width="300" height="640">

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
  
  ```swift
  static let AppId: String = 声网AppID
  static let Certificate: String? = 声网App证书
  
  //普通语聊房需要配置IM Key
  static var IMAppKey: String? = 环信AppKey
  static var IMClientId: String? = 环信ClientId
  static var IMClientSecret: String? = 环信ClientSecret
  
  ```
- 更新pod

   打开终端，cd到Podfile所在目录，执行 `pod install`
   如果xcode14编译失败并遇到下图错误
  >
  >  ![xxx](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/show/PodError.png)
  >  
   在终端执行如下命令将cocoapods升级到1.12.0以上：
  >  
  >  sudo gem install -n /usr/local/bin cocoapods
  >  

  >  如果网络不好升级失败，也可以手动给三方库签名 
  >  
  >  将Team里的None替换成你自己的签名即可
  >  

- 运行项目即可开始您的体验


## 3. 项目介绍

### 3.1 概述

> **AI陪聊**项目是声网弹幕玩法场景的开源代码，开发者可以获取并添加到您的 APP 工程里，本源码会伴随声动互娱 Demo 同步更新，为了获取更多新的功能和更佳的体验，强烈推荐您下载最新代码集成。

### 3.2 场景代码目录

```
├── AIChat
│   └── AIChat
│       ├── Assets 
│       │   AIChat.bundle  AI陪聊资源文件，包括图片和国际化文案
│       └── Classes  
│           ├── AIChatAPI  相关服务接口、模型
│           │       ├── Implement  相关网络请求和数据模型包装类
│           │       ├── Model  相关模型  
│           │       └── Service  相关核心服务，包括RTC、IM、文字转语音、语音转文字等
│           ├── Core  基础模块，包括日志、UI相关的扩展等
│           ├── Controllers   业务控制器
│           ├── Cells   滚动容器Cell
│           └── Views   相关UI组件
│
└── AIChat.podspec  AI陪聊玩法场景的podspec文件，支持通过Cocoapods一键集成场景



## 4. FAQ

- 集成遇到困难，该如何联系声网获取协助
  - 方案1：可以从智能客服获取帮助或联系技术支持人员 [声网支持](https://ticket.shengwang.cn/form?type_id=&sdk_product=&sdk_platform=&sdk_version=&current=0&project_id=&call_id=&channel_name=)
  - 方案2：加入微信群提问
  
    ![](https://download.agora.io/demo/release/SDHY_QA.jpg)