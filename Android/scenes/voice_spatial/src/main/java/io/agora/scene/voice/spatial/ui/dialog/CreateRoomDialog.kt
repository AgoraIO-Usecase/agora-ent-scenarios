package io.agora.scene.voice.spatial.ui.dialog

import android.annotation.SuppressLint
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
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.AppCompatEditText
import androidx.lifecycle.ViewModelProvider
import io.agora.scene.base.component.BaseBottomSheetDialogFragment
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.voice.spatial.R
import io.agora.scene.voice.spatial.databinding.VoiceSpatialDialogCreateRoomBinding
import io.agora.scene.voice.spatial.model.VoiceRoomModel
import io.agora.scene.voice.spatial.service.VoiceServiceProtocol
import io.agora.scene.voice.spatial.ui.activity.ChatroomLiveActivity
import io.agora.scene.voice.spatial.viewmodel.VoiceCreateViewModel
import io.agora.voice.common.net.OnResourceParseCallback
import io.agora.voice.common.net.Resource
import io.agora.voice.common.ui.IParserSource
import io.agora.voice.common.utils.ToastTools
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class CreateRoomDialog(
    private val context: Context,
): BaseBottomSheetDialogFragment<VoiceSpatialDialogCreateRoomBinding>(), IParserSource {

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
                mBinding.etCode1.visibility = View.VISIBLE
                mBinding.etCode2.visibility = View.VISIBLE
                mBinding.etCode3.visibility = View.VISIBLE
                mBinding.etCode4.visibility = View.VISIBLE
                mBinding.tvPWDTips.visibility = View.VISIBLE
                mBinding.etCode1.requestFocus()
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(mBinding.etCode1, InputMethodManager.SHOW_IMPLICIT)
            } else {
                hideInput()
                mBinding.etCode1.visibility = View.GONE
                mBinding.etCode2.visibility = View.GONE
                mBinding.etCode3.visibility = View.GONE
                mBinding.etCode4.visibility = View.GONE
                mBinding.tvPWDTips.visibility = View.GONE
            }
        }
        mBinding.btnCreateRoom.setOnClickListener {
            createRoom()
        }
        mBinding.etCode1.onFocusChangeListener = editFocusListener
        mBinding.etCode2.onFocusChangeListener = editFocusListener
        mBinding.etCode3.onFocusChangeListener = editFocusListener
        mBinding.etCode4.onFocusChangeListener = editFocusListener

        mBinding.etCode1.setOnKeyListener(onKeyListener)
        mBinding.etCode2.setOnKeyListener(onKeyListener)
        mBinding.etCode3.setOnKeyListener(onKeyListener)
        mBinding.etCode4.setOnKeyListener(onKeyListener)

        mBinding.etCode1.addTextChangedListener(textWatcher)
        mBinding.etCode2.addTextChangedListener(textWatcher)
        mBinding.etCode3.addTextChangedListener(textWatcher)
        mBinding.etCode4.addTextChangedListener(textWatcher)

        activity?.window?.let { window ->
            val initialWindowHeight = Rect().apply { window.decorView.getWindowVisibleDisplayFrame(this) }.height()
            mBinding.root.viewTreeObserver.addOnGlobalLayoutListener {
                Handler(Looper.getMainLooper()).postDelayed({
                    val currentWindowHeight = Rect().apply { window.decorView.getWindowVisibleDisplayFrame(this) }.height()
                    if (currentWindowHeight < initialWindowHeight) {
                    } else {
                        mBinding.etCode4.clearFocus()
                        mBinding.etRoomName.clearFocus()
                    }
                }, 300)
            }
        }

        roomCreateViewModel.createRoomObservable().observe(this) { response: Resource<VoiceRoomModel> ->
            parseResource(response, object : OnResourceParseCallback<VoiceRoomModel>() {
                override fun onSuccess(voiceRoomModel: VoiceRoomModel?) {
                    voiceRoomModel?.let { roomCreateViewModel.joinRoom(it.roomId) }
                }

                override fun onError(code: Int, message: String?) {
                    super.onError(code, message)
                    hideLoadingView()
                    when (code) {
                        VoiceServiceProtocol.ERR_LOGIN_ERROR -> {
                            activity?.let { ToastTools.show(it, getString(R.string.voice_room_login_exception)) }
                        }
                        VoiceServiceProtocol.ERR_ROOM_NAME_INCORRECT -> {
                            activity?.let { ToastTools.show(it, getString(R.string.voice_room_name_rule)) }
                        }
                        else -> {
                            activity?.let { ToastTools.show(it, getString(R.string.voice_room_create_error)) }
                        }
                    }
                }
            })
        }
        roomCreateViewModel.joinRoomObservable().observe(this) { response: Resource<VoiceRoomModel> ->
            parseResource(response, object : OnResourceParseCallback<VoiceRoomModel>() {
                override fun onSuccess(result: VoiceRoomModel?) {
                    hideLoadingView()
                    val a = activity
                    if (result != null && a != null) {
                        ChatroomLiveActivity.startActivity(a, result)
                    }
                    dismiss()
                }

                override fun onError(code: Int, message: String?) {
                    super.onError(code, message)
                    hideLoadingView()
                }
            })
        }
    }

    private fun randomName() {
        val date = Date()
        val month = SimpleDateFormat("MM").format(date) //获取月份
        val day = SimpleDateFormat("dd").format(date) //获取分钟
        val roomName = getString(R.string.voice_room_create_chat_3d_room) + "-" + month + day + "-" + (Math.random() * 999 + 1).roundToInt()
        mBinding.etRoomName.setText(roomName)
    }

    private fun createRoom() {
        val roomName = mBinding.etRoomName.text.toString()
        if (TextUtils.isEmpty(roomName)) {
            ToastUtils.showToast(R.string.voice_please_input_room_name)
            return
        }
        val isPrivate = mBinding.cbPassword.isChecked
        var password = (mBinding.etCode1.text.toString()
                + mBinding.etCode2.text
                + mBinding.etCode3.text
                + mBinding.etCode4.text)
        if (isPrivate && password.length < 4) {
            ToastUtils.showToast(getString(R.string.voice_please_input_4_pwd))
            return
        }
        if (!isPrivate) { password = "" }
        showLoadingView()
        roomCreateViewModel.createSpatialRoom(roomName, 0, password)
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

    /**
     * 记录当前焦点位置
     */
    @SuppressLint("NonConstantResourceId")
    private val editFocusListener =
        OnFocusChangeListener { view: View, hasFocus: Boolean ->
            if (hasFocus) {
                when (view.id) {
                    R.id.etCode1 -> { currentPosition = 0 }
                    R.id.etCode2 -> { currentPosition = 1 }
                    R.id.etCode3 -> { currentPosition = 2 }
                    R.id.etCode4 -> { currentPosition = 3 }
                }
            }
        }

    /**
     * 删除前一个输入内
     */
    private val onKeyListener =
        View.OnKeyListener { v: View?, keyCode: Int, event: KeyEvent ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    if (v is AppCompatEditText) {
                        if (v.text?.length == 0) {
                            findNextFocus(false)
                        }
                        v.setText("")
                    }
                }
            }
            false
        }

    /**
     * 寻找下一个需要获取焦点的控件
     *
     * @param isNext true 下一个  false 上一个
     */
    private fun findNextFocus(isNext: Boolean) {
        if (isNext) {
            when (currentPosition) {
                0 -> {
                    mBinding.etCode2.isEnabled = true
                    mBinding.etCode2.requestFocus()
                }
                1 -> {
                    mBinding.etCode3.isEnabled = true
                    mBinding.etCode3.requestFocus()
                }
                2 -> {
                    mBinding.etCode4.isEnabled = true
                    mBinding.etCode4.requestFocus()
                }
                3 -> {
                    //全部填完
                    hideInput()
                }
            }
        } else {
            when (currentPosition) {
                1 -> mBinding.etCode1.requestFocus()
                2 -> mBinding.etCode2.requestFocus()
                3 -> mBinding.etCode3.requestFocus()
            }
        }
    }

    private fun hideInput() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val v = window?.peekDecorView()
        if (v != null) {
            imm.hideSoftInputFromWindow(v.windowToken, 0)
            mBinding.etCode1.clearFocus()
            mBinding.etCode2.clearFocus()
            mBinding.etCode3.clearFocus()
            mBinding.etCode4.clearFocus()
        }
    }

    /**
     * 监听每一个输入框输状态
     */
    private val textWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            oldInput = charSequence.toString()
        }
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun afterTextChanged(editable: Editable) {
            if (editable.isNotEmpty()) {
                if (editable.length > 1) {
                    //检查是否要替换当前输入内容
                    val newInput = if (editable[0].toString() == oldInput) {
                        editable[1].toString()
                    } else {
                        editable[0].toString()
                    }
                    when (currentPosition) {
                        0 -> mBinding.etCode1.setText(newInput)
                        1 -> mBinding.etCode2.setText(newInput)
                        2 -> mBinding.etCode3.setText(newInput)
                        3 -> mBinding.etCode4.setText(newInput)
                    }
                } else {
                    //寻焦
                    findNextFocus(true)
                }
            }
        }
    }
}