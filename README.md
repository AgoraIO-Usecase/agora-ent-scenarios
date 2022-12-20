# 声网语聊房-iOS-中文
# 1.项目介绍
## 1.1 概述
**声动语聊**项目是声网语聊房场景的开源代码，开发者可以获取并添加到您的APP工程里，本源码会伴随声动语聊Demo同步更新，为了获取更多新的功能和更佳的音效，强烈推荐您下载最新代码集成。
## 1.2 功能介绍
声动语聊目前已涵盖以下功能，您可以参考注释按需从代码中调用：
### 房间管理：房间列表管理，创建房间
    - 功能代码路径：AgoraScene_iOS/AgoraScene_iOS/Business/RoomService/
### 席位管理：踢人，麦位静音，麦位锁定
    - 麦位管理相关功能主要依托于组件MicPosView，拖拽到项目中即可使用
    - AgoraChatRoomNormalRtcView：基础麦位组件，支持麦位的上下麦，换麦，静音/解除静音，锁麦/解锁，麦位的单个刷新，音量更新等麦位功能。
    - 麦位相关操作逻辑请参考VoiceRoomViewController.swift文件
### 互动：弹幕，打赏
    - IM相关的弹幕和打赏请参考VoiceRoomViewController+IM.swift文件
    - 当前版本使用的环信IM，用户可以自主选择集成的IM
### 音效：声网最佳音效，AI降噪
    - 音效方法参考：VoiceRoomViewController+ChatBar.swift文件的 func showEQView()
    - 该方法支持对音效功能的统一处理
# 2.使用场景
声网声动语聊源码，最终目的是方便开发者快速按需集成，减少开发者搭建语聊房的工作量。在现有源码的基础上，您可以按需自由定制，包括UI/UE，前端逻辑，权限体系等

# 3.快速开始

- 在集成的同时，需要去声网合环信的官网注册好对应的账号，同时开通对应的权限从而快速开始你的体验
- 然后pod install成功之后，打开项目即可开始您的体验
- 在项目的keycenter.swift文件中。需要填写对应的账号和token
```
AppId：声网appid
Certificate：声网Certificate
Token：默认为nil，这个会在服务端生成，可以忽略
HostUrl：这个是服务端Url，由服务端确认
IMAppKey：环信appkey
IMClientId：环信IMClientId
IMClientSecret：环信IMClientSecret
onlineBaseServerUrl：获取IM，RTC的信息的url前缀，这个服务端来指定
```
配置好这些参数之后。就可以快速开始体验了♥️


# 4.FAQ

## 4.1 如何获取声网和环信APPID：
- 声网APPID申请：[https://www.agora.io/cn/](https://www.agora.io/cn/)
- 环信APPID申请：[https://www.easemob.com/](https://www.easemob.com/)
## 4.2 语聊房中的弹幕组件使用的是哪家？是否可以自己选择供应商？
声动语聊源码使用的是环信AgoraChat的IM和信令服务，您也可以使用自己的服务

## 4.3 集成遇到困难，该如何联系声网获取协助
方案1：如果您已经在使用声网服务或者在对接中，可以直接联系对接的销售或服务；

方案2：发送邮件给support@agora.io咨询
