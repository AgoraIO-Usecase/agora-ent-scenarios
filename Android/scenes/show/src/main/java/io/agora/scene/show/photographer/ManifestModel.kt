package io.agora.scene.show.photographer

data class ManifestModel constructor(
    val url: String,
    val uri: String,
    val md5: String,
    val size: Int,
    val encrypt: Boolean,
    val autodownload: Boolean,
    val group: String,
    val desc: String,
)

data class ManifestFileModel constructor(
    val files:List<ManifestModel>,
    val customMsg:String,
    val timestamp:Int
)
