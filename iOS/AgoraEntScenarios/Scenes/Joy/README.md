# 小玩法

> 本文档主要介绍如何快速跑通 <mark>小玩法</mark> 示例工程
> 
> Demo 效果:
> 
> <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/ent-scenarios/images/joy/screenshot/ios/SamplePicture1.png" width="300" height="640"><img src="https://fullapp.oss-cn-beijing.aliyuncs.com/ent-scenarios/images/joy/screenshot/ios/SamplePicture2.png" width="300" height="640"><img src="https://fullapp.oss-cn-beijing.aliyuncs.com/ent-scenarios/images/joy/screenshot/ios/SamplePicture3.png" width="300" height="640">
---

## 1. 环境准备

- <mark>最低兼容 iOS 13.0</mark>
- Xcode

---

## 2. 运行示例
- 2.1 进入声网控制台获取 APP ID 和 APP 证书 [控制台入口](https://console.shengwang.cn/overview)

  - 点击创建项目

    ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/ent-full/sdhy_1.jpg)

  - 选择项目基础配置, 鉴权机制需要选择**安全模式**

    ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/ent-full/sdhy_2.jpg)

  - 拿到项目 APP ID 与 APP 证书

    ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/ent-full/sdhy_3.jpg)

  - <mark>云游戏服务配置（CloudPlayer）<mark>
      ```json
      注: 请联系声网技术支持为您的 APPID 开通云主机权限, 开通权限后才能启动云游戏到房间推流
      ```
    
- 2.2 在项目的[KeyCenter.swift](../../KeyCenter.swift)里填写需要的声网 App ID 和 App证书
  
  ![xxx](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/show/KeyCenter.png)
  
  ```texag-0-1gpap96h0ag-1-1gpap96h0ag-0-1gpap96h0ag-1-1gpap96h0ag-0-1gpap96h0ag-1-1gpap96h0ag-0-1gpap96h0ag-1-1gpap96h0ag-0-1gpap96h0ag-1-1gpap96h0
  static let AppId: String = 声网AppID
  static let Certificate: String? = 声网App证书
  ```

- 2.3 更新pod

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

- 2.4 运行项目即可开始您的体验

---
  


## 3. 项目介绍

### 3.1 概述

> **小玩法**项目是声网小玩法场景的开源代码，开发者可以获取并添加到您的 APP 工程里，本源码会伴随声动互娱 Demo 同步更新，为了获取更多新的功能和更佳的体验，强烈推荐您下载最新代码集成。

### 3.2 场景代码目录

```
├── Joy
│   └── Joy
│       ├── Resources 
│       │   └────Joy.bundle  小玩法资源文件，包括图片和国际化文案
│       └── Classes  
│           ├── Core  基础模块，包括日志、UI相关的扩展等
│           ├── Service   房间管理逻辑模块  
│           │   ├── JoyModel.swift    房间管理模型
│           │   ├── JoyServiceProtocol.swift   房间管理协议
│           │   └── JoyServiceImp.swift   房间管理业务实现类
│           └──  UI   业务UI组件
│               ├── Room   房间页面相关
│               │    └── RoomViewController.swift  游戏房间控制器
│               └── RoomList   房间列表页面相关
│                    └── RoomListViewController.swift  房间列表控制器
└── Joy.podspec  小玩法场景的podspec文件，支持通过Cocoapods一键集成场景
```

## 4. FAQ

- 集成遇到困难，该如何联系声网获取协助
  - 方案1：可以从智能客服获取帮助或联系技术支持人员 [声网支持](https://ticket.shengwang.cn/form?type_id=&sdk_product=&sdk_platform=&sdk_version=&current=0&project_id=&call_id=&channel_name=)
  - 方案2：加入微信群提问
  
    ![](https://download.agora.io/demo/release/SDHY_QA.jpg)
