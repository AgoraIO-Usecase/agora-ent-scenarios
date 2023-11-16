package io.agora.scene.voice.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.*
import android.text.style.ForegroundColorSpan
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.ViewModelProvider
import io.agora.scene.base.component.BaseBottomSheetDialogFragment
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.voice.R
import io.agora.scene.voice.databinding.VoiceDialogCreateRoomBinding
import io.agora.scene.voice.ui.activity.VoiceRoomSoundSelectionActivity
import io.agora.scene.voice.viewmodel.VoiceCreateViewModel
import io.agora.voice.common.ui.IParserSource
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class CreateRoomDialog constructor(
    private val context: Context,
): BaseBottomSheetDialogFragment<VoiceDialogCreateRoomBinding>(), IParserSource {

    /** 当前选中的是第几个输入框*/
    private var currentPosition = 0

    private lateinit var roomCreateViewModel: VoiceCreateViewModel

    private var window: Window? = null
    private var loadingView: View? = null

    /** 输入历史记录 */
    private var oldInput = ""

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        window = dialog.window
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        roomCreateViewModel = ViewModelProvider(this)[VoiceCreateViewModel::class.java]
        // 用户提示颜色
        val spannableString = SpannableString(getString(R.string.voice_create_room_tips))
        spannableString.setSpan(ForegroundColorSpan(Color.parseColor("#FA396A")), 77, 118, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
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
                mBinding.tvPWDTips.visibility = View.GONE
            }
        }
        mBinding.btnCreateRoom.setOnClickListener {
            createRoom()
        }
        mBinding.etCode.setOnTextChangeListener {  }

        activity?.window?.let { window ->
            val initialWindowHeight = Rect().apply { window.decorView.getWindowVisibleDisplayFrame(this) }.height()
            mBinding.root.viewTreeObserver.addOnGlobalLayoutListener {
                Handler(Looper.getMainLooper()).postDelayed({
                    if (mBinding == null) { return@postDelayed }
                    val currentWindowHeight = Rect().apply { window.decorView.getWindowVisibleDisplayFrame(this) }.height()
                    if (currentWindowHeight < initialWindowHeight) {
                    } else {
                        mBinding.etCode.clearFocus()
                        mBinding.etRoomName.clearFocus()
                    }
                }, 300)
            }
        }
    }

    private fun randomName() {
        val date = Date()
        val month = SimpleDateFormat("MM").format(date) //获取月份
        val day = SimpleDateFormat("dd").format(date) //获取分钟
        val roomName =  getString(R.string.voice_room_create_chat_room) + "-" + month + day + "-" + (Math.random() * 999 + 1).roundToInt()
        mBinding.etRoomName.setText(roomName)
    }

    private fun createRoom() {
        val roomName = mBinding.etRoomName.text.toString()
        if (TextUtils.isEmpty(roomName)) {
            ToastUtils.showToast(R.string.voice_please_input_room_name)
            return
        }
        val isPrivate = mBinding.cbPassword.isChecked
        val password = mBinding.etCode.text.toString()
        if (isPrivate && password.length < 4) {
            ToastUtils.showToast(getString(R.string.voice_please_input_4_pwd))
            return
        }
        dismiss()
        activity?.let {
            VoiceRoomSoundSelectionActivity.startActivity(it, roomName, !isPrivate, password, 0)
        }
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
                if (loadingView != null) {
                    loadingView?.visibility = View.GONE
                }
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