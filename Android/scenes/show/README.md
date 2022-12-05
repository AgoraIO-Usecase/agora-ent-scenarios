# 秀场直播

## 美颜配置

### 1 准备资源

- 商汤美颜SDK
- 商汤美颜resource
- 商汤美颜证书(证书和app的ApplicationId对应)

### 2 项目配置

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

## 代码许可

示例项目遵守 MIT 许可证。

