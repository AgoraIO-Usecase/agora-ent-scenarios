package io.agora.scene.cantata.widget.song

import android.os.Bundle
import android.view.View
import android.widget.RadioGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.scwang.smart.refresh.layout.api.RefreshLayout
import io.agora.scene.base.component.BaseBottomSheetDialogFragment
import io.agora.scene.cantata.R
import io.agora.scene.cantata.databinding.CantataDialogChooseSongBinding
import io.agora.scene.cantata.live.listener.SongActionListenerImpl
import io.agora.scene.widget.utils.UiUtils

/**
 * Song menu
 */
class SongDialog : BaseBottomSheetDialogFragment<CantataDialogChooseSongBinding?>() {
    private var chooseSongListener: OnSongActionListener? = null
    private val songChosenFragment = SongChosenFragment()
    private val songChooseFragment = SongChooseFragment()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(requireDialog().window!!.decorView) { v: View?, insets: WindowInsetsCompat ->
            val inset = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            mBinding?.pager?.setPadding(0, 0, 0, inset.bottom)
            WindowInsetsCompat.CONSUMED
        }
        mBinding?.apply {
            rBtnChooseSong.isChecked = true
            pager.getChildAt(0)?.overScrollMode = View.OVER_SCROLL_NEVER
            songChooseFragment.listener = object : SongChooseFragment.Listener {
                override fun onClickSongItem(songItem: SongItem) {
                    if (UiUtils.isFastClick(500)) return
                    chooseSongListener?.onChooseSongChosen(this@SongDialog, songItem)
                }


                override fun onRefresh(refreshLayout: RefreshLayout) {
                    chooseSongListener?.onChooseSongRefreshing(this@SongDialog)
                }

                override fun onLoadMore(refreshLayout: RefreshLayout) {
                }
            }
            songChosenFragment.setListener(object : SongChosenFragment.Listener {
                override fun onSongDeleteClicked(song: SongItem?) {
                    chooseSongListener?.onChosenSongDeleteClicked(this@SongDialog, song!!)
                }

                override fun onSongTopClicked(song: SongItem?) {
                    chooseSongListener?.onChosenSongTopClicked(this@SongDialog, song!!)
                }
            })
            val fragments = arrayOf<Fragment>(songChooseFragment, songChosenFragment)
            pager.isSaveEnabled = false
            pager.adapter = object : FragmentStateAdapter(childFragmentManager, viewLifecycleOwner.lifecycle) {
                override fun getItemCount(): Int {
                    return fragments.size
                }

                override fun createFragment(position: Int): Fragment {
                    return fragments[position]
                }
            }
            pager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    if (position == 0) {
                        rBtnChooseSong.isChecked = true
                    } else {
                        rBtnChorus.isChecked = true
                    }
                }
            })
            setChosenSongCount(0)
        }

    }

    override fun onStart() {
        super.onStart()
        mBinding?.radioGroup?.setOnCheckedChangeListener { radioGroup: RadioGroup?, i: Int ->
            if (i == R.id.rBtnChooseSong) {
                mBinding?.pager?.currentItem = 0
            } else if (i == R.id.rBtnChorus) {
                mBinding?.pager?.currentItem = 1
            }
        }
    }

    /**
     * Set event listener
     */
    fun setChooseSongListener(chooseSongListener: SongActionListenerImpl) {
        this.chooseSongListener = chooseSongListener
    }


    /**
     * Song selection - Update item selected status
     */
    fun setChooseSongItemStatus(songItem: SongItem, isChosen: Boolean) {
        songChooseFragment.setSongItemStatus(songItem, isChosen)
    }


    /**
     * Song selection - Reset list when pull-to-refresh
     */
    fun setChooseRefreshingResult(list: List<SongItem>) {
        songChooseFragment.setRefreshingResult(list)
    }


    /**
     * Selected song list - Set whether to delete, top, etc.
     */
    fun setChosenControllable(controllable: Boolean) {
        songChosenFragment.setControllable(controllable)
    }

    /**
     * Selected song list - Reset list
     */
    fun resetChosenSongList(songs: List<SongItem?>?) {
        songChosenFragment.resetSongList(songs)
        setChosenSongCount(songChosenFragment.songSize)
    }

    /**
     * Selected song list - Add song
     */
    fun addChosenSongItem(song: SongItem?) {
        songChosenFragment.addSongItem(song)
        setChosenSongCount(songChosenFragment.songSize)
    }

    /**
     * Selected song list - Delete song
     */
    fun deleteChosenSongItem(song: SongItem?) {
        songChosenFragment.deleteSongItem(song!!)
        setChosenSongCount(songChosenFragment.songSize)
    }

    /**
     * Selected song list - Top song
     */
    fun topUpChosenSongItem(song: SongItem?) {
        songChosenFragment.topUpSongItem(song!!)
    }

    private fun setChosenSongCount(count: Int) {
        var localCount = count
        mBinding?.apply {
            if (count > 0) {
                tvChoosedSongCount.visibility = View.VISIBLE
                if (localCount > 99) {
                    localCount = 99
                }
                tvChoosedSongCount.text = count.toString()
            } else {
                tvChoosedSongCount.visibility = View.GONE
            }
        }
    }
}