package io.agora.scene.show

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import io.agora.scene.show.databinding.ShowLiveDetailActivityBinding
import io.agora.scene.show.service.ShowRoomDetailModel
import io.agora.scene.show.utils.PermissionHelp
import io.agora.scene.widget.utils.StatusBarUtil


class LiveDetailActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_ROOM_DETAIL_INFO_LIST = "roomDetailInfoList"

        fun launch(context: Context, roomDetail: ArrayList<ShowRoomDetailModel>) {
            context.startActivity(Intent(context, LiveDetailActivity::class.java).apply {
                putExtra(EXTRA_ROOM_DETAIL_INFO_LIST, roomDetail)
            })
        }
    }

    internal lateinit var mPermissionHelp: PermissionHelp

    private val mRoomInfoList by lazy {
        intent.getParcelableArrayListExtra<ShowRoomDetailModel>(
            EXTRA_ROOM_DETAIL_INFO_LIST
        )!!
    }
    private val mBinding by lazy { ShowLiveDetailActivityBinding.inflate(LayoutInflater.from(this)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.hideStatusBar(window, false)
        setContentView(mBinding.root)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        mPermissionHelp = PermissionHelp(this)

        mBinding.viewPager2.offscreenPageLimit = 1
        mBinding.viewPager2.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = Int.MAX_VALUE

            override fun createFragment(position: Int) =
                LiveDetailFragment.newInstance(mRoomInfoList[position % mRoomInfoList.size])

        }
    }



}