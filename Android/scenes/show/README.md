# 秀场直播

> 本文档主要介绍如何快速跑通 <mark>秀场直播</mark> 示例工程
>
> Demo 效果:
>
> <img src="https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/show/showRoom_3.jpg" width="300" height="640"><img src="https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/show/showRoom_4.jpg" width="300" height="640">
---

## 1.环境准备

- 最低兼容 Android 7.0（SDK API Level 24）
- Android Studio 4.0及以上版本
- Android 7.0 及以上的手机设备

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
    ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/ent-full/sdhy_6.jpg)
    

- 2.2 在项目的 [**gradle.properties**](../../gradle.properties) 里填写需要的声网 APP ID 和 APP 证书、RESTFUL KEY 和 SECRET
    ![xxx](image/SamplePicture1.png)

  ```texag-0-1gpap96h0ag-1-1gpap96h0ag-0-1gpap96h0ag-1-1gpap96h0ag-0-1gpap96h0ag-1-1gpap96h0ag-0-1gpap96h0ag-1-1gpap96h0ag-0-1gpap96h0ag-1-1gpap96h0
  AGORA_APP_ID：声网 APP ID
  AGORA_APP_CERTIFICATE：声网 APP 证书
  RESTFUL_API_KEY：声网RESTful API key
  RESTFUL_API_SECRET：声网RESTful API secret
  ```

- 2.3 美颜配置
  ```json
  注: 项目使用的美颜资源需要向第三方美颜提供商获取, 没有美颜资源仅影响直播过程中的美颜效果, 不会影响 Demo 的运行
  ```

  **商汤美颜配置**
  美颜资源请联系商汤科技商务获取。

  ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/show/beauty_1.jpg)

  > - 将资源文件复制到 **scenes/show/src/main/assets/beauty_sensetime** 目录下。这个项目用到的资源文件列举如下：
  >
  >   - license/SenseME.lic : 证书资源
  >   - models/*.model : AI等训练模型资源
  >   - sticker_face_shape/lianxingface.zip : 贴纸资源
  >   - style_lightly/*.zip : 风格妆资源

  **相芯美颜配置**
  美颜资源请联系相芯商务获取。

  ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/show/beauty_2.jpg)

  > - 将相芯证书 authpack.java 放在 **scenes/show/src/main/java/io/agora/scene/show/beauty** 目录下
  >
  > - 将资源文件复制到 **scenes/show/src/main/assets/beauty_faceunity** 目录下。这个项目用到的资源文件列举如下：
  >
  >   - makeup : 风格妆资源
  >   - sticker : 贴纸资源

  **火山美颜配置**
  美颜资源请联系火山商务获取。

  ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/show/beauty_3.jpg)

  > - 将资源文件复制到 **scenes/show/src/main/assets/beauty_bytedance** 目录下。这个项目用到的资源文件列举如下：
  >
  >   - LicenseBag.bundle : 证书资源
  >   - ModelResource.bundle : AI等训练模型资源
  >   - StickerResource.bundle : 贴纸资源
  >   - ComposeMakeup.bundle : 风格妆资源
  >
  > - 修改 **scenes/show/src/main/java/io/agora/scene/show/beauty/ByteDanceBeautySDK.kt** 中 LICENSE_NAME 为证书文件名

- 2.4 运行项目即可开始您的体验

---
## 3.项目介绍
### 3.1 概述
> 秀场直播项目是声网秀场直播场景的开源代码，开发者可以获取并添加到您的 APP 工程里，本源码会伴随声动互娱 Demo 同步更新，为了获取更多新的功能和更佳的体验，强烈推荐您下载最新代码集成。
### 3.2 功能介绍
> 秀场直播场景目前已涵盖以下功能
> - PK 和连麦
    >
    >   相关代码请参考：[LiveDetailFragment](src/main/java/io/agora/scene/show/LiveDetailFragment.kt) 中的 updatePKingMode() 和 updateLinkingMode() 的实现。
> - 秒切
    >
    >   相关代码请参考：[LiveDetailActivity](src/main/java/io/agora/scene/show/LiveDetailActivity.kt) 中的 OnPageChangeCallback 的实现。
> - 美颜
>
>   美颜是通过注册视频帧观测器，在视频观测器的 onCaptureVideoFrame 回调中通过商汤美颜SDK处理视频帧数据并替换实现美颜功能。
>
>   商汤美颜功能的详细封装请参考：[SenseTimeBeautyAPIImpl](src/main/java/io/agora/beautyapi/sensetime/SenseTimeBeautyAPIImpl.kt) 的实现。
>   相芯美颜功能的详细封装请参考：[FaceUnityBeautyAPIImpl](src/main/java/io/agora/beautyapi/faceunity/FaceUnityBeautyAPIImpl.kt) 的实现。
>   火山美颜功能的详细封装请参考：[ByteDanceBeautyAPIImpl](src/main/java/io/agora/beautyapi/bytedance/ByteDanceBeautyAPIImpl.kt) 的实现。
>
>   ``` 
>    @Override
>    public boolean onCaptureVideoFrame(VideoFrame videoFrame) {
>        ...
>      videoFrame.replaceBuffer(newBuffer, videoFrame.getRotation(), videoFrame.getTimestampNs());
>    }
>   ```
### 3.3 场景化 API

声动互娱-秀场直播项目内使用了
* 美颜场景化 API (简称 BeautyAPI)来实现多第三方美颜快速接入, 对应代码文件: [BeautyAPI](src/main/java/io/agora/beautyapi), 如果您想进一步了解 BeautyAPI, 可以参考 [BeautyAPI Demo](https://github.com/AgoraIO-Community/BeautyAPI)
* 秒开秒切场景化 API (简称 VideoLoaderAPI)来实现观众看播视频秒出图、秒切换直播间的能力, 对应代码文件: [VideoLoaderAPI](src/main/java/io/agora/videoloaderapi), 如果您想进一步了解 VideoLoaderAPI, 可以参考 [VideoLoaderAPI Demo](https://github.com/AgoraIO-Community/VideoLoaderAPI)

  
## 4. FAQ

- 集成遇到困难，该如何联系声网获取协助
  - 方案1：可以从智能客服获取帮助或联系技术支持人员 [声网支持](https://ticket.shengwang.cn/form?type_id=&sdk_product=&sdk_platform=&sdk_version=&current=0&project_id=&call_id=&channel_name=)
  - 方案2：加入微信群提问
  
    ![](https://download.agora.io/demo/release/SDHY_QA.jpg)


