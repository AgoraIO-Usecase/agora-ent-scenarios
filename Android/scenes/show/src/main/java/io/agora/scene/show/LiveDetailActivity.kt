package io.agora.scene.show

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import io.agora.scene.show.databinding.ShowLiveDetailActivityBinding
import io.agora.scene.show.service.ShowRoomDetailModel
import io.agora.scene.show.service.ShowServiceProtocol
import io.agora.scene.widget.utils.StatusBarUtil

class LiveDetailActivity : AppCompatActivity() {

    companion object{
        private val EXTRA_ROOM_DETAIL_INFO = "roomDetailInfo"

        fun launch(context: Context, roomDetail: ShowRoomDetailModel){
            context.startActivity(Intent(context, LiveDetailActivity::class.java).apply {
                putExtra(EXTRA_ROOM_DETAIL_INFO, roomDetail)
            })
        }
    }

    private val mRoomInfo by lazy { intent.getSerializableExtra(EXTRA_ROOM_DETAIL_INFO) as ShowRoomDetailModel }
    private val mBinding by lazy { ShowLiveDetailActivityBinding.inflate(LayoutInflater.from(this)) }
    private val mService by lazy { ShowServiceProtocol.getImplInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.setDarkStatusIcon(window, false)
        setContentView(mBinding.root)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        mService.leaveRoom()
    }


}