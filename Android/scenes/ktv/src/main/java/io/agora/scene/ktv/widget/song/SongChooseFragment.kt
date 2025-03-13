package io.agora.scene.ktv.widget.song

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import io.agora.scene.base.component.BaseViewBindingFragment
import io.agora.scene.base.component.OnItemClickListener
import io.agora.scene.ktv.databinding.KtvFragmentSongListBinding
import java.util.Objects

/**
 * Song list
 */
class SongChooseFragment : BaseViewBindingFragment<KtvFragmentSongListBinding>(),
    OnItemClickListener<SongItem?> {
    private var listener: Listener? = null

    private val mSearchAdapter: SongChooseViewAdapter = object : SongChooseViewAdapter() {
        override fun onSongChosen(song: SongItem, position: Int) {
            listener?.onSongItemChosen(song)
        }
    }

    private val mRankListAdapter: SongChooseViewAdapter = object : SongChooseViewAdapter() {
        override fun onSongChosen(song: SongItem, position: Int) {
            listener?.onSongItemChosen(song)
        }
    }

    override fun getViewBinding(layoutInflater: LayoutInflater, viewGroup: ViewGroup?): KtvFragmentSongListBinding {
        return KtvFragmentSongListBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerSearchResult.adapter = mSearchAdapter

        binding.layoutResult.rvRankList.setAdapter(mRankListAdapter)
        binding.layoutResult.smartRefreshLayout.setOnRefreshListener {
            listener?.onSongsRefreshing()
        }
        binding.layoutResult.smartRefreshLayout.setOnLoadMoreListener {
            listener?.onSongsLoadMore()
        }
        binding.layoutResult.smartRefreshLayout.autoRefresh()
    }

    override fun onResume() {
        super.onResume()
        if (Objects.requireNonNull(binding.etSearch.text).toString() != "") {
            onSongsSearching(binding.etSearch.text.toString())
        }
    }

    override fun initListener() {
        binding.llEmpty.setOnClickListener { v: View? ->
            listener?.onSongsRefreshing()
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
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun afterTextChanged(editable: Editable) {
                if (editable.isEmpty()) {
                    binding.iBtnClear.visibility = View.GONE
                    binding.recyclerSearchResult.visibility = View.GONE
                    binding.llEmpty.visibility = View.GONE
                } else {
                    binding.iBtnClear.visibility = View.VISIBLE
                    binding.recyclerSearchResult.visibility = View.VISIBLE
                    binding.llEmpty.visibility = View.GONE
                }
            }
        })
        binding.iBtnClear.setOnClickListener { binding.etSearch.setText("") }
    }

    fun setSongItemStatus(songItem: SongItem, isChosen: Boolean) {
        if (binding!!.recyclerSearchResult.visibility == View.VISIBLE) {
            val searchCount = mSearchAdapter.itemCount
            for (i in 0 until searchCount) {
                val item = mSearchAdapter.getItem(i)
                if (item != null && item.songNo == songItem.songNo) {
                    item.isChosen = isChosen
                    item.loading = songItem.loading
                    mSearchAdapter.notifyItemChanged(i)
                    break
                }
            }
        } else {
            val itemCount = mRankListAdapter.itemCount
            for (i in 0 until itemCount) {
                val item = mRankListAdapter.getItem(i)
                if (item != null && item.songNo == songItem.songNo) {
                    item.isChosen = isChosen
                    item.loading = songItem.loading
                    mRankListAdapter.notifyItemChanged(i)
                    break
                }
            }
        }
    }

    fun setSearchResult(list: List<SongItem?>?) {
        binding.llEmpty.isVisible = list.isNullOrEmpty()
        mSearchAdapter.resetAll(list)
    }

    fun setRefreshingResult(list: List<SongItem>?) {
        binding.llEmpty.isVisible = list.isNullOrEmpty()
        mRankListAdapter.resetAll(list)
        binding.layoutResult.smartRefreshLayout.setEnableLoadMore(true)
        binding.layoutResult.smartRefreshLayout.finishRefresh()
    }

    fun setLoadMoreResult(list: List<SongItem?>?, hasMore: Boolean) {
        mRankListAdapter.insertAll(list)
        binding.layoutResult.smartRefreshLayout.finishLoadMore()
        binding.layoutResult.smartRefreshLayout.setEnableLoadMore(hasMore)
    }


    private fun onSongItemChosen(songItem: SongItem) {
        listener?.onSongItemChosen(songItem)
    }

    private fun onSongsSearching(condition: String) {
        listener?.onSongsSearching(condition)
    }

    private fun onSongsRefreshing() {
        listener?.onSongsRefreshing()
    }

    private fun onSongsLoadMore() {
        listener?.onSongsLoadMore()
    }

    fun setRestSongStatus(chosenSongs: List<SongItem>) {

        if (binding != null) {
            setRestResultSongStatus(chosenSongs)
            setRestSearchSongStatus(chosenSongs)

        }
    }

    private fun setRestSearchSongStatus(chosenSongs: List<SongItem>) {
        val dataList = mSearchAdapter.dataList
        var update = false
        for (i in dataList.indices) {
            val oldItem = dataList[i]
            if (oldItem != null) {
                oldItem.loading = false
                var newItem: SongItem? = null

                for (song in chosenSongs) {
                    if (oldItem.songNo != null && oldItem.songNo == song.songNo) {
                        newItem = song
                        break
                    }
                }
                if (newItem != null) {
                    update = true
                    oldItem.isChosen = newItem.isChosen
                }
            }
        }
        if (update) {
            mSearchAdapter.notifyDataSetChanged()
        }
    }

    private fun setRestResultSongStatus(chosenSongs: List<SongItem>) {
        val dataList = mRankListAdapter.dataList
        var update = false
        for (i in dataList.indices) {
            val oldItem = dataList[i]
            if (oldItem != null) {
                oldItem.loading = false
                var newItem: SongItem? = null

                for (song in chosenSongs) {
                    if (oldItem.songNo != null && oldItem.songNo == song.songNo) {
                        newItem = song
                        break
                    }
                }
                if (newItem != null) {
                    update = true
                    oldItem.isChosen = newItem.isChosen
                }
            }
        }
        if (update) {
            mRankListAdapter.notifyDataSetChanged()
        }
    }

    fun setListener(listener: Listener?) {
        this.listener = listener
    }

    interface Listener {
        fun onSongItemChosen(songItem: SongItem)

        fun onSongsSearching(condition: String?)

        fun onSongsRefreshing()

        fun onSongsLoadMore()
    }
}
