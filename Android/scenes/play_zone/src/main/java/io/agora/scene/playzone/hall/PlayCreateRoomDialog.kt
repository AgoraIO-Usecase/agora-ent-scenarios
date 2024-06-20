package io.agora.scene.playzone.hall

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.ViewModelProvider
import io.agora.scene.base.component.BaseBottomSheetDialogFragment
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.playzone.R
import io.agora.scene.playzone.databinding.PlayZoneDialogCreateRoomBinding
import io.agora.scene.playzone.live.PlayRoomGameActivity
import io.agora.scene.playzone.service.api.PlayGameInfoModel
import java.util.Random

class PlayCreateRoomDialog constructor(
    private val context: Context,
) : BaseBottomSheetDialogFragment<PlayZoneDialogCreateRoomBinding>() {

    companion object {
        const val Key_GameInfo = "key_gameInfo"
    }

    private val mGameInfo: PlayGameInfoModel by lazy {
        arguments?.getSerializable(Key_GameInfo) as PlayGameInfoModel
    }

    private lateinit var roomCreateViewModel: PlayHallViewModel

    private var window: Window? = null
    private var loadingView: View? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        window = dialog.window
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        roomCreateViewModel = ViewModelProvider(this)[PlayHallViewModel::class.java]
        // 用户提示颜色
        val spannableString = SpannableString(getString(R.string.play_zone_create_room_tips))
        spannableString.setSpan(
            ForegroundColorSpan(Color.parseColor("#FA396A")),
            77,
            118,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        mBinding.tvTips.text = spannableString
        // 随机名称
        randomName()
        mBinding.btnRandom.setOnClickListener {
            randomName()
        }
        mBinding.cbPassword.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                mBinding.layoutPassword.visibility = View.VISIBLE
                mBinding.tvPWDTips.visibility = View.VISIBLE
                mBinding.etCode.requestFocus()
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(mBinding.etCode, InputMethodManager.SHOW_IMPLICIT)
            } else {
                hideInput()
                mBinding.layoutPassword.visibility = View.GONE
            }
        }
        mBinding.btnCreateRoom.setOnClickListener {
            createRoom()
        }

        mBinding.etCode.setOnTextChangeListener {

        }

        activity?.window?.let { window ->
            val initialWindowHeight = Rect().apply { window.decorView.getWindowVisibleDisplayFrame(this) }.height()
            mBinding.root.viewTreeObserver.addOnGlobalLayoutListener {
                Handler(Looper.getMainLooper()).postDelayed({
                    if (mBinding == null) {
                        return@postDelayed
                    }
                    val currentWindowHeight =
                        Rect().apply { window.decorView.getWindowVisibleDisplayFrame(this) }.height()
                    if (currentWindowHeight < initialWindowHeight) {
                    } else {
                        mBinding.etCode.clearFocus()
                        mBinding.etRoomName.clearFocus()
                    }
                }, 300)
            }
        }

        roomCreateViewModel.createRoomInfoLiveData.observe(this) { roomInfo ->
            hideLoadingView()
            if (roomInfo != null) {
                dismiss()
                PlayRoomGameActivity.launch(context, roomInfo)
            } else {
                // 创建房间失败
            }
        }
    }

    private fun randomName() {
        mBinding.etRoomName.setText(
            resources.getStringArray(R.array.play_zone_roomName_array)[Random().nextInt(21)]
        )
    }

    private fun createRoom() {
        val roomName = mBinding.etRoomName.text.toString()
        if (TextUtils.isEmpty(roomName)) {
            ToastUtils.showToast(R.string.play_zone_please_input_room_name)
            return
        }
        val isPrivate = mBinding.cbPassword.isChecked
        val password = mBinding.etCode.text.toString()
        if (isPrivate && password.length < 4) {
            ToastUtils.showToast(getString(R.string.play_zone_please_input_4_pwd))
            return
        }
        showLoadingView()
        roomCreateViewModel.createRoom(mGameInfo, roomName, password)
    }

    private fun showLoadingView() {
        window?.apply {
            decorView.post { addLoadingView() }
            decorView.postDelayed({ hideLoadingView() }, 5000)
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

    private fun hideInput() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val v = window?.peekDecorView()
        if (v != null) {
            imm.hideSoftInputFromWindow(v.windowToken, 0)
            mBinding.etCode.clearFocus()
        }
    }
}