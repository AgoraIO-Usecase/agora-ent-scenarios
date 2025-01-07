package io.agora.scene.cantata.widget.song

/**
 * Song bean
 */
class SongItem constructor(
    var songNo: String?,  // Song unique identifier
    var songName: String?,
    var imageUrl: String?,   // Song cover
    var singer: String?,// Singer name
    var chooser: String, // Whether it has been chosen
    var isChosen: Boolean,
    var chooserId: String?,
    var loading :Boolean = false
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

    // Used to store original data
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