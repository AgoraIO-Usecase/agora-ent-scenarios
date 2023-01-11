# 秀场直播

## 美颜配置

### 1 准备资源

- 商汤美颜SDK
- 商汤美颜resource
- 商汤美颜证书(证书和app的ApplicationId对应)

### 2 项目配置

1. 将STMobileJNI-release.aar放在**scenes/show/aars/STMobileJNI**目录下

2. 将SenseArSourceManager-release.aar放在**scenes/show/aars/SenseArSourceManager**目录下

3. 将SDK里的资源文件复制到**scenes/show/src/main/assets** 目录下。这个项目用到的资源文件列举如下： 
- license/SenseME.lic : 证书资源
- models/*.model : AI等训练模型资源
- sticker_face_shape/lianxingface.zip : 贴纸资源
- style_lightly/*.zip : 风格妆资源


## 代码许可

示例项目遵守 MIT 许可证。

