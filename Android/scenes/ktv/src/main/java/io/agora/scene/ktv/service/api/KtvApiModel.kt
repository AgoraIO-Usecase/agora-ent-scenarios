package io.agora.scene.ktv.service.api

data class KtvCommonResp<Data>(
    val code: Int = 0,
    val data: Data?,
    val msg: String?,
    val tips: String?
)

data class KtvSongApiListModel constructor(
    val songs: List<KtvSongApiModel>
)

data class KtvSongApiModel constructor(
    val songCode: String,
    val name: String,
    val singer: String,
    val music: String,
    val lyric: String
)