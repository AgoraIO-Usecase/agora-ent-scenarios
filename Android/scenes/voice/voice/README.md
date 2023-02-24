# 语聊房-Android-中文

### 1.项目介绍

##### 1.1 概述

项目名称：声动语聊
声动语聊项目是声网语聊房场景的开源代码，开发者可以获取并添加到您的APP工程里，本源码会伴随声动语聊Demo同步更新，为了获取更多新的功能和更佳的音效，强烈推荐您下载最新代码集成。

##### 1.2 功能介绍

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

### 2.使用场景

声网声动语聊源码，最终目的是方便开发者快速按需集成，减少开发者搭建语聊房的工作量。在现有源码的基础上，您可以按需自由定制，包括UI/UE，前端逻辑，权限体系等。

### 3.快速开始

- 在集成的同时，需要去声网合环信的官网注册好对应的账号，同时开通对应的权限从而快速开始你的体验
- 然后[下载项目](https://github.com/AgoraIO-Usecase/agora-ent-scenarios)到本地，打开项目即可开始您的体验。
- 运行前需要先完成配置项，在项目根目录下的gradle.properties文件中配置。

    ```
	gradle.properties：
		AGORA_APP_ID= （从声网console获取）
		AGORA_APP_CERTIFICATE=（从声网console获取）
    ```

- 在voice模块下 创建voice_gradle.properties文件 可支持配置生产环境和开发环境。

    ```
	voice_gradle.properties：(配置参数从环信IM Console获取)
	isBuildTypesTest=true 开发环境：
		IM_APP_KEY_TEST="开发环境 IM APPKEY"
    IM_APP_CLIENT_ID_TEST="开发环境 IM Client ID"
    IM_APP_CLIENT_SECRET_TEST="开发环境 IM ClientSecret"

	isBuildTypesTest=false 生产环境
		IM_APP_KEY_RELEASE="生产环境 IM APPKEY"	
    IM_APP_CLIENT_ID_RELEASE="生产环境 IM Client ID"
    IM_APP_CLIENT_SECRET_RELEASE="生产环境 IM ClientSecret"
    ```

### 3.1 重要类介绍

AgoraRtc管理类：[AgoraRtcEngineController](src/main/java/io/agora/scene/voice/rtckit/AgoraRtcEngineController.kt)

IM 配置管理类(主要包括初始化IM SDK 设置回调监听)：[ChatroomConfigManager](src/main/java/io/agora/scene/voice/imkit/manager/ChatroomConfigManager.java)

自定义消息帮助类（主要用来发送自定义消息 解析自定义消息需要的属性)[CustomMsgHelper](src/main/java/io/agora/scene/voice/imkit/custorm/CustomMsgHelper.java)

IM管理类（包含加入房间、登录、退出登录等）[ChatroomIMManager](src/main/java/io/agora/scene/voice/imkit/manager/ChatroomIMManager.java)


### 4.FAQ
- 如何获取声网和环信APPID：
  - 声网APPID申请：https://www.agora.io/cn/
  - 环信APPID申请：https://www.easemob.com/
- 语聊房中的弹幕组件使用的是哪家？是否可以自己选择供应商？
  声动语聊源码使用的是环信AgoraChat的IM和信令服务，您也可以使用自己的服务。
- 集成遇到困难，该如何联系声网获取协助
  - 方案1：如果您已经在使用声网服务或者在对接中，可以直接联系对接的销售或服务；
  - 方案2：发送邮件给support@agora.io咨询。



