package io.agora.scene.aichat.service.api

import androidx.annotation.IntDef

/**
 * Create user type
 *
 * @constructor Create empty Create user type
 */
@Retention(AnnotationRetention.SOURCE)
@IntDef(CreateUserType.User, CreateUserType.Agent, CreateUserType.Group)
annotation class CreateUserType {
    companion object {
        const val User = 0
        const val Agent = 1
        const val Group = 2
    }
}