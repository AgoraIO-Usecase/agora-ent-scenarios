package io.agora.scene.show.widget.pk

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.DrawableRes
import androidx.annotation.IntDef
import androidx.annotation.StringRes
import io.agora.scene.show.R
import io.agora.scene.show.databinding.ShowWidgetLinkSettingsDialogBinding
import io.agora.scene.show.databinding.ShowWidgetSettingDialogItemBinding
import io.agora.scene.show.widget.BottomDarkDialog
import io.agora.scene.show.widget.SettingDialog
import io.agora.scene.show.widget.link.LiveLinkAudienceSettingsDialog
import io.agora.scene.widget.basic.BindingSingleAdapter
import io.agora.scene.widget.basic.BindingViewHolder

// 连麦时点击主播View弹出
class LivePKSettingsDialog(context: Context) : BottomDarkDialog(context) {
    companion object {
        const val ITEM_ID_SWITCH_CAMERA = 1
        const val ITEM_ID_CAMERA = 2
        const val ITEM_ID_MIC = 3
        const val ITEM_ID_STOP_PK = 4

        @IntDef(
            ITEM_ID_SWITCH_CAMERA,
            ITEM_ID_CAMERA,
            ITEM_ID_MIC,
            ITEM_ID_STOP_PK
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

    private val mHostItemList = listOf(
        SettingItem(
            ITEM_ID_SWITCH_CAMERA,
            R.mipmap.show_setting_ic_camera,
            R.mipmap.show_setting_ic_camera,
            R.string.show_setting_switch_camera,
            R.string.show_setting_switch_camera
        ),
        SettingItem(
            ITEM_ID_CAMERA,
            R.mipmap.show_setting_ic_video_off,
            R.mipmap.show_setting_ic_video_on,
            R.string.show_setting_video_off,
            R.string.show_setting_video_on,
            true
        ),
        SettingItem(
            ITEM_ID_MIC,
            R.mipmap.show_setting_ic_mic_off,
            R.mipmap.show_setting_ic_mic_on,
            R.string.show_setting_mic_off,
            R.string.show_setting_mic_on,
            true
        ),
        SettingItem(
            ITEM_ID_STOP_PK,
            R.mipmap.show_stop_link,
            R.mipmap.show_stop_link,
            R.string.show_stop_linking,
            R.string.show_stop_linking
        )
    )

    private var onItemActivatedChangeListener: ((dialog: LivePKSettingsDialog, itemId: Int, activated: Boolean)->Unit)? = null

    private val mBinding by lazy {
        ShowWidgetLinkSettingsDialogBinding.inflate(
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
                    if (item.itemId == SettingDialog.ITEM_ID_VIDEO) {
                        isVideoActivated = activate
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
                        this@LivePKSettingsDialog,
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
        mAdapter.resetAll(mHostItemList)
        mBinding.userName.setText(R.string.show_pk)
    }

    private var isVideoActivated = true;
    fun resetSettingsItem(mute: Boolean) {
        val itemList = listOf(
            SettingItem(
                ITEM_ID_SWITCH_CAMERA,
                R.mipmap.show_setting_ic_camera,
                R.mipmap.show_setting_ic_camera,
                R.string.show_setting_switch_camera,
                R.string.show_setting_switch_camera
            ),
            SettingItem(
                ITEM_ID_CAMERA,
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
                !mute
            ),
            SettingItem(
                ITEM_ID_STOP_PK,
                R.mipmap.show_stop_link,
                R.mipmap.show_stop_link,
                R.string.show_stop_linking,
                R.string.show_stop_linking
            )
        )
        mAdapter.resetAll(itemList)
    }

    fun resetItemStatus(@ItemId itemId: Int, activate: Boolean) {
        when (itemId) {
            ITEM_ID_CAMERA -> isVideoActivated = activate
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

    fun setOnItemActivateChangedListener(listener: (dialog: LivePKSettingsDialog, itemId: Int, activated: Boolean)->Unit) {
        this.onItemActivatedChangeListener = listener
    }
}