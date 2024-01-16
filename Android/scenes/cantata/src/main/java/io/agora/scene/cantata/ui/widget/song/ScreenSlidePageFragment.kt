package io.agora.scene.cantata.ui.widget.song

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.api.RefreshLayout
import io.agora.scene.base.utils.GsonUtil
import io.agora.scene.cantata.R

class ScreenSlidePageFragment : Fragment() {
    private var rvRankList: RecyclerView? = null
    private var smartRefreshLayout: SmartRefreshLayout? = null
    private var callBack: OnScreenSlidePageFragmentCallBack? = null
    private var position = 0
    private val mRankListAdapter: SongChooseViewAdapter = object : SongChooseViewAdapter() {
        override fun onSongChosen(song: SongItem, position: Int) {
            callBack?.onClickSongItem(song)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.cantata_fragment_screen_slide_page, container, false)
    }


    fun setCallBack(callBack: OnScreenSlidePageFragmentCallBack, position: Int): ScreenSlidePageFragment {
        this.callBack = callBack
        this.position = position
        return this
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvRankList = view.findViewById(R.id.rvRankList)
        smartRefreshLayout = view.findViewById(R.id.smart_refresh_layout)
        rvRankList?.adapter = mRankListAdapter
        smartRefreshLayout?.setOnRefreshListener { refreshLayout: RefreshLayout ->
            callBack?.onRefresh(refreshLayout)
        }
        smartRefreshLayout?.setOnLoadMoreListener { refreshLayout: RefreshLayout ->
            callBack?.onLoadMore(refreshLayout)
        }
    }

    fun setRefreshingResult(list: List<SongItem?>) {
        mRankListAdapter.resetAll(list)
        smartRefreshLayout?.setEnableLoadMore(true)
        smartRefreshLayout?.finishRefresh()
    }

    fun setLoadMoreResult(list: List<SongItem?>, hasMore: Boolean) {
        mRankListAdapter.insertAll(list)
        smartRefreshLayout?.finishLoadMore()
        smartRefreshLayout?.setEnableLoadMore(hasMore)
    }

    fun setSongItemStatus(songItem: SongItem, isChosen: Boolean) {
        Log.e(
            "liu0228",
            "setSongItemStatus    songItem = " + GsonUtil.getInstance().toJson(songItem) + "    isChosen = " + isChosen
        )
        val itemCount = mRankListAdapter.itemCount
        for (i in 0 until itemCount) {
            val item = mRankListAdapter.getItem(i)?:continue
            if (item.songNo == songItem.songNo) {
                item.isChosen = isChosen
                mRankListAdapter.notifyItemChanged(i)
                break
            }
        }
    }

    fun onTabSelected(position: Int) {
        mRankListAdapter.resetAll(null)
    }

    interface OnScreenSlidePageFragmentCallBack {
        fun onRefresh(refreshLayout: RefreshLayout)
        fun onLoadMore(refreshLayout: RefreshLayout)
        fun onClickSongItem(songItem: SongItem?)
    }
}