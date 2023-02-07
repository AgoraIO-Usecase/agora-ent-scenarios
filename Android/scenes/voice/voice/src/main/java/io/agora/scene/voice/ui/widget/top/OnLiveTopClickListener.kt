package io.agora.scene.voice.ui.widget.top

import android.view.View

interface OnLiveTopClickListener {

    /**
     * 返回
     */
    fun onClickBack(view: View)

    /**
     * 排行榜
     */
    fun onClickRank(view: View)

    /**
     * 公告
     */
    fun onClickNotice(view: View)

    /**
     * 音效
     */
    fun onClickSoundSocial(view: View)

    /**
     * 成员数
     */
    fun onClickMemberCount(view:View)
}