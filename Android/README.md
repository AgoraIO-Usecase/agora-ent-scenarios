# Agora泛娱乐FullDemo

## 说明

本项目目标为声网泛娱乐场景进行全方位demo，当前已支持在线K歌房Demo，未来将支持元语聊、元直播、互动游戏的场景。

## 配置与编译

### 配置项目

* 打开`gradle.properties`，修改相关配置为你的服务配置
* 配置`HOST`为你的服务器地址
* 配置`APP_ID`为你的声网APP_ID
* 配置`APP_CERTIFICATE`为你的声网APP证书token

### 编译项目

* 使用Android Studio（4.0.0及以上版本），点击Sync Project with Gradle Files按钮，直到第三方依赖库同步完成
* 点击Run按钮运行项目，运行成功后，你的手机中将安装完成“声动互娱“应用

本项目因涉及到较多的音视频处理操作，因此最好在真机调试运行，否则某些场景可能无法运行。

## 软件架构
Android mvvm
