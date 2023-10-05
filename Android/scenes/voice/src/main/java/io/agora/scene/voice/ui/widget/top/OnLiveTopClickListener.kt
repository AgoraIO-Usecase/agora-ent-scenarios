package io.agora.scene.voice.ui.widget.top

import android.view.View

interface OnLiveTopClickListener {

    /**
     * 返回
     */
    fun onClickBack(view: View)

    /**
     * 更多
     */
    fun onClickMore(view: View)

    /**
     * 排行榜
     */
    fun onClickRank(view: View,pageIndex:Int = 0)

    /**
     * 公告
     */
    fun onClickNotice(view: View)

    /**
     * 音效
     */
    fun onClickSoundSocial(view: View)

    /**
     * 背景音乐
     */
    fun onClickBGM(view: View)
    /** 背景音乐且原唱/伴唱
     */
    fun onClickBGMSinger(view: View)
}