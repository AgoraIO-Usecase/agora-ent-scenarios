package io.agora.scene.playzone.service.api

import com.google.gson.annotations.SerializedName

data class SubGameRequestModel constructor(
    val platform: Int, // 客户端平台 (默认值为1) 1:iOS 2:Android 3:Web
    val unity_engine_version: String? = null, // unity游戏引擎版本 (默认2020.3.25f1c1)
)

data class SubCommonResp<Data> constructor(
    val ret_code: Int = 0,
    val data: Data?,
    val ret_msg: String?,
)

data class SubGameListRequestModel constructor(
    val app_id: String,
    val app_secret: String? = null,
)

data class SubGameApiInfo(
    val api: GameApi,
    val bullet_api: BulletApi,
    val cross_app_api: CrossAppApi,
    val match_api: MatchApi,
    val web3_api: Web3Api
)

data class GameApi(
    val auth_app_list: String,
    val auth_room_list: String,
    val get_game_report_info: String,
    val get_game_report_info_page: String,
    val get_mg_info: String,
    val get_mg_list: String,
    val push_event: String,
    val query_game_report_info: String,
    val report_game_round_bill: String
)

data class BulletApi(
    val command: String,
    @SerializedName("init")
    val bulletInit: String,
    val refresh: String
)

data class CrossAppApi(
    val auth_app_list: String,
    val auth_room_list: String
)

data class MatchApi(
    val cancel_match: String,
    val create_match: String,
    val query_game_config: String,
    val query_user_matching: String
)

data class Web3Api(
    val get_nft_details: String,
    val refresh_details: String
)

data class SubGameResp constructor(
    val mg_info_list: List<SubGameInfo>?,
)

data class SubGameInfo constructor(
    val big_loading_pic: SubGamePic,
    val desc: SubGameModel,
    val game_mode_list: List<GameMode>,
    val mg_id: String,
    val name: SubGameModel,
    val thumbnail128x128: SubGamePic,
    val thumbnail192x192: SubGamePic,
    val thumbnail332x332: SubGamePic,
    val thumbnail80x80: SubGamePic
)

data class SubGamePic constructor(
    val default: String,
    @SerializedName("zh-CN")
    val zh_CN: String,
    @SerializedName("zh-HK")
    val zh_HK: String
)

data class SubGameModel(
    val default: String,
    @SerializedName("en-GB")
    val en_GB: String,
    @SerializedName("en-US")
    val en_US: String,
    @SerializedName("ms-BN")
    val ms_BN: String,
    @SerializedName("ms-MY")
    val ms_MY: String,
    @SerializedName("zh-CN")
    val zh_CN: String,
    @SerializedName("zh-HK")
    val zh_HK: String,
    @SerializedName("zh-MO")
    val zh_MO: String,
    @SerializedName("zh—SG")
    val zh_SG: String,
    @SerializedName("zh-TW")
    val zh_TW: String
)

data class GameMode(
    val count: List<Int>,
    val mode: Int,
    val rule: String,
    val team_count: List<Int>,
    val team_member_count: List<Int>
)