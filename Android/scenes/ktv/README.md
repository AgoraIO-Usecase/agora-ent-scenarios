# 在线K歌房

# 1.项目介绍
## 1.1 概述
**在线K歌房**项目是声网在线K歌房场景的开源代码，开发者可以获取并添加到您的APP工程里，本源码会伴随声动互娱Demo同步更新，为了获取更多新的功能和更佳的音效，强烈推荐您下载最新代码集成。

## 1.2 功能介绍
在线K歌房场景目前已涵盖以下功能，您可以参考注释按需从代码中调用：

### 场景功能代码根目录

**Android/scenes/ktv**


### 相关网络请求交互
- 标准房间内消息管理
    房间内消息管理包括对房间内的基本交互请求和响应，例如用户的变化、已点歌曲列表的变化，通过[**KTVServiceProtocol**](src/main/java/io/agora/scene/ktv/service/KTVServiceProtocol.kt)来定义协议，通过[**KTVSyncManagerServiceImp**](src/main/java/io/agora/scene/ktv/service/KTVSyncManagerServiceImp.kt)来实现，您可以通过自己实现的其他ServiceImp来一键替换，无需改动业务代码。
- 歌曲列表管理
    通过声网RtcEngine的IAgoraMusicContentCenter来获取，可以获取实时的歌曲排行榜列表，可以参考[**RoomLivingViewModel**](src/main/java/io/agora/scene/ktv/live/RoomLivingViewModel.java)里的initRTCPlayer
### 房间管理
- 包含了房间的创建和房间列表的获取
- 相关代码请参考：
    [**RoomCreateViewModel**](src/main/java/io/agora/scene/ktv/create/RoomCreateViewModel.java)，分别依赖[**KTVServiceProtocol**](src/main/java/io/agora/scene/ktv/service/KTVServiceProtocol.kt)的下列方法去交互
```kotlin
    fun getRoomList(completion: (error: Exception?, list: List<RoomListModel>?) -> Unit)
    fun createRoom(
        inputModel: CreateRoomInputModel,
        completion: (error: Exception?, out: CreateRoomOutputModel?) -> Unit
    )
    fun joinRoom(
        inputModel: JoinRoomInputModel,
        completion: (error: Exception?, out: JoinRoomOutputModel?) -> Unit
    )
```
### 歌词展示
- 歌词模块通过[**LrcControlView**](src/main/java/io/agora/scene/ktv/widget/LrcControlView.java)这个视图来集成歌词的渲染，其内部实际上是通过[**config.gradle**](../../config.gradle)里依赖的lyricsView这个组件来做歌词的展示和互动
- 其中歌词的交互部分通过[**LrcControlView#setPitchViewOnActionListener**](src/main/java/io/agora/scene/ktv/widget/LrcControlView.java)，非歌词的交互部分通过[**LrcControlView#setOnLrcClickListener**](src/main/java/io/agora/scene/ktv/widget/LrcControlView.java)接口来回调到[**RoomLivingActivity**](src/main/java/io/agora/scene/ktv/live/RoomLivingActivity.java)上做处理，两者统一实现在[**LrcActionListenerImpl**](src/main/java/io/agora/scene/ktv/live/listener/LrcActionListenerImpl.java)
### 麦位管理
- 观众上麦下麦、房主强制观众下麦、静音、开启摄像头
- 麦位管理的ui实现在[**RoomLivingActivity**](src/main/java/io/agora/scene/ktv/live/RoomLivingActivity.java)，内部使用一个RecyclerView实现，通过**mRoomSpeakerAdapter**更新麦位信息
- 通过[**RoomLivingViewModel**](src/main/java/io/agora/scene/ktv/live/RoomLivingViewModel.java)的**seatListLiveData**和**seatLocalLiveData**来通知麦位更新
### 歌曲管理
- 点歌、已点歌曲删除、已点歌曲置顶
- 歌曲列表菜单：请参考[**RoomLivingActivity#showChooseSongDialog**]((src/main/java/io/agora/scene/ktv/live/RoomLivingViewModel.java))
### 音效/美声
- 声网最佳美声
- 实现参考[**MusicSettingDialog#Callback**](src/main/java/io/agora/scene/ktv/widget/MusicSettingDialog.java)里的**onEffectChanged**实现

# 2.使用场景
声网在线K歌房源码，最终目的是方便开发者快速按需集成，减少开发者搭建K歌房的工作量。在现有源码的基础上，您可以按需自由定制，包括UI/UE，前端逻辑，权限体系等

# 3.快速开始

- 在项目的[**gradle.properties**](../../gradle.properties)里填写需要的声网 appId 和 appCertificate (**需要联系销售给 appId 开通 K 歌权限**)
```
AGORA_APP_ID：声网appid
AGORA_APP_CERTIFICATE：声网Certificate
```
- 用 Android Studio 打开项目即可开始您的体验

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