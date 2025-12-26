# iOS 美颜模型类型选择功能实现对比

## 一、功能概述

实现美颜算法模型类型选择功能，允许用户在三种模式间切换：
- **自适应模式（Adaptive）**：SDK 根据设备性能自动选择模型
- **大模型（Large）**：强制所有设备使用大模型（高质量，高消耗）
- **小模型（Small）**：强制所有设备使用小模型（低质量，低消耗）

---

## 二、Android 实现逻辑

### 2.1 数据模型定义

**文件：** `AgoraBeautySDK.kt`

```kotlin
// 模型类型常量
const val MODEL_TYPE_ADAPTIVE = 0
const val MODEL_TYPE_LARGE = 1
const val MODEL_TYPE_SMALL = 2

// SharedPreferences key
private const val KEY_MODEL_TYPE = "show_model_type"
```

### 2.2 数据持久化

**文件：** `AgoraBeautySDK.kt`

```kotlin
// 获取当前模型类型
fun getCurrentModelType(): Int {
    return SPUtil.getInt(KEY_MODEL_TYPE, MODEL_TYPE_ADAPTIVE)
}

// 保存模型类型
fun saveModelType(modelType: Int) {
    SPUtil.putInt(KEY_MODEL_TYPE, modelType)
}
```

### 2.3 UI 初始化

**文件：** `RoomListActivity.kt`

```kotlin
private fun setupModelSelector() {
    updateModelSelectorText(getCurrentModelType())
    mBinding.fabModelSelector.setOnClickListener { view ->
        showModelSelectorMenu(view)
    }
}

private fun updateModelSelectorText(currentModelType: Int) {
    val text = when (currentModelType) {
        MODEL_TYPE_ADAPTIVE -> getString(R.string.show_model_selector_adaptive)
        MODEL_TYPE_LARGE -> getString(R.string.show_model_selector_large)
        MODEL_TYPE_SMALL -> getString(R.string.show_model_selector_small)
        else -> getString(R.string.show_model_selector_adaptive)
    }
    mBinding.fabModelSelector.text = text
}
```

### 2.4 菜单显示与选择

**文件：** `RoomListActivity.kt`

```kotlin
private fun showModelSelectorMenu(anchor: View) {
    val popupMenu = PopupMenu(this, anchor)
    popupMenu.menuInflater.inflate(R.menu.show_model_selector_menu, popupMenu.menu)
    
    // 设置当前选中项
    val currentModelType = getCurrentModelType()
    when (currentModelType) {
        MODEL_TYPE_ADAPTIVE -> popupMenu.menu.findItem(R.id.menu_model_adaptive).isChecked = true
        MODEL_TYPE_LARGE -> popupMenu.menu.findItem(R.id.menu_model_large).isChecked = true
        MODEL_TYPE_SMALL -> popupMenu.menu.findItem(R.id.menu_model_small).isChecked = true
    }

    popupMenu.setOnMenuItemClickListener { item ->
        when (item.itemId) {
            R.id.menu_model_adaptive -> {
                AgoraBeautySDK.saveModelType(MODEL_TYPE_ADAPTIVE)
                updateModelSelectorText(MODEL_TYPE_ADAPTIVE)
                RtcEngineInstance.destroy()
                mBinding.root.postDelayed({ initRtc() }, 500)
                true
            }
            R.id.menu_model_large -> {
                AgoraBeautySDK.saveModelType(MODEL_TYPE_LARGE)
                updateModelSelectorText(MODEL_TYPE_LARGE)
                RtcEngineInstance.destroy()
                mBinding.root.postDelayed({ initRtc() }, 500)
                true
            }
            R.id.menu_model_small -> {
                AgoraBeautySDK.saveModelType(MODEL_TYPE_SMALL)
                updateModelSelectorText(MODEL_TYPE_SMALL)
                RtcEngineInstance.destroy()
                mBinding.root.postDelayed({ initRtc() }, 500)
                true
            }
            else -> false
        }
    }
    popupMenu.show()
}
```

### 2.5 RTC Engine 重新初始化

**文件：** `RoomListActivity.kt`

```kotlin
private fun initRtc() {
    CoroutineScope(Dispatchers.Main).launch {
        val rtcEngine = withContext(Dispatchers.IO) {
            RtcEngineInstance.rtcEngine  // Lazy getter，会重新创建
        }
        setModelTypeParameter(rtcEngine)
        val handler = object : OnRoomListScrollEventHandler(rtcEngine, UserManager.getInstance().user.id.toInt()) {}
        mBinding.rvRooms.addOnScrollListener(handler)
        onRoomListScrollEventHandler = handler

        // Set video best practices for audience on device
        initVideoSettings()
    }
}
```

### 2.6 设置模型类型参数

**文件：** `RoomListActivity.kt`

```kotlin
private fun setModelTypeParameter(rtcEngine: RtcEngine) {
    val modelType = getCurrentModelType()
    when (modelType) {
        MODEL_TYPE_LARGE -> {
            // 所有设备使用大模型
            val ret = rtcEngine.setParameters("{\"che.video.low_alg_score_4_beauty\":0}")
            ShowLogger.d("Alien", "Set model type to LARGE, result: $ret")
        }

        MODEL_TYPE_SMALL -> {
            // 所有设备使用小模型
            val ret = rtcEngine.setParameters("{\"che.video.low_alg_score_4_beauty\":100}")
            ShowLogger.d("Alien", "Set model type to SMALL, result: $ret")
        }

        MODEL_TYPE_ADAPTIVE -> {
            // 自适应模式：不设置参数，SDK 根据设备分数自动选择
            ShowLogger.d("Alien", "Model type is ADAPTIVE, using default behavior")
        }
    }
}
```

### 2.7 Android 完整流程

```
用户点击按钮
    ↓
showModelSelectorMenu()
    ↓
显示 PopupMenu（标记当前选中项）
    ↓
用户选择菜单项
    ↓
setOnMenuItemClickListener
    ↓
1. AgoraBeautySDK.saveModelType() - 保存模型类型
2. updateModelSelectorText() - 更新按钮文本
3. RtcEngineInstance.destroy() - 销毁 RTC Engine
4. postDelayed(500ms) - 延迟 500ms
    ↓
initRtc()
    ↓
1. RtcEngineInstance.rtcEngine - 重新创建 Engine（lazy getter）
2. setModelTypeParameter() - 设置模型类型参数
3. 设置 scroll handler
4. initVideoSettings() - 初始化视频设置
```

---

## 三、iOS 实现逻辑

### 3.1 数据模型定义

**文件：** `AgoraBeautyManager.swift`

```swift
enum AgoraBeautyModelType: Int {
    case adaptive = 0  // 自适应模式：SDK 根据设备分数自动选择模型
    case large = 1     // 大模型：强制所有设备使用大模型
    case small = 2     // 小模型：强制所有设备使用小模型
}

// UserDefaults key
private static let kModelTypeKey = "show_model_type"
```

### 3.2 数据持久化

**文件：** `AgoraBeautyManager.swift`

```swift
/// 获取当前模型类型
static func getCurrentModelType() -> AgoraBeautyModelType {
    let rawValue = UserDefaults.standard.integer(forKey: kModelTypeKey)
    return AgoraBeautyModelType(rawValue: rawValue) ?? .adaptive
}

/// 保存模型类型
static func saveModelType(_ modelType: AgoraBeautyModelType) {
    UserDefaults.standard.set(modelType.rawValue, forKey: kModelTypeKey)
    UserDefaults.standard.synchronize()
}
```

### 3.3 UI 初始化

**文件：** `ShowRoomListVC.swift`

```swift
private lazy var modelSelectorButton: UIButton = {
    let button = UIButton(type: .custom)
    button.setTitle(getModelTypeDisplayText(), for: .normal)
    button.titleLabel?.font = UIFont.systemFont(ofSize: 12)
    button.setTitleColor(.white, for: .normal)
    button.backgroundColor = UIColor(red: 0, green: 0, blue: 0, alpha: 0.5)
    button.layer.cornerRadius = 4
    button.contentEdgeInsets = UIEdgeInsets(top: 8, left: 12, bottom: 8, right: 12)
    button.addTarget(self, action: #selector(didClickModelSelector), for: .touchUpInside)
    return button
}()

private func getModelTypeDisplayText() -> String {
    return getModelTypeText(AgoraBeautyManager.getCurrentModelType())
}

private func getModelTypeText(_ modelType: AgoraBeautyModelType) -> String {
    switch modelType {
    case .adaptive:
        return "自适应"
    case .large:
        return "大模型"
    case .small:
        return "小模型"
    }
}
```

### 3.4 菜单显示与选择

**文件：** `ShowRoomListVC.swift`

```swift
@objc private func didClickModelSelector() {
    let currentModelType = AgoraBeautyManager.getCurrentModelType()
    let alertController = UIAlertController(title: nil, message: nil, preferredStyle: .actionSheet)
    
    // 自适应选项
    let adaptiveAction = UIAlertAction(title: getModelTypeText(.adaptive), style: .default) { [weak self] _ in
        self?.switchModelType(.adaptive)
    }
    if currentModelType == .adaptive {
        adaptiveAction.setValue(true, forKey: "checked")
    }
    alertController.addAction(adaptiveAction)
    
    // 大模型选项
    let largeAction = UIAlertAction(title: getModelTypeText(.large), style: .default) { [weak self] _ in
        self?.switchModelType(.large)
    }
    if currentModelType == .large {
        largeAction.setValue(true, forKey: "checked")
    }
    alertController.addAction(largeAction)
    
    // 小模型选项
    let smallAction = UIAlertAction(title: getModelTypeText(.small), style: .default) { [weak self] _ in
        self?.switchModelType(.small)
    }
    if currentModelType == .small {
        smallAction.setValue(true, forKey: "checked")
    }
    alertController.addAction(smallAction)
    
    // 取消按钮
    alertController.addAction(UIAlertAction(title: "show_alert_cancel_btn_title".show_localized, style: .cancel))
    
    // iPad 支持
    if let popover = alertController.popoverPresentationController {
        popover.sourceView = modelSelectorButton
        popover.sourceRect = modelSelectorButton.bounds
    }
    
    present(alertController, animated: true)
}
```

### 3.5 切换模型类型

**文件：** `ShowRoomListVC.swift`

```swift
private func switchModelType(_ modelType: AgoraBeautyModelType) {
    // 1. 保存模型类型
    AgoraBeautyManager.saveModelType(modelType)
    
    // 2. 更新按钮文本
    modelSelectorButton.setTitle(getModelTypeDisplayText(), for: .normal)
    
    // 3. 销毁 RTC Engine
    ShowAgoraKitManager.shared.destoryEngine()
    
    // 4. 延迟 0.5s 后重新初始化
    DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) { [weak self] in
        ShowAgoraKitManager.shared.prepareEngine()
        // 检查设备设置（对应 Android 的 initVideoSettings()）
        self?.checkDevice()
    }
}
```

### 3.6 RTC Engine 初始化

**文件：** `ShowAgoraKitManager.swift`

```swift
func prepareEngine() {
    let engine = AgoraRtcEngineKit.sharedEngine(with: engineConfig(), delegate: nil)
    self.engine = engine
    
    // 在初始化美颜 SDK 之前设置模型类型参数
    AgoraBeautyManager.setModelTypeParameter(rtcEngine: engine)
    
    let loader = VideoLoaderApiImpl.shared
    loader.addListener(listener: self)
    let config = VideoLoaderConfig()
    config.rtcEngine = engine
    loader.setup(config: config)
    engine.setVideoScenario(.applicationLiveShowScenario)
    ShowLogger.info("load AgoraRtcEngineKit, sdk version: \(AgoraRtcEngineKit.getSdkVersion())", context: kShowLogBaseContext)
}
```

### 3.7 设置模型类型参数

**文件：** `AgoraBeautyManager.swift`

```swift
/// 设置模型类型私有参数
static func setModelTypeParameter(rtcEngine: AgoraRtcEngineKit) {
    let modelType = getCurrentModelType()
    let parameter: String
    
    switch modelType {
    case .large:
        // 所有设备使用大模型（score = 0）
        parameter = "{\"che.video.low_alg_score_4_beauty\":0}"
    case .small:
        // 所有设备使用小模型（score = 100）
        parameter = "{\"che.video.low_alg_score_4_beauty\":100}"
    case .adaptive:
        // 自适应模式：不设置参数，SDK 根据设备分数自动选择
        ShowLogger.info("Model type is ADAPTIVE, using default behavior", context: "AgoraBeautyManager")
        return
    }
    
    let ret = rtcEngine.setParameters(parameter)
    ShowLogger.info("Set model type to \(modelType), result: \(ret)", context: "AgoraBeautyManager")
}
```

### 3.8 设备检查（对应 Android 的 initVideoSettings）

**文件：** `ShowRoomListVC.swift`

```swift
private func checkDevice() {
    let score = ShowAgoraKitManager.shared.engine?.queryDeviceScore() ?? 0
    if (score < 85) {
        ShowAgoraKitManager.shared.deviceLevel = .low
    } else if (score < 90) {
        ShowAgoraKitManager.shared.deviceLevel = .medium
    } else {
        ShowAgoraKitManager.shared.deviceLevel = .high
    }
    ShowAgoraKitManager.shared.deviceScore = Int(score)
}
```

### 3.9 iOS 完整流程

```
用户点击按钮
    ↓
didClickModelSelector()
    ↓
显示 UIAlertController (ActionSheet)
    ↓
用户选择选项
    ↓
switchModelType()
    ↓
1. AgoraBeautyManager.saveModelType() - 保存模型类型
2. modelSelectorButton.setTitle() - 更新按钮文本
3. ShowAgoraKitManager.shared.destoryEngine() - 销毁 RTC Engine
4. DispatchQueue.main.asyncAfter(0.5s) - 延迟 0.5s
    ↓
prepareEngine()
    ↓
1. AgoraRtcEngineKit.sharedEngine() - 创建新的 Engine
2. AgoraBeautyManager.setModelTypeParameter() - 设置模型类型参数
3. 设置 VideoLoader
4. 设置视频场景
    ↓
checkDevice()
    ↓
根据设备分数设置设备等级
```

---

## 四、对比分析

### 4.1 相同点 ✅

| 功能点 | Android | iOS | 状态 |
|--------|---------|-----|------|
| 数据模型定义 | `MODEL_TYPE_*` 常量 | `AgoraBeautyModelType` 枚举 | ✅ 一致 |
| 数据持久化 | `SPUtil` (SharedPreferences) | `UserDefaults` | ✅ 一致 |
| 默认值 | `MODEL_TYPE_ADAPTIVE` (0) | `.adaptive` (0) | ✅ 一致 |
| 保存/获取方法 | `saveModelType()` / `getCurrentModelType()` | `saveModelType()` / `getCurrentModelType()` | ✅ 一致 |
| UI 初始化 | `setupModelSelector()` | `modelSelectorButton` lazy init | ✅ 一致 |
| 菜单显示 | `PopupMenu` | `UIAlertController` (ActionSheet) | ✅ 一致 |
| 标记当前选中项 | `menu.findItem().isChecked = true` | `action.setValue(true, forKey: "checked")` | ✅ 一致 |
| 切换流程 | 保存 → 更新UI → 销毁Engine → 延迟 → 重新初始化 | 保存 → 更新UI → 销毁Engine → 延迟 → 重新初始化 | ✅ 一致 |
| 延迟时间 | 500ms | 0.5s | ✅ 一致 |
| 参数设置 | `setParameters("{\"che.video.low_alg_score_4_beauty\":0/100}")` | `setParameters("{\"che.video.low_alg_score_4_beauty\":0/100}")` | ✅ 一致 |
| 自适应模式处理 | 不设置参数 | 不设置参数 | ✅ 一致 |
| 设备检查 | `initVideoSettings()` | `checkDevice()` | ✅ 一致 |
| 日志记录 | `ShowLogger.d()` | `ShowLogger.info()` | ✅ 一致 |

### 4.2 差异点 ⚠️

| 功能点 | Android | iOS | 说明 |
|--------|---------|-----|------|
| 存储键名 | `"show_model_type"` | `"show_model_type"` | ✅ 一致 |
| 参数设置位置 | `RoomListActivity.setModelTypeParameter()` | `AgoraBeautyManager.setModelTypeParameter()` | ⚠️ 位置不同，但逻辑一致 |
| 重新初始化方法 | `initRtc()` (在 Activity 中) | `prepareEngine()` (在 Manager 中) | ⚠️ 位置不同，但逻辑一致 |
| UI 组件 | `FloatingActionButton` (fabModelSelector) | `UIButton` (modelSelectorButton) | ⚠️ 平台差异，功能一致 |
| 菜单组件 | `PopupMenu` | `UIAlertController` | ⚠️ 平台差异，功能一致 |
| 异步处理 | `CoroutineScope` + `postDelayed` | `DispatchQueue.main.asyncAfter` | ⚠️ 平台差异，功能一致 |
| Scroll Handler | 在 `initRtc()` 中设置（需要 RTC Engine） | 在 `viewDidLoad()` 中设置（不依赖 RTC Engine） | ✅ **实现不同但逻辑正确** |

### 4.3 潜在遗漏点 ⚠️

#### 4.3.1 Scroll Handler 设置

**Android：**
```kotlin
private fun initRtc() {
    // ...
    val handler = object : OnRoomListScrollEventHandler(rtcEngine, UserManager.getInstance().user.id.toInt()) {}
    mBinding.rvRooms.addOnScrollListener(handler)
    onRoomListScrollEventHandler = handler
    // ...
}
```
- `OnRoomListScrollEventHandler` **需要 RTC Engine 作为参数**
- 所以必须在 `initRtc()` 中重新创建 handler

**iOS：**
```swift
private lazy var delegateHandler: ShowCollectionLoadingDelegateHandler = {
    let handler = ShowCollectionLoadingDelegateHandler(localUid: UInt(UserInfo.userId)!)
    return handler
}()

// 在 createViews() 中设置
collectionView.delegate = delegateHandler
```
- `ShowCollectionLoadingDelegateHandler` **不依赖 RTC Engine**，只需要 `localUid`
- Handler 在 `viewDidLoad()` 时通过 lazy 属性初始化，作为 `collectionView.delegate` 设置
- **不需要在 `prepareEngine()` 中重新设置**

**结论：** ✅ iOS 实现正确，因为 scroll handler 不依赖 RTC Engine，所以不需要在切换模型类型时重新设置。

#### 4.3.2 首次初始化时的参数设置

**Android：**
- 在 `onCreate()` 中调用 `initRtc()`
- `initRtc()` 中会调用 `setModelTypeParameter()`

**iOS：**
- 在 `viewDidLoad()` 中调用 `prepareEngine()`
- `prepareEngine()` 中会调用 `setModelTypeParameter()`

**状态：** ✅ 已实现，逻辑一致

---

## 五、总结

### 5.1 实现完整性

| 功能模块 | Android | iOS | 状态 |
|----------|---------|-----|------|
| 数据模型 | ✅ | ✅ | 完整 |
| 数据持久化 | ✅ | ✅ | 完整 |
| UI 初始化 | ✅ | ✅ | 完整 |
| 菜单显示 | ✅ | ✅ | 完整 |
| 切换逻辑 | ✅ | ✅ | 完整 |
| 参数设置 | ✅ | ✅ | 完整 |
| 设备检查 | ✅ | ✅ | 完整 |
| Scroll Handler | ✅ | ✅ | **实现不同但逻辑正确** |

### 5.2 关键逻辑验证

1. ✅ **参数设置时机**：都在 Engine 创建后、美颜 SDK 初始化前设置
2. ✅ **切换流程**：保存 → 更新UI → 销毁Engine → 延迟 → 重新初始化
3. ✅ **参数值**：大模型 = 0，小模型 = 100，自适应 = 不设置
4. ✅ **延迟时间**：都是 500ms/0.5s
5. ✅ **设备检查**：都在重新初始化后执行

### 5.3 待确认项

1. ✅ **Scroll Handler**：已确认 iOS 的 scroll handler 不依赖 RTC Engine，不需要重新设置
2. ⚠️ **日志上下文**：Android 使用 `"Alien"`，iOS 使用 `"AgoraBeautyManager"`，建议统一为 `"Alien"` 以保持一致性

---

## 六、建议

1. **确认 Scroll Handler**：检查 iOS 的 scroll handler 设置位置，确保切换模型类型后正常工作
2. **统一日志上下文**：建议 iOS 也使用 `"Alien"` 作为日志上下文，与 Android 保持一致
3. **添加单元测试**：验证模型类型切换的正确性
4. **性能监控**：监控切换模型类型时的性能影响

