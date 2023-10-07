# 1v1私密房-Android

> 本文档主要介绍如何快速跑通 1v1私密房场景 示例工程
>
> Demo 效果:
>
> <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/ent-scenarios/readme/image/android/liveto1v1/sample_roomlist.png" width="300" height="640"><img src="https://fullapp.oss-cn-beijing.aliyuncs.com/ent-scenarios/readme/image/android/liveto1v1/sample_createroom.png" width="300" height="640"><img src="https://fullapp.oss-cn-beijing.aliyuncs.com/ent-scenarios/readme/image/android/liveto1v1/sample_show_live.png" width="300" height="640"><img src="https://fullapp.oss-cn-beijing.aliyuncs.com/ent-scenarios/readme/image/android/liveto1v1/sample_show_call.png" width="300" height="640">
---

## 1. 环境准备

- <mark>最低兼容 Android 5.0</mark>（SDK API Level 21）
- Android Studio 3.5及以上版本。
- Android 5.0 及以上的手机设备。

---

## 2. 运行示例

- 获取声网App ID 和 App 证书  
  [声网Agora - 文档中心 - 如何获取 App ID](https://docs.agora.io/cn/Agora%20Platform/get_appid_token?platform=All%20Platforms#%E8%8E%B7%E5%8F%96-app-id)

  - 点击创建应用

    ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/create_app_1.jpg)

  - 选择你要创建的应用类型

    ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/create_app_2.jpg)

  - 得到App ID与App 证书

    ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/get_app_id.jpg)

  [声网Agora - 文档中心 - 获取 App 证书](https://docs.agora.io/cn/Agora%20Platform/get_appid_token?platform=All%20Platforms#%E8%8E%B7%E5%8F%96-app-%E8%AF%81%E4%B9%A6)

- 在项目的[**gradle.properties**](../../gradle.properties)里填写需要的声网 App ID

  ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/config_app_id_android.jpg)

``` 
AGORA_APP_ID= （从声网console获取）
AGORA_APP_CERTIFICATE=（从声网console获取）
```

- 用 Android Studio 运行项目即可开始您的体验

---

## 3.项目介绍

### 3.1 概述

项目名称：声动互娱-秀场转1v1私密房  
声动互娱-秀场转1v1私密房项目包含声网秀场转1v1私密房场景的Demo和开源代码，目的是方便开发者快速体验RTC和RTM在秀场转1v1私密房场景的集成效果，并给开发者提供代码参考，便于开发者快速搭建秀场转1v1
私密房场景。在现有源码的基础上，您可以按需自由定制，包括UI/UE，前端逻辑，权限体系等。源码会伴随声动秀场转1v1私密房Demo同步更新，为了获取更佳的使用体验，强烈推荐您下载最新代码集成。

### 3.2 功能介绍

声动互娱-秀场转1v1私密房目前已涵盖以下功能，您可以参考注释按需从代码中调用：

- 用户列表业务 -- 房间管理及用户列表获取：
  - 场景的房间管理类，在内部对创建用户、移除用户、获取用户列表[ShowTo1v1ServiceImpl](src/main/java/io/agora/scene/showTo1v1/service/ShowTo1v1ServiceImpl.kt)
- 1v1私密房业务 -- 拨打、接通、拒接、挂断：
  - 业务创建与准备：程序在这里进行1v1私密房之前的准备，以接收和响应其他客户端的呼叫 [ShowTo1v1Manger](src/main/java/io/agora/scene/showTo1v1/ShowTo1v1Manger.kt)
  - 业务核心代码：程序在这里处理1v1私密房场景的核心逻辑，开发者可以复用这里的逻辑接入自己的业务 [callAPI](src/main/java/io/agora/scene/showTo1v1/callAPI)
- Activities：
  - 房间列表: 用户可以在这个界面查询到其他在线用户并可小窗查看主播直播，可以进行呼叫 [RoomListActivity](src/main/java/io/agora/scene/showTo1v1/ui/RoomListActivity.kt)
  - 创建房间: 用户可以在这个界面创建直播房间 [RoomCreateActivity](src/main/java/io/agora/scene/showTo1v1/ui/RoomCreateActivity.kt)
  - 通话界面：秀场1v1私密房单主播直播界面，观众点击呼叫可以进行双向音视频互通[RoomDetailActivity](src/main/java/io/agora/scene/showTo1v1/ui/RoomDetailActivity.kt)
- 数据面板及参数展示：
  - 显示秀场转1v1私密房互通时RTC SDK在互通时的视频及网络状况参数：[DashboardFragment](src/main/java/io/agora/scene/showTo1v1/ui/fragment/DashboardFragment.kt)

---

## 4. FAQ
- 如何获取声网和环信APP ID：
  - 声网APP ID申请：https://www.agora.io/cn/
  - 环信APP ID申请：https://www.easemob.com/

- 集成遇到困难，该如何联系声网获取协助
  - 方案1：可以从智能客服获取帮助或联系销售人员[Agora 支持](https://agora-ticket.agora.io/) ；
  - 方案2：如果您已经在使用声网服务或者在对接中，可以直接联系对接的销售或服务；
  - 方案3：发送邮件给 [support@agora.io](mailto:support@agora.io) 咨询

## 代码许可

示例项目遵守 MIT 许可证。

---