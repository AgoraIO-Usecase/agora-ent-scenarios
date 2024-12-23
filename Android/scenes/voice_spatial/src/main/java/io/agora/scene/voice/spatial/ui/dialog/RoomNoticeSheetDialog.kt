package io.agora.scene.voice.spatial.ui.dialog

import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.view.View
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import io.agora.scene.base.component.BaseBottomSheetDialogFragment
import io.agora.scene.voice.spatial.VoiceSpatialLogger
import io.agora.scene.voice.spatial.databinding.VoiceSpatialDialogRoomNoticeBinding
import io.agora.scene.voice.spatial.model.RoomKitBean
import java.util.regex.Pattern

/**
 * @author create by zhangwei03
 *
 * 公告
 */
class RoomNoticeSheetDialog constructor() :
    BaseBottomSheetDialogFragment<VoiceSpatialDialogRoomNoticeBinding>() {

    companion object {
        const val TAG = "RoomNoticeSheetDialog"
        const val KEY_ROOM_KIT_BEAN = "room_kit_bean"
    }

    private val roomKitBean: RoomKitBean by lazy {
        arguments?.getSerializable(KEY_ROOM_KIT_BEAN) as RoomKitBean
    }

    var confirmCallback: ((str: String) -> Unit)? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding?.apply {
            mbEdit.isInvisible = !roomKitBean.isOwner
            mtContent.text = contentText
            etInput.setText(contentText)
            val filters = arrayOf<InputFilter>(NameLengthFilter())
            mbEdit.filters = filters
            mbEdit.setOnClickListener {
                mbEdit.isInvisible = true
                mtCancel.isVisible = true
                mbConfirm.isVisible = true
                textInputLayout.isVisible = true
                mtContent.isVisible = false


                etInput.isFocusable = true;
                etInput.isFocusableInTouchMode = true;
                etInput.requestFocus()
                etInput.setSelection(etInput.text?.length?:0)
                showKeyboard(etInput)
            }
            mtCancel.setOnClickListener {
                mbEdit.isInvisible = false
                mtCancel.isVisible = false
                mbConfirm.isVisible = false
                textInputLayout.isVisible = false
                mtContent.isVisible = true
                hideKeyboard(etInput)
            }
            mbConfirm.setOnClickListener {
                confirmCallback?.invoke(etInput.text.toString().trim())
                dismiss()
                hideKeyboard(etInput)
            }
        }
    }

    private var contentText: String = ""

    fun contentText(contentText: String) = apply {
        this.contentText = contentText
    }

    inner class NameLengthFilter constructor(private val maxEn: Int = 280) : InputFilter {
        private val regEx = "[\\u4e00-\\u9fa5]"
        override fun filter(
            source: CharSequence, start: Int, end: Int,
            dest: Spanned, dstart: Int, dend: Int
        ): CharSequence {
            val destCount = (dest.toString().length + getChineseCount(dest.toString()))
            val sourceCount = (source.toString().length + getChineseCount(source.toString()))
            VoiceSpatialLogger.d(TAG,"NameLengthFilter $destCount $sourceCount")
            return if (destCount + sourceCount > maxEn) {
                ""
            } else {
                source
            }
        }

        private fun getChineseCount(str: String): Int {
            var count = 0
            val p = Pattern.compile(regEx)
            val m = p.matcher(str)
            while (m.find()) {
                for (i in 0..m.groupCount()) {
                    count += 1
                }
            }
            return count
        }
    }
}