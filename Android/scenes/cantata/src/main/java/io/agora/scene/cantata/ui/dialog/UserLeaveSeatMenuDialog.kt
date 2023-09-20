package io.agora.scene.cantata.ui.dialog

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import com.bumptech.glide.Glide
import io.agora.scene.base.component.BaseDialog
import io.agora.scene.cantata.R
import io.agora.scene.cantata.databinding.CantataDialogUserSeatMenuBinding
import io.agora.scene.cantata.ui.widget.OnClickJackingListener

/**
 * 房间用户菜单
 */
class UserLeaveSeatMenuDialog constructor(context: Context) : BaseDialog<CantataDialogUserSeatMenuBinding?>(context) {
    override fun getViewBinding(layoutInflater: LayoutInflater): CantataDialogUserSeatMenuBinding {
        return CantataDialogUserSeatMenuBinding.inflate(layoutInflater)
    }

    fun setAgoraMember(name: String?, headUrl: String?) {
        binding!!.tvName.text = name
        Glide.with(context)
            .load(headUrl).error(R.mipmap.userimage)
            .into(binding!!.ivUser)
    }

    public override fun initView() {
        setCanceledOnTouchOutside(true)
        window?.setWindowAnimations(R.style.popup_window_style_bottom)
        binding?.apply {
            btSeatoff.setOnClickListener(object :OnClickJackingListener(){
                override fun onClickJacking(view: View) {
                    seatOff(view)
                }
            })
            btLeaveChorus.setOnClickListener(object :OnClickJackingListener(){
                override fun onClickJacking(view: View) {
                    leaveChorus(view)
                }
            })
        }
    }

    private fun seatOff(v: View) {
        if (getOnButtonClickListener() != null) {
            dismiss()
            getOnButtonClickListener().onRightButtonClick()
        }
    }

    private fun leaveChorus(v: View) {
        if (getOnButtonClickListener() != null) {
            dismiss()
            getOnButtonClickListener().onLeftButtonClick()
        }
    }

    override fun setGravity() {
        window!!.attributes.gravity = Gravity.BOTTOM
    }
}