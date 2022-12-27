# 在线K歌房
# 1.项目介绍
## 1.1 概述
**在线K歌房**项目是声网在线K歌房场景的开源代码，开发者可以获取并添加到您的APP工程里，本源码会伴随声动互娱Demo同步更新，为了获取更多新的功能和更佳的音效，强烈推荐您下载最新代码集成。
## 1.2 功能介绍
在线K歌房场景目前已涵盖以下功能，您可以参考注释按需从代码中调用：
### 场景功能代码根目录：iOS/AgoraEntScenarios/Scenes/KTV
### 相关网络请求交互
  - 标准房间内消息管理
    房间内消息管理包括对房间内的基本交互请求和响应，例如用户的变化、已点歌曲列表的变化，通过Service/KTVServiceProtocol.h来定义协议，通过KTVSyncManagerServiceImp.swift来实现，您可以通过自己实现的其他ServiceImp来一键替换，无需改动业务代码。
    注意，KTVSyncManagerServiceImp只是用于Demo展示，切勿用于线上生产发布。
  - 歌曲列表管理
    通过声网AgoraRtcEngine的AgoraMusicContentCenter来获取，可以获取实时的歌曲排行榜列表。
    可以参考View/DianGe/VLSearchSongResultView里的loadSearchDataWithKeyWord:ifRefresh:
### 房间管理
  - 包含了房间的创建和房间列表的获取
  - 相关代码请参考：
    ViewController/VLCreateRoomViewController.m
    ViewController/VLOnLineListVC.m
    分别依赖KTVServiceProtocol的下列方法去交互
    getRoomListWithPage:completion:
    createRoomWithInput:completion:
    joinRoomWithInput:completion:
### 歌词展示
  - 歌词模块通过VLKTVMVView这个视图来集成歌词的渲染，其内部实际上是通过Podfile里依赖的AgoraLyricsScore这个组件来做歌词的展示和互动
  - 其中歌词的交互部分通过AgoraLrcViewDelegate，非歌词的交互部分通过VLKTVMVViewDelegate来回调到VLKTVViewController上做处理
### 麦位管理
  - 观众上麦下麦、房主强制观众下麦、静音、开启摄像头
  - 麦位管理相关功能主要依托于组件VLRoomPersonView，支持麦位的上下麦，静音、摄像头开关功能
  - 通过VLRoomPersonViewDelegate协议和VLKTVViewController做交互
### 歌曲管理
  - 点歌、已点歌曲删除、已点歌曲置顶
  - 歌曲列表菜单：请参考VLSelectSongView
  - 已点歌曲列表菜单：请参考VLPopChooseSongView
### 音效
  - 声网最佳音效，AI降噪
  - 音效方法请参考LSTPopView+KTVModal.h里的popSetSoundEffectViewWithParentView:withDelegate:
### 美声
  - 声网最佳美声
  - 音效方法请参考LSTPopView+KTVModal.h里的popBelcantoViewWithParentView:withBelcantoModel:withDelegate:

# 2.使用场景
声网在线K歌房源码，最终目的是方便开发者快速按需集成，减少开发者搭建K歌房的工作量。在现有源码的基础上，您可以按需自由定制，包括UI/UE，前端逻辑，权限体系等

# 3.快速开始

- 将项目的iOS/AgoraEntScenarios/KeyCenter.swift.bak重命名为KeyCenter.swift
- 在KeyCenter.swift文件中填写需要的 appId 和 appCertificate (**需要联系销售给 appId 开通 K 歌权限**)
```
AppId：声网appid
Certificate：声网Certificate
```
- 在集成的同时，需要去声网与环信的官网注册好对应的账号，同时开通对应的权限从而快速开始你的体验
- 然后pod install成功之后，打开项目即可开始您的体验

配置好这些参数之后。就可以快速开始体验了


# 4.FAQ

## 4.1 如何获取声网和环信APPID：
- 声网APPID申请：[https://www.agora.io/cn/](https://www.agora.io/cn/)
- 环信APPID申请：[https://www.easemob.com/](https://www.easemob.com/)
## 4.2 程序运行后，歌曲列表为空：
- 需要联系销售给 appId 开通 K 歌权限
## 4.3 K歌房中的歌曲资源使用的是哪家？是否可以自己选择供应商？
K歌房的歌曲资源使用的是Agora内容中心服务，暂不支持自行切换供应商，详情请查看https://docs.agora.io/cn/online-ktv/API%20Reference/ios_ng/API/toc_drm.html

## 4.4 集成遇到困难，该如何联系声网获取协助
方案1：如果您已经在使用声网服务或者在对接中，可以直接联系对接的销售或服务；

方案2：发送邮件给support@agora.io咨询
