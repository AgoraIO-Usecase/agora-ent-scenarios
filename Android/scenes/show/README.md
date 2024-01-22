# 秀场直播

> 本文档主要介绍如何快速跑通 <mark>秀场直播</mark> 示例工程
> 
> Demo 效果:
> 
> <img src="https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/ent-full/showRoom_1.png" width="300" height="640"><img src="https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/ent-full/showRoom_2.png" width="300" height="640">
---

## 1 环境准备

- 最低兼容 Android 7.0（SDK API Level 24）
- Android Studio 4.0及以上版本。
- Android 7.0 及以上的手机设备。
---

## 2 运行示例

### 2.1 获取声网App ID -------- [声网Agora - 文档中心 - 如何获取 App ID](https://docportal.shengwang.cn/cn/Agora%20Platform/get_appid_token?platform=All%20Platforms#%E8%8E%B7%E5%8F%96-app-id)
 
   - 点击创建应用  
     
     ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/create_app_1.jpg)
   
   - 选择你要创建的应用类型  
     
     ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/create_app_2.jpg)
   
   - 得到App ID与App 证书  
     
     ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/get_app_id.jpg)
   
   - 秒切机器人`CloudPlayer`服务配置，请联系销售人员为您的 appid 添加权限(如果您没有销售人员的联系方式可通过智能客服联系销售人员 [Agora 支持](https://agora-ticket.agora.io/))
  
     ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/show/config_cloud_player_android.png)
     ```json
     如果不填写CloudPlayer配置，机器人房间将无法出图
     ```

  获取App 证书 ----- [声网Agora - 文档中心 - 获取 App 证书](https://docportal.shengwang.cn/cn/Agora%20Platform/get_appid_token?platform=All%20Platforms#%E8%8E%B7%E5%8F%96-app-%E8%AF%81%E4%B9%A6)

### 2.2 在项目的[**gradle.properties**](../../gradle.properties)里填写需要的声网 App ID 和 App证书

  ![xxx](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/config_app_id_android.jpg)  
  
  ```texag-0-1gpap96h0ag-1-1gpap96h0ag-0-1gpap96h0ag-1-1gpap96h0ag-0-1gpap96h0ag-1-1gpap96h0ag-0-1gpap96h0ag-1-1gpap96h0ag-0-1gpap96h0ag-1-1gpap96h0
  AGORA_APP_ID：声网appid  
  AGORA_APP_CERTIFICATE：声网Certificate 
  CLOUD_PLAYER_KEY：声网RESTful API key
  CLOUD_PLAYER_SECRET：声网RESTful API secret
  ```

### 2.3 配置美颜功能
  
**美颜资源请联系"商汤科技"商务获取。**

1. 添加资源：
   - 将资源文件复制到**scenes/show/src/main/assets/beauty_sensetime** 目录下。这个项目用到的资源文件列举如下：
     - models/*.model : AI等训练模型资源
     - sticker_face_shape/lianxingface.zip : 贴纸资源
     - style_lightly/*.zip : 风格妆资源
   ```json
   如果不添加美颜资源无法体验美颜效果
   ```

2. 添加license：
   - 将证书文件复制到**scenes/show/src/main/assets/beauty_sensetime/license/SenseME.lic**路径下  
   ```json
   如果不添加美颜证书无法体验美颜效果
   ```

### 2.4 用 Android Studio 运行项目即可开始您的体验

---
## 3 项目介绍

### 3.1 概述
> 秀场直播项目是声网秀场直播场景的开源代码，开发者可以获取并添加到您的APP工程里，本源码会伴随声动互娱Demo同步更新，为了获取更多新的功能和更佳的体验，强烈推荐您下载最新代码集成。

### 3.2 功能介绍

 秀场直播场景目前已涵盖以下功能：
 - PK 和连麦 
 
   相关代码请参考：[LiveDetailFragment](src/main/java/io/agora/scene/show/LiveDetailFragment.kt) 中的 updatePKingMode() 和 updateLinkingMode() 的实现。

 - 秒切

   相关代码请参考：[LiveDetailActivity](src/main/java/io/agora/scene/show/LiveDetailActivity.kt) 中的 OnPageChangeCallback 的实现。
 - 美颜

   美颜是通过注册视频帧观测器，在视频观测器的 onCaptureVideoFrame 回调中通过商汤美颜SDK处理视频帧数据并替换实现美颜功能。
   
   商汤美颜功能的详细封装请参考：[BeautySenseTimeImpl](src/main/java/io/agora/scene/show/beauty/sensetime/BeautySenseTimeImpl.java) 的实现。
   
   ``` 
    @Override
    public boolean onCaptureVideoFrame(VideoFrame videoFrame) {
        ...
      videoFrame.replaceBuffer(newBuffer, videoFrame.getRotation(), videoFrame.getTimestampNs());
    }
   ```
 - 虚拟背景和虚化背景
   
   相关代码参考：[BeautyDialog](src/main/java/io/agora/scene/show/widget/BeautyDialog.kt) 中 onItemSelected.GROUP_ID_VIRTUAL_BG 部分。

## 4.FAQ

### 想体验更多场景

> 详情请查看 [声动互娱](../../../README.md)

### 集成遇到困难，该如何联系声网获取协助
  - 方案1：可以从智能客服获取帮助或联系销售人员[Agora 支持](https://agora-ticket.agora.io/) ；
  - 方案2：发送邮件给 [support@agora.io](mailto:support@agora.io) 咨询
  - 方案3：加入微信群提问
  
    ![xxx](https://download.agora.io/demo/release/SDHY_QA.jpg)

---

## 代码许可

示例项目遵守 MIT 许可证。

---


