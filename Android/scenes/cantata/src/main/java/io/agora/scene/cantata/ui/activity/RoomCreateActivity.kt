package io.agora.scene.cantata.ui.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import androidx.appcompat.widget.AppCompatEditText
import androidx.lifecycle.ViewModelProvider
import io.agora.scene.base.GlideApp
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.base.manager.UserManager
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.cantata.R
import io.agora.scene.cantata.databinding.CantataActivityRoomCreateBinding
import io.agora.scene.cantata.service.CreateRoomOutputModel
import io.agora.scene.cantata.service.JoinRoomOutputModel
import io.agora.scene.cantata.ui.viewmodel.RoomCreateViewModel
import io.agora.scene.widget.utils.CenterCropRoundCornerTransform
import java.util.Random

class RoomCreateActivity : BaseViewBindingActivity<CantataActivityRoomCreateBinding>() {

    companion object{
        fun launch(context: Context){
            context.startActivity(Intent(context, RoomCreateActivity::class.java))
        }
    }

    private val roomCreateViewModel by lazy {
        ViewModelProvider(this)[RoomCreateViewModel::class.java]
    }

    /**
     * 当前选中的是第几个输入框
     */
    private var currentPosition = 0

    private var bgOption: String = ""

    private var positionCover = 1

    override fun getViewBinding(inflater: LayoutInflater): CantataActivityRoomCreateBinding {
        return CantataActivityRoomCreateBinding.inflate(inflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        setRandomRoomTitleAndCover()
        setOnApplyWindowInsetsListener(binding.superLayout)
    }

    override fun initListener() {
        super.initListener()
        binding.ivRoomCover.setOnClickListener { view -> setCover() }
        binding.superLayout.setOnClickListener { view -> hideInput() }
        roomCreateViewModel.joinRoomResult.observe(this) { out: JoinRoomOutputModel? ->
                if (out != null) {
                    RoomLivingActivity.launch(this@RoomCreateActivity, out)
                    finish()
                    hideLoadingView()
                } else {
                    hideLoadingView()
                }
            }
        roomCreateViewModel.createRoomResult.observe(this) { out: CreateRoomOutputModel? ->
                if (out != null) {
                    roomCreateViewModel.joinRoom(out.roomNo, out.password)
                } else {
                    hideLoadingView()
                    binding.btnCreate.isEnabled = true
                }
            }
        binding.cbOpen.setOnCheckedChangeListener { compoundButton, check ->
            if (check) {
                binding.cbUnOpen.isChecked = false
                binding.etCode1.visibility = View.GONE
                binding.etCode2.visibility = View.GONE
                binding.etCode3.visibility = View.GONE
                binding.etCode4.visibility = View.GONE
                binding.tvSet.visibility = View.GONE
            }
        }
        binding.cbUnOpen.setOnCheckedChangeListener { compoundButton, check ->
            if (check) {
                binding.cbOpen.isChecked = false
                binding.etCode1.visibility = View.VISIBLE
                binding.etCode2.visibility = View.VISIBLE
                binding.etCode3.visibility = View.VISIBLE
                binding.etCode4.visibility = View.VISIBLE
                binding.tvSet.visibility = View.VISIBLE
            }
        }
        binding.etCode1.onFocusChangeListener = editFocusListener
        binding.etCode2.onFocusChangeListener = editFocusListener
        binding.etCode3.onFocusChangeListener = editFocusListener
        binding.etCode4.onFocusChangeListener = editFocusListener

        binding.etCode1.setOnKeyListener(onKeyListener)
        binding.etCode2.setOnKeyListener(onKeyListener)
        binding.etCode3.setOnKeyListener(onKeyListener)
        binding.etCode4.setOnKeyListener(onKeyListener)

        binding.etCode1.addTextChangedListener(textWatcher)
        binding.etCode2.addTextChangedListener(textWatcher)
        binding.etCode3.addTextChangedListener(textWatcher)
        binding.etCode4.addTextChangedListener(textWatcher)

        binding.btnCreate.setOnClickListener { view ->
            if (binding.cbUnOpen.isChecked && !isAllInput()) {
                ToastUtils.showToast(getString(R.string.cantata_please_input_4_pwd))
            } else {
                val code = (binding.etCode1.text.toString()
                        + binding.etCode2.text
                        + binding.etCode3.text
                        + binding.etCode4.text)
                if (TextUtils.isEmpty(binding.etRoomName.text)) {
                    ToastUtils.showToast(getString(R.string.cantata_please_input_room_name))
                } else if (code.length > 4) {
                    ToastUtils.showToast(getString(R.string.cantata_please_input_4_pwd))
                } else {
                    binding.btnCreate.isEnabled = false
                    createRoom(code)
                }
            }
        }
        binding.btnRandom.setOnClickListener { view -> setRandomRoomTitleAndCover() }
    }

    private fun setRandomRoomTitleAndCover() {
        binding.etRoomName.setText(resources.getStringArray(R.array.cantata_roomName)[Random().nextInt(21)])
        setCover()
    }


    private fun setCover() {
        if (positionCover > 9) {
            positionCover = 1
        }
        bgOption = positionCover++.toString()
        GlideApp.with(binding.ivRoomCover.context).load(getCoverRes(bgOption))
            .transform(CenterCropRoundCornerTransform(40))
            .into(binding.ivRoomCover)
    }

    private fun getCoverRes(cover: String): Int {
        return when (cover) {
            "1" -> R.mipmap.icon_room_cover1
            "2" -> R.mipmap.icon_room_cover2
            "3" -> R.mipmap.icon_room_cover3
            "4" -> R.mipmap.icon_room_cover4
            "5" -> R.mipmap.icon_room_cover5
            "6" -> R.mipmap.icon_room_cover6
            "7" -> R.mipmap.icon_room_cover7
            "8" -> R.mipmap.icon_room_cover8
            "9" -> R.mipmap.icon_room_cover9
            else -> R.mipmap.icon_room_cover1
        }
    }


    /**
     * 创建房间流程
     *
     * @param password 密码
     */
    private fun createRoom(password: String) {
        showLoadingView()
        val roomName = binding.etRoomName.text.toString()
        if (TextUtils.isEmpty(roomName)) {
            ToastUtils.showToast(R.string.cantata_please_input_room_name)
            return
        }
        val userNo = UserManager.getInstance().user.id.toString()
        val isPrivate: Int = if (TextUtils.isEmpty(password)) {
            0
        } else {
            1
        }
        roomCreateViewModel.createRoom(isPrivate, roomName, password, userNo, bgOption)
    }

    /**
     * 记录当前焦点位置
     */
    @SuppressLint("NonConstantResourceId")
    private val editFocusListener =
        OnFocusChangeListener { view: View, hasFocus: Boolean ->
            if (hasFocus) {
                when (view.id) {
                    R.id.etCode1 -> currentPosition = 0
                    R.id.etCode2 -> currentPosition = 1
                    R.id.etCode3 -> currentPosition = 2
                    R.id.etCode4 -> currentPosition = 3
                }
            }
        }

    /**
     * 删除前一个输入内
     */
    private val onKeyListener = View.OnKeyListener { v: View?, keyCode: Int, event: KeyEvent ->
        if (event.action == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_DEL) {
                if (v is AppCompatEditText) {
                    if (v.text.isNullOrEmpty()) {
                        findNextFocus(false)
                    }
                    v.setText("")
                }
                return@OnKeyListener true
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
                    binding.etCode2.isEnabled = true
                    binding.etCode2.requestFocus()
                }

                1 -> {
                    binding.etCode3.isEnabled = true
                    binding.etCode3.requestFocus()
                }

                2 -> {
                    binding.etCode4.isEnabled = true
                    binding.etCode4.requestFocus()
                }

                3 -> {

                    //全部填完
                    hideInput()
                }
            }
        } else {
            when (currentPosition) {
                1 -> binding.etCode1.requestFocus()
                2 -> binding.etCode2.requestFocus()
                3 -> binding.etCode3.requestFocus()
            }
        }
    }

    /**
     * 输入历史记录
     */
    private var oldInput = ""

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
                    val newInput: String = if (editable[0].toString() == oldInput) {
                        editable[1].toString()
                    } else {
                        editable[0].toString()
                    }
                    when (currentPosition) {
                        0 -> binding.etCode1.setText(newInput)
                        1 -> binding.etCode2.setText(newInput)
                        2 -> binding.etCode3.setText(newInput)
                        3 -> binding.etCode4.setText(newInput)
                    }
                } else {
                    //寻焦
                    findNextFocus(true)
                }
            }
        }
    }

    /**
     * 检查是否已输入完毕
     */
    private fun isAllInput(): Boolean {
        if (TextUtils.isEmpty(binding.etCode1.text)) {
            return false
        }
        if (TextUtils.isEmpty(binding.etCode2.text)) {
            return false
        }
        return if (TextUtils.isEmpty(binding.etCode3.text)) {
            false
        } else !TextUtils.isEmpty(binding.etCode4.text)
    }
}