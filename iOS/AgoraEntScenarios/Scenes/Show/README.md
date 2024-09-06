# 秀场直播

> 本文档主要介绍如何快速跑通 <mark>秀场直播</mark> 示例工程
> 
> Demo 效果:
> 
> <img src="https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/show/showRoom_3.jpg" width="300" height="640"><img src="https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/show/showRoom_4.jpg" width="300" height="640">
---

## 1. 环境准备

- <mark>最低兼容 iOS 13.0</mark>
- Xcode

---

## 2. 运行示例

- 2.1 进入声网控制台获取 APP ID 和 APP 证书 [控制台入口](https://console.shengwang.cn/overview)

  - 点击创建项目

    ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/ent-full/sdhy_1.jpg)

  - 选择项目基础配置, 鉴权机制需要选择**安全模式**

    ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/ent-full/sdhy_2.jpg)

  - 拿到项目 APP ID 与 APP 证书

    ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/ent-full/sdhy_3.jpg)

  - 秒切机器人服务配置（CloudPlayer）
      ```json
      注: 请联系声网技术支持为您的 APPID 开通 rte-cloudplayer 权限, 开通权限后才能启动默认的机器人房间推流
      ```
    
    ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/ent-full/sdhy_4.jpg)
    ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/ent-full/sdhy_5.jpg)
    ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/ent-full/cloud_player_key_ios.jpg)

- 2.2 在项目的[KeyCenter.swift](../../KeyCenter.swift)里填写需要的声网 App ID 和 App证书
  
  ![xxx](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/show/KeyCenter.png)
  
  ```texag-0-1gpap96h0ag-1-1gpap96h0ag-0-1gpap96h0ag-1-1gpap96h0ag-0-1gpap96h0ag-1-1gpap96h0ag-0-1gpap96h0ag-1-1gpap96h0ag-0-1gpap96h0ag-1-1gpap96h0
  static let AppId: String = 声网AppID
  static let Certificate: String? = 声网App证书
  ```



- 2.3 美颜配置
   ```json
  注: 项目使用的美颜资源需要向第三方美颜提供商获取, 没有美颜资源仅影响直播过程中的美颜效果, 不会影响 Demo 的运行
  ```

商汤
  1. 
  > - 新建一个文件夹命名为SenseLib放在Podfile的同级目录下，并将商汤SDK里的资源文件复制到SenseLib 目录下。如图：
  > 
  >  ![xxx](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/show/SenseLibSource.png)
  >  
  >  <mark> 注意：以上只包含基础美颜功能。</mark>  
  >  如果需要贴纸或者风格效果，需要联系商汤获取，同样将资源放到SenseLib路径下即可。如图：
  > 
  >  ![xxx](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/show/SenceLibStickerStyle.png)
  >  
  2.
  >
  > - 将申请到的商汤的license文件命名为“SENSEME.lic”并拖入工程 如图：
  > 
  >   ![xxx](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/show/SensemeLicFile.png)
  >   
  > - 注意事项：
  > 
  >   **申请商汤美颜的bundleId一定要和工程的bundleId保持一致，否则美颜是无效的**
  > 
  
  相芯
  
  1. 
  > - 新建一个文件夹命名为FULib放在Podfile的同级目录下，并将相芯SDK里的资源文件复制到FULib 目录下。如图：
  > 
  >  ![xxx](./README_Resources/fu_resources.jpg)
  >  
  >  <mark> 注意：以上只包含基础美颜功能。</mark>  
  >  如果需要贴纸或者风格效果，需要联系相芯获取，同样将资源放到FULib/Resources路径下即可。如图：
  > 
  >  ![xxx](./README_Resources/fu_sticker.jpg)
  >  
  2.
  >
  > - 将申请到的相芯的license文件命名为“authpack.h”并拖入FULib路径下 如图：
  > 
  >   ![xxx](./README_Resources/fu_license.jpg)
  >   
  > - 注意事项：
  > 
  >   **申请相芯美颜的bundleId一定要和工程的bundleId保持一致，否则美颜是无效的**
  > 
  
  火山/字节
  
  1. 
  > - 新建一个文件夹命名为ByteEffectLib放在Podfile的同级目录下，并将火山SDK里的资源文件复制到ByteEffectLib 目录下。如图：
  > 
  >  ![xxx](./README_Resources/bytes_effect_sdk.jpg)
  >  
  >  <mark> 注意：以上只包含基础美颜功能。</mark>  
  >  如果需要贴纸或者风格效果，需要联系相芯获取，同样将资源放到ByteEffectLib/Resources路径下即可。如图：
  > 
  >  ![xxx](./README_Resources/bytes_resource.jpg)
  >  
  2.
  >
  > - 将申请到的火山的license文件“LicenseBag.bundle”拖入ByteEffectLib/Resources路径下,并修改Config.h文件中的LICENSE_NAME 如图：
  > 
  >   ![xxx](./README_Resources/bytes_license.jpg)
  >   
  > - 注意事项：
  > 
  >   **申请火山美颜的bundleId一定要和工程的bundleId保持一致，否则美颜是无效的**
  > 

- 更新pod

   打开终端，cd到Podfile所在目录，执行
  > 
  > pod install
  > 
   如果xcode14编译失败并遇到下图错误
  >
  >  ![xxx](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/show/PodError.png)
  >  
   在终端执行如下命令将cocoapods升级到1.12.0以上：
  >  
  >  sudo gem install -n /usr/local/bin cocoapods
  >  

  >  如果网络不好升级失败，也可以手动给三方库签名 
  >  
  >  将Team里的None替换成你自己的签名即可
  >  

- 运行项目即可开始您的体验

---
  


## 3. 项目介绍

### 3.1 概述

> **秀场直播**项目是声网秀场直播场景的开源代码，开发者可以获取并添加到您的APP工程里，本源码会伴随声动互娱Demo同步更新，为了获取更多新的功能和更佳的体验，强烈推荐您下载最新代码集成。

### 3.2 功能介绍

> 秀场直播场景目前已涵盖以下功能
> 
> - PK 和连麦
> 
>   相关代码请参考：[ShowLiveViewController](Controller/ShowLiveViewController.swift )中的 _onStartInteraction中对应的.pking和. onSeat的实现
> 
> - 秒切  
> 
>    相关代码请参考：[ ShowLivePagesViewController](Controller/ShowLivePagesViewController.swift )
> 
> - 美颜
> 
>   美颜SDK的调用入口是在 [ShowAgoraKitManager](Models/ShowAgoraKitManager.swift )中AgoraVideoFrameDelegate的回调方法
> ```texag-0-1gpap96h0ag-1-1gpap96h0ag-0-1gpap96h0ag-1-1gpap96h0ag-0-1gpap96h0ag-1-1gpap96h0ag-0-1gpap96h0ag-1-1gpap96h0ag-0-1gpap96h0ag-1-1gpap96h0
>    func onCapture(_ videoFrame: AgoraOutputVideoFrame) -> Bool {
>         videoFrame.pixelBuffer = BeautyManager.shareManager.processFrame(pixelBuffer: videoFrame.pixelBuffer)
>         return true
>     }
>  ```
>  
>    商汤美颜功能的详细封装请参考[SenseBeautyManager](Beauty/SenseBeaufy/SenseBeautyManager.swift)
>  
> - 虚拟背景和虚化背景
> 
>    相关代码请参考： [ShowAgoraKitManager](Models/ShowAgoraKitManager.swift )的函数enableVirtualBackground和seVirtualtBackgoundImage
>  

### 3.3 场景化 API

> 声动互娱-秀场直播项目内使用了
> 
> - 美颜场景化 API (简称 BeautyAPI)来实现多第三方美颜快速接入, 对应代码文件: [BeautyAPI](Beauty/BeautyAPI),如果您想进一步了解 BeautyAPI, 可以参考 [BeautyAPI Demo](https://github.com/AgoraIO-Community/BeautyAPI)
> 
>  - 秒开秒切场景化 API (简称 VideoLoaderAPI)来实现观众看播视频秒出图、秒切换直播间的能力, 对应代码文件: [VideoLoaderAPI](../../Common/API/VideoLoaderAPI/VideoLoaderAPI), 如果您想进一步了解 VideoLoaderAPI, 可以参考 [VideoLoaderAPI Demo](https://github.com/AgoraIO-Community/VideoLoaderAPI)
> 

## 4. FAQ

- 集成遇到困难，该如何联系声网获取协助
  - 方案1：可以从智能客服获取帮助或联系技术支持人员 [声网支持](https://ticket.shengwang.cn/form?type_id=&sdk_product=&sdk_platform=&sdk_version=&current=0&project_id=&call_id=&channel_name=)
  - 方案2：加入微信群提问
  
    ![](https://download.agora.io/demo/release/SDHY_QA.jpg)
