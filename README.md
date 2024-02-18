# 在线K歌房-iOS

本文档主要介绍如何快速跑通 1v1 私密房场景示例工程

>
> Demo 效果:
>
> <img src="https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/ent-full/ent_home_scenes.jpg" width="300" height="640"><img src="https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/ent-full/ent_home_explore.jpg" width="300" height="640">

## 1. 环境准备

- <mark>最低兼容 iOS 13.0.0 </mark> 
- Xcode 13.0.0 及以上版本。
- iPhone 6 及以上的手机设备(系统需要 iOS 13.0.0 及以上)。

---

## 2. 运行示例

- 获取声网 App ID -------- [声网Agora - 文档中心 - 如何获取 App ID](https://docs.agora.io/cn/Agora%20Platform/get_appid_token?platform=All%20Platforms#%E8%8E%B7%E5%8F%96-app-id)
  
  > - 点击创建应用
  >
  >   ![xxx](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/create_app_1.jpg)
  >
  > - 选择你要创建的应用类型
  >
  >   ![xxx](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/create_app_2.jpg)
  >
  > - 得到 App ID 与 App 证书
  >
  >   ![xxx](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/get_app_id.jpg)

- 获取 App 证书 ----- [声网Agora - 文档中心 - 获取 App 证书](https://docs.agora.io/cn/Agora%20Platform/get_appid_token?platform=All%20Platforms#%E8%8E%B7%E5%8F%96-app-%E8%AF%81%E4%B9%A6)

- 联系销售给  AppID  开通 K 歌权限 </mark>(如果您没有销售人员的联系方式可通过智能客服联系销售人员 [Agora 支持](https://agora-ticket.agora.io/)

  - 注: 拉取榜单、歌单、歌词等功能是需要开通权限的

- 在项目的 agora-ent-scenarios/iOS/AgoraEntScenarios/ 目录下会有一个 KeyCenter.swift 文件，需要在 KeyCenter.swift 里填写需要的声网 App ID 和 App 证书

  ![xxx](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/ktv/img_ktv_keys_ios.png)

  ```texag-0-1gpap96h0ag-1-1gpap96h0ag-0-1gpap96h0ag-1-1gpap96h0ag-0-1gpap96h0ag-1-1gpap96h0ag-0-1gpap96h0ag-1-1gpap96h0ag-0-1gpap96h0ag-1-1gpap96h0
  AppId：声网 appid
  Certificate：声网 Certificate
  ```
- 项目的第三方库使用 pod 集成，需要在 agora-ent-scenarios/iOS 目录下执行 pod install ,然后再开始体验项目
- 在 agora-ent-scenarios/iOS 目录下，找到 AgoraEntScenarios.xcworkspace 文件
- 用 Xcode 运行 .xcworkspace 文件 即可开始您的体验

---
 
## 3. 项目简介

### 3.1 概述

项目名称：在线K歌房 
在线K歌房项目包含声网 K歌房，嗨歌抢唱，抢买接唱，大合唱四个场景的 Demo 和开源代码，目的是方便开发者快速体验 RTC 和 RTM 在 各个场景的集成效果，并给开发者提供代码参考，便于开发者快速搭建 K歌 系列场景。在现有源码的基础上，您可以按需自由定制，包括 UI/UE，前端逻辑，权限体系等。源码会伴随声动 在线K歌房 Demo 同步更新，为了获取更佳的使用体验，强烈推荐您下载最新代码集成

### 3.2 功能介绍

在线K歌房目前已涵盖以下功能，您可以参考注释按需从代码中调用：
1.在线K歌房
[在线K歌场景代码](https://github.com/AgoraIO-Usecase/agora-ent-scenarios/tree/feat/scene/ktv_4.3.0/iOS/AgoraEntScenarios/Scenes/KTV)
2.嗨歌抢唱
[嗨歌抢唱场景代码](https://github.com/AgoraIO-Usecase/agora-ent-scenarios/tree/feat/scene/ktv_4.3.0/iOS/AgoraEntScenarios/Scenes/SBG)
3.抢麦接唱
[抢麦接唱场景代码](https://github.com/AgoraIO-Usecase/agora-ent-scenarios/tree/feat/scene/ktv_4.3.0/iOS/AgoraEntScenarios/Scenes/SingRelay)
4.大合唱
[大合唱场景代码](https://github.com/AgoraIO-Usecase/agora-ent-scenarios/tree/feat/scene/ktv_4.3.0/iOS/AgoraEntScenarios/Scenes/Cantata)

### 3.3 场景化 API

在线K歌房 项目内使用了 ktvApi来实现 K歌房全场景能力, 对应代码文件: [ktvApi](https://github.com/AgoraIO-Usecase/agora-ent-scenarios/tree/feat/scene/ktv_4.3.0/iOS/AgoraEntScenarios/Common/KTVApi), 如果您想进一步了解 ktvApi, 可以参考 [ktvAPI Demo](https://github.com/AgoraIO-Community/ktvAPI)

## 4. FAQ

- 集成遇到困难，该如何联系声网获取协助
  - 方案1：可以从智能客服获取帮助或联系技术支持人员 [声网支持](https://ticket.shengwang.cn/form?type_id=&sdk_product=&sdk_platform=&sdk_version=&current=0&project_id=&call_id=&channel_name=)
  - 方案2：加入微信群提问
  
    ![](https://download.agora.io/demo/release/SDHY_QA.jpg)
