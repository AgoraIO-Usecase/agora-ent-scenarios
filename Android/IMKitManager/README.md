### 场景接入
#### 1.初始化配置，创建IAUIIMManagerService 对象
```kotlin
val cxt = AgoraApplication.the().applicationContext
val chatRoomConfig = AUIChatCommonConfig(
    context = cxt,
    appId = PlayCenter.mAppId,
    owner = AUIChatUserInfo(
        userId = UserManager.getInstance().user.id.toString(),
        userName = UserManager.getInstance().user.name,
        userAvatar = UserManager.getInstance().user.headUrl,
    ),
    host = ServerConfig.toolBoxUrl,
    imAppKey = BuildConfig.IM_APP_KEY,
    chatLogCallback = object : ChatLogCallback {
        override fun onDebugInfo(tag: String, message: String) {
            PlayLogger.d(TAG, "$tag $message")
        }
        override fun onErrorInfo(tag: String, message: String) {
            PlayLogger.e(TAG, "$tag $message")
        }
    }
)
imManagerService = AUIIMManagerServiceImpl(chatRoomConfig)

```

#### 2. 登录IM, 推荐进入场景立马调用登录
```kotlin
mChatRoomService.imManagerService.loginChat { error ->
     // 处理登录返回
}
```

#### 3. 创建聊天室
```kotlin
mChatRoomService.imManagerService.createChatRoom(
    roomName = roomName,
    description = "welcome",
    completion = { chatId, error ->
         // 处理创建聊天室返回
    })
```

#### 4. 加入聊天室，进入场景房间后加入聊天室
```kotlin
val chatRoomId = mRoomInfo.customPayload[PlayZoneParameters.CHAT_ID] as? String ?: return
val chatRoomInfo = AUIChatRoomInfo(mRoomOwner, chatRoomId)
mChatRoomService.imManagerService.joinChatRoom(chatRoomInfo, completion = { error ->
    // 处理进入聊天室返回，比如插入console配置
    if (error == null) {
        insertLocalMessage(context().getString(R.string.play_zone_room_welcome), 0)
    }
})
```

#### 5.退出聊天室, 退出场景前离开聊天室
```kotlin
mChatRoomService.imManagerService.leaveChatRoom { }
```

#### 6.登出 IM, 离开场景前登出 IM
```kotlin
mChatRoomService.imManagerService.logoutChat { }
```

#### 7. xml 中添加 AUIChatListView 控件
```xml
<io.agora.imkitmanager.ui.impl.AUIChatListView
    android:id="@+id/chatListView"
    app:layout_constraintLeft_toLeftOf="parent"
    android:layout_width="match_parent"
    android:layout_height="220dp"
    android:layout_marginBottom="10dp"
    android:paddingBottom="8dp"
    style="@style/play_zone_chatListView_style"
    app:layout_constraintBottom_toTopOf="@+id/layoutBottom"/>
```

#### 8.imManagerService 绑定 IAUIChatListView 即 AUIChatListView，内部实现列表刷新
```kotlin
imManagerService.setChatListView(chatListView)
```

#### 9.如果不需要内部刷新列表，则不需要绑定 IAUIChatListView
```kotlin
// imManagerService绑定 AUIIMManagerRespObserver 消息回调
mChatRoomService.imManagerService.registerRespObserver(object : AUIIMManagerRespObserver {
    
    // 接收到消息
    override fun messageDidReceive(chatRoomId: String, message: IAUIIMManagerService.AgoraChatTextMessage) {
    }
    // 用户加入聊天室
    override fun onUserDidJoinRoom(chatRoomId: String, message: IAUIIMManagerService.AgoraChatTextMessage) {
    }
})
```
