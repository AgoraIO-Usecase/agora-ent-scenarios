# 语聊房-Android

> 本文档主要介绍如何快速跑通 语聊房 示例工程
> 
> Demo 效果:
> 
> <img src="https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/ent-full/voiceRoom_1.png" width="300" height="640"><img src="https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/ent-full/voiceRoom_2.png" width="300" height="640">
---

## 1. 环境准备

- 最低兼容 Android 7.0（SDK API Level 24）
- Android Studio 4.0及以上版本。
- Android 7.0 及以上的手机设备。

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
   
- 获取环信的App Key
  [环信](https://www.easemob.com/)

  - 创建应用

    ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/im_create_app.jpg)
  
  - 查看应用的App Key  
  
    ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/im_get_app_id.jpg)
  
- 在项目的[**gradle.properties**](../../gradle.properties)里填写需要的声网 App ID 和 环信的 App Key

  ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/config_app_id_android.jpg)

``` 
AGORA_APP_ID= （从声网console获取）
AGORA_APP_CERTIFICATE=（从声网console获取）
  
IM_APP_KEY= （从环信IM Console获取）
IM_APP_CLIENT_ID= （从环信IM Console获取）
IM_APP_CLIENT_SECRET= （从环信IM Console获取）
```

- 用 Android Studio 运行项目即可开始您的体验

---

## 3.项目介绍

### 3.1 概述

项目名称：声动语聊
声动语聊项目是声网语聊房场景的开源代码，开发者可以获取并添加到您的APP工程里，本源码会伴随声动语聊Demo同步更新，为了获取更多新的功能和更佳的音效，强烈推荐您下载最新代码集成。

### 3.2 使用场景

声网声动语聊源码，最终目的是方便开发者快速按需集成，减少开发者搭建语聊房的工作量。在现有源码的基础上，您可以按需自由定制，包括UI/UE，前端逻辑，权限体系等。

### 3.3 功能介绍

相关类restApi网络请求交互
- 房间管理以及对语聊房内的基本交互请求和响应，例如麦位的变化、消息的变化、礼物收发、定向消息转发、成员变化等，通过VoiceServiceProtocol来定义协议，通过VoiceSyncManagerServiceImp来实现，您可以通过自己实现的其他ServiceImp来一键替换，无需改动业务代码。

声动语聊目前已涵盖以下功能，您可以参考注释按需从代码中调用：

- 房间管理：房间列表管理，创建房间：
  - 协议文件[VoiceSyncManagerServiceImp](src/main/java/io/agora/scene/voice/service/VoiceSyncManagerServiceImp.kt)
- 席位管理：踢人，麦位静音，麦位锁定：
  - 麦位管理相关功能主要依托于组件[Room2DMicLayout](src/main/java/io/agora/scene/voice/ui/widget/mic/Room2DMicLayout.kt)
  - 管理房间头部以及麦位置数据变化代理，支持麦位的上下麦，换麦，静音/解除静音，锁麦/解锁，麦位的单个刷新，音量更新等麦位功能参考：[RoomObservableViewDelegate](src/main/java/io/agora/scene/voice/ui/RoomObservableViewDelegate.kt)
- 互动：弹幕，打赏：
  - IM相关的弹幕和打赏请参考[ChatroomGiftView](src/main/java/io/agora/scene/voice/ui/widget/gift/ChatroomGiftView.java) 和 [ChatroomMessagesView](src/main/java/io/agora/scene/voice/ui/widget/barrage/ChatroomMessagesView.java)
  - 当前使用的环信IM SDK 1.0.8 版本
- 音效：声网最佳音效，AI降噪：
  - 音效、AI降噪参考：[AgoraRtcEngineController](src/main/java/io/agora/scene/voice/rtckit/AgoraRtcEngineController.kt)
  - 该类支持对音效功能的统一处理

### 3.4 重要类介绍

AgoraRtc管理类：[AgoraRtcEngineController](src/main/java/io/agora/scene/voice/rtckit/AgoraRtcEngineController.kt)

IM 配置管理类(主要包括初始化IM SDK 设置回调监听)：[ChatroomConfigManager](src/main/java/io/agora/scene/voice/imkit/manager/ChatroomConfigManager.java)

自定义消息帮助类（主要用来发送自定义消息 解析自定义消息需要的属性)[CustomMsgHelper](src/main/java/io/agora/scene/voice/imkit/custorm/CustomMsgHelper.java)

IM管理类（包含加入房间、登录、退出登录等）[ChatroomIMManager](src/main/java/io/agora/scene/voice/imkit/manager/ChatroomIMManager.java)

---

### 4. FAQ
- 如何获取声网和环信APP ID：
  - 声网APP ID申请：https://www.agora.io/cn/
  - 环信APP ID申请：https://www.easemob.com/
- 语聊房中的弹幕组件使用的是哪家？是否可以自己选择供应商？
  声动语聊源码使用的是环信AgoraChat的IM和信令服务，您也可以使用自己的服务
- 想体验更多场景
  - 详情请查看 [声动互娱](../../../README.md)
- 集成遇到困难，该如何联系声网获取协助
  - 方案1：如果您已经在使用声网服务或者在对接中，可以直接联系对接的销售或服务
  - 方案2：发送邮件给 [support@agora.io](mailto:support@agora.io) 咨询
  - 方案3：扫码加入我们的微信交流群提问

     <img src="https://download.agora.io/demo/release/SDHY_QA.jpg" width="360" height="360">
---
  
