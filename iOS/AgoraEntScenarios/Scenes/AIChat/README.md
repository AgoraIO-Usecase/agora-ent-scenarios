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
 ```json
  注: AI陪聊后端服务暂未开源，目前仅供体验
 ```
体验 APP [下载地址](https://testflight.apple.com/join/9VGbJGub) 


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
```


## 4. FAQ

- 集成遇到困难，该如何联系声网获取协助
  - 方案1：可以从智能客服获取帮助或联系技术支持人员 [声网支持](https://ticket.shengwang.cn/form?type_id=&sdk_product=&sdk_platform=&sdk_version=&current=0&project_id=&call_id=&channel_name=)
  - 方案2：加入微信群提问
  
    ![](https://download.agora.io/demo/release/SDHY_QA.jpg)