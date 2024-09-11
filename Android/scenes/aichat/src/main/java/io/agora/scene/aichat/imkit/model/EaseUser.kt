package io.agora.scene.aichat.imkit.model

import android.text.TextUtils
import io.agora.scene.aichat.imkit.ChatClient
import io.agora.scene.aichat.imkit.ChatMessage
import io.agora.scene.aichat.imkit.EaseIM
import io.agora.scene.aichat.imkit.extensions.getUserInfo
import io.agora.scene.aichat.imkit.provider.getSyncUser
import org.json.JSONObject
import java.io.Serializable

/**
 * User bean for easeUI
 * @param userId The user id assigned from app, which should be unique in the application
 * @param nickname The user's nickname, default is userId
 * @param initialLetter The initial letter of user's nickname or userId, it will be used in contact list
 * @param avatar The user's avatar
 * @param contact 0: normal, 1: black ,3: no friend
 * @param email The user's email
 * @param phone The user's phone
 * @param gender The user's gender
 * @param sign The user's signature
 * @param birth The user's birth
 * @param ext The user's ext
 * @param remark The user's remark
 */
data class EaseUser @JvmOverloads constructor(
    val userId: String,
    var nickname: String? = userId,
    var initialLetter: String? = null,
    var avatar: String? = null,
    var contact: Int = 0,
    var email: String? = null,
    var phone: String? = null,
    var gender: Int = 0,
    var sign: String? = null,
    var birth: String? = null,
    var ext: String? = null,
    var remark: String? = null
) : Serializable {
    /**
     * the timestamp when set initialLetter
     */
    var modifyInitialLetterTimestamp: Long = 0


    /**
     * the timestamp when last modify
     */
    val lastModifyTimestamp: Long = 0

    fun getRemarkOrName(): String? {
        return if (remark.isNullOrEmpty()) getNickname() else remark
    }

    override fun toString(): String {
        return "userId: $userId \n nickname: $nickname \n avatar: $avatar \n remark: $remark"
    }

    fun getPrompt(): String {
        var prompt = ""
        try {
            ext?.let {
                val js = JSONObject(it)
                prompt = js.optString("prompt")
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return prompt
    }
}

/**
 * Get the user's nickname, if nickname is null or empty, return userId
 */
fun EaseUser.getNickname(): String? {
    return if (nickname.isNullOrEmpty()) userId else nickname
}

//fun EaseUser.getUserInitialLetter():String?{
//    return if (initialLetter.isNullOrEmpty() || lastModifyTimestamp > modifyInitialLetterTimestamp) {
//        if (!TextUtils.isEmpty(nickname)) {
//            nickname?.let { it.getInitialLetter() }
//        } else userId.getInitialLetter()
//    } else initialLetter
//}

internal fun EaseUser.setUserInitialLetter(initialLetter: String?) {
    this.initialLetter = initialLetter
    modifyInitialLetterTimestamp = System.currentTimeMillis()
}

fun EaseUser.setUserInitialLetter() {
    var letter = "#"
    val nickname = getRemarkOrName()

    if (nickname.isNullOrEmpty()) {
        setUserInitialLetter(letter)
        return
    }
//    nickname?.let {
//        setUserInitialLetter(it.getUpperFirstWord())
//    } ?: kotlin.run {
//        setUserInitialLetter(letter)
//    }
}

/**
 * Check whether the user is current user.
 */
fun EaseUser.isCurrentUser(): Boolean {
    return userId == ChatClient.getInstance().currentUser
}

/**
 * Get the user info from message.
 */
fun EaseUser.getMessageUser(message: ChatMessage): EaseUser {
    // get user info from message cache
    var profile = message.getUserInfo()
    if (profile != null && TextUtils.equals(profile.id, userId)) return profile.toUser()
    // get user info from contact provider
    profile = EaseIM.getUserProvider()?.getSyncUser(userId)
    if (profile != null) return profile.toUser()
    return this
}

/**
 * Convert [EaseUser] to [EaseProfile].
 */
fun EaseUser.toProfile(): EaseProfile {
    val easeProfile =
        EaseProfile(id = userId, name = nickname, avatar = avatar, remark = remark, sign = sign, prompt = getPrompt())
    return easeProfile
}