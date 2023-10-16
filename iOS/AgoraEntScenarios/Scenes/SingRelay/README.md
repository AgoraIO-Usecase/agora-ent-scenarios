# 抢麦接唱

> 本文档主要介绍如何快速跑通 <mark>在线K歌房</mark> 示例工程
>
> Demo 效果:
> 
> <img src="https://download.agora.io/demo/release/SingRelayRoom_1.png" width="300" height="640"><img src="https://download.agora.io/demo/release/SingRelayRoom_2.png" width="300" height="640">
---

## 1. 环境准备

- <mark>最低兼容 iOS 13.0.0 </mark> 
- Xcode 13.0.0 及以上版本。
- iPhone 6 及以上的手机设备(系统需要 iOS 13.0.0 及以上)。

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

- 联系销售给  AppID  开通 K 歌权限 </mark>(如果您没有销售人员的联系方式可通过智能客服联系销售人员 [Agora 支持](https://agora-ticket.agora.io/)

  - 注: 拉取榜单、歌单、歌词等功能是需要开通权限的

- 在项目的 agora-ent-scenarios/iOS/AgoraEntScenarios/ 目录下会有一个 KeyCenter.swift 文件，需要在 KeyCenter.swift 里填写需要的声网 App ID 和 App 证书

  ![xxx](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/ktv/img_ktv_keys_ios.png)

  ```texag-0-1gpap96h0ag-1-1gpap96h0ag-0-1gpap96h0ag-1-1gpap96h0ag-0-1gpap96h0ag-1-1gpap96h0ag-0-1gpap96h0ag-1-1gpap96h0ag-0-1gpap96h0ag-1-1gpap96h0
  AppId：声网 appid
  Certificate：声网 Certificate
  ```
- 项目的第三方库使用 pod 集成，需要在 agora-ent-scenarios/iOS 目录下执行 pod install ,然后再开始体验项目
- 在 agora-ent-scenarios/iOS 目录下，找到 AgoraEntScenarios.xcworkspace 文件
- 用 Xcode 运行 .xcworkspace 文件 即可开始您的体验

---

## 3. 项目介绍

### 3.1 概述

> **抢麦接唱**项目是声网在线 抢麦接唱场景的开源代码，开发者可以获取并添加到您的 APP 工程里，本源码会伴随声动互娱 Demo 同步更新，为了获取更多新的功能和更佳的音效，强烈推荐您下载最新代码集成。

### 3.2 项目文件结构简介

```
├── Scenes
│   ├── SingRelay
│   │   └────FileDownloadCache #歌词下载工具类文件
│   │   └────Debug            #抢麦接唱 Debug工具类
│   │   └────Utils            #抢麦接唱的宏和Log
│   │   └────Model            #抢麦接唱里面用到的Model
│   │   └────View             #抢麦接唱里面用到的自定义View
│   │   └────Model            #抢麦接唱里面用到的Model
│   │   └────Service          #抢麦接唱里面用到的service接口和实现
│   │       ├── SRServiceProtocol.h #抢麦接唱里面用到的service接口
│   │       └── SRSyncManagerServiceImp.swift #抢麦接唱里面用到的service实现
│   │   └────ViewController          #抢麦接唱里面用到的控制器
│   │       ├── VLSRViewController.m #抢麦接唱主控制器
│   │   └────SRAPI          #抢麦接唱里面SRAPI
│   │       ├── SRApi.swift #SRAPI的声明
│   │       └── SRApiImpl.swift #SRAPI的实现
│   │   └────SRResource         #抢麦接唱里面用到的图片资源文件和国际化文件
├── KeyCenter  #项目的基础账号配置(appid、app证书)   
├── Common     #共用的UI视图，拓展，分类，工具类等文件
├── Extension  #Swift的extension
├── ThirdParty #第三方库
├── Context    #APP配置文件工具类，国际化工具类，日志工具类
├── HomeMenu   #项目的整体结构
└── Resource   #项目的公用资源文件
```

### 3.3 功能介绍

> 抢麦接唱场景目前已涵盖以下功能，您可以参考注释按需从代码中调用
>
> 场景功能代码根目录 **AgoraEntScenarios/AgoraEntScenarios/scenes/SingRelay**
>
> ---
>
> #### 抢麦接唱场景化API
>
> ![xxx](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/ktv/img_ktv_api_ios.png)
>
> 抢麦接唱 场景化 API 是一个帮助您快速集成声网 抢麦接唱 能力的模块, 使用这个模块, 您可以非常便捷的获取歌单信息、加载歌曲、切换演唱角色、控制音乐播放, 通过 [**SRApi**](SRAPI/SRApi.swift) 来定义协议，通过 [**SRApiImp**](SRAPI/SRApiImp.swift) 来实现, 您可以直接将这两个文件拷贝到您的项目中使用, 快速集成声网抢麦接唱能力
>
> * 拉取歌单
>
>   包含了榜单、歌单、搜索歌曲的功能
>
>   ~~~swift
>   /**
>     * 获取歌曲榜单
>     * Parameter completion: 榜单列表回调
>     */
>    func fetchMusicCharts(completion:@escaping MusicChartCallBacks)
>    
>    /**
>    * 根据歌曲榜单类型搜索歌单
>    *  Parameters:
>    *  musicChartId: 榜单id
>    *  page: 榜单的查询页数
>    *  pageSize: 查询每页的数据长度
>    *  jsonOption: 自定义过滤模式
>    *  completion: 歌曲列表回调
>    */
>    func searchMusic(musicChartId: Int,
>                     page: Int,
>                     pageSize: Int,
>                     jsonOption: String,
>                     completion:@escaping MusicResultCallBacks)
>    
>    /**
>    * 根据关键字搜索歌曲
>    *  Parameters:
>    *  keyword: 搜索关键字
>    *  page: 榜单的查询页数
>    *  pageSize: 查询每页的数据长度
>    *  jsonOption: 自定义过滤模式
>    *  completion: 歌曲列表回调
>    */
>    func searchMusic(keyword: String,
>                     page: Int, pageSize: Int,
>                     jsonOption: String,
>                    completion: @escaping MusicResultCallBacks)
>   ~~~
>
> * 加载歌曲
>
>   通过这个接口, 您可以完成音乐和歌词的加载, 加载歌曲的进度、状态会通过回调通知您
>
>   ~~~swift
>   /**
>    * 异步加载歌曲，同时只能为一首歌loadSong，loadSong结果会通过回调通知业务层
>    * @param config 加载歌曲配置
>    * @param mode  加载歌曲的模式
>    * @param onMusicLoadStateListener 加载歌曲结果回调
>    *
>    * 推荐调用：
>    * 歌曲开始时：
>    * 主唱 loadMusic(songCode, SRLoadMusicConfiguration(autoPlay=true, mode= .loadMusicAndLrc, mainSingerUid)) , switchSingerRole(SoloSinger)
>    * 观众 loadMusic(songCode, SRLoadMusicConfiguration(autoPlay=false, mode = .loadLrcOnly, mainSingerUid)) 
>    * 加入合唱时：
>    * 准备加入合唱者：loadMusic(songCode, SRLoadMusicConfiguration(autoPlay=false, mode = .loadMusicOnly, mainSingerUid)) 
>    * loadMusic成功后switchSingerRole(CoSinger)
>    */
>   func loadMusic(
>                config: SRSongConfiguration, 
>                mode: SRLoadMusicMode, 
>                onMusicLoadStateListener: >IMusicLoadStateListener)
>
> * 切换角色
>
>   通过这个接口, 您可以完成演唱过程中不同角色的切换, 切换角色的结果会通过回调通知您
>
>   ~~~swift
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
>   func switchSingerRole(
>     newRole: SRSingRole,
>     onSwitchRoleState:@escaping ISwitchRoleStateListener
>   )
>   ~~~
>
> * 控制歌曲
>
>   ~~~swift
>   /**
>   * 开始播放
>   */
>   func startSing(songCode: Int, startPos: Int)
>
>   /**
>   * 恢复播放
>   */
>   func resumeSing()
>
>   /**
>   * 暂停播放
>   */
>   func pauseSing()
>
>   /**
>   * 调整进度
>   */
>   func seekSing(time: Int)
>   ~~~
>
> * 与歌词组件配合使用
>
>   支持您传入您自定义的歌词组件与 SRApi 模块配合使用, 您需要让您的歌词组件继承 **ILrcView** 类并实现以下三个接口, SRApi 模块回通过下列三个回调将演唱 pitch、歌曲播放进度、歌词url 发送给您的歌词组件
>
>   ~~~swift
>   @objc public protocol SRLrcViewDelegate: NSObjectProtocol {
>       /**
>        * SRApi内部更新音高pitch时会主动调用此方法将pitch值传给你的歌词组件
>        * @param pitch 音高值
>        */
>        func onUpdatePitch(pitch: Float)
>       /**
>        * srApi内部更新音乐播放进度progress时会主动调用此方法将进度值progress传给你的歌词组件，50ms回调一次
>        * @param progress 歌曲播放的真实进度 50ms回调一次
>        */
>        func onUpdateProgress(progress: Int)
>        /**
>        * SRApi获取到歌词地址时会主动调用此方法将歌词地址url传给你的歌词组件，您需要在这个回调内完成歌词的下载
>        */
>        func onDownloadLrcData(url: String)
>   }
>   
>   /**
>    * 设置歌词组件，在任意时机设置都可以生效
>    * @param view 传入的歌词组件view， 需要继承ILrcView并实现ILrcView的三个接口
>    */
>   func setLrcView(view: SRLrcViewDelegate)
>   ~~~
>
> 
>
> #### 业务服务器交互模块
>
> ![xxx](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/ktv/img_ktv_service_ios.png)
>
> 场景内和业务服务器的交互主要是场景内基本交互请求和响应，例如房间的变化、用户的变化、麦位的变化、已点歌曲列表的变化，通过 [**SRServiceProtocol**](Service/SRServiceProtocol.swift) 来定义协议，通过 [**SRSyncManagerServiceImp**](Service/SRSyncManagerServiceImp.swift) 来实现，您可以通过自己实现的其他ServiceImp来一键替换，无需改动业务代码。
>
> - 房间管理
>
>   包含了房间的创建和房间列表的获取
>
>   相关代码请参考：[**SRServiceModel**](Service/SRServiceModel.swift)，分别依赖 [**SRServiceProtocol**](Service/SRServiceProtocol.swift) 的下列方法去交互
>
>   ```Swift
>   - (void)getRoomListWithPage:(NSUInteger)page
>                 completion:(void(^)(NSError* _Nullable, NSArray<VLRoomListModel*>* _Nullable))completion;
>
>   - (void)createRoomWithInput:(SRCreateRoomInputModel*)inputModel
>                 completion:(void (^)(NSError*_Nullable, SRCreateRoomOutputModel*_Nullable))completion;
>
>   - (void)joinRoomWithInput:(SRJoinRoomInputModel*)inputModel
>               completion:(void (^)(NSError* _Nullable, SRJoinRoomOutputModel*_Nullable))completion;
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
>   歌曲列表菜单：请参考  [**VLPopSongList**](View/SingRelay/View/SRSongGallery/VLSRPopSongList.m)
>
> 
>
> #### 其他功能
>
> * 音效、美声
>   声网最佳音效
>
>    实现参考  [**VLSRViewContolller**](ViewController/VLSRViewController.m) 里的 **effectItemClickAction** 实现
>
>   声网最佳美声
>
>    实现参考  [**VLSRViewContolller**](ViewController/VLSRViewController.m) 里的 **onVLChooseBelcantoView** 实现

---

## 4. FAQ

### 如何获取声网 APPID

> 声网 APPID 申请：[https://www.agora.io/cn/](https://www.agora.io/cn/)

### 程序运行后，歌曲列表为空

> 需要联系销售给 APPID 开通 K 歌权限

### 抢麦接唱中的歌曲资源使用的是哪家？是否可以自己选择供应商？

> 抢麦接唱的歌曲资源使用的是Agora内容中心服务，暂不支持自行切换供应商，详情请查看 [版权音乐 - 在线 K 歌房 - 文档中心 - 声网Agora](https://docs.agora.io/cn/online-ktv/API%20Reference/ios_ng/API/toc_drm.html)

### 集成遇到困难，该如何联系声网获取协助

> 方案1：如果您已经在使用声网服务或者在对接中，可以直接联系对接的销售或服务；
>
> 方案2：发送邮件给 [support@agora.io](mailto:support@agora.io) 咨询

---
