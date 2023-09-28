package io.agora.scene.cantata.ui.widget.song

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.agora.scene.base.GlideApp
import io.agora.scene.base.component.BaseViewBindingFragment
import io.agora.scene.base.manager.UserManager
import io.agora.scene.cantata.R
import io.agora.scene.cantata.databinding.CantataFragmentSongOrderListBinding
import io.agora.scene.cantata.databinding.CantataItemChoosedSongListBinding
import io.agora.scene.widget.basic.BindingSingleAdapter
import io.agora.scene.widget.basic.BindingViewHolder
import io.agora.scene.widget.utils.CenterCropRoundCornerTransform

/**
 * 已点歌单列表
 */
class SongChosenFragment : BaseViewBindingFragment<CantataFragmentSongOrderListBinding>() {
    private var controllable = false
    private var listener: Listener? = null
    private val mAdapter: BindingSingleAdapter<SongItem, CantataItemChoosedSongListBinding> =
        object : BindingSingleAdapter<SongItem, CantataItemChoosedSongListBinding>() {
            override fun onBindViewHolder(holder: BindingViewHolder<CantataItemChoosedSongListBinding>, position: Int) {
                val item = getItem(position) ?: return
                val binding = holder.binding
                binding.tvNo.text = (position + 1).toString()
                binding.tvMusicName.text = item.songName
                binding.tvChooser.text = item.chooser
                GlideApp.with(binding.ivCover).load(item.imageUrl)
                    .fallback(R.drawable.cantata_ic_song_default)
                    .error(R.drawable.cantata_ic_song_default)
                    .transform(CenterCropRoundCornerTransform(10))
                    .into(binding.ivCover)
                if (position == 0) {
                    binding.tvSing.visibility = View.VISIBLE
                    binding.ivSinging.visibility = View.VISIBLE
                    binding.ivToDel.visibility = View.GONE
                    binding.ivToTop.visibility = View.GONE
                } else if (position == 1 && controllable) {
                    binding.ivToDel.setOnClickListener { v: View? -> onSongDeleteClicked(item) }
                    binding.tvSing.visibility = View.GONE
                    binding.ivSinging.visibility = View.GONE
                    binding.ivToDel.visibility = View.VISIBLE
                    binding.ivToTop.visibility = View.GONE
                } else if (controllable) {
                    binding.ivToDel.setOnClickListener { v: View? -> onSongDeleteClicked(item) }
                    binding.ivToTop.setOnClickListener { v: View? -> onSongTopClicked(item) }
                    binding.tvSing.visibility = View.GONE
                    binding.ivSinging.visibility = View.GONE
                    binding.ivToDel.visibility = View.VISIBLE
                    binding.ivToTop.visibility = View.VISIBLE
                } else {
                    binding.tvSing.visibility = View.GONE
                    binding.ivSinging.visibility = View.GONE
                    binding.ivToTop.visibility = View.GONE
                    if (item.chooserId == UserManager.getInstance().user.id.toString()) {
                        binding.ivToDel.setOnClickListener { v: View? -> onSongDeleteClicked(item) }
                        binding.ivToDel.visibility = View.VISIBLE
                    } else {
                        binding.ivToDel.visibility = View.GONE
                    }
                }
                binding.tvChorus.text = getString(R.string.cantata_song_ordering_person)
            }
        }

    override fun getViewBinding(
        layoutInflater: LayoutInflater,
        viewGroup: ViewGroup?
    ): CantataFragmentSongOrderListBinding {
        return CantataFragmentSongOrderListBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    override fun initView() {
        binding.list.adapter = mAdapter
    }

    fun setControllable(controllable: Boolean) {
        this.controllable = controllable
    }

    fun resetSongList(songs: List<SongItem?>?) {
        mAdapter.resetAll(songs)
    }

    fun addSongItem(song: SongItem?) {
        mAdapter.insertLast(song)
    }

    fun deleteSongItem(song: SongItem) {
        for (i in 0 until mAdapter.itemCount) {
            val item = mAdapter.getItem(i)
            if (song.songNo == item!!.songNo) {
                mAdapter.remove(i)
                break
            }
        }
    }

    fun topUpSongItem(song: SongItem) {
        for (i in 0 until mAdapter.itemCount) {
            val item = mAdapter.getItem(i)
            if (song.songNo == item!!.songNo) {
                if (i > 1) {
                    mAdapter.remove(i)
                    mAdapter.insert(1, item)
                }
                break
            }
        }
    }

    val songSize: Int
        get() = mAdapter.itemCount

    fun setListener(listener: Listener) {
        this.listener = listener
    }

    private fun onSongDeleteClicked(song: SongItem) {
        listener?.onSongDeleteClicked(song)
    }

    private fun onSongTopClicked(song: SongItem) {
        listener?.onSongTopClicked(song)
    }

    interface Listener {
        fun onSongDeleteClicked(song: SongItem?)
        fun onSongTopClicked(song: SongItem?)
    }
}