package io.agora.imkitmanager.service.http

import androidx.annotation.IntDef
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.UUID

// 0: 同时处理用户注册/返回用户token和创建聊天室
const val CHATROOM_CREATE_TYPE_USER_ROOM = 0

// 1: 只处理用户注册/返回用户token
const val CHATROOM_CREATE_TYPE_USER = 1

// 2: 只处理创建聊天室
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
    @ChatRoomCreateType var type: Int = CHATROOM_CREATE_TYPE_USER_ROOM, // 0：创建用户和房间, 1：仅创建用户, 2：仅创建房间
    @SerializedName("im")
    val imConfig: ChatIMConfig?, // 环信账号信息
    @SerializedName("chat")
    var chatRoomConfig: ChatRoomConfig? = null, // 环信聊天室信息
    @SerializedName("user")
    var chatUserConfig: ChatUserConfig? = null, // 环信用户信息
    val src: String = "Android", // 来源/请求方
    var traceId: String = UUID.randomUUID().toString() // 请求ID, 用于全链路排查
)

data class ChatIMConfig constructor(
    var appKey: String? = null, // 环信appKey, 用于转换成请求地址
    var clientId: String? = null, // App 的 client_id
    var clientSecret: String? = null, // App 的 client_secret
)

data class ChatRoomConfig constructor(
    @SerializedName("id")
    var chatRoomId: String? = null, // 聊天室ID, 这里是接口返回的chatId字段值
    @SerializedName("name")
    var chatRoomName: String? = null, // 聊天室名称
    @SerializedName("description")
    var description: String? = null, // 聊天室描述
    @SerializedName("owner") // 聊天室的管理员
    var roomOwner: String? = null,
)

data class ChatUserConfig constructor(
    // 用户ID，长度不可超过 64 个字节长度。不可设置为空。支持以下字符集：
    //
    //- 26 个小写英文字母 a-z；
    //- 26 个大写英文字母 A-Z；
    //- 10 个数字 0-9；
    //- “_”, “-”, “.”。
    var username: String = "",
    // 用户的登录密码，长度不可超过 64 个字符。
    var password: String = "12345678", // 默认密码
    // 推送消息时，在消息推送通知栏内显示的用户昵称，并非用户个人信息的昵称。长度不可超过 100 个字符。支持以下字符集：
    //- 26 个小写英文字母 a-z；
    //- 26 个大写英文字母 A-Z；
    //- 10 个数字 0-9；
    //- 中文；
    //- 特殊字符。
    var nickname: String = "",
)

data class CreateChatRoomResponse(
    val appId: String?, // appId
    val chatId: String?, // 环信聊天室ID, 环信接口返回
    val chatToken: String?, // 环信聊天室用户登录Token, 有效期24小时
    val userName: String?, // 环信用户ID, 这里直接返回请求参数user.username值
)