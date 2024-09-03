package io.agora.scene.aichat.imkit.model



/**
 * The class is used to display the group information.
 * @param id The group id.
 * @param name The group name.
 * @param avatar The group avatarUrl.
 */
class EaseGroupProfile(
    id: String,
    name: String? = null,
    avatar: String? = null
) : EaseProfile(id, name, avatar)