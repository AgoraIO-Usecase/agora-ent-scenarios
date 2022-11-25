package io.agora.scene.show

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import io.agora.scene.show.databinding.ShowLivePrepareActivityBinding
import io.agora.scene.show.service.ShowServiceProtocol
import io.agora.scene.widget.utils.StatusBarUtil

class LivePrepareActivity : AppCompatActivity(){

    private val mBinding by lazy { ShowLivePrepareActivityBinding.inflate(LayoutInflater.from(this)) }
    private val mService by lazy { ShowServiceProtocol.getImplInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.hideStatusBar(window, false)
        setContentView(mBinding.root)

        mBinding.btnCreateRoom.setOnClickListener {
            mBinding.btnCreateRoom.isEnabled = false
            mService.createRoom("Testing", {
                mService.joinRoom(it.roomId, { roomDetailInfo ->
                    runOnUiThread {
                        LiveDetailActivity.launch(this@LivePrepareActivity, roomDetailInfo)
                        finish()
                    }
                }, {
                    runOnUiThread {
                        mBinding.btnCreateRoom.isEnabled = true
                    }
                })
            }, {
                runOnUiThread {
                    mBinding.btnCreateRoom.isEnabled = true
                }
            })
        }
    }
}