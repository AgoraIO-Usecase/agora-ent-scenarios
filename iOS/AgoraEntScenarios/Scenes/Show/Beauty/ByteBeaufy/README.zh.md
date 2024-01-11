# 字节/火山美颜场景化API

[English](README.md) | 中文

## 前提条件
- 项目里已经集成了Agora RTC SDK
- 联系字节客服拿到字节/火山的美颜SDK及其资源文件

## 快速集成
2.解压字节/火山SDK并将以下aar库、资源文件、证书配置到项目对应目录下

| 字节/火山SDK文件/目录                                 | 项目目录                            |
|-----------------------------------------------|---------------------------------|
| BytedEffects/app/Resource                       | iOS/ByteEffectLib/Resource           |
| byted_effect_ios_static/iossample\_static/libeffect-sdk.a                    | iOS/ByteEffectLib/ibeffect-sdk.a           |
| byted_effect_ios_static/iossample_static/effect-sdk.framework                    | iOS/ByteEffectLib/effect-sdk.framework           |

2.配置依赖库

```podfile
	pod 'bytedEffect', :path => 'bytedEffect.podspec'
```

3.复制以下场景化接口及实现到项目里
```
BeautyAPI
    ├── BeautyAPI.{h,m}
    └── Render/BytesRender
```

4.初始化

```swift
private lazy var beautyAPI = BeautyAPI()
private lazy var bytesRender = BytesBeautyRender()

let config = BeautyConfig()
config.rtcEngine = rtcEngine
config.captureMode = .agora
config.beautyRender = bytesRender
config.statsEnable = false
config.statsDuration = 1
config.eventCallback = { stats in
    print("min == \(stats.minCostMs)")
    print("max == \(stats.maxCostMs)")
    print("averageCostMs == \(stats.averageCostMs)")
}
let result = beautyAPI.initialize(config)
if result != 0 {
    print("initialize error == \(result)")
}
```

5.美颜开关(默认关)

```swift
beautyAPI.enable(true)
```

6.本地渲染

```swift
beautyAPI.setupLocalVideo(localView, renderMode: .hidden)
rtcEngine.startPreview()
```

7.设置推荐美颜参数
```swift
beautyAPI.setBeautyPreset(.default) // BeautyPreset.CUSTOM：自己实现美颜参数
```

8.销毁美颜

```swift
rtcEngine.leaveChannel()
beautyAPI.destroy()
AgoraRtcEngineKit.destroy()
```

## 自定义采集模式
美颜场景API除了能够内部直接使用RTC 祼数据接口进行美颜处理，也支持由外部传入视频帧进行处理，实现步骤如下：

1.初始化时配置captureMode为CaptureMode.Custom

```swift
let config = BeautyConfig()
config.rtcEngine = rtcEngine
config.captureMode = .custom
config.beautyRender = bytesRender
config.statsEnable = false
config.statsDuration = 1
config.eventCallback = { stats in
    print("min == \(stats.minCostMs)")
    print("max == \(stats.maxCostMs)")
    print("averageCostMs == \(stats.averageCostMs)")
}
let result = beautyAPI.initialize(config)
if result != 0 {
    print("initialize error == \(result)")
}
```
2.将外部数据帧通过onFrame接口传入，处理成功会替换VideoFrame的buffer数据，即videoFrame参数既为输入也为输出

```swift
beautyAPI.onFrame(pixelBuffer) { pixelBuffer in
    videoFrame.pixelBuffer = pixelBuffer
}
```

## 联系我们

- 如果你遇到了困难，可以先参阅 [常见问题](https://docs.agora.io/cn/faq)
- 如果你想了解更多官方示例，可以参考 [官方SDK示例](https://github.com/AgoraIO)
- 如果你想了解声网SDK在复杂场景下的应用，可以参考 [官方场景案例](https://github.com/AgoraIO-usecase)
- 如果你想了解声网的一些社区开发者维护的项目，可以查看 [社区](https://github.com/AgoraIO-Community)
- 完整的 API 文档见 [文档中心](https://docs.agora.io/cn/)
- 若遇到问题需要开发者帮助，你可以到 [开发者社区](https://rtcdeveloper.com/) 提问
- 如果需要售后技术支持, 你可以在 [Agora Dashboard](https://dashboard.agora.io) 提交工单
- 如果发现了示例代码的 bug，欢迎提交 [issue](https://github.com/AgoraIO-Community/BeautyAPI/issues)

## 代码许可

The MIT License (MIT)