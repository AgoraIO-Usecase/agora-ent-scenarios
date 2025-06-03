package io.agora.scene.voice.ui.widget.top

import android.view.View

interface OnLiveTopClickListener {

    /**
     * Back
     */
    fun onClickBack(view: View)

    /**
     * More
     */
    fun onClickMore(view: View)

    /**
     * Ranking
     */
    fun onClickRank(view: View,pageIndex:Int = 0)

    /**
     * Notice
     */
    fun onClickNotice(view: View)

    /**
     * Sound Effect
     */
    fun onClickSoundSocial(view: View)
}