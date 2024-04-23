package io.agora.rtmsyncmanager.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class AgoraChatMessage(
    /**
     * chatId : string
     * messageId : string
     * mContent : string
     * user: AUiUserThumbnailInfo
     */
    @SerializedName("chatId") var chatId: String?,
    @SerializedName("messageId") var messageId: String?,
    @SerializedName("content") var content: String?,
    @SerializedName("user") var user: AUIUserThumbnailInfo?,

    ): Serializable
