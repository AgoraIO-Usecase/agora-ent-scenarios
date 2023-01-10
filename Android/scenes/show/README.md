# 秀场直播
**秀场**项目是声网秀场直播场景的开源代码，开发者可以获取并添加到您的APP工程里，本源码会伴随声动互娱Demo同步更新，为了获取更多新的功能和更佳的美颜效果，强烈推荐您下载最新代码集成。

## 功能
- 房间管理
- 美颜
- 单主播、1v1连麦、主播PK
- 音效
- 音视频参数配置

## 快速开始

### 1 配置美颜

#### 1.1 准备资源

- 商汤美颜SDK
- 商汤美颜resource
- 商汤美颜证书(证书和app的ApplicationId对应)

#### 1.2 项目配置

1. 将effectAAR-release.aar放在**scenes/show/aars**目录下

2. 将SDK里的资源文件复制到**scenes/show/src/main/assets** 目录下。这个项目用到的资源文件列举如下： 
- resource/LicenseBag.bundle : 证书资源
- resource/ModelResource.bundle : AI等训练模型资源
- resource/StickerResource.bundle : 贴纸资源
- resource/ComposeMakeup.bundle : 美妆资源
- resource/FilterResource.bundle : 滤镜资源

3. 将证书放到**scenes/show/src/main/assets/resource/LicenseBag.bundle**目录下，然后在**
   scenes/show/src/main/java/com/bytedance/labcv/core/Config.java**配置
```java
class Config {
    public static final String LICENSE_NAME = "证书文件名";
}
```

### 2 配置声网
- 在[**声网官网**](https://www.agora.io/cn/)注册账号并创建项目，获取项目的AppId，如果项目开启了证书认证，还需要获取项目的AppCertificate
- 在项目的[**gradle.properties**](../../gradle.properties)里填写需要的声网 AppId 和 AppCertificate（如果没有开启证书认证，此字段留空）
```xml
#rtc rtm SDK app_id
AGORA_APP_ID=<=YOUR APP ID=>
#rtc rtm SDK app_certifate
AGORA_APP_CERTIFICATE=<=YOUR APP CERTIFICATE=>
```
### 3 运行项目
用 [**Android Studio**](https://developer.android.com/studio) 打开项目即可开始您的体验

## 代码许可

示例项目遵守 MIT 许可证。

