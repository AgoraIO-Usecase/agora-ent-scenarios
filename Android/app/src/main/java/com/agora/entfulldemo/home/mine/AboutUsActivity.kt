package com.agora.entfulldemo.home.mine

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.agora.entfulldemo.R
import com.agora.entfulldemo.databinding.AppActivityAboutUsBinding
import com.alibaba.android.arouter.facade.annotation.Route
import io.agora.scene.base.PagePathConstant
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.base.component.OnButtonClickListener
import io.agora.scene.base.manager.PagePilotManager
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.widget.dialog.CommonDialog

@Route(path = PagePathConstant.pageMineAboutUs)
class AboutUsActivity : BaseViewBindingActivity<AppActivityAboutUsBinding>() {

    private val servicePhone = "400-632-6626"
    private val webSite = "https://www.agora.io/cn/about-us/"

    private var counts = 0
    private val debugModeOpenTime: Long = 2000
    private var beginTime: Long = 0

    override fun getViewBinding(inflater: LayoutInflater): AppActivityAboutUsBinding {
        return AppActivityAboutUsBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupClickPhoneAction()
        setupClickWebAction()
        setupDebugMode()
        // set versions
        binding.tvVersion.text = "20230110-2.1.0-4.1.1"
        binding.tvServiceNumber.text = servicePhone
        binding.tvHomeWebSite.text = webSite
        binding.tvChatRoomVersion.text = "YL-2.1.0"
        binding.tvSpaceVoiceVersion.text = "YLSA-2.1.0"
        binding.tvOnlineKTVVersion.text = "KTV-2.1.0"
    }

    private fun setupClickWebAction() {
        binding.vHomeWebPage.setOnClickListener {
            PagePilotManager.pageWebView(webSite)
        }
    }

    private fun setupClickPhoneAction() {
        binding.vServicePhone.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CALL_PHONE),1)
            } else {
                val dialog = CallPhoneDialog().apply {
                    arguments = Bundle().apply {
                        putString(CallPhoneDialog.KEY_PHONE, servicePhone)
                    }
                }
                dialog.onClickCallPhone = {
                    val intent = Intent(Intent.ACTION_CALL)
                    val uri = Uri.parse("tel:" + servicePhone)
                    intent.setData(uri)
                    startActivity(intent)
                }
                dialog.show(supportFragmentManager, "CallPhoneDialog")
            }
        }
    }

    private fun setupDebugMode() {
        binding.tvDebugMode.visibility = View.INVISIBLE
        binding.tvVersion.setOnClickListener {
            if (counts == 0) {
                beginTime = System.currentTimeMillis();
            }
            counts ++;
            if (counts == 5) {
                if (System.currentTimeMillis() - beginTime > debugModeOpenTime) {
                    counts = 0;
                    return@setOnClickListener;
                }
                counts = 0;
                binding.tvDebugMode.visibility = View.VISIBLE
                AgoraApplication.the().enableDebugMode(true)
                ToastUtils.showToast("Debug模式已打开");
            }
        }
        binding.tvDebugMode.setOnClickListener { v ->
            showDebugModeCloseDialog()
        }
        if (AgoraApplication.the().isDebugModeOpen) {
            binding.tvDebugMode.visibility = View.VISIBLE
        }
    }

    private fun showDebugModeCloseDialog() {
        val dialog = CommonDialog(this)
        dialog.setDialogTitle("确定退出Debug模式么？")
        dialog.setDescText("退出debug模式后， 设置页面将恢复成正常的设置页面哦～")
        dialog.setDialogBtnText(getString(R.string.cancel),
            getString(R.string.app_exit))
        dialog.onButtonClickListener = object : OnButtonClickListener {
            override fun onLeftButtonClick() {}
            override fun onRightButtonClick() {
                counts = 0
                binding.tvDebugMode.visibility = View.GONE
                AgoraApplication.the().enableDebugMode(false)
                ToastUtils.showToast("Debug模式已关闭")
            }
        }
        dialog.show()
    }
}