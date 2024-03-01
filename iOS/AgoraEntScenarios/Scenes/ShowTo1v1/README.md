# 秀场转1v1私密房

> 本文档主要介绍如何快速跑通 <mark>秀场转1v1私密房</mark> 示例工程
> 
> Demo 效果:
> 
> <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/ent-scenarios/readme/image/android/liveto1v1/sample_roomlist.png" width="300" height="640"><img src="https://fullapp.oss-cn-beijing.aliyuncs.com/ent-scenarios/readme/image/android/liveto1v1/sample_show_live.png" width="300" height="640"><img src="https://fullapp.oss-cn-beijing.aliyuncs.com/ent-scenarios/readme/image/android/liveto1v1/sample_show_call.png" width="300" height="640">
---

## 1. 环境准备

- <mark>最低兼容 iOS 13.0</mark>
- Xcode 14+

---

## 2. 运行示例

- 进入声网控制台获取 APP ID 和 APP 证书 [控制台入口](https://console.shengwang.cn/overview)

  - 点击创建项目

    ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/ent-full/sdhy_1.jpg)

  - 选择项目基础配置, 鉴权机制需要选择**安全模式**

    ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/ent-full/sdhy_2.jpg)

  - 在项目的功能配置中启用"实时消息 RTM"功能
     ```json
     注: 如果没有启动"实时消息 RTM"功能, 将无法体验项目完整功能
     ```

    ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/ent-full/sdhy_7.jpg)

  - 拿到项目 APP ID 与 APP 证书

    ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/ent-full/sdhy_3.jpg)

- 在项目的[KeyCenter.swift](../../KeyCenter.swift)里填写需要的声网 App ID 和 App证书
  
  ![xxx](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/show/KeyCenter.png)
  
  ```texag-0-1gpap96h0ag-1-1gpap96h0ag-0-1gpap96h0ag-1-1gpap96h0ag-0-1gpap96h0ag-1-1gpap96h0ag-0-1gpap96h0ag-1-1gpap96h0ag-0-1gpap96h0ag-1-1gpap96h0
  static let AppId: String = 声网AppID
  static let Certificate: String? = 声网App证书
  ```


- 更新pod

   打开终端，cd到[Podfile](../../../Podfile)所在目录，执行`pod install`生成`AgoraEntScenarios.xcworkspace`
 
  > 
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

- 双击打开`AgoraEntScenarios.xcworkspace`运行项目即可开始您的体验

---
  


## 3. 项目介绍

### 3.1 概述

> **秀场转1v1私密房**项目是声网秀场转1v1私密房场景的开源代码，开发者可以获取并添加到您的APP工程里，本源码会伴随声动互娱Demo同步更新，为了获取更多新的功能和更佳的体验，强烈推荐您下载最新代码集成。

### 3.2 场景代码目录

```
├── ShowTo1v1
│   └── ShowTo1v1
│       ├── Resources 
│       │   └────ShowTo1v1.bundle  秀场转1v1私密房资源文件，包括图片和国际化文案
│       └── Classes  
│           ├── Core  基础模块，包括日志、UI相关的扩展等
│           ├── Service   房间管理逻辑模块  
│           │   ├── ShowTo1v1Model.swift    房间管理模型
│           │   ├── ShowTo1v1ServiceProtocol.swift   房间管理协议
│           │   └── ShowTo1v1ServiceImp.swift   房间管理业务实现类
│           └──  UI   业务UI组件
│               ├── Room   房间页面相关
│               │    ├── BroadcasterViewController.swift   主播展示控制器
│               │    └── CallViewController.swift  1v1通话控制器
│               └── UserList   房间列表页面相关
│                    └── RoomListViewController.swift  房间列表控制器
└── ShowTo1v1.podspec  秀场转1v1私密房场景的podspec文件，支持通过Cocoapods一键集成场景
```

### 3.3 功能介绍

秀场转1v1私密房场景目前已涵盖以下功能
- 场景内用户管理
  包含场景内用户的加入和退出，用户列表的获取等，相关协议请参考: [ShowTo1v1ServiceProtocol.swift](ShowTo1v1/Classes/Service/ShowTo1v1ServiceProtocol.swift)， 相关协议的实现请参考[ShowTo1v1ServiceImp.swift](Pure1v1/Classes/Service/ShowTo1v1ServiceImp.swift)

### 3.4 场景化 API

声动互娱-1v1 私密房项目内使用了 
* 1v1 呼叫连麦场景化 API (简称 CallAPI)来实现 1v1 场景下秒接通、秒出图的能力, 对应代码文件: [CallAPI](../../Common/API/VideoLoaderAPI/VideoLoaderAPI), 如果您想进一步了解 CallAPI, 可以参考 [CallAPI Demo](https://github.com/AgoraIO-Community/CallAPI)
* 秒开秒切场景化 API (简称 VideoLoaderAPI)来实现观众看播视频秒出图、秒切换直播间的能力, 对应代码文件: [VideoLoaderAPI](../../Common/API/VideoLoaderAPI/VideoLoaderAPI), 如果您想进一步了解 VideoLoaderAPI, 可以参考 [VideoLoaderAPI Demo](https://github.com/AgoraIO-Community/VideoLoaderAPI)


---

## 4.集成遇到困难，该如何联系声网获取协助

- 集成遇到困难，该如何联系声网获取协助
  - 方案1：可以从智能客服获取帮助或联系技术支持人员 [声网支持](https://ticket.shengwang.cn/form?type_id=&sdk_product=&sdk_platform=&sdk_version=&current=0&project_id=&call_id=&channel_name=)
  - 方案2：加入微信群提问
  
    ![](https://download.agora.io/demo/release/SDHY_QA.jpg)
