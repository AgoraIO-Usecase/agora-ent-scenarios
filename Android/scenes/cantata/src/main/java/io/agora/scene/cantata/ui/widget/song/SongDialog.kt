package io.agora.scene.cantata.ui.widget.song

import android.os.Bundle
import android.view.View
import android.widget.RadioGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import io.agora.scene.base.component.BaseBottomSheetDialogFragment
import io.agora.scene.cantata.R
import io.agora.scene.cantata.databinding.CantataDialogChooseSongBinding
import io.agora.scene.widget.utils.UiUtils

/**
 * 点歌菜单
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
            songChooseFragment.setListener(object : SongChooseFragment.Listener {
                override fun onSongItemChosen(songItem: SongItem) {
                    if (UiUtils.isFastClick(500)) return
                    chooseSongListener?.onChooseSongChosen(this@SongDialog, songItem)
                }

                override fun onSongsSearching(condition: String?) {
                    chooseSongListener?.onChooseSongSearching(this@SongDialog, condition)
                }

                override fun onSongsRefreshing(tagIndex: Int) {
                    chooseSongListener?.onChooseSongRefreshing(this@SongDialog, tagIndex)
                }

                override fun onSongsLoadMore(tagIndex: Int) {
                    chooseSongListener?.onChooseSongLoadMore(this@SongDialog, tagIndex)
                }
            })
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
     * 设置事件监听
     */
    fun setChooseSongListener(chooseSongListener: SongActionListenerImpl) {
        this.chooseSongListener = chooseSongListener
        chooseSongListener.songTypeList
    }

    /**
     * 点歌-标题设置
     */
    fun setChooseSongTabsTitle(titles: List<String>, types: List<Int>, defaultIndex: Int) {
        songChooseFragment.setSongTagsTitle(titles, types, defaultIndex)
    }

    /**
     * 点歌-更新item选中状态
     */
    fun setChooseSongItemStatus(songItem: SongItem, isChosen: Boolean) {
        songChooseFragment.setSongItemStatus(songItem, isChosen)
    }

    /**
     * 点歌-更新搜索列表
     */
    fun setChooseSearchResult(list: List<SongItem>) {
        songChooseFragment.setSearchResult(list)
    }

    /**
     * 点歌-下拉刷新重置列表
     */
    fun setChooseRefreshingResult(list: List<SongItem>, index: Int) {
        songChooseFragment.setRefreshingResult(list, index)
    }

    /**
     * 点歌-加载更多刷新列表
     */
    fun setChooseLoadMoreResult(list: List<SongItem>, hasMore: Boolean, index: Int) {
        songChooseFragment.setLoadMoreResult(list, hasMore, index)
    }

    /**
     * 已点歌单-设置是否可以做删除置顶等操作
     */
    fun setChosenControllable(controllable: Boolean) {
        songChosenFragment.setControllable(controllable)
    }

    /**
     * 已点歌单-重置列表
     */
    fun resetChosenSongList(songs: List<SongItem?>?) {
        songChosenFragment.resetSongList(songs)
        setChosenSongCount(songChosenFragment.songSize)
    }

    /**
     * 已点歌单-添加歌曲
     */
    fun addChosenSongItem(song: SongItem?) {
        songChosenFragment.addSongItem(song)
        setChosenSongCount(songChosenFragment.songSize)
    }

    /**
     * 已点歌单-删除歌曲
     */
    fun deleteChosenSongItem(song: SongItem?) {
        songChosenFragment.deleteSongItem(song!!)
        setChosenSongCount(songChosenFragment.songSize)
    }

    /**
     * 已点歌单-置顶歌曲
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