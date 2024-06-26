# 1v1私密房-Android

> 本文档主要介绍如何快速跑通 1v1私密房场景 示例工程
>
> Demo 效果:
>
> <img src="https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/private1v1/sample_receive_android.jpg" width="300" height="640"><img src="https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/private1v1/sample_calling_android.jpg" width="300" height="640">
---

## 1. 环境准备

- 最低兼容 Android 7.0（SDK API Level 24）
- Android Studio 4.0及以上版本。
- Android 7.0 及以上的手机设备。

---

## 2. 运行示例

- 获取声网App ID 和 App 证书  
  [声网Agora - 文档中心 - 如何获取 App ID]((https://docportal.shengwang.cn/cn/Agora%20Platform/get_appid_token?platform=All%20Platforms#%E8%8E%B7%E5%8F%96-app-id))

  - 点击创建应用

    ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/create_app_1.jpg)

  - 选择你要创建的应用类型

    ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/create_app_2.jpg)

  - 在"服务配置"中启用"RTM"功能

    ![图片](https://fullapp.oss-cn-beijing.aliyuncs.com/scenario_api/callapi/config/rtm_config1.jpg)

    ![图片](https://fullapp.oss-cn-beijing.aliyuncs.com/scenario_api/callapi/config/rtm_config2.jpg)

    ![图片](https://fullapp.oss-cn-beijing.aliyuncs.com/scenario_api/callapi/config/rtm_config3.jpg)

  - 得到App ID与App 证书

    ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/get_app_id.jpg)

- 获取App 证书 ----- [声网Agora - 文档中心 - 获取 App 证书](https://docportal.shengwang.cn/cn/Agora%20Platform/get_appid_token?platform=All%20Platforms#%E8%8E%B7%E5%8F%96-app-%E8%AF%81%E4%B9%A6)

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

项目名称：声动互娱-1v1私密房  
声动互娱-1v1私密房项目包含声网1v1私密房场景的Demo和开源代码，目的是方便开发者快速体验RTC和RTM在1v1私密房场景的集成效果，并给开发者提供代码参考，便于开发者快速搭建1v1私密房场景。在现有源码的基础上，您可以按需自由定制，包括UI/UE，前端逻辑，权限体系等。源码会伴随声动1v1私密房Demo同步更新，为了获取更佳的使用体验，强烈推荐您下载最新代码集成。

### 3.2 功能介绍

声动互娱-1v1私密房目前已涵盖以下功能，您可以参考注释按需从代码中调用：

- 用户列表业务 -- 房间管理及用户列表获取：
  - 场景的房间管理类，在内部对创建用户、移除用户、获取用户列表[Pure1v1ServiceImp](src/main/java/io/agora/scene/pure1v1/service/Pure1v1ServiceImp.kt)
- 1v1私密房业务 -- 拨打、接通、拒接、挂断：
  - 业务创建与准备：程序在这里进行1v1私密房之前的准备，以接收和响应其他客户端的呼叫 [CallServiceManager](src/main/java/io/agora/scene/pure1v1/service/CallServiceManager.kt)
  - 业务核心代码：程序在这里处理1v1私密房场景的核心逻辑，开发者可以复用这里的逻辑接入自己的业务 [callAPI](src/main/java/io/agora/scene/pure1v1/callAPI)
- Activities：
  - 用户列表: 用户可以在这个界面查询到其他在线用户，并进行呼叫和被叫 [RoomListActivity](src/main/java/io/agora/scene/pure1v1/ui/RoomListActivity.kt)
  - 通话界面：1v1私密房成功接通，会跳转到这里进行双向音视频互通[CallDetailActivity](src/main/java/io/agora/scene/pure1v1/ui/CallDetailActivity.kt)
- 数据面板及参数展示：
  - 显示1v1私密房互通时RTC SDK在互通时的视频及网络状况参数：[DashboardFragment](src/main/java/io/agora/scene/pure1v1/ui/DashboardFragment.kt)

---

## 4. FAQ

- 集成遇到困难，该如何联系声网获取协助
  - 方案1：可以从智能客服获取帮助或联系销售人员[Agora 支持](https://agora-ticket.agora.io/)
  - 方案2：发送邮件给 [support@agora.io](mailto:support@agora.io) 咨询
  - 方案3：加入微信群提问
  
    ![](https://download.agora.io/demo/release/SDHY_QA.jpg)

## 代码许可

示例项目遵守 MIT 许可证。

---