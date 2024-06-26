# 秀场转1v1私密房

> 本文档主要介绍如何快速跑通 <mark>秀场转1v1私密房</mark> 示例工程
> 
> Demo 效果:
> 
> <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/ent-scenarios/readme/image/showto1v1_ios_guide1.jpg" width="300" height="640"><img src="https://fullapp.oss-cn-beijing.aliyuncs.com/ent-scenarios/readme/image/showto1v1_ios_guide2.jpg" width="300" height="640"><img src="https://fullapp.oss-cn-beijing.aliyuncs.com/ent-scenarios/readme/image/showto1v1_ios_guide3.jpg" width="300" height="640">
---

## 1. 环境准备

- <mark>最低兼容 iOS 13.0</mark>
- Xcode 14+

---

## 2. 运行示例

- 获取声网App ID -------- [声网Agora - 文档中心 - 如何获取 App ID](https://docportal.shengwang.cn/cn/Agora%20Platform/get_appid_token?platform=All%20Platforms#%E8%8E%B7%E5%8F%96-app-id)
  
  > - 点击创建应用
  >   
  >   ![](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/create_app_1.jpg)
  > 
  > - 选择你要创建的应用类型
  >   
  >   ![](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/create_app_2.jpg)
  > 

- 获取App 证书 ----- [声网Agora - 文档中心 - 获取 App 证书](https://docportal.shengwang.cn/cn/Agora%20Platform/get_appid_token?platform=All%20Platforms#%E8%8E%B7%E5%8F%96-app-%E8%AF%81%E4%B9%A6)
  
  > 在声网控制台的项目管理页面，找到你的项目，点击配置。
  > ![](https://fullapp.oss-cn-beijing.aliyuncs.com/scenario_api/callapi/config/1641871111769.png)
  > 点击主要证书下面的复制图标，即可获取项目的 App 证书。
  > ![](https://fullapp.oss-cn-beijing.aliyuncs.com/scenario_api/callapi/config/1637637672988.png)

- 开启RTM
  > ![](https://fullapp.oss-cn-beijing.aliyuncs.com/scenario_api/callapi/config/rtm_config1.jpg)
  > 
  > ![](https://fullapp.oss-cn-beijing.aliyuncs.com/scenario_api/callapi/config/rtm_config2.jpg)
  > 
  > ![](https://fullapp.oss-cn-beijing.aliyuncs.com/scenario_api/callapi/config/rtm_config3.jpg)

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
- 1v1呼叫场景化API
  1v1呼叫API是声网为了帮助您快速实现1v1呼叫视频通话能力的模块，使用这个模块您可以非常便捷的实现呼叫、挂断等功能，相关代码请参考: [CallApi](https://github.com/AgoraIO-Community/CallAPI)

- 场景内用户管理
  包含场景内用户的加入和退出，用户列表的获取等，相关协议请参考: [ShowTo1v1ServiceProtocol.swift](ShowTo1v1/Classes/Service/ShowTo1v1ServiceProtocol.swift)， 相关协议的实现请参考[ShowTo1v1ServiceImp.swift](Pure1v1/Classes/Service/ShowTo1v1ServiceImp.swift)



## 4.集成遇到困难，该如何联系声网获取协助

> 方案1：可以从智能客服获取帮助或联系销售人员 [Agora 支持](https://agora-ticket.agora.io/) 
> 
> 方案2：发送邮件给[support@agora.io](mailto:support@agora.io)咨询
>
> 方案3：加入微信群提问
>
> ![](https://download.agora.io/demo/release/SDHY_QA.jpg)
---
