# 语聊房-iOS-中文

### 1.项目介绍

##### 1.1 概述

项目名称：空间音频语聊房  

空间音频语聊房项目是声网空间音频场景的开源代码，开发者可以获取并添加到您的APP工程里，本源码会伴随声动语聊Demo同步更新，为了获取更多新的功能和更佳的音效，强烈推荐您下载最新代码集成。

##### 1.2 功能介绍

相关类restApi网络请求交互
- 房间管理以及对语聊房内的基本交互请求和响应，例如麦位的变化、消息的变化、成员变化等，通过SpatialAudioServiceProtocol来定义协议，通过SpatialAudioSyncSerciceImp来实现，您可以通过自己实现的其他ServiceImp来一键替换，无需改动业务代码。

空间音频语聊目前已涵盖以下功能，您可以参考注释按需从代码中调用：

- 房间管理：房间列表管理，创建房间：
  - 协议文件[SpatialAudioSyncSerciceImp](AgoraEntScenarios/Scenes/SpatialAudio/Service/SpatialAudioSyncSerciceImp.swift)
- 席位管理：踢人，麦位静音，麦位锁定：
  - 麦位管理相关功能主要依托于组件[SA3DRtcView](AgoraEntScenarios/Scenes/SpatialAudio/Views/VoiceChat/SA3DRtcView.swift)
  - 管理房间头部以及麦位置数据变化代理，支持麦位的上下麦，换麦，静音/解除静音，锁麦/解锁，麦位的单个刷新，音量更新等麦位功能参考：[SARoomViewController+Mic](AgoraEntScenarios/Scenes/SpatialAudio/Controllers/VoiceChat/SARoomViewController+Mic.swift)
- 空间音频：空间音频参数设置，空间位置更新，空间位置计算
  - 空间音频启动与参数设置的API封装在[SARTCManager](AgoraEntScenarios/Scenes/SpatialAudio/Compoment/AgoraRtcKit/SARTCManager.swift)
  - 空间位置设置前，将视图坐标的转化为直角坐标系中的坐标[SA3DRtcView](AgoraEntScenarios/Scenes/SpatialAudio/Views/VoiceChat/SA3DRtcView.swift)

### 2.使用场景

空间音频语聊源码，最终目的是方便开发者快速按需集成，减少开发者搭建语聊房的工作量。在现有源码的基础上，您可以按需自由定制，包括UI/UE，前端逻辑，权限体系等。

### 3.快速开始

- 在集成的同时，需要去声网合环信的官网注册好对应的账号，同时开通对应的权限从而快速开始你的体验
- 然后[下载项目](https://github.com/AgoraIO-Usecase/agora-ent-scenarios)到本地，打开项目即可开始您的体验。
- 运行前需要先完成配置项，
-  [KeyCenter](AgoraEntScenarios/KeyCenter.swift.bak)在keyCenter文件中配置`AppId `和`Certificate `
-  删除`KeyCenter`文件后缀`.bak`

### 3.1 重要类介绍

AgoraRtc管理类：[SARTCManager](AgoraEntScenarios/Scenes/SpatialAudio/Compoment/AgoraRtcKit/SARTCManager.swift)  

空间位置坐标计算类：[SA3DRtcView](AgoraEntScenarios/Scenes/SpatialAudio/Views/VoiceChat/SA3DRtcView.swift)  

### 4.FAQ
- 如何获取声网和环信APPID：
  - 声网APPID申请：https://www.agora.io/cn/
- 集成遇到困难，该如何联系声网获取协助
  - 方案1：如果您已经在使用声网服务或者在对接中，可以直接联系对接的销售或服务；
  - 方案2：发送邮件给support@agora.io咨询。



