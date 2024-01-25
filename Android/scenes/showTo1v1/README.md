# 秀场转1v1私密房-Android

> 本文档主要介绍如何快速跑通 秀场转1v1私密房场景 示例工程
>
> Demo 效果:
>
> <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/ent-scenarios/readme/image/android/liveto1v1/sample_roomlist.png" width="300" height="640"><img src="https://fullapp.oss-cn-beijing.aliyuncs.com/ent-scenarios/readme/image/android/liveto1v1/sample_show_live.png" width="300" height="640"><img src="https://fullapp.oss-cn-beijing.aliyuncs.com/ent-scenarios/readme/image/android/liveto1v1/sample_show_call.png" width="300" height="640">
---

## 1. 环境准备

- <mark>最低兼容 Android 7.0</mark>（SDK API Level 24）
- Android Studio 3.5及以上版本
- Android 7.0 及以上的手机设备

---

## 2. 运行示例

- 进入声网控制台获取 APP ID 和 APP 证书 [控制台入口](https://console.shengwang.cn/overview)

  - 点击创建项目

    ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/ent-full/sdhy_1.jpg)

  - 选择项目基础配置, 鉴权机制需要选择**安全模式**

    ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/ent-full/sdhy_2.jpg)

  - 在项目的功能配置中启用"实时消息 RTM"功能
     ```json
     注: 如果没有启动"实时消息 RTM"功能, 将无法体验项目完整功能
     ```

    ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/ent-full/sdhy_7.jpg)

  - 拿到项目 APP ID 与 APP 证书

    ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/ent-full/sdhy_3.jpg)

- 在项目的 [**gradle.properties**](../../gradle.properties) 里填写声网 APP ID 和 APP 证书

  ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/config_app_id_android.jpg)

``` 
AGORA_APP_ID=声网 APP ID
AGORA_APP_CERTIFICATE=声网 APP 证书
```

- 用 Android Studio 运行项目即可开始您的体验

---

## 3.项目介绍

### 3.1 概述

项目名称：声动互娱-秀场转1v1私密房  
声动互娱-秀场转1v1私密房项目包含声网秀场转1v1私密房场景的 Demo 和开源代码，目的是方便开发者快速体验RTC和RTM在秀场转1v1私密房场景的集成效果，并给开发者提供代码参考，便于开发者快速搭建秀场转1v1
私密房场景。在现有源码的基础上，您可以按需自由定制，包括 UI/UE，前端逻辑，权限体系等。源码会伴随声动秀场转1v1私密房 Demo 同步更新，为了获取更佳的使用体验，强烈推荐您下载最新代码集成。

### 3.2 功能介绍

声动互娱-秀场转1v1私密房目前已涵盖以下功能，您可以参考注释按需从代码中调用：

- 用户列表业务 -- 房间管理及用户列表获取：
  - 场景的房间管理类，在内部对创建用户、移除用户、获取用户列表 [ShowTo1v1ServiceImpl](src/main/java/io/agora/scene/showTo1v1/service/ShowTo1v1ServiceImpl.kt)
- 秀场转1v1私密房业务 -- 拨打、自动接通、超时挂断、挂断：
  - 业务创建与准备：程序在这里进行1v1私密房之前的准备，以接收和响应其他客户端的呼叫 [ShowTo1v1Manger](src/main/java/io/agora/scene/showTo1v1/ShowTo1v1Manger.kt)
  - 业务核心代码：程序在这里处理1v1私密房场景的核心逻辑，开发者可以复用这里的逻辑接入自己的业务 [callAPI](src/main/java/io/agora/scene/showTo1v1/callapi)
- Activities：
  - 房间列表: 用户可以在这个界面查询到其他在线用户并可小窗查看主播直播，可以进行呼叫 [RoomListActivity](src/main/java/io/agora/scene/showTo1v1/ui/RoomListActivity.kt)
  - 创建房间: 用户可以在这个界面创建直播房间 [RoomCreateActivity](src/main/java/io/agora/scene/showTo1v1/ui/RoomCreateActivity.kt)
  - 通话界面：秀场1v1私密房单主播直播界面，观众点击呼叫可以进行双向音视频互通 [RoomDetailActivity](src/main/java/io/agora/scene/showTo1v1/ui/RoomDetailActivity.kt)
- 数据面板及参数展示：
  - 显示秀场转1v1私密房互通时 RTC SDK 在单主播直播/互通时的视频及网络状况参数：[DashboardFragment](src/main/java/io/agora/scene/showTo1v1/ui/fragment/DashboardFragment.kt)

### 3.3 场景化 API

声动互娱-1v1 私密房项目内使用了 
* 1v1 呼叫连麦场景化 API (简称 CallAPI)来实现 1v1 场景下秒接通、秒出图的能力, 对应代码文件: [CallAPI](src/main/java/io/agora/scene/showTo1v1/callapi), 如果您想进一步了解 CallAPI, 可以参考 [CallAPI Demo](https://github.com/AgoraIO-Community/CallAPI)
* 秒开秒切场景化 API (简称 VideoLoaderAPI)来实现观众看播视频秒出图、秒切换直播间的能力, 对应代码文件: [VideoLoaderAPI](src/main/java/io/agora/videoloaderapi), 如果您想进一步了解 VideoLoaderAPI, 可以参考 [VideoLoaderAPI Demo](https://github.com/AgoraIO-Community/VideoLoaderAPI)


---

## 4. FAQ

- 集成遇到困难，该如何联系声网获取协助
  - 方案1：可以从智能客服获取帮助或联系技术支持人员 [声网支持](https://ticket.shengwang.cn/form?type_id=&sdk_product=&sdk_platform=&sdk_version=&current=0&project_id=&call_id=&channel_name=)
  - 方案2：加入微信群提问
  
    ![](https://download.agora.io/demo/release/SDHY_QA.jpg)
