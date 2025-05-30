package io.agora.scene.aichat.imkit

// Copy from io.agora.uikit.common
// It is a mapping file for the Chat SDK.
// manager
typealias ChatClient = io.agora.chat.ChatClient
typealias ChatManager = io.agora.chat.ChatManager
typealias ChatThreadManager = io.agora.chat.ChatThreadManager
typealias ChatGroupManager = io.agora.chat.GroupManager
typealias ChatUserInfoManager = io.agora.chat.UserInfoManager
typealias ChatPushManager = io.agora.chat.PushManager
typealias ChatPresenceManager = io.agora.chat.PresenceManager
typealias ChatOptions = io.agora.chat.ChatOptions
typealias ChatContactManager = io.agora.chat.ContactManager
typealias ChatroomManager = io.agora.chat.ChatRoomManager
typealias FileHelper = io.agora.util.FileHelper

typealias ChatHttpClientManager = io.agora.cloud.HttpClientManager
typealias ChatHttpClientManagerBuilder = io.agora.cloud.HttpClientManager.Builder
typealias ChatHttpResponse = io.agora.cloud.HttpResponse

// callback
typealias ChatCallback = io.agora.CallBack
typealias ChatValueCallback<T> = io.agora.ValueCallBack<T>
typealias ChatCursorResult<T> = io.agora.chat.CursorResult<T>
typealias ChatPageResult<T> = io.agora.chat.PageResult<T>

typealias ChatException = io.agora.exceptions.ChatException
typealias ChatError = io.agora.Error
typealias ChatLog = io.agora.util.EMLog

// Group
typealias ChatGroup = io.agora.chat.Group
typealias ChatGroupStyle = io.agora.chat.GroupManager.GroupStyle
typealias ChatGroupInfo = io.agora.chat.GroupInfo
typealias ChatGroupOptions = io.agora.chat.GroupOptions
typealias ChatShareFile = io.agora.chat.MucSharedFile
typealias ChatGroupReadAck = io.agora.chat.GroupReadAck


//Contact
//typealias ChatContact = io.agora.chat.Contact

// utils
typealias ChatImageUtils = io.agora.util.ImageUtils
typealias ChatPathUtils = io.agora.util.PathUtil
typealias ChatVersionUtils = io.agora.util.VersionUtils
typealias ChatDensityUtils = io.agora.util.DensityUtil
typealias ChatTimeInfo = io.agora.util.TimeInfo
typealias ChatTextFormater = io.agora.util.TextFormater

// java bean
typealias Chatroom = io.agora.chat.ChatRoom
typealias ChatUserInfo = io.agora.chat.UserInfo
//typealias ChatLoginExtensionInfo = io.agora.chat.EMLoginExtensionInfo
//typealias ChatRecallMessageInfo = io.agora.chat.EMRecallMessageInfo

// Chat
//typealias ChatFetchMessageOption = io.agora.chat.FetchMessageOption // 待转换
typealias ChatMessageReaction = io.agora.chat.MessageReaction
typealias ChatMessageReactionChange = io.agora.chat.MessageReactionChange
//typealias ChatMessageReactionOperation = io.agora.chat.MessageReactionOperation

// ChatMessage
typealias ChatConversation = io.agora.chat.Conversation
typealias ChatConversationType = io.agora.chat.Conversation.ConversationType
typealias ChatSearchDirection = io.agora.chat.Conversation.SearchDirection
//typealias ChatSearchScope = io.agora.chat.Conversation.ChatMessageSearchScope
typealias ChatMessage = io.agora.chat.ChatMessage
typealias ChatType = io.agora.chat.ChatMessage.ChatType
typealias ChatMessageType = io.agora.chat.ChatMessage.Type
typealias ChatTextMessageBody = io.agora.chat.TextMessageBody
typealias ChatCustomMessageBody = io.agora.chat.CustomMessageBody
//typealias ChatCombineMessageBody = io.agora.chat.CombineMessageBody
typealias ChatNormalFileMessageBody = io.agora.chat.NormalFileMessageBody
typealias ChatFileMessageBody = io.agora.chat.FileMessageBody
typealias ChatImageMessageBody = io.agora.chat.ImageMessageBody
typealias ChatLocationMessageBody = io.agora.chat.LocationMessageBody
typealias ChatVideoMessageBody = io.agora.chat.VideoMessageBody
typealias ChatVoiceMessageBody = io.agora.chat.VoiceMessageBody
typealias ChatCmdMessageBody = io.agora.chat.CmdMessageBody
typealias ChatMessageStatus = io.agora.chat.ChatMessage.Status
typealias ChatMessageDirection = io.agora.chat.ChatMessage.Direct
typealias ChatMessageBody = io.agora.chat.MessageBody
typealias ChatDownloadStatus = io.agora.chat.FileMessageBody.EMDownloadStatus

// presence
typealias ChatPresence = io.agora.chat.Presence

// thread
typealias ChatThread = io.agora.chat.ChatThread
typealias ChatThreadEvent = io.agora.chat.ChatThreadEvent

// pin
//typealias ChatMessagePinInfo = io.agora.chat.MessagePinInfo
//typealias ChatMessagePinOperation = io.agora.chat.MessagePinInfo.PinOperation

// push
typealias ChatPushHelper = io.agora.push.PushHelper
typealias ChatPushType = io.agora.push.PushType
typealias ChatPushListener = io.agora.push.PushListener
typealias PushConfig = io.agora.push.PushConfig
typealias PushConfigBuilder = io.agora.push.PushConfig.Builder
typealias ChatPushConfigs = io.agora.chat.PushConfigs
typealias ChatSilentModeParam = io.agora.chat.SilentModeParam
typealias ChatSilentModeResult = io.agora.chat.SilentModeResult
typealias ChatSilentModeTime = io.agora.chat.SilentModeTime
typealias ChatSilentModelType = io.agora.chat.SilentModeParam.SilentModeParamType
typealias ChatPushRemindType = io.agora.chat.PushManager.PushRemindType

// user info
typealias ChatUserInfoType = io.agora.chat.UserInfo.UserInfoType

// translation
typealias ChatTranslationInfo = io.agora.chat.TextMessageBody.TranslationInfo

// Listeners

// Listeners
typealias ChatConnectionListener = io.agora.ConnectionListener
typealias ChatMessageListener = io.agora.MessageListener
typealias ChatRoomChangeListener = io.agora.ChatRoomChangeListener
typealias ChatGroupChangeListener = io.agora.GroupChangeListener
typealias ChatMultiDeviceListener = io.agora.MultiDeviceListener
typealias ChatContactListener = io.agora.ContactListener
typealias ChatConversationListener = io.agora.ConversationListener
typealias ChatPresenceListener = io.agora.PresenceListener
typealias ChatThreadChangeListener = io.agora.ChatThreadChangeListener

interface ChatEventResultListener {
    fun onEventResult(function: String, errorCode: Int, errorMessage: String?)
}
