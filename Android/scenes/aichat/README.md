# AI陪聊-Android

> 本文档主要介绍如何快速跑通 <mark>AI陪聊</mark> 示例工程
> 
> Demo 效果:
> 
> <img src="https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/aichat/AiChat1.png" width="300" height="640">
> <img src="https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/aichat/AiChat2.png" width="300" height="640">
> <img src="https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/aichat/AiChat3.png" width="300" height="640">
> <img src="https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/aichat/AiChat4.png" width="300" height="640">

---

## 1. 环境准备

- <mark>最低兼容 Android 7.0</mark>（SDK API Level 24）
- Android Studio 3.5及以上版本。
- Android 7.0 及以上的手机设备。

---

## 2. 运行示例
 ```json
  注: AI陪聊后端服务暂未开源，目前仅供体验
 ```
体验 APP [下载地址](https://www.pgyer.com/OaAKsG4i) 


## 3. 项目介绍

### 3.1 概述

> **AI陪聊**项目是声网AI语聊场景的开源代码，开发者可以获取并添加到您的 APP 工程里，本源码会伴随声动互娱 Demo 同步更新，为了获取更多新的功能和更佳的体验，强烈推荐您下载最新代码集成。

### 3.2 场景代码目录

```
├── scenes
│   ├── aichat
│   │   └── main
│   │       ├── java
│   │       │   └── io.agora.scene.aichat              
│   │       │                       ├── chat                      #房间内业务逻辑模块
│   │       │                       ├── ceate                     #创建智能体、群聊模块
│   │       │                       ├── groupmanager              #群聊管理模块
│   │       │                       ├── imkit                     #环信业务封装模块
│   │       │                       ├── list                      #智能体、会话列表模块
│   │       │                       ├── service                   #网络模块
│   │       │                       ├── AIChatCenter.kt           #AiChat 全局属性
│   │       │                       ├── AILogger.kt               #Log模块
│   │       │                       └── AIChatProtocolService.kt  #业务请求核心模块
│   │       ├── res              #资源文件
│   │       │   ├── drawable
│   │       │   ├── layout
│   │       │   ├── mipmap
│   │       │   └── values
│   │       └── AndroidManifest.xml
│   │   
│   ├── build.gradle
│   ├── build
│   └── gradle
│       └── wrapper
│           ├── gradle-wrapper.jar
│           └── gradle-wrapper.properties
├── build.gradle     
├── config.gradle       #共用的依赖配置
├── gradle
│   └── wrapper
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── gradlew
├── gradlew.bat
├── gradle.properties  #项目的基础账号配置(appid、app证书)
└── settings.gradle
```


## 4. FAQ

- 集成遇到困难，该如何联系声网获取协助
  - 方案1：可以从智能客服获取帮助或联系技术支持人员 [声网支持](https://ticket.shengwang.cn/form?type_id=&sdk_product=&sdk_platform=&sdk_version=&current=0&project_id=&call_id=&channel_name=)
  - 方案2：加入微信群提问
  
    ![](https://download.agora.io/demo/release/SDHY_QA.jpg)