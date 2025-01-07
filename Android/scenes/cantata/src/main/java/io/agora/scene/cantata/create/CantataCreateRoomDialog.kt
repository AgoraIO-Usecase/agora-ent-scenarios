package io.agora.scene.cantata.create

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
import io.agora.scene.base.SceneConfigManager
import io.agora.scene.base.component.BaseBottomSheetDialogFragment
import io.agora.scene.base.manager.UserManager
import io.agora.scene.cantata.databinding.CantataDialogCreateRoomBinding
import io.agora.scene.cantata.R
import io.agora.scene.cantata.service.CreateRoomOutputModel
import io.agora.scene.cantata.service.JoinRoomOutputModel
import io.agora.scene.cantata.live.RoomLivingActivity
import io.agora.scene.widget.toast.CustomToast
import java.util.*

/**
 * Create Room Dialog
 */
class CantataCreateRoomDialog constructor(
    private val context: Context,
) : BaseBottomSheetDialogFragment<CantataDialogCreateRoomBinding>() {

    private lateinit var roomCreateViewModel: RoomCreateViewModel

    private var window: Window? = null
    private var loadingView: View? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        window = dialog.window
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        roomCreateViewModel = ViewModelProvider(this)[RoomCreateViewModel::class.java]
        // User prompt color
        val spannableString = SpannableString(getString(R.string.cantata_create_room_tips))
        spannableString.setSpan(
            ForegroundColorSpan(Color.parseColor("#FA396A")),
            77,
            118,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        mBinding.tvNotice.text = spannableString
        // Random name
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

        roomCreateViewModel.joinRoomResult.observe(this) { out: JoinRoomOutputModel? ->
            hideLoadingView()
            if (out != null) {
                dismiss()
                RoomLivingActivity.launch(context, out)
            } else {
                // Failed to join room
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
            resources.getStringArray(R.array.cantata_roomName)[Random().nextInt(21)]
        )
    }

    private fun createRoom() {
        val roomName = mBinding.etRoomName.text.toString()
        if (TextUtils.isEmpty(roomName)) {
            CustomToast.show(R.string.cantata_please_input_room_name)
            return
        }
        val isPrivate = mBinding.cbPassword.isChecked
        val password = mBinding.etCode.text.toString()
        if (isPrivate && password.length < 4) {
            CustomToast.show(getString(R.string.cantata_please_input_4_pwd))
            return
        }
        val userNo = UserManager.getInstance().user.id.toString()
        val numPrivate = if (isPrivate) 1 else 0
        showLoadingView()
        val delayType = if (mBinding.rbtDelay.isChecked) {
            1
        } else {
            if (mBinding.rbtTopN.isChecked) {
                2
            } else {
                3
            }
        }
        SceneConfigManager.fetchSceneConfig({
            roomCreateViewModel.createRoom(numPrivate, roomName, password, userNo, "1", delayType)
        }, {
            CustomToast.show(getString(R.string.cantata_enter_room_tips))
        })
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
            this.loadingView = LayoutInflater.from(context).inflate(io.agora.scene.base.R.layout.view_base_loading, rootView, false)
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