# 在线K歌房

> 本文档主要介绍如何快速跑通 <mark>在线K歌房</mark> 示例工程
>
> Demo 效果:
> 
> <img src="https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/ent-full/ktvRoom_1.png" width="300" height="640"><img src="https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/ent-full/ktvRoom_2.png" width="300" height="640">
---

## 1. 环境准备

- <mark>最低兼容 Android 7.0</mark>（SDK API Level 24）
- Android Studio 3.5及以上版本。
- Android 7.0 及以上的手机设备。

---

## 2. 运行示例

- 获取声网 App ID -------- [声网Agora - 文档中心 - 如何获取 App ID](https://docs.agora.io/cn/Agora%20Platform/get_appid_token?platform=All%20Platforms#%E8%8E%B7%E5%8F%96-app-id)
  
  > - 点击创建应用
  >
  >   ![xxx](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/create_app_1.jpg)
  >
  > - 选择你要创建的应用类型
  >
  >   ![xxx](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/create_app_2.jpg)
  >
  > - 得到 App ID 与 App 证书
  >
  >   ![xxx](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/get_app_id.jpg)

- 获取 App 证书 ----- [声网Agora - 文档中心 - 获取 App 证书](https://docs.agora.io/cn/Agora%20Platform/get_appid_token?platform=All%20Platforms#%E8%8E%B7%E5%8F%96-app-%E8%AF%81%E4%B9%A6)

- 联系销售给 AppID 开通 K 歌权限(如果您没有销售人员的联系方式可通过智能客服联系销售人员 [Agora 支持](https://agora-ticket.agora.io/))

  ```json
  注: 拉取榜单、歌单、歌词等功能是需要开通权限的
  ```

- 在项目的 [**gradle.properties**](../../gradle.properties) 里填写需要的声网 App ID 和 App 证书

  ![xxx](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/ktv/config_app_id_android.png)

  ```texag-0-1gpap96h0ag-1-1gpap96h0ag-0-1gpap96h0ag-1-1gpap96h0ag-0-1gpap96h0ag-1-1gpap96h0ag-0-1gpap96h0ag-1-1gpap96h0ag-0-1gpap96h0ag-1-1gpap96h0
  AGORA_APP_ID：声网appid
  AGORA_APP_CERTIFICATE：声网Certificate
  ```

- 用 Android Studio 运行项目即可开始您的体验

---

## 3. 项目介绍

### 3.1 概述

> **在线K歌房**项目是声网在线K歌房场景的开源代码，开发者可以获取并添加到您的 APP 工程里，本源码会伴随声动互娱 Demo 同步更新，为了获取更多新的功能和更佳的音效，强烈推荐您下载最新代码集成。

### 3.2 项目文件结构简介

```
├── scene
│   ├── ktv
│   │   └── main
│   │       ├── java
│   │       │   └── io.agora.scene.ktv
│   │       │                       ├── bean           #数据类
│   │       │                       ├── create         #房间列表模块
│   │       │                       ├── debugSettings  #debug页面模块
│   │       │                       ├── live           #房间内业务逻辑模块
│   │       │                       ├── service        #网络模块
│   │       │                       ├── widget         #UI模块
│   │       │                       └── KTVLogger.kt   #Log模块
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

### 3.3 功能介绍

> 在线K歌房场景目前已涵盖以下功能，您可以参考注释按需从代码中调用
>
> 场景功能代码根目录 **Android/scenes/ktv**
>
> ---
>
> #### K歌房场景化API
>
> ![xxx](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/ktv/KTVSamplePicture5.png)
>
> K歌房场景化API是一个帮助您快速集成声网K歌房能力的模块, 使用这个模块, 您可以非常便捷的获取歌单信息、加载歌曲、切换演唱角色、控制音乐播放, 通过 [**KTVApi**](src/main/java/io/agora/scene/ktv/live/KTVApi.kt) 来定义协议，通过 [**KTVApiImp**](src/main/java/io/agora/scene/ktv/live/KTVApiImp.kt) 来实现, 您可以直接将这两个文件拷贝到您的项目中使用, 快速集成声网K歌房能力
>
> * 拉取歌单
>
>   包含了榜单、歌单、搜索歌曲的功能
>
>   ~~~kotlin
>   /**
>    * 获取歌曲榜单
>    * @param onMusicChartResultListener 榜单列表回调
>    */
>   fun fetchMusicCharts(
>     onMusicChartResultListener: (
>       requestId: String?,
>       status: Int,
>       list: Array<out MusicChartInfo>?
>     ) -> Unit
>   )
>   
>   /**
>    * 根据歌曲榜单类型获取歌单
>    * @param musicChartId 榜单id
>    * @param page 歌曲列表回调
>    * @param pageSize 歌曲列表回调
>    * @param jsonOption 自定义过滤模式
>    * @param onMusicCollectionResultListener 歌曲列表回调
>    */
>   fun searchMusicByMusicChartId(
>     musicChartId: Int,
>     page: Int,
>     pageSize: Int,
>     jsonOption: String,
>     onMusicCollectionResultListener: (
>       requestId: String?,
>       status: Int,
>       page: Int,
>       pageSize: Int,
>       total: Int,
>       list: Array<out Music>?
>     ) -> Unit
>   )
>   
>   /**
>    * 根据关键字搜索歌曲
>    * @param keyword 关键字
>    * @param page 歌曲列表回调
>    * @param jsonOption 自定义过滤模式
>    * @param onMusicCollectionResultListener 歌曲列表回调
>    */
>   fun searchMusicByKeyword(
>     keyword: String,
>     page: Int, pageSize: Int,
>     jsonOption: String,
>     onMusicCollectionResultListener: (
>       requestId: String?,
>       status: Int,
>       page: Int,
>       pageSize: Int,
>       total: Int,
>       list: Array<out Music>?
>     ) -> Unit
>   )
>   ~~~
>
> * 加载歌曲
>
>   通过这个接口, 您可以完成音乐和歌词的加载, 加载歌曲的进度、状态会通过回调通知您
>
>   ~~~kotlin
>   /**
>    * 异步加载歌曲，同时只能为一首歌loadSong，loadSong结果会通过回调通知业务层
>    * @param config 加载歌曲配置
>    * @param onMusicLoadStateListener 加载歌曲结果回调
>    *
>    * 推荐调用：
>    * 歌曲开始时：
>    * 主唱 loadMusic(songCode, KTVLoadMusicConfiguration(songId, autoPlay=true, mode=LOAD_MUSIC_AND_LRC, mainSingerUid)) switchSingerRole(SoloSinger)
>    * 观众 loadMusic(songCode, KTVLoadMusicConfiguration(songId, autoPlay=false, mode=LOAD_LRC_ONLY, mainSingerUid))
>    * 加入合唱时：
>    * 准备加入合唱者：loadMusic(KTVLoadMusicConfiguration(autoPlay=false, mode=LOAD_MUSIC_ONLY, songCode, mainSingerUid))
>    * loadMusic成功后switchSingerRole(CoSinger)
>    */
>   fun loadMusic(
>     songCode: Long,
>     config: KTVLoadMusicConfiguration,
>     onMusicLoadStateListener: OnMusicLoadStateListener
>   )
>   ~~~
>
> * 切换角色
>
>   通过这个接口, 您可以完成演唱过程中不同角色的切换, 切换角色的结果会通过回调通知您
>
>   ~~~kotlin
>   /**
>    * 异步切换演唱身份，结果会通过回调通知业务层
>    * @param newRole 新演唱身份
>    * @param onSwitchRoleState 切换演唱身份结果
>    *
>    * 允许的调用路径：
>    * 1、Audience -》SoloSinger 自己点的歌播放时
>    * 2、Audience -》LeadSinger 自己点的歌播放时， 且歌曲开始时就有合唱者加入
>    * 3、SoloSinger -》Audience 独唱结束时
>    * 4、Audience -》CoSinger   加入合唱时
>    * 5、CoSinger -》Audience   退出合唱时
>    * 6、SoloSinger -》LeadSinger 当前第一个合唱者加入合唱时，主唱由独唱切换成领唱
>    * 7、LeadSinger -》SoloSinger 最后一个合唱者退出合唱时，主唱由领唱切换成独唱
>    * 8、LeadSinger -》Audience 以领唱的身份结束歌曲时
>    */
>   fun switchSingerRole(
>     newRole: KTVSingRole,
>     onSwitchRoleStateListener: OnSwitchRoleStateListener?
>   )
>   ~~~
>
> * 控制歌曲
>
>   ~~~kotlin
>   /**
>   * 开始播放
>   */
>   fun startSing(songCode: Long, startPos: Long)
>   
>   /**
>   * 恢复播放
>   */
>   fun resumeSing()
>   
>   /**
>   * 暂停播放
>   */
>   fun pauseSing()
>   
>   /**
>   * 调整进度
>   */
>   fun seekSing(time: Long)
>   ~~~
>
> * 与歌词组件配合使用
>
>   支持您传入您自定义的歌词组件与 KTVApi 模块配合使用, 您需要让您的歌词组件继承 **ILrcView** 类并实现以下三个接口, KTVApi 模块回通过下列三个回调将演唱 pitch、歌曲播放进度、歌词url 发送给您的歌词组件
>
>   ~~~kotlin
>   interface ILrcView {
>       /**
>        * ktvApi内部更新音高pitch时会主动调用此方法将pitch值传给你的歌词组件
>        * @param pitch 音高值
>        */
>       fun onUpdatePitch(pitch: Float?)
>       
>       /**
>        * ktvApi内部更新音乐播放进度progress时会主动调用此方法将进度值progress传给你的歌词组件，50ms回调一次
>        * @param progress 歌曲播放的真实进度 50ms回调一次
>        */
>       fun onUpdateProgress(progress: Long?)
>       
>       /**
>        * ktvApi获取到歌词地址时会主动调用此方法将歌词地址url传给你的歌词组件，您需要在这个回调内完成歌词的下载
>        */
>       fun onDownloadLrcData(url: String?)
>   }
>       
>   /**
>    * 设置歌词组件，在任意时机设置都可以生效
>    * @param view 传入的歌词组件view， 需要继承ILrcView并实现ILrcView的三个接口
>    */
>   fun setLrcView(view: ILrcView)
>   ~~~
>
>   
>
> #### 业务服务器交互模块
>
> ![xxx](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/ktv/KTVSamplePicture6.png)
>
> 场景内和业务服务器的交互主要是场景内基本交互请求和响应，例如房间的变化、用户的变化、麦位的变化、已点歌曲列表的变化，通过 [**KTVServiceProtocol**](src/main/java/io/agora/scene/ktv/service/KTVServiceProtocol.kt) 来定义协议，通过 [**KTVSyncManagerServiceImp**](src/main/java/io/agora/scene/ktv/service/KTVSyncManagerServiceImp.kt) 来实现，您可以通过自己实现的其他ServiceImp来一键替换，无需改动业务代码。
>
> - 房间管理
>
>   包含了房间的创建和房间列表的获取
>
>   相关代码请参考：[**RoomCreateViewModel**](src/main/java/io/agora/scene/ktv/create/RoomCreateViewModel.java)，分别依赖 [**KTVServiceProtocol**](src/main/java/io/agora/scene/ktv/service/KTVServiceProtocol.kt) 的下列方法去交互
>
>   ```kotlin
>   fun getRoomList(completion: (error: Exception?, list: List<RoomListModel>?) -> Unit)
>   fun createRoom(
>     inputModel: CreateRoomInputModel,
>     completion: (error: Exception?, out: CreateRoomOutputModel?) -> Unit
>   )
>   fun joinRoom(
>     inputModel: JoinRoomInputModel,
>     completion: (error: Exception?, out: JoinRoomOutputModel?) -> Unit
>   )
>   ```
>
> - 麦位管理
>
>   包含上麦、下麦、开关麦、开关摄像头等状态的同步
>
> - 歌曲管理
>
>   点歌、已点歌曲删除、已点歌曲置顶、切歌等状态的同步
>
>   歌曲列表菜单：请参考 [**RoomLivingActivity#showChooseSongDialog**]((src/main/java/io/agora/scene/ktv/live/RoomLivingViewModel.java))
>
>   
>
> #### 其他功能
>
> * 音效、美声
>
>   声网最佳美声
>
>    实现参考 [**MusicSettingDialog#Callback**](src/main/java/io/agora/scene/ktv/widget/MusicSettingDialog.java)里的**onEffectChanged**实现

---

## 4. FAQ

### 程序运行后，歌曲列表为空

> 需要联系销售给 APPID 开通 K 歌权限

### K歌房中的歌曲资源使用的是哪家？是否可以自己选择供应商？

> K歌房的歌曲资源使用的是Agora内容中心服务，暂不支持自行切换供应商，详情请查看 [版权音乐 - 在线 K 歌房 - 文档中心 - 声网Agora](https://docs.agora.io/cn/online-ktv/API%20Reference/ios_ng/API/toc_drm.html)

### 想体验更多场景

> 详情请查看 [声动互娱](../../../README.md)

### 集成遇到困难，该如何联系声网获取协助

> 方案1：如果您已经在使用声网服务或者在对接中，可以直接联系对接的销售或服务；
>
> 方案2：发送邮件给 [support@agora.io](mailto:support@agora.io) 咨询
>
> 方案3：扫码加入我们的微信交流群提问
>
> <img src="https://download.agora.io/demo/release/SDHY_QA.jpg" width="360" height="360">
---
