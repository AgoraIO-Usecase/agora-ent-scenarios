package io.agora.scene.playzone.service.api

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class PlayZoneCommonResp<Data> constructor(
    val code: Int = 0,
    val data: Data?,
    val msg: String?,
    val tips: String?
)

data class PlayZoneGameListModel constructor(
    val carousel: List<PlayZoneGameBanner>?,
)

data class PlayZoneGameBanner constructor(
    @Expose
    @SerializedName("index")
    var index: Int? = null,
    @Expose
    @SerializedName("url")
    var url: String? = null,
)

data class PlayZoneGameModel constructor(
    val vendorId:String, // 厂商id
    val gameType: String, // 游戏类型
    val gameId: Int,
    val name: String?,
    val icon:String?
)

