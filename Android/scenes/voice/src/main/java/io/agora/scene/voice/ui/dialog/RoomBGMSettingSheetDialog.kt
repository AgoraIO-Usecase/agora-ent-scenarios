package io.agora.scene.voice.ui.dialog

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.agora.musiccontentcenter.Music
import io.agora.scene.voice.R
import io.agora.scene.voice.databinding.VoiceDialogChatroomBgmSettingBinding
import io.agora.scene.voice.rtckit.AgoraRtcEngineController
import io.agora.voice.common.ui.dialog.BaseSheetDialog

class RoomBGMSettingSheetDialog: BaseSheetDialog<VoiceDialogChatroomBgmSettingBinding>() {

    private var mOnBGMChanged: (() -> Unit)? = null

    private val adapter = MusicAdapter(mutableListOf())

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): VoiceDialogChatroomBgmSettingBinding {
        return VoiceDialogChatroomBgmSettingBinding.inflate(inflater, container, false)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = R.style.voice_BottomSheetDialogAnimation
        setupRecycleView()
        setupView()
        fetchData()
    }

    fun setOnBGMChanged(action: (() -> Unit)?) {
        mOnBGMChanged = action
    }

    private fun setupRecycleView() {
        val layout = LinearLayoutManager(context)
        layout.orientation = LinearLayoutManager.VERTICAL
        binding?.rvMusicList?.layoutManager = layout
        adapter.setOnClickItemAction { music ->
            setPlayOn(true)
            AgoraRtcEngineController.get().bgmManager.loadMusic(music)
            adapter.updateSelected(music)
            binding?.tvMusic?.text = music.name
            binding?.tvSinger?.text = music.singer
        }
        binding?.rvMusicList?.adapter = adapter
    }
    private fun setupView() {
        val bgmManager = AgoraRtcEngineController.get().bgmManager
        // 原唱伴唱
        if (bgmManager.params.isSingerOn) {
            binding?.ivSinging?.setImageResource(R.drawable.voice_icon_bgm_sing_on)
        } else {
            binding?.ivSinging?.setImageResource(R.drawable.voice_icon_bgm_sing_off)
        }
        binding?.ivSinging?.setOnClickListener {
            val toState = !bgmManager.params.isSingerOn
            bgmManager.setSingerOn(toState)
            if (toState) {
                binding?.ivSinging?.setImageResource(R.drawable.voice_icon_bgm_sing_on)
            } else {
                binding?.ivSinging?.setImageResource(R.drawable.voice_icon_bgm_sing_off)
            }
        }
        // 播放/暂停
        if (bgmManager.params.isAutoPlay) {
            binding?.ivPlay?.setImageResource(R.drawable.voice_icon_bgm_pause)
        } else {
            binding?.ivPlay?.setImageResource(R.drawable.voice_icon_bgm_play)
        }
        binding?.ivPlay?.setOnClickListener {
            val toState = !AgoraRtcEngineController.get().bgmManager.params.isAutoPlay
            setPlayOn(toState)
        }
        binding?.ivNext?.setOnClickListener {
            adapter.selectNext()
        }
        // 音量
        binding?.ivVolume?.setOnClickListener {
            val isSelected = binding?.ivVolume?.isSelected ?: true
            if (isSelected) {
                binding?.ivVolume?.isSelected = false
                binding?.slVolume?.visibility = View.INVISIBLE
                binding?.cvVolume?.visibility = View.INVISIBLE
            } else {
                binding?.ivVolume?.isSelected = true
                binding?.slVolume?.visibility = View.VISIBLE
                binding?.cvVolume?.visibility = View.VISIBLE
            }
        }
        binding?.slVolume?.max = 100
        binding?.slVolume?.progress = AgoraRtcEngineController.get().bgmManager.params.volume
        binding?.slVolume?.setOnSeekBarChangeListener(object: OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                AgoraRtcEngineController.get().bgmManager.setVolume(p1)
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {

            }
            override fun onStopTrackingTouch(p0: SeekBar?) {

            }
        })
    }
    private fun fetchData() {
        val bgmManager = AgoraRtcEngineController.get().bgmManager
        bgmManager.fetchBGMList { list ->
            binding?.rvMusicList?.post {
                adapter.updateDataSource(list?.toList() ?: listOf())
                binding?.tvDialogTitle?.text = "背景音乐(${list?.size ?: 0})"
                AgoraRtcEngineController.get().bgmManager.setAutoPlay(false)
                list?.firstOrNull()?.let { music ->
                    AgoraRtcEngineController.get().bgmManager.loadMusic(music)
                    adapter.updateSelected(music)
                    binding?.tvMusic?.text = music?.name ?: ""
                    binding?.tvSinger?.text = music?.singer ?: ""
                }
            }
        }
    }
    private fun setPlayOn(isOn: Boolean) {
        AgoraRtcEngineController.get().bgmManager.setAutoPlay(isOn)
        if (isOn) {
            binding?.ivPlay?.setImageResource(R.drawable.voice_icon_bgm_pause)
        } else {
            binding?.ivPlay?.setImageResource(R.drawable.voice_icon_bgm_play)
        }
    }
}

private class MusicAdapter (  // 数据源
    private var mData: List<Music>,
    private var mSelected: Music? = null
) : RecyclerView.Adapter<MusicAdapter.MusicViewHolder>() {

    private var mOnClickItemAction: ((music: Music) -> Unit)? = null
    fun updateDataSource(data: List<Music>) {
        mData = data
        notifyDataSetChanged()
    }
    fun updateSelected(selected: Music?) {
        mSelected = selected
        notifyDataSetChanged()
    }
    fun selectNext() {
        if (mData == null) {
            return
        }
        if (mSelected == null) {
            val music = mData.first()
            mOnClickItemAction?.invoke(music)
        }
        var nextIs = false
        for (music in mData) {
            if (nextIs) {
                mOnClickItemAction?.invoke(music)
                break
            }
            if (music.songCode == mSelected?.songCode) {
                nextIs = true
            }
        }
    }
    fun setOnClickItemAction(action: ((music: Music) -> Unit)?) {
        mOnClickItemAction = action
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        Log.d("list_view_log", "onCreateViewHolder")
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.voice_room_music_item_layout, parent, false)
        return MusicViewHolder(view)
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        Log.d("list_view_log", "onBindViewHolder")
        val music = mData[position]
        holder.tvTitle.text = music.name
        holder.tvSinger.text = music.singer
        if (music.songCode == mSelected?.songCode) {
            holder.ivSelected.visibility = View.VISIBLE
            holder.tvTitle.setTextColor(Color.rgb(10, 122, 255))
            holder.tvSinger.setTextColor(Color.rgb(10, 122, 255))
        } else {
            holder.tvTitle.setTextColor(Color.rgb(60, 66, 103))
            holder.tvSinger.setTextColor(Color.rgb(60, 66, 103))
            holder.ivSelected.visibility = View.INVISIBLE
        }
        holder.itemView.setOnClickListener {
            if (mSelected?.songCode != music.songCode) {
                mOnClickItemAction?.invoke(music)
            }
        }
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    // ViewHolder类
    internal class MusicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView
        var tvSinger: TextView
        var ivSelected: ImageView

        init {
            tvTitle = itemView.findViewById<TextView>(R.id.tvTitle)
            tvSinger = itemView.findViewById<TextView>(R.id.tvSinger)
            ivSelected = itemView.findViewById<ImageView>(R.id.ivSelected)
        }
    }
}
