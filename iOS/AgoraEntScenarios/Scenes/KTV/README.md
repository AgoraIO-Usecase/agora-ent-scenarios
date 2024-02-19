# 在线K歌房

> 本文档主要介绍如何快速跑通 <mark>在线K歌房</mark> 示例工程
> 
> Demo 效果:
> 
> <img src="https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/ent-full/ktvRoom_1.png" width="300" height="640"><img src="https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/ent-full/ktvRoom_2.png" width="300" height="640">

---

## 1. 环境准备

- <mark>最低兼容 iOS 13.0.0 </mark>
- Xcode 13.0.0 及以上版本。
- iPhone 6 及以上的手机设备(系统需要 iOS 13.0.0 及以上)。

---

## 2. 运行示例

- 2.1 进入声网控制台获取 APP ID 和 APP 证书 [控制台入口](https://console.shengwang.cn/overview)
  
  - 点击创建项目
    
    ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/ent-full/sdhy_1.jpg)
  - 选择项目基础配置, 鉴权机制需要选择**安全模式**
    
    ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/ent-full/sdhy_2.jpg)
  - 拿到项目 APP ID 与 APP 证书
    
    ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/ent-full/sdhy_3.jpg)
- 2.2 在项目的[KeyCenter.swift](../../KeyCenter.swift)里填写需要的声网 App ID 和 App证书
  
  ![xxx](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/show/KeyCenter.png)
  
  ```texag-0-1gpap96h0ag-1-1gpap96h0ag-0-1gpap96h0ag-1-1gpap96h0ag-0-1gpap96h0ag-1-1gpap96h0ag-0-1gpap96h0ag-1-1gpap96h0ag-0-1gpap96h0ag-1-1gpap96h0
  static let AppId: String = 声网AppID
  static let Certificate: String? = 声网App证书
  ```
- 项目的第三方库使用 pod 集成，需要在 agora-ent-scenarios/iOS 目录下执行 pod install ,然后再开始体验项目
- 在 agora-ent-scenarios/iOS 目录下，找到 AgoraEntScenarios.xcworkspace 文件
- 用 Xcode 运行 .xcworkspace 文件 即可开始您的体验

---

## 3. 项目介绍

### 3.1 概述

> **在线K歌房**项目是声网在线K歌场景的开源代码，开发者可以获取并添加到您的APP工程里，本源码会伴随声动互娱Demo同步更新，为了获取更多新的功能和更佳的体验，强烈推荐您下载最新代码集成。

### 3.2 功能介绍

> 在线K歌房场景目前已涵盖以下功能
> 
> - K歌
>   k歌相关代码参考:[KTVViewController.m](https://github.com/AgoraIO-Usecase/agora-ent-scenarios/blob/feat/scene/ktv_4.3.0/iOS/AgoraEntScenarios/Scenes/KTV/ViewController/VLKTVViewController.m)
> - 调音台(主要是针对音效的各种配置)
> - 调音台相关代码请参考[KTVViewController.m](https://github.com/AgoraIO-Usecase/agora-ent-scenarios/blob/feat/scene/ktv_4.3.0/iOS/AgoraEntScenarios/Scenes/KTV/ViewController/VLKTVViewController.m) 的showSettingView 方法为代码入口

### 3.3 场景化 API

> 声动互娱-在线K歌项目内使用了 K歌场景化 API (简称KTVAPI)来实现 K歌，角色切换，合唱等功能, 对应代码文件: [KTVAPI](https://github.com/AgoraIO-Usecase/agora-ent-scenarios/tree/feat/scene/ktv_4.3.0/iOS/AgoraEntScenarios/Scenes/Cantata/Cantata/KTVAPI), 如果您想进一步了解 KTVAPI, 可以参考 [KTVAPI Demo](https://github.com/AgoraIO-Community/KTVAPI)

---

## 4. FAQ

### 程序运行后，歌曲列表为空

**<span style="font-size: larger; color: red;">需要联系销售给 APPID 开通 K 歌权限</span>**

### K歌房中的歌曲资源使用的是哪家？是否可以自己选择供应商？

> K歌房的歌曲资源使用的是Agora内容中心服务，暂不支持自行切换供应商，详情请查看 [版权音乐 - 在线 K 歌房 - 文档中心 - 声网Agora](https://docs.agora.io/cn/online-ktv/API%20Reference/ios_ng/API/toc_drm.html)

### 想体验更多场景

> 详情请查看 [声动互娱](../../../../README.md)

### 集成遇到困难，该如何联系声网获取协助

> 方案1：如果您已经在使用声网服务或者在对接中，可以直接联系对接的销售或服务；
> 
> 方案2：发送邮件给 [support@agora.io](mailto:support@agora.io) 咨询
> 
> 方案3：扫码加入我们的微信交流群提问
> 
> <img src="https://download.agora.io/demo/release/SDHY_QA.jpg" width="360" height="360">

---
