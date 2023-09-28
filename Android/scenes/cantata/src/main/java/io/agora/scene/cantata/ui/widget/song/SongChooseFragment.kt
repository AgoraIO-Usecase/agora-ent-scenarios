package io.agora.scene.cantata.ui.widget.song

import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.scwang.smart.refresh.layout.api.RefreshLayout
import io.agora.scene.base.component.BaseViewBindingFragment
import io.agora.scene.base.component.OnItemClickListener
import io.agora.scene.cantata.databinding.CantataFragmentSongListBinding
import java.util.Objects

/**
 * 歌单列表
 */
class SongChooseFragment : BaseViewBindingFragment<CantataFragmentSongListBinding>(), OnItemClickListener<SongItem?> {
    private var listener: Listener? = null
    private val pendingViewCreatedRuns: MutableList<Runnable> = ArrayList()
    private val mSearchAdapter: SongChooseViewAdapter = object : SongChooseViewAdapter() {
        public override fun onSongChosen(song: SongItem, position: Int) {
            onSongItemChosen(song)
        }
    }
    private val fragments = arrayOfNulls<ScreenSlidePageFragment>(4)
    override fun getViewBinding(layoutInflater: LayoutInflater, viewGroup: ViewGroup?): CantataFragmentSongListBinding {
        return CantataFragmentSongListBinding.inflate(layoutInflater)
    }

    override fun initView() {
        binding.tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val position = tab.position
                fragments[position]?.onTabSelected(position)
                onSongsRefreshing(position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        val callBack: ScreenSlidePageFragment.OnScreenSlidePageFragmentCallBack =
            object : ScreenSlidePageFragment.OnScreenSlidePageFragmentCallBack {
                override fun onRefresh(refreshLayout: RefreshLayout) {
                    onSongsRefreshing(binding.tabLayout.selectedTabPosition)
                }

                override fun onLoadMore(refreshLayout: RefreshLayout) {
                    onSongsLoadMore(binding.tabLayout.selectedTabPosition)
                }

                override fun onClickSongItem(songItem: SongItem?) {
                    songItem ?: return
                    onSongItemChosen(songItem)
                }
            }
        binding.mViewPager2.adapter = object : FragmentStateAdapter(activity!!) {
            override fun createFragment(position: Int): Fragment {
                if (fragments[position] == null) {
                    fragments[position] = ScreenSlidePageFragment()
                }
                fragments[position]!!.setCallBack(callBack, position)
                return fragments[position]!!
            }

            override fun getItemCount(): Int {
                return fragments.size
            }
        }
        binding.mViewPager2.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (binding.tabLayout.selectedTabPosition == position) {
                    return
                }
                val tabAt = binding.tabLayout.getTabAt(position) ?: return
                binding.tabLayout.selectTab(tabAt)
            }
        })
        binding.recyclerSearchResult.adapter = mSearchAdapter
        val iterator: Iterator<Runnable> = pendingViewCreatedRuns.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            next.run()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Objects.requireNonNull(binding.etSearch.text).toString() != "") {
            onSongsSearching(binding.etSearch.text.toString())
        }
    }

    override fun initListener() {
        binding.llEmpty.setOnClickListener { v: View? ->
            onSongsRefreshing(
                binding.tabLayout.selectedTabPosition
            )
        }
        binding.etSearch.setOnKeyListener { view: View?, keyCode: Int, keyEvent: KeyEvent ->
            if (keyEvent.action == KeyEvent.ACTION_UP) {
                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_SEARCH) {
                    onSongsSearching(binding.etSearch.text.toString())
                }
                return@setOnKeyListener true
            }
            false
        }
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
                if (editable.isEmpty()) {
                    binding.iBtnClear.visibility = View.GONE
                    binding.mViewPager2.visibility = View.VISIBLE
                    binding.recyclerSearchResult.visibility = View.GONE
                    binding.hScrollView.visibility = View.VISIBLE
                    binding.llEmpty.visibility = View.GONE
                } else {
                    binding.iBtnClear.visibility = View.VISIBLE
                    binding.mViewPager2.visibility = View.GONE
                    binding.recyclerSearchResult.visibility = View.VISIBLE
                    binding.hScrollView.visibility = View.GONE
                    binding.llEmpty.visibility = View.GONE
                }
            }
        })
        binding.iBtnClear.setOnClickListener { view: View? -> binding.etSearch.setText("") }
    }

    private fun runOnViewCreated(runnable: Runnable) {
        val view = view
        if (view == null) {
            pendingViewCreatedRuns.add(runnable)
        } else {
            runnable.run()
        }
    }

    val chooseCurrentTabIndex: Int
        get() = if (view == null) {
            0
        } else binding!!.tabLayout.selectedTabPosition

    fun setSongTagsTitle(titles: List<String>, types: List<Int>, defaultIndex: Int) {
        runOnViewCreated {
            for (title in titles) {
                binding.tabLayout.addTab(binding.tabLayout.newTab().setText(title))
            }
            val tabAt = binding.tabLayout.getTabAt(defaultIndex) ?: return@runOnViewCreated
            binding.tabLayout.selectTab(tabAt)
        }
    }

    fun setSongItemStatus(songItem: SongItem, isChosen: Boolean) {
        if (binding!!.recyclerSearchResult.visibility == View.VISIBLE) {
            val searchCount = mSearchAdapter.itemCount
            for (i in 0 until searchCount) {
                val item = mSearchAdapter.getItem(i) ?: continue
                if (item.songNo == songItem.songNo) {
                    item.isChosen = isChosen
                    mSearchAdapter.notifyItemChanged(i)
                    break
                }
            }
        } else {
            for (fragment in fragments) {
                fragment ?: continue
                fragment.setSongItemStatus(songItem, isChosen)
            }
        }
    }

    fun setSearchResult(list: List<SongItem>) {
        if (list.isEmpty()) {
            binding.llEmpty.visibility = View.VISIBLE
        } else {
            binding.llEmpty.visibility = View.GONE
        }
        mSearchAdapter.resetAll(list)
    }

    fun setRefreshingResult(list: List<SongItem>, index: Int) {
        if (list.isEmpty()) {
            binding.llEmpty.visibility = View.VISIBLE
        } else {
            binding.llEmpty.visibility = View.GONE
        }
        fragments[index]?.setRefreshingResult(list)
        enableTabLayoutClick(true)
    }

    fun setLoadMoreResult(list: List<SongItem>, hasMore: Boolean, index: Int) {
        fragments[index]?.setLoadMoreResult(list, hasMore)
        enableTabLayoutClick(true)
    }

    private fun enableTabLayoutClick(enable: Boolean) {
        val binding = binding ?: return
        val tabLayout = binding.tabLayout
        for (i in 0 until tabLayout.tabCount) {
            tabLayout.getTabAt(i)!!.view.isClickable = enable
        }
    }

    private fun onSongItemChosen(songItem: SongItem) {
        listener?.onSongItemChosen(songItem)
    }

    private fun onSongsSearching(condition: String) {
        listener?.onSongsSearching(condition)
    }

    private fun onSongsRefreshing(tagIndex: Int) {
        enableTabLayoutClick(false)
        listener?.onSongsRefreshing(tagIndex)
        binding.mViewPager2.currentItem = tagIndex
    }

    private fun onSongsLoadMore(tagIndex: Int) {
        enableTabLayoutClick(false)
        listener?.onSongsLoadMore(tagIndex)
    }

    fun setListener(listener: Listener?) {
        this.listener = listener
    }

    interface Listener {
        fun onSongItemChosen(songItem: SongItem)
        fun onSongsSearching(condition: String?)
        fun onSongsRefreshing(tagIndex: Int)
        fun onSongsLoadMore(tagIndex: Int)
    }
}