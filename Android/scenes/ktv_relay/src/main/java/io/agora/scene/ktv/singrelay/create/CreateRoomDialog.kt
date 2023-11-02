package io.agora.scene.ktv.singrelay.create

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
import io.agora.scene.base.manager.UserManager
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.ktv.singrelay.R
import io.agora.scene.ktv.singrelay.databinding.KtvRelayDialogCreateRoomBinding
import io.agora.scene.ktv.singrelay.live.RoomLivingActivity
import io.agora.scene.ktv.singrelay.service.CreateRoomOutputModel
import io.agora.scene.ktv.singrelay.service.JoinRoomOutputModel
import java.util.*

class CreateRoomDialog(
    private val context: Context,
): BaseBottomSheetDialogFragment<KtvRelayDialogCreateRoomBinding>() {

    /** 当前选中的是第几个输入框*/
    private var currentPosition = 0

    private lateinit var roomCreateViewModel: RoomCreateViewModel

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
        roomCreateViewModel = ViewModelProvider(this)[RoomCreateViewModel::class.java]
        // 用户提示颜色
        val spannableString = SpannableString(getString(R.string.ktv_relay_create_room_tips))
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
                    if (mBinding == null) { return@postDelayed }
                    val currentWindowHeight = Rect().apply { window.decorView.getWindowVisibleDisplayFrame(this) }.height()
                    if (currentWindowHeight < initialWindowHeight) {
                    } else {
                        mBinding.etCode4.clearFocus()
                        mBinding.etRoomName.clearFocus()
                    }
                }, 300)
            }
        }

        roomCreateViewModel.joinRoomResult.observe(this) { out: JoinRoomOutputModel? ->
            hideLoadingView()
            if (out != null) {
                dismiss()
                RoomLivingActivity.launch(context, out)
            } else {
                // 加入房间失败
            }
        }
        roomCreateViewModel.createRoomResult.observe(this) { out: CreateRoomOutputModel? ->
            if (out != null) {
                roomCreateViewModel.joinRoom(out.roomNo, out.password)
            } else {
                hideLoadingView()
                mBinding.btnCreateRoom.isEnabled = true
            }
        }

    }

    private fun randomName() {
        mBinding.etRoomName.setText(
            resources.getStringArray(R.array.ktv_relay_roomName)[Random().nextInt(21)]
        )
    }

    private fun createRoom() {
        val roomName = mBinding.etRoomName.text.toString()
        if (TextUtils.isEmpty(roomName)) {
            ToastUtils.showToast(R.string.ktv_relay_please_input_room_name)
            return
        }
        val isPrivate = mBinding.cbPassword.isChecked
        val password = (mBinding.etCode1.text.toString()
                + mBinding.etCode2.text
                + mBinding.etCode3.text
                + mBinding.etCode4.text)
        if (isPrivate && password.length < 4) {
            ToastUtils.showToast(getString(R.string.ktv_relay_please_input_4_pwd))
            return
        }
        val userNo = UserManager.getInstance().user.id.toString()
        val numPrivate = if (isPrivate) 1 else 0
        showLoadingView()
        roomCreateViewModel.createRoom(numPrivate, roomName, password, userNo, "1")
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