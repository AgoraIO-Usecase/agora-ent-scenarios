# 语聊房
# 1.项目介绍
## 1.1 概述
**声动语聊**项目是声网语聊房场景的开源代码，开发者可以获取并添加到您的APP工程里，本源码会伴随声动语聊Demo同步更新，为了获取更多新的功能和更佳的音效，强烈推荐您下载最新代码集成。
## 1.2 功能介绍

### 场景功能代码根目录

**iOS/AgoraEntScenarios/Scenes/VoiceChatRoom**

### 相关类restApi网络请求交互
- 房间管理及语聊房内的交互请求和响应，如麦位变化、消息变化、礼物收发、定向消息转发、成员变化等，通过[**ChatRoomServiceProtocol**](https://github.com/AgoraIO-Usecase/agora-ent-scenarios/blob/feat/scene/voicechat_ios_merge/iOS/AgoraEntScenarios/Scenes/VoiceChatRoom/Service/ChatRoomServiceProtocol.swift)来定义协议，通过[**ChatRoomServiceImp**](https://github.com/AgoraIO-Usecase/agora-ent-scenarios/blob/feat/scene/voicechat_ios_merge/iOS/AgoraEntScenarios/Scenes/VoiceChatRoom/Service/ChatRoomServiceImp.swift)来实现，您可以通过自己实现的ServiceImp来一键替换，无需改动业务代码。

### 语聊房场景目前已涵盖以下功能，您可以参考注释按需从代码中调用：
### 房间管理：房间列表管理，创建房间

- 功能代码路径：[RoomService](https://github.com/AgoraIO-Usecase/agora-ent-scenarios/tree/feat/scene/voicechat_ios_merge/iOS/AgoraEntScenarios/Scenes/VoiceChatRoom/Controllers/RoomManager)

### 席位管理：踢人，麦位静音，麦位锁定
    
- 麦位管理相关功能主要依托于组件MicPosView，拖拽到项目中即可使用
- [AgoraChatRoomNormalRtcView](https://github.com/AgoraIO-Usecase/agora-ent-scenarios/blob/feat/scene/voicechat_ios_merge/iOS/AgoraEntScenarios/Scenes/VoiceChatRoom/Views/VoiceChat/AgoraChatRoomNormalRtcView.swift)：基础麦位组件，支持麦位的上下麦，换麦，静音/解除静音，锁麦/解锁，麦位的单个刷新，音量更新等麦位功能。
- 麦位相关操作逻辑请参考[VoiceRoomViewController+Mic.swift](https://github.com/AgoraIO-Usecase/agora-ent-scenarios/blob/feat/scene/voicechat_ios_merge/iOS/AgoraEntScenarios/Scenes/VoiceChatRoom/Controllers/VoiceChat/VoiceRoomViewController%2BMic.swift)文件
### 互动：弹幕，打赏
- IM相关的弹幕以及回调[VoiceRoomViewController+IM.swift](https://github.com/AgoraIO-Usecase/agora-ent-scenarios/blob/feat/scene/voicechat_ios_merge/iOS/AgoraEntScenarios/Scenes/VoiceChatRoom/Controllers/VoiceChat/VoiceRoomViewController%2BIM.swift)文件
- IM相关的打赏 [VoiceRoomViewController+ChatBar.swift](https://github.com/AgoraIO-Usecase/agora-ent-scenarios/blob/feat/scene/voicechat_ios_merge/iOS/AgoraEntScenarios/Scenes/VoiceChatRoom/Controllers/VoiceChat/VoiceRoomViewController%2BChatBar.swift)
- 当前版本使用的环信IM，用户可以自主选择集成的IM

### 音效：声网最佳音效，AI降噪
- 音效方法参考：[VoiceRoomViewController+ChatBar.swift](https://github.com/AgoraIO-Usecase/agora-ent-scenarios/blob/feat/scene/voicechat_ios_merge/iOS/AgoraEntScenarios/Scenes/VoiceChatRoom/Controllers/VoiceChat/VoiceRoomViewController%2BChatBar.swift)文件的 func showEQView()
- 该方法支持对音效功能的统一处理

# 2.使用场景

声网声动语聊源码，最终目的是方便开发者快速按需集成，减少开发者搭建语聊房的工作量。在现有源码的基础上，您可以按需自由定制，包括UI/UE，前端逻辑，权限体系等

# 3.快速开始

- 在集成的同时，需要去声网合环信的官网注册好对应的账号
- 将项目的iOS/AgoraEntScenarios/KeyCenter.swift.bak重命名为KeyCenter.swift
- 在KeyCenter.swift文件中填写需要的声网的 appId 和 appCertificate 及环信的 IMAppKey, IMClientId 和 IMClientSecret
- 然后pod install成功之后，打开项目即可开始您的体验
```
AppId：声网appid
Certificate：声网Certificate
IMAppKey：环信appkey
IMClientId：环信IMClientId
IMClientSecret：环信IMClientSecret
```
配置好这些参数之后。就可以快速开始体验了♥️


# 4.FAQ

## 4.1 如何获取声网和环信APPID：
- 声网APPID申请：[https://www.agora.io/cn/](https://www.agora.io/cn/)
- 环信APPKey申请：[https://www.easemob.com/](https://www.easemob.com/)
## 4.2 语聊房中的弹幕组件使用的是哪家？是否可以自己选择供应商？
声动语聊源码使用的是环信AgoraChat的IM和信令服务，您也可以使用自己的服务

## 4.3 集成遇到困难，该如何联系声网获取协助
方案1：如果您已经在使用声网服务或者在对接中，可以直接联系对接的销售或服务；

方案2：发送邮件给support@agora.io咨询
