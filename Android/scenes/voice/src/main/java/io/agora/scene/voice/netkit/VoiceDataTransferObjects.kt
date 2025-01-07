package io.agora.scene.voice.netkit

import androidx.annotation.IntDef
import com.google.gson.annotations.SerializedName
import java.util.UUID

// 0: Process user registration/return user token and create chatroom simultaneously
const val CHATROOM_CREATE_TYPE_USER_ROOM = 0

// 1: Only process user registration/return user token
const val CHATROOM_CREATE_TYPE_USER = 1

// 2: Only process chatroom creation
const val CHATROOM_CREATE_TYPE_ROOM = 2

@IntDef(
    CHATROOM_CREATE_TYPE_USER_ROOM,
    CHATROOM_CREATE_TYPE_USER,
    CHATROOM_CREATE_TYPE_ROOM
)
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class ChatRoomCreateType

data class CreateChatRoomInput constructor(
    var chatRoomId: String? = null,
    var chatRoomName: String? = null,
    var chatDescription: String? = null,
    var chatRoomOwner: String? = null,
    @ChatRoomCreateType var type: Int,
)

data class CreateChatRoomRequest constructor(
    val appId: String,
    @ChatRoomCreateType var type: Int = CHATROOM_CREATE_TYPE_USER_ROOM, // 0: Create user and room, 1: Create user only, 2: Create room only
    @SerializedName("im")
    val imConfig: ChatIMConfig?, // HuanXin account information
    @SerializedName("chat")
    var chatRoomConfig: ChatRoomConfig? = null, // HuanXin chatroom information
    @SerializedName("user")
    var chatUserConfig: ChatUserConfig? = null, // HuanXin user information
    val src: String = "Android", // Source/Requester
    var traceId: String = UUID.randomUUID().toString() // Request ID for full link tracking
)

data class ChatIMConfig constructor(
    var appKey: String? = null, // HuanXin appKey, used to convert to request URL
    var clientId: String? = null, // App's client_id
    var clientSecret: String? = null, // App's client_secret
)

data class ChatRoomConfig constructor(
    @SerializedName("id")
    var chatRoomId: String? = null, // Chatroom ID, this is the chatId field value returned by the API
    @SerializedName("name")
    var chatRoomName: String? = null, // Chatroom name
    @SerializedName("description")
    var description: String? = null, // Chatroom description
    @SerializedName("owner") // Chatroom administrator
    var roomOwner: String? = null,
)

data class ChatUserConfig constructor(
    // User ID, cannot exceed 64 bytes in length. Cannot be empty. Supports the following character sets:
    //
    //- 26 lowercase English letters a-z
    //- 26 uppercase English letters A-Z
    //- 10 numbers 0-9
    //- "_", "-", "."
    var username: String = "",
    // User's login password, cannot exceed 64 characters.
    var password: String = "12345678", // Default password
    // The user nickname displayed in the message push notification bar when pushing messages, not the nickname in user's personal information. Cannot exceed 100 characters. Supports the following character sets:
    //- 26 lowercase English letters a-z
    //- 26 uppercase English letters A-Z
    //- 10 numbers 0-9
    //- Chinese characters
    //- Special characters
    var nickname: String = "",
)

data class ChatCommonResp<Data>(
    val tip: String = "",
    val code: Int = 0,
    val msg: String?,
    val data: Data?
)

data class CreateChatRoomResponse(
    val appId: String?, // appId
    val chatId: String?, // HuanXin chatroom ID, returned by HuanXin API
    val chatToken: String?, // HuanXin chatroom user login token, valid for 24 hours
    val userName: String?, // HuanXin user ID, directly returns the request parameter user.username value
)