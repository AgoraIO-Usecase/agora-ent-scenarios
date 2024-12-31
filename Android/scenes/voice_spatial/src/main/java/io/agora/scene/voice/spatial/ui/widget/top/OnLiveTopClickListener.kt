package io.agora.scene.voice.spatial.ui.widget.top

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
    fun onClickRank(view: View)

    /**
     * Notice
     */
    fun onClickNotice(view: View)
    /**
     * Sound effect
     */
    fun onClickSoundSocial(view: View)
}