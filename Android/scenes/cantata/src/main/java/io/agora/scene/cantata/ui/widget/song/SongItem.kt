package io.agora.scene.cantata.ui.widget.song

class SongItem constructor(
    var songNo: String?,  // 歌曲的唯一标识
    var songName: String?,
    var imageUrl: String?,   // 歌曲封面
    var singer: String?,// 歌手名
    var chooser: String, // 是否已被点
    var isChosen: Boolean,
    var chooserId: String?
) {
    constructor(songNo: String?, songName: String?, imageUrl: String?, singer: String?, chooserId: String?) : this(
        songNo,
        songName,
        imageUrl,
        singer,
        "",
        false,
        chooserId
    )

    // 用于存放原始数据
    private var tag: Any? = null
    fun <T> setTag(tag: T) {
        this.tag = tag
    }

    fun <T> getTag(clazz: Class<T>): T? {
        return if (!clazz.isInstance(tag)) {
            null
        } else tag as T?
    }
}