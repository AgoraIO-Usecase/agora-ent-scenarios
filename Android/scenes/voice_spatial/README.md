# 空间音频语聊房-Android

> 本文档主要介绍如何快速跑通 空间音频 示例工程
> 
> Demo 效果:
> 
> <img src="https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/ent-full/spatialAudioVoiceRoom_1.png" width="300" height="640"><img src="https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/ent-full/spatialAudioVoiceRoom_2.png" width="300" height="640">
---

## 1. 环境准备

- <mark>最低兼容 Android 5.0</mark>（SDK API Level 21）
- Android Studio 3.5及以上版本。
- Android 5.0 及以上的手机设备。

---

## 2. 运行示例

- 获取声网App ID 和 App 证书  
  [声网Agora - 文档中心 - 如何获取 App ID](https://docs.agora.io/cn/Agora%20Platform/get_appid_token?platform=All%20Platforms#%E8%8E%B7%E5%8F%96-app-id)  
  [声网Agora - 文档中心 - 获取 App 证书](https://docs.agora.io/cn/Agora%20Platform/get_appid_token?platform=All%20Platforms#%E8%8E%B7%E5%8F%96-app-%E8%AF%81%E4%B9%A6)

  - 点击创建应用

    ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/create_app_1.jpg)

  - 选择你要创建的应用类型

    ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/create_app_2.jpg)

  - 得到App ID与App 证书

    ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/get_app_id.jpg)

- 在项目的[**gradle.properties**](../../gradle.properties)里填写需要的声网 App ID 和 App证书

  ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/config_app_id_android.jpg)

``` 
AGORA_APP_ID= （从声网console获取）
AGORA_APP_CERTIFICATE=（从声网console获取）
```

- 用 Android Studio 运行项目即可开始您的体验

---

## 3. 项目介绍

### 3.1 概述

项目名称：空间音频语聊房  

空间音频语聊房项目是声网空间音频场景的开源代码，开发者可以获取并添加到您的APP工程里，本源码会伴随声动语聊Demo同步更新，为了获取更多新的功能和更佳的音效，强烈推荐您下载最新代码集成。

### 3.2 使用场景

空间音频语聊源码，最终目的是方便开发者快速按需集成，减少开发者搭建语聊房的工作量。在现有源码的基础上，您可以按需自由定制，包括UI/UE，前端逻辑，权限体系等。

### 3.3 功能介绍

相关类restApi网络请求交互
- 房间管理以及对语聊房内的基本交互请求和响应，例如麦位的变化、消息的变化、成员变化等，通过VoiceServiceProtocol来定义协议，通过VoiceSyncManagerServiceImp来实现，您可以通过自己实现的其他ServiceImp来一键替换，无需改动业务代码。

空间音频语聊目前已涵盖以下功能，您可以参考注释按需从代码中调用：

- 房间管理：房间列表管理，创建房间：
  - 协议文件[VoiceSyncManagerServiceImp](src/main/java/io/agora/scene/voice/spatial/service/VoiceSyncManagerServiceImp.kt)
- 席位管理：踢人，麦位静音，麦位锁定：
  - 麦位管理相关功能主要依托于组件[Room3DMicLayout](src/main/java/io/agora/scene/voice/spatial/ui/widget/mic/Room3DMicLayout.kt)
  - 管理房间头部以及麦位置数据变化代理，支持麦位的上下麦，换麦，静音/解除静音，锁麦/解锁，麦位的单个刷新，音量更新等麦位功能参考：[RoomObservableViewDelegate](src/main/java/io/agora/scene/voice/spatial/ui/RoomObservableViewDelegate.kt)
- 空间音频：空间音频参数设置，空间位置更新，空间位置计算
  - 空间音频启动与参数设置的API封装在[AgoraRtcEngineController](src/main/java/io/agora/scene/voice/spatial/service/VoiceSyncManagerServiceImp.kt)
  - 空间位置设置前，将视图坐标的转化为直角坐标系中的坐标[Room3DMicLayout](src/main/java/io/agora/scene/voice/spatial/ui/widget/mic/Room3DMicLayout.kt)
  - 远端用户上麦后，对其进行空间音频设置的开启及位置设置[RoomObservableViewDelegate](src/main/java/io/agora/scene/voice/spatial/ui/RoomObservableViewDelegate.kt)

### 3.4 重要类介绍

AgoraRtc管理类：[AgoraRtcEngineController](src/main/java/io/agora/scene/voice/spatial/service/VoiceSyncManagerServiceImp.kt)  

房间成员空间参数设置类：[RoomObservableViewDelegate](src/main/java/io/agora/scene/voice/spatial/ui/RoomObservableViewDelegate.kt)  

空间位置坐标计算类：[Room3DMicLayout](src/main/java/io/agora/scene/voice/spatial/ui/widget/mic/Room3DMicLayout.kt)  

---

### 4. FAQ
- 如何获取声网的APP ID：
  - 声网APP ID申请：https://www.agora.io/cn/
- 集成遇到困难，该如何联系声网获取协助
  - 方案1：可以从智能客服获取帮助或联系销售人员[Agora 支持](https://agora-ticket.agora.io/) ；
  - 方案2：如果您已经在使用声网服务或者在对接中，可以直接联系对接的销售或服务；
  - 方案3：发送邮件给support@agora.io咨询。