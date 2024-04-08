package io.agora.scene.show.photographer

interface AGExtensionHandler {
    fun onStart(provider: String, ext: String)

    fun onStop(provider: String, ext: String)

    fun onEvent(provider: String, ext: String, key: String, msg: String)

    fun onError(provider: String, ext: String, key: Int, msg: String)
}