package io.agora.scene.aichat.imkit.extensions

import io.agora.scene.aichat.imkit.EaseIMCache
import io.agora.scene.aichat.imkit.model.EaseProfile
import io.agora.scene.aichat.imkit.model.isGroupAgent
import io.agora.scene.aichat.imkit.model.isPublicAgent
import io.agora.scene.aichat.imkit.model.isUserAgent

/**
 * 公开+用户创建的
 *
 * @return
 */
internal fun EaseIMCache.getAllAgent(): List<EaseProfile> {
    return getAllUsers()
}

/**
 * 公开智能体
 *
 * @return
 */
internal fun EaseIMCache.getPublicAgentList(): List<EaseProfile> {
    return getAllUsers().filter { it.isPublicAgent() }
}

/**
 * 用户创建的智能体
 *
 * @return
 */
internal fun EaseIMCache.getUserAgentList(): List<EaseProfile> {
    return getAllUsers().filter { it.isUserAgent() }
}

/**
 * 群聊
 *
 * @return
 */
internal fun EaseIMCache.getGroupAgentList(): List<EaseProfile> {
    return getAllUsers().filter { it.isGroupAgent() }
}