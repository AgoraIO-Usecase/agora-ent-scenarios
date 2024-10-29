# 互动玩法-休闲玩法-Android

> 本文档主要介绍如何快速跑通 互动玩法-休闲玩法场景 示例工程
> 
> Demo 效果:
> 
> <img src="https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/release/playzone/gameLobby.png" width="300" height="640"><img src="https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/release/playzone/roomList.png" width="300" height="640"><img src="https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/release/playzone/roomDetail.png" width="300" height="640">
---

## 1. 环境准备

- 最低兼容 Android 7.0（SDK API Level 24）
- Android Studio 4.0及以上版本。
- Android 7.0 及以上的手机设备。

---

## 2. 运行示例

- 获取声网App ID 和 App 证书  
  [声网Agora - 文档中心 - 如何获取 App ID](https://docs.agora.io/cn/Agora%20Platform/get_appid_token?platform=All%20Platforms#%E8%8E%B7%E5%8F%96-app-id)  
  [声网Agora - 文档中心 - 获取 App 证书](https://docs.agora.io/cn/Agora%20Platform/get_appid_token?platform=All%20Platforms#%E8%8E%B7%E5%8F%96-app-%E8%AF%81%E4%B9%A6)

   - 点击创建应用
  
     ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/create_app_1.jpg)
  
   - 选择你要创建的应用类型
  
     ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/create_app_2.jpg)
  
   - 得到App ID与App 证书
      
     ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/get_app_id.jpg)
   
- 获取环信的App Key
  [环信](https://www.easemob.com/)

  - 创建应用

    ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/im_create_app.jpg)
  
  - 查看应用的App Key  
  
    ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/im_get_app_id.jpg)
  
- 在项目的[**gradle.properties**](../../gradle.properties)里填写需要的声网 App ID 和 环信的 App Key

  ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/config_app_id_android.jpg)

``` 
AGORA_APP_ID= （从声网console获取）
AGORA_APP_CERTIFICATE=（从声网console获取）
  
IM_APP_KEY= （从环信IM Console获取）
IM_APP_CLIENT_ID= （从环信IM Console获取）
IM_APP_CLIENT_SECRET= （从环信IM Console获取）
```


- [需要联系联系Sud.Tech](https://sud.tech/) 获取 appId、 appKey、 appSecret
 ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/release/playzone/appkey.png)

忽然前端接口参考：[SUD游戏化互动平台](https://docs.sud.tech/zh-CN/app/Client/StartUp-Android.html)

忽然hello-sud-plus-android参考：[SudMGPWrapper](https://github.com/SudTechnology/hello-sud-plus-android/tree/master/project/SudMGPWrapper)


- 用 Android Studio 运行项目即可开始您的体验

---

## 3.项目介绍

### 3.1 概述

项目名称：互动玩法-休闲玩法
此模块是声网互动玩法-休闲玩法场景的开源代码，开发者可以获取并添加到您的APP工程里，本源码会伴随声动互娱Demo同步更新，为了获取更多新的功能和更好的玩法，强烈推荐您下载最新代码集成。

### 3.2 项目文件结构简介

```
├── scene
│   ├── play_zone
│   │   └── src
|   |       ├── main
│   │       |    ├── java
│   │       │    |      └── io.agora.scene.playzone              
│   │       │    |                           ├── hall                  #游戏大厅&&房间列表模块
│   │       │    |                           ├── live                  #游戏房间内业务逻辑模块
│   │       │    |                           ├── service               #网络模块
│   │       │    |                           ├── widget                #UI模块
│   │       │    |                           ├── PlayCenter.kt         #场景全局配置
│   │       │    |                           └── PlayLogger.kt         #Log模块
│   │       |    ├── res            #资源文件
│   │       │    │      ├── drawable
│   │       │    │      ├── layout
│   │       │    │      ├── mipmap
│   │       │    │      └── values
│   │       |    └── AndroidManifest.xml
|   |       |
|   |       └── SudMGPWrapper      #忽然模块
│   │   
│   ├── build.gradle   #忽然appid 在此配置
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
---

### 4. FAQ
- 如何获取声网和环信APP ID：
  - 声网APP ID申请：https://www.agora.io/cn/
  - 环信APP ID申请：https://www.easemob.com/
  - 忽然APP ID申请：[需要联系联系Sud.Tech](https://sud.tech/)
- 休闲玩法的弹幕组件使用的是哪家？是否可以自己选择供应商？
  声动休闲玩法源码使用的是环信AgoraChat的IM服务，您也可以使用自己的服务
- 想体验更多场景
  - 详情请查看 [声动互娱](../../../README.md)
- 集成遇到困难，该如何联系声网获取协助
  - 方案1：如果您已经在使用声网服务或者在对接中，可以直接联系对接的销售或服务
  - 方案2：发送邮件给 [support@agora.io](mailto:support@agora.io) 咨询
  - 方案3：扫码加入我们的微信交流群提问

     <img src="https://download.agora.io/demo/release/SDHY_QA.jpg" width="360" height="360">
---
  
