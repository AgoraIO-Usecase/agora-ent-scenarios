package io.agora.rtmsyncmanager.model

import java.io.Serializable

data class AUIChatEntity (

    /**
     * chatId : string
     * user : AUIUserThumbnailInfo
     * content: String
     * joined : Boolean
     */
    var user: AUIUserThumbnailInfo?,
    var content: String?,
    var joined: Boolean

): Serializable