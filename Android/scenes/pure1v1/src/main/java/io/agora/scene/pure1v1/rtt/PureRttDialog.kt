package io.agora.scene.pure1v1.rtt

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.agora.rtmsyncmanager.utils.ThreadManager
import io.agora.scene.base.TokenGenerator
import io.agora.scene.base.TokenGenerator.AgoraTokenType
import io.agora.scene.base.TokenGenerator.TokenGeneratorType
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.base.utils.UiUtil
import io.agora.scene.pure1v1.R
import io.agora.scene.pure1v1.databinding.Pure1v1RttDialogBinding
import io.agora.scene.widget.utils.UiUtils
import java.util.concurrent.CountDownLatch

class PureRttDialog : BottomSheetDialog {

    private val mBinding by lazy {
        Pure1v1RttDialogBinding.inflate(LayoutInflater.from(context))
    }

    private var loadingView: View? = null

    private var channelName: String = ""

    constructor(context: Context, channelName: String) : this(context, R.style.Pure1v1_bottom_dialog, channelName)

    constructor(context: Context, theme: Int, channelName: String) : super(context, theme) {
        super.setContentView(mBinding.root)
        this.channelName = channelName
        initView()
    }

    private fun initView() {
        mBinding.tvSourceLanguage.text = PureRttManager.currentSourceLanguageName
        mBinding.tvTargetLanguage.text = PureRttManager.currentTargetLanguageName
        mBinding.btnEnableRtt.isActivated = !PureRttManager.isRttEnabled
        if (PureRttManager.isRttEnabled) {
            mBinding.btnEnableRtt.text = context.getString(R.string.pure1v1_disable_this_feature)
        } else {
            mBinding.btnEnableRtt.text = context.getString(R.string.pure1v1_enable_this_feature)
        }
        mBinding.tvSourceLanguage.setOnClickListener {
            if (UiUtils.isFastClick()) return@setOnClickListener
            if (PureRttManager.isRttEnabled) {
                ToastUtils.showToast(R.string.pure1v1_switch_language_tips)
                return@setOnClickListener
            }
            val dialog = PureRttSelectLanguageDialog(context, PureRttSelectLanguageDialog.SourceLanguageType)
            dialog.setOnDismissListener {
                mBinding.tvSourceLanguage.text = PureRttManager.currentSourceLanguageName
            }
            dialog.show()
        }
        mBinding.tvTargetLanguage.setOnClickListener {
            if (UiUtils.isFastClick()) return@setOnClickListener
            if (PureRttManager.isRttEnabled) {
                ToastUtils.showToast(R.string.pure1v1_switch_language_tips)
                return@setOnClickListener
            }
            val dialog = PureRttSelectLanguageDialog(context, PureRttSelectLanguageDialog.TargetLanguageType)
            dialog.setOnDismissListener {
                mBinding.tvTargetLanguage.text = PureRttManager.currentTargetLanguageName
            }
            dialog.show()
        }
        mBinding.btnEnableRtt.setOnClickListener {
            if (UiUtils.isFastClick()) return@setOnClickListener
            addLoadingView()
            if (!PureRttManager.isRttEnabled) {
                mBinding.btnEnableRtt.isActivated = false
                generateTokens { success ->
                    if (success) {
                        PureRttManager.enableRtt(channelName, completion = { success ->
                            hideLoadingView()
                            if (success) {
                                mBinding.btnEnableRtt.isActivated = false
                                mBinding.btnEnableRtt.text = context.getString(R.string.pure1v1_disable_this_feature)
                                ToastUtils.showToast(R.string.pure1v1_rtt_enable_success)
                                dismiss()
                            } else {
                                mBinding.btnEnableRtt.isActivated = true
                                ToastUtils.showToast(R.string.pure1v1_rtt_enable_fail)
                            }
                        })
                    } else {
                        mBinding.btnEnableRtt.isActivated = true
                        ToastUtils.showToast(R.string.pure1v1_rtt_enable_fail)
                    }
                }
            } else {
                PureRttManager.disableRtt(false, completion = { success ->
                    hideLoadingView()
                    if (success) {
                        mBinding.btnEnableRtt.isActivated = true
                        mBinding.btnEnableRtt.text = context.getString(R.string.pure1v1_enable_this_feature)
                        ToastUtils.showToast(R.string.pure1v1_rtt_disable_success)
                    } else {
                        ToastUtils.showToast(R.string.pure1v1_rtt_disable_fail)
                    }
                })
            }
        }
    }

    private fun addLoadingView() {
        if (this.loadingView == null) {
            val rootView = window?.decorView?.findViewById<ViewGroup>(android.R.id.content)?.getChildAt(0) as ViewGroup
            this.loadingView = LayoutInflater.from(context).inflate(R.layout.view_base_loading, rootView, false)
            rootView.addView(
                this.loadingView,
                ViewGroup.LayoutParams(-1, -1)
            )
        }
        this.loadingView?.visibility = View.VISIBLE
    }

    private fun hideLoadingView() {
        if (loadingView == null) {
            return
        }
        window?.apply {
            decorView.post {
                loadingView?.visibility = View.GONE
            }
        }
    }

    private fun generateTokens(completion: (success: Boolean) -> Unit) {
        if (PureRttManager.subBotToken.isNotEmpty() && PureRttManager.pubBotToken.isNotEmpty()) {
            completion(true)
            return
        }
        ThreadManager.getInstance().runOnIOThread {
            val latch = CountDownLatch(2)
            var isSuccess = true
            TokenGenerator.generateToken(channelName,
                PureRttManager.subBotUid,
                TokenGeneratorType.token007,
                AgoraTokenType.rtc,
                success = {
                    PureRttManager.subBotToken = it
                    isSuccess = isSuccess.or(true)
                    latch.countDown()
                },
                failure = {
                    isSuccess = isSuccess.or(false)
                    latch.countDown()
                })
            TokenGenerator.generateToken(channelName,
                PureRttManager.pubBotUid,
                TokenGeneratorType.token007,
                AgoraTokenType.rtc,
                success = {
                    PureRttManager.pubBotToken = it
                    isSuccess = isSuccess.or(true)
                    latch.countDown()
                },
                failure = {
                    isSuccess = isSuccess.or(false)
                    latch.countDown()
                })

            try {
                latch.await()
                ThreadManager.getInstance().runOnMainThread {
                    completion.invoke(isSuccess)
                }
            } catch (e: Exception) {
                ThreadManager.getInstance().runOnMainThread {
                    completion.invoke(false)
                }
            }
        }
    }
}