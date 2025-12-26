# Android 美颜功能变更总结

> 本文档总结了 Android 端美颜功能的更新，用于指导 iOS 版本的实现
> 
> 分析时间：2025-12-16
> 涉及提交：11 个 commit

---

## 📋 提交列表

| Commit | 日期 | 说明 |
|--------|------|------|
| `9d9047e89c43a2a37a70961a24d9f77add5a4143` | 2025-12-16 | update agora beauty mirror |
| `982bc948422356f5c1abd138dc83fff89402aea7` | 2025-12-11 | update agora beauty |
| `7b5cd5ce49984283d5dfbd47206cd67af74d2bc7` | 2025-12-11 | update bare face |
| `95e498e5d30dc11522091076c7badf0ae1790759` | 2025-12-11 | fixs first install agora beauty not work |
| `bb3684e23c96c40c3de923c00202f0956f70e3fa` | 2025-12-10 | update beauty |
| `0a2db8acfa5aaf201ebdc4d0326edd4453011070` | 2025-12-10 | update faceunity beauty |
| `c14150c066eb725393b7c0f0839e1eb82be41576` | 2025-12-10 | update agora beauty |
| `dc4a98a4e8d7cce03f179fc4a88df1af32a01b75` | 2025-12-10 | add agora beauty mode |
| `9349d1d1cb7eb916a831f7fa3430cc83d4a9eb3d` | 2025-12-10 | Add app cup && memory (⚠️ 与美颜功能无关，可忽略) |
| `0234423578beaaf87ce5bc41735d23772b991752` | 2025-12-09 | add beauty contrast switch |
| `1a292ba3cf82fe7da2fbe8cf3445077dcabcf604` | 2025-12-09 | update agora beauty |

---

## 🔍 主要功能变更

### 1. 镜像模式（Mirror Mode）功能 ⭐⭐⭐

**提交**: `9d9047e89c43a2a37a70961a24d9f77add5a4143`

#### 变更内容

**新增枚举类型**：
```kotlin
enum class MirrorMode {
    MIRROR_LOCAL_REMOTE,  // 本地和远程都镜像
    MIRROR_LOCAL_ONLY,   // 仅本地镜像
    MIRROR_REMOTE_ONLY,  // 仅远程镜像
    MIRROR_NONE          // 都不镜像
}

data class CameraConfig(
    val frontMirror: MirrorMode = MirrorMode.MIRROR_LOCAL_REMOTE,
    val backMirror: MirrorMode = MirrorMode.MIRROR_NONE
)
```

**新增方法**：
- `setupLocalVideo(view: View, renderMode: Int)`: 统一处理本地视频设置和镜像
- `onCaptureVideoFrame(videoFrame: VideoFrame)`: 处理视频帧并动态更新镜像状态
- `getMirrorApplied(): Boolean`: 获取当前镜像应用状态

**核心逻辑**：
- 根据前后摄像头和配置，动态计算 capture mirror 和 render mirror
- 贴纸启用时，根据前后摄像头自动调整编码器镜像模式：
  - 前置摄像头：`MIRROR_MODE_ENABLED`
  - 后置摄像头：`MIRROR_MODE_AUTO`

**涉及文件**：
- `AgoraBeautySDK.kt`: 新增镜像相关方法和配置
- `BeautyManager.kt`: 更新 `setupLocalVideo()` 和 `onCaptureVideoFrame()` 调用

#### iOS 实现建议

**需要实现的功能**：
1. 在 `AgoraBeautyManager.swift` 中添加镜像模式枚举
2. 实现 `setupLocalVideo()` 方法，处理本地视频视图和镜像设置
3. 实现视频帧回调处理镜像逻辑（使用 `AgoraVideoFrameDelegate`）
4. 在贴纸功能中根据摄像头方向调整镜像设置

**关键代码位置**：
- `iOS/AgoraEntScenarios/Scenes/Show/Beauty/AgoraBeautyManager.swift`
- `iOS/AgoraEntScenarios/Scenes/Show/Beauty/BeautyAPI/AgoraRender/AgoraBeautyRender.m`

---

### 2. 美颜模型类型选择功能 ⭐⭐⭐

**提交**: `dc4a98a4e8d7cce03f179fc4a88df1af32a01b75`  
**补充提交**: `982bc948422356f5c1abd138dc83fff89402aea7`

#### 变更内容

**新增常量**：
```kotlin
const val MODEL_TYPE_ADAPTIVE = 0  // 自适应（默认）
const val MODEL_TYPE_LARGE = 1     // 大模型
const val MODEL_TYPE_SMALL = 2    // 小模型
```

**新增方法**：
- `getCurrentModelType(): Int`: 从 SharedPreferences 读取当前模型类型
- `saveModelType(modelType: Int)`: 保存模型类型到 SharedPreferences
- `setModelTypeParameter(rtcEngine: RtcEngine, context: Context)`: 设置私有参数

**私有参数设置**：
```kotlin
// 大模型：强制所有设备使用大模型
rtcEngine.setParameters("{\"che.video.low_alg_score_4_beauty\":0}")

// 小模型：强制所有设备使用小模型
rtcEngine.setParameters("{\"che.video.low_alg_score_4_beauty\":100}")

// 自适应：不设置参数，SDK 自动根据设备性能选择
```

**UI 实现**：
- 在 `RoomListActivity` 中添加模型选择器按钮（`fabModelSelector`）
- 使用 `PopupMenu` 显示三个选项：自适应、大模型、小模型
- 切换模型类型时需要：
  1. 保存新的模型类型
  2. 销毁当前 RTC Engine (`RtcEngineInstance.destroy()`)
  3. 延迟 500ms 后重新初始化 (`initRtc()`)

**涉及文件**：
- `AgoraBeautySDK.kt`: 模型类型相关方法
- `RoomListActivity.kt`: UI 实现
- `show_room_list_activity.xml`: 布局文件
- `show_model_selector_menu.xml`: 菜单资源

#### iOS 实现建议

> ⚠️ **重要**：此功能是基础配置，**建议最先实现**，因为：
> - 模型类型需要在初始化美颜 SDK 时设置，影响后续所有美颜功能
> - 如果先实现其他功能，后续添加可能需要重构初始化流程
> - 这是性能优化的基础，应该在功能开发前就确定

**需要实现的功能**：
1. 在 `AgoraBeautyManager.swift` 中添加模型类型枚举和 UserDefaults 存储
2. 在初始化美颜 SDK 前调用 `setParameters()` 设置模型类型
3. 在房间列表页面添加模型选择器 UI（使用 ActionSheet 或 Alert）
4. 切换模型类型时需要重新初始化 RTC Engine

**关键代码位置**：
- `iOS/AgoraEntScenarios/Scenes/Show/Beauty/AgoraBeautyManager.swift`
- iOS 房间列表相关 ViewController

**注意事项**：
- iOS 的私有参数名称可能与 Android 相同，需要验证
- 切换模型类型需要重新初始化 RTC Engine，可能影响用户体验
- **建议与初始化流程优化一起实现**，确保初始化流程正确

---

### 3. 素颜对比功能（Bare Face Comparison） ⭐⭐⭐

**提交**: `7b5cd5ce49984283d5dfbd47206cd67af74d2bc7`  
**提交**: `0234423578beaaf87ce5bc41735d23772b991752`

#### 变更内容

**方法重命名**：
- `actionDown()` → `actionBareFace()`
- `actionUp()` → `actionBeauty()`

**实现逻辑**：
```kotlin
// 保存当前状态并禁用美颜
fun actionBareFace() {
    savedBeautyState = beautyConfig.beauty
    savedFaceShapeState = beautyConfig.faceShape
    savedMakeupName = beautyConfig.makeupName
    savedFilterName = beautyConfig.filterName
    savedStickerName = beautyConfig.stickerName
    
    // 临时禁用所有美颜效果
    beautyConfig.beauty = false
    beautyConfig.faceShape = false
    beautyConfig.makeupName = null
    beautyConfig.filterName = null
    beautyConfig.stickerName = null
}

// 恢复之前保存的状态
fun actionBeauty() {
    beautyConfig.beauty = savedBeautyState
    beautyConfig.faceShape = savedFaceShapeState
    beautyConfig.makeupName = savedMakeupName
    beautyConfig.filterName = savedFilterName
    beautyConfig.stickerName = savedStickerName
}
```

**UI 实现**：
- 在 `MultiBeautyDialog` 中添加素颜对比按钮（`ivSuyan`）
- 使用 `OnTouchListener` 实现按下/释放逻辑：
  - `ACTION_DOWN`: 调用 `actionBareFace()`
  - `ACTION_UP` / `ACTION_CANCEL`: 调用 `actionBeauty()`
- 仅在 Agora 美颜类型时显示该按钮

**多 SDK 支持**：
- `AgoraBeautySDK`: 保存 beauty、faceShape、makeup、filter、sticker
- `FaceUnityBeautySDK`: 保存 beauty、makeup、sticker
- `SenseTimeBeautySDK`: 保存所有美颜参数（20+ 个参数）

**涉及文件**：
- `AgoraBeautySDK.kt`: 实现素颜对比方法
- `FaceUnityBeautySDK.kt`: 实现素颜对比方法
- `SenseTimeBeautySDK.kt`: 实现素颜对比方法（保存所有参数）
- `MultiBeautyDialog.kt`: UI 实现
- `BaseControllerView.kt`: 添加 `beautyCompareListener`

#### iOS 实现建议

**需要实现的功能**：
1. 在 `AgoraBeautyManager.swift` 中添加 `actionBareFace()` 和 `actionBeauty()` 方法
2. 保存当前美颜配置状态（beauty、faceShape、makeup、filter、sticker 等）
3. 在美颜 UI 中添加长按按钮，按下时调用 `actionBareFace()`，释放时调用 `actionBeauty()`
4. 同样需要在 `FUBeautyManager` 和 `SenseBeautyManager` 中实现相同功能

**关键代码位置**：
- `iOS/AgoraEntScenarios/Scenes/Show/Beauty/AgoraBeautyManager.swift`
- `iOS/AgoraEntScenarios/Scenes/Show/Beauty/FUBeautyManager.swift`
- `iOS/AgoraEntScenarios/Scenes/Show/Beauty/SenseBeautyManager.swift`
- iOS 美颜 UI 相关 ViewController

**注意事项**：
- SenseTime 需要保存的参数较多，需要仔细处理
- UI 交互方式可以使用 `UILongPressGestureRecognizer` 或按钮的 `touchDown`/`touchUp` 事件

---

### 4. 美颜参数范围调整 ⭐⭐

**提交**: `0234423578beaaf87ce5bc41735d23772b991752`

#### 变更内容

**参数范围调整**（从 `-100..100` 调整为 `-50..50`）：

**美颜参数**：
- 清晰度（clarity）
- 长脸（faceLength）
- 下巴长度（chinLength）
- 眼位（eyePosition）
- 眼距（eyeDistance）
- 内眼角（eyeInnercorner）
- 外眼角（eyeOutercorner）
- 长鼻（noseLength）
- 鼻综合（noseGeneral）
- 嘴型（mouthSize）
- 眉位（eyebrowPosition）
- 眉粗细（eyebrowThickness）

**调整参数**：
- 色调（hue）
- 色温（temperature）
- 饱和度（saturation）
- 亮度（brightness）

**实现方式**：
```kotlin
// UI 显示范围：-50..50
valueRange = -50f..50f

// 实际值转换：UI 值 * 2 = SDK 值
beautyConfig.faceLength = value.toInt() * 2  // UI: -50..50, SDK: -100..100
beautyConfig.clarity = value / 50  // UI: -50..50, SDK: -1.0..1.0
```

**涉及文件**：
- `AgoraControllerView.kt`: 所有美颜参数的范围和转换逻辑

#### iOS 实现建议

**需要实现的功能**：
1. 在美颜 UI 的滑块控件中调整取值范围为 `-50..50`
2. 确保参数映射逻辑与 Android 一致（UI 显示 -50..50，实际传给 SDK 的值需要转换）

**关键代码位置**：
- iOS 美颜 UI 相关 ViewController 和 Slider 控件

**注意事项**：
- 需要确认 iOS SDK 的参数取值范围是否与 Android 一致
- 转换逻辑需要与 Android 保持一致，确保用户体验一致

---

### 5. 初始化流程优化和 Bug 修复 ⭐⭐⭐

**提交**: `c14150c066eb725393b7c0f0839e1eb82be41576`  
**提交**: `95e498e5d30dc11522091076c7badf0ae1790759`

#### 变更内容

**新增方法**：
```kotlin
fun isInitialized(): Boolean {
    return beautyEffect != null
}
```

**优化 `unInitBeautySDK()`**：
- 添加异常处理（try-catch）
- 确保先销毁 `beautyEffect`，再禁用 extension
- 添加 finally 块确保资源清理
- 重置所有标志位和配置

**修复首次安装问题**：
- **问题**：首次安装时，设置美颜参数时调用 `addOrUpdateVideoEffect` 会覆盖美颜效果
- **解决**：移除参数设置时的 `addOrUpdateVideoEffect` 调用，只设置参数值

**代码变更**：
```kotlin
// 修改前
if (value) {
    effect.addOrUpdateVideoEffect(
        IVideoEffectObject.VIDEO_EFFECT_NODE_ID.BEAUTY.value,
        beautyName
    )
}
beautyEffect?.setVideoEffectBoolParam("beauty_effect_option", "enable", value)

// 修改后
// Just set the parameter, don't call addOrUpdateVideoEffect to avoid overriding beauty effect
beautyEffect?.setVideoEffectBoolParam("beauty_effect_option", "enable", value)
```

**权限检查**：
- 在 `BaseViewBindingActivity` 中添加 `hasMicPerm()` 和 `hasCameraPerm()` 方法
- 在 `LivePrepareActivity.onResume()` 中添加摄像头权限检查

**涉及文件**：
- `AgoraBeautySDK.kt`: 初始化流程优化
- `BeautyManager.kt`: 初始化检查
- `BaseViewBindingActivity.java`: 权限检查方法
- `LivePrepareActivity.kt`: 权限检查调用

#### iOS 实现建议

**需要实现的功能**：
1. 在 `AgoraBeautyManager` 中添加初始化状态检查方法
2. 优化销毁方法，确保资源正确释放
3. 检查并修复首次安装时的初始化时序问题
4. 在启动预览前检查摄像头权限

**关键代码位置**：
- `iOS/AgoraEntScenarios/Scenes/Show/Beauty/AgoraBeautyManager.swift`
- iOS 预览相关 ViewController

**注意事项**：
- iOS 需要在 `Info.plist` 中声明摄像头权限（`NSCameraUsageDescription`）
- 使用 `AVCaptureDevice.authorizationStatus(for: .video)` 检查权限状态

---

### 6. FaceUnity 美颜优化 ⭐

**提交**: `bb3684e23c96c40c3de923c00202f0956f70e3fa`  
**提交**: `0a2db8acfa5aaf201ebdc4d0326edd4453011070`

#### 变更内容

**自动启用美颜开关**：
- 在设置各个美颜参数时，自动设置 `beauty = true`
- 确保用户调整参数时美颜功能自动启用

**代码示例**：
```kotlin
var smooth = 0.5f
    set(value) {
        field = value
        runOnBeautyThread {
            beauty = true  // 自动启用美颜
            faceBeauty.blurIntensity = value * 6.0
        }
    }
```

**涉及文件**：
- `FaceUnityBeautySDK.kt`: 所有美颜参数的 setter 方法

#### iOS 实现建议

**需要实现的功能**：
- 在 `FUBeautyManager` 中，设置美颜参数时自动启用美颜开关

**关键代码位置**：
- `iOS/AgoraEntScenarios/Scenes/Show/Beauty/FUBeautyManager.swift`

---

## 📊 实现优先级建议

> ⚠️ **重要提示**：建议按照以下顺序实现，因为模型类型选择是基础配置，需要在初始化时设置，影响后续所有美颜功能。

### 🔴 高优先级（必须实现）

1. **美颜模型类型选择** ⭐ **建议最先实现**
   - **基础配置功能**，需要在初始化时设置
   - 影响后续所有美颜功能的性能和行为
   - 实现复杂度中等
   - 如果先实现其他功能，后续添加可能需要重构
   - 需要重新初始化 RTC Engine

2. **初始化流程优化和 Bug 修复**
   - 稳定性要求
   - 修复关键 Bug
   - 影响应用稳定性
   - 建议与模型类型选择一起实现，确保初始化流程正确

3. **素颜对比功能**
   - 用户体验核心功能
   - 实现相对简单
   - 影响用户满意度

4. **美颜参数范围调整**
   - UI 一致性要求
   - 实现简单
   - 影响用户体验

### 🟡 中优先级（建议实现）

5. **镜像模式功能**
   - 功能相对独立
   - 实现复杂度较高
   - 需要视频帧回调处理
   - 如果 iOS 已有类似实现可延后

### 🟢 低优先级（可选实现）

6. **FaceUnity 美颜优化**
   - 仅影响 FaceUnity SDK
   - 实现简单
   - 可后续优化

---

## ⚠️ 注意事项

### 1. 模型类型切换
- Android 实现中切换模型类型需要销毁并重新创建 RTC Engine
- iOS 也需要类似处理，可能影响用户体验
- 建议添加加载提示，告知用户正在切换模型

### 2. 参数映射
- Android 和 iOS SDK 的参数名称和取值范围可能不同
- 需要仔细对照 Agora SDK 文档
- 确保参数转换逻辑正确

### 3. 状态保存
- 素颜对比功能需要保存完整的美颜状态
- 包括所有子功能：美颜、塑形、妆容、滤镜、贴纸
- SenseTime SDK 需要保存的参数较多（20+ 个）

### 4. 权限检查
- iOS 需要在 `Info.plist` 中声明摄像头权限
- 使用 `AVCaptureDevice.authorizationStatus(for: .video)` 检查权限
- 在启动预览前必须检查权限，避免崩溃

### 5. 初始化时序
- 首次安装时可能存在初始化时序问题
- 确保在正确的时机调用初始化方法
- 避免重复初始化导致的问题

### 6. 资源清理
- 确保在销毁时正确释放所有资源
- 使用 try-catch 处理异常情况
- 在 finally 块中确保资源清理

---

## 📝 相关文件清单

### Android 文件
- `Android/scenes/show/src/main/java/io/agora/scene/show/beauty/AgoraBeautySDK.kt`
- `Android/scenes/show/src/main/java/io/agora/scene/show/beauty/BeautyManager.kt`
- `Android/scenes/show/src/main/java/io/agora/scene/show/beauty/FaceUnityBeautySDK.kt`
- `Android/scenes/show/src/main/java/io/agora/scene/show/beauty/SenseTimeBeautySDK.kt`
- `Android/scenes/show/src/main/java/io/agora/scene/show/widget/beauty/AgoraControllerView.kt`
- `Android/scenes/show/src/main/java/io/agora/scene/show/widget/beauty/MultiBeautyDialog.kt`
- `Android/scenes/show/src/main/java/io/agora/scene/show/RoomListActivity.kt`

### iOS 对应文件
- `iOS/AgoraEntScenarios/Scenes/Show/Beauty/AgoraBeautyManager.swift`
- `iOS/AgoraEntScenarios/Scenes/Show/Beauty/BeautyManager.swift`
- `iOS/AgoraEntScenarios/Scenes/Show/Beauty/FUBeautyManager.swift`
- `iOS/AgoraEntScenarios/Scenes/Show/Beauty/SenseBeautyManager.swift`
- `iOS/AgoraEntScenarios/Scenes/Show/Beauty/BeautyAPI/AgoraRender/AgoraBeautyRender.m`

---

## 🔗 参考资源

- [Agora RTC SDK iOS 文档](https://docs.agora.io/cn/video-call-4.x/product-overview?platform=iOS)
- [Agora 美颜扩展文档](https://docs.agora.io/cn/video-call-4.x/video-beauty-effect-ios?platform=iOS)

---

**文档生成时间**: 2025-12-16  
**最后更新**: 2025-12-16

