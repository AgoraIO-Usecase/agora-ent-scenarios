# 弹幕玩法

> 本文档主要介绍如何快速跑通 <mark>弹幕玩法</mark> 示例工程
>
> Demo 效果:
>
> <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/ent-scenarios/images/joy/screenshot/android/SamplePicture1.png" width="300" /> <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/ent-scenarios/images/joy/screenshot/android/SamplePicture2.png" width="300" /> <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/ent-scenarios/images/joy/screenshot/android/SamplePicture3.png" width="300" />

---

## 1. 环境准备

- <mark>最低兼容 Android 7.0</mark>（SDK API Level 24）
- Android Studio 3.5及以上版本。
- Android 7.0 及以上的手机设备。

---


## 2. 运行示例


**获取声网 App ID --------** [声网Agora - 文档中心 - 如何获取 App ID](https://docs.agora.io/cn/Agora%20Platform/get_appid_token?platform=All%20Platforms#%E8%8E%B7%E5%8F%96-app-id)

**获取 App 证书 -----** [声网Agora - 文档中心 - 获取 App 证书](https://docs.agora.io/cn/Agora%20Platform/get_appid_token?platform=All%20Platforms#%E8%8E%B7%E5%8F%96-app-%E8%AF%81%E4%B9%A6)

- 点击创建项目

  ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/ent-full/sdhy_1.jpg)

- 选择项目基础配置, 鉴权机制需要选择**安全模式**

  ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/ent-full/sdhy_2.jpg)

- 拿到项目 APP ID 与 APP 证书

  ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/ent-full/sdhy_3.jpg)

- 在项目的功能配置中启用"实时消息 RTM"功能, 同时启用下列子模块
  * 状态同步（Storage） 配置
  * 分布式锁（Lock ）配置
  ```json
  注: 如果没有启动"实时消息 RTM"功能, 将无法体验项目完整功能
  ```

  ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/ent-full/sdhy_7.jpg)    

- <mark>云游戏服务配置（CloudPlayer）<mark>
  ```json
  注: 请联系声网技术支持为您的 APPID 开通云主机权限, 开通权限后才能启动云游戏到房间推流
  ```
    

- 在项目的 [**gradle.properties**](../../gradle.properties) 里填写需要的声网 APP ID 和 APP 证书
    ```
     AGORA_APP_ID：声网 APP ID
     AGORA_APP_CERTIFICATE：声网 APP 证书
    ```

- 运行项目即可开始您的体验

---
## 3.项目介绍
### 3.1 概述
> 弹幕玩法项目是声网弹幕玩法场景的开源代码，开发者可以获取并添加到您的 APP 工程里，本源码会伴随声动互娱 Demo 同步更新，为了获取更多新的功能和更佳的体验，强烈推荐您下载最新代码集成。
### 3.2 项目文件结构简介

```
├── scene
│   ├── joy
│   │   └── main
│   │       ├── java
│   │       │   └── io.agora.scene.joy              
│   │       │                       ├── create                #房间列表模块
│   │       │                       ├── live                  #房间内业务逻辑模块
│   │       │                       ├── service               #网络模块
│   │       │                       ├── widget                #UI模块
│   │       │                       ├── JoyLogger.kt          #Log模块
│   │       │                       └── RtcEngineInstance.kt  #Rtc引擎模块
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


