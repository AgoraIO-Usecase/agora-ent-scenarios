package io.agora.scene.show.widget

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.DrawableRes
import androidx.annotation.IntDef
import androidx.annotation.StringRes
import io.agora.scene.show.R
import io.agora.scene.show.databinding.ShowWidgetSettingDialogBinding
import io.agora.scene.show.databinding.ShowWidgetSettingDialogItemBinding
import io.agora.scene.widget.basic.BindingSingleAdapter
import io.agora.scene.widget.basic.BindingViewHolder

class SettingDialog(context: Context) : BottomDarkDialog(context) {


    companion object {
        const val ITEM_ID_CAMERA = 1
        const val ITEM_ID_VIDEO = 2
        const val ITEM_ID_MIC = 3
        const val ITEM_ID_STATISTIC = 4
        const val ITEM_ID_QUALITY = 5
        const val ITEM_ID_SETTING = 6

        @IntDef(
            ITEM_ID_CAMERA,
            ITEM_ID_VIDEO,
            ITEM_ID_MIC,
            ITEM_ID_STATISTIC,
            ITEM_ID_QUALITY,
            ITEM_ID_SETTING
        )
        @Retention(AnnotationRetention.RUNTIME)
        @Target(
            AnnotationTarget.TYPEALIAS,
            AnnotationTarget.FUNCTION,
            AnnotationTarget.PROPERTY_GETTER,
            AnnotationTarget.PROPERTY_SETTER,
            AnnotationTarget.VALUE_PARAMETER,
            AnnotationTarget.FIELD,
            AnnotationTarget.LOCAL_VARIABLE
        )
        annotation class ItemId
    }


    private data class SettingItem(
        @ItemId val itemId: Int,
        @DrawableRes val icon: Int,
        @DrawableRes val activatedIcon: Int,
        @StringRes val text: Int,
        @StringRes val activatedText: Int,
        var activated: Boolean = false
    )

    private var isHostView = false

    private val mAudienceItemList = listOf(
        SettingItem(
            ITEM_ID_STATISTIC,
            R.mipmap.show_setting_ic_statistic,
            R.mipmap.show_setting_ic_statistic,
            R.string.show_setting_statistic,
            R.string.show_setting_statistic
        ),
        SettingItem(
            ITEM_ID_SETTING,
            R.mipmap.show_setting_ic_setting,
            R.mipmap.show_setting_ic_setting,
            R.string.show_setting_advance_setting,
            R.string.show_setting_advance_setting
        )
    )

    private var isVideoActivated = true;
    private var isVoiceActivated = true;

    private val mHostItemList = listOf(
        SettingItem(
            ITEM_ID_CAMERA,
            R.mipmap.show_setting_ic_camera,
            R.mipmap.show_setting_ic_camera,
            R.string.show_setting_switch_camera,
            R.string.show_setting_switch_camera
        ),
        SettingItem(
            ITEM_ID_VIDEO,
            R.mipmap.show_setting_ic_video_off,
            R.mipmap.show_setting_ic_video_on,
            R.string.show_setting_video_off,
            R.string.show_setting_video_on,
            isVideoActivated
        ),
        SettingItem(
            ITEM_ID_MIC,
            R.mipmap.show_setting_ic_mic_off,
            R.mipmap.show_setting_ic_mic_on,
            R.string.show_setting_mic_off,
            R.string.show_setting_mic_on,
            isVoiceActivated
        ),
        SettingItem(
            ITEM_ID_STATISTIC,
            R.mipmap.show_setting_ic_statistic,
            R.mipmap.show_setting_ic_statistic,
            R.string.show_setting_statistic,
            R.string.show_setting_statistic
        ),
//        SettingItem(
//            ITEM_ID_QUALITY,
//            R.mipmap.show_setting_ic_quality,
//            R.mipmap.show_setting_ic_quality,
//            R.string.show_setting_quality,
//            R.string.show_setting_quality
//        ),
        SettingItem(
            ITEM_ID_SETTING,
            R.mipmap.show_setting_ic_setting,
            R.mipmap.show_setting_ic_setting,
            R.string.show_setting_advance_setting,
            R.string.show_setting_advance_setting
        )
    )

    private var onItemActivatedChangeListener: ((dialog: SettingDialog, itemId: Int, activated: Boolean)->Unit)? = null

    private val mBinding by lazy {
        ShowWidgetSettingDialogBinding.inflate(
            LayoutInflater.from(
                context
            )
        )
    }

    private val mAdapter by lazy {
        object : BindingSingleAdapter<SettingItem, ShowWidgetSettingDialogItemBinding>() {
            override fun onBindViewHolder(
                holder: BindingViewHolder<ShowWidgetSettingDialogItemBinding>,
                position: Int
            ) {
                val item = getItem(position) ?: return
                val activated = item.activated
                holder.binding.text.setCompoundDrawables(
                    null,
                    context.getDrawable(if (activated) item.activatedIcon else item.icon)?.apply {
                        setBounds(0, 0, intrinsicWidth, intrinsicHeight)
                    },
                    null, null,
                )
                holder.binding.text.text =
                    context.getString(if (activated) item.activatedText else item.text)
                holder.binding.text.isActivated = activated
                holder.binding.text.setOnClickListener {
                    val activate = !it.isActivated
                    if (item.itemId == ITEM_ID_VIDEO) {
                        isVideoActivated = activate
                    } else if (item.itemId == ITEM_ID_MIC) {
                        isVoiceActivated = activate
                    }
                    it.isActivated = activate
                    item.activated = activate

                    holder.binding.text.setCompoundDrawables(
                        null,
                        context.getDrawable(if (activate) item.activatedIcon else item.icon)
                            ?.apply {
                                setBounds(0, 0, intrinsicWidth, intrinsicHeight)
                            },
                        null, null,
                    )
                    holder.binding.text.text =
                        context.getString(if (activate) item.activatedText else item.text)

                    onItemActivatedChangeListener?.invoke(
                        this@SettingDialog,
                        item.itemId,
                        activate
                    )
                }
            }
        }
    }

    init {
        setBottomView(mBinding.root)
        mBinding.recycleView.adapter = mAdapter
        mAdapter.resetAll(if (isHostView) mHostItemList else mAudienceItemList)
    }

    fun setItemActivated(@ItemId itemId: Int, activate: Boolean){
        for (i in 0 .. mAdapter.itemCount){
            mAdapter.getItem(i)?.let {
                if (it.itemId == itemId) {
                    it.activated = activate
                    mAdapter.notifyItemChanged(i)
                    return
                }
            }
        }
    }


    /**
     * 观众设置按钮在连麦和非连麦时候item 个数不同
     * fix:观众连麦中关闭摄像头后结束连麦，再打开设置弹框，item 个数只有2个
     */
    fun resetItemStatus(@ItemId itemId: Int, activate: Boolean) {
        when (itemId) {
            ITEM_ID_VIDEO -> isVideoActivated = activate
            else -> {}
        }
        for (i in 0..mAdapter.itemCount) {
            mAdapter.getItem(i)?.let {
                if (it.itemId == itemId) {
                    it.activated = activate
                    mAdapter.notifyItemChanged(i)
                    return
                }
            }
        }
    }

    fun setHostView(isHost: Boolean) {
        if (isHostView == isHost) {
            return
        }
        isHostView = isHost
        mAdapter.resetAll(if (isHost) mHostItemList else mAudienceItemList)
    }

    fun isHostView() = isHostView

    fun setOnItemActivateChangedListener(listener: (dialog: SettingDialog, itemId: Int, activated: Boolean)->Unit) {
        this.onItemActivatedChangeListener = listener
    }

    fun resetSettingsItem(mute: Boolean) {
        isVoiceActivated = !mute
        val itemList = listOf(SettingItem(
            ITEM_ID_CAMERA,
            R.mipmap.show_setting_ic_camera,
            R.mipmap.show_setting_ic_camera,
            R.string.show_setting_switch_camera,
            R.string.show_setting_switch_camera
        ),
        SettingItem(
            ITEM_ID_VIDEO,
            R.mipmap.show_setting_ic_video_off,
            R.mipmap.show_setting_ic_video_on,
            R.string.show_setting_video_off,
            R.string.show_setting_video_on,
            isVideoActivated
        ),
        SettingItem(
            ITEM_ID_MIC,
            R.mipmap.show_setting_ic_mic_off,
            R.mipmap.show_setting_ic_mic_on,
            R.string.show_setting_mic_off,
            R.string.show_setting_mic_on,
            isVoiceActivated
        ),
        SettingItem(
            ITEM_ID_STATISTIC,
            R.mipmap.show_setting_ic_statistic,
            R.mipmap.show_setting_ic_statistic,
            R.string.show_setting_statistic,
            R.string.show_setting_statistic
        ),
        SettingItem(
            ITEM_ID_SETTING,
            R.mipmap.show_setting_ic_setting,
            R.mipmap.show_setting_ic_setting,
            R.string.show_setting_advance_setting,
            R.string.show_setting_advance_setting
        ))
        mAdapter.resetAll(itemList)
    }
}