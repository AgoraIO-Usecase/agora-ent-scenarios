package io.agora.scene.base

import io.agora.scene.base.utils.SPUtil

object ServerConfig {

    const val Env_Mode = "env_mode"

    var envRelease: Boolean = SPUtil.getBoolean(Env_Mode, true)
        set(newValue) {
            field = newValue
            SPUtil.putBoolean(Env_Mode, newValue)
        }

    val toolBoxUrl: String
        get() {
            return if (envRelease) {
                BuildConfig.TOOLBOX_SERVER_HOST
            } else {
                BuildConfig.TOOLBOX_SERVER_HOST_DEV
            }
        }

    val roomManagerUrl: String
        get() {
            return if (envRelease) {
                BuildConfig.TOOLBOX_SERVER_HOST.replace("toolbox", "room-manager")
            } else {
                BuildConfig.TOOLBOX_SERVER_HOST_DEV.replace("toolbox", "room-manager")
            }
        }
}