package io.agora.scene.show.widget.beauty

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.agora.rtc2.video.SegmentationProperty
import io.agora.rtc2.video.VirtualBackgroundSource
import io.agora.scene.base.utils.FileUtils
import io.agora.scene.show.R
import io.agora.scene.show.RtcEngineInstance
import io.agora.scene.show.beauty.BeautyManager
import io.agora.scene.show.databinding.ShowWidgetBeautyMultiDialogBinding
import io.agora.scene.show.databinding.ShowWidgetBeautyMultiDialogVirtualBgBinding
import io.agora.scene.widget.utils.StatusBarUtil

class MultiBeautyDialog : BottomSheetDialog {

    private val mBinding by lazy {
        ShowWidgetBeautyMultiDialogBinding.inflate(LayoutInflater.from(context))
    }

    constructor(context: Context) : this(context, R.style.show_bottom_dialog)

    constructor(context: Context, theme: Int) : super(context, theme) {
        super.setContentView(mBinding.root)
        initView()
    }

    override fun onStart() {
        super.onStart()
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        StatusBarUtil.hideStatusBar(window, false)
    }


    private fun initView() {
        when (BeautyManager.beautyType) {
            BeautyManager.BeautyType.SenseTime -> {
                mBinding.ctvBeauty.setText(R.string.show_multi_beauty_sensetime)
                mBinding.rgBeauty.check(R.id.rbSenseTime)
            }

            BeautyManager.BeautyType.FaceUnity -> {
                mBinding.ctvBeauty.setText(R.string.show_multi_beauty_faceunity)
                mBinding.rgBeauty.check(R.id.rbFaceUnity)
            }

            BeautyManager.BeautyType.ByteDance -> {
                mBinding.ctvBeauty.setText(R.string.show_multi_beauty_bytedance)
                mBinding.rgBeauty.check(R.id.rbByteDance)
            }

            BeautyManager.BeautyType.Agora -> {
                mBinding.ctvBeauty.setText(R.string.show_multi_beauty_agora)
                mBinding.rgBeauty.check(R.id.rbAgora)
            }
        }

        mBinding.ctvBeauty.setOnClickListener {
            mBinding.ctvBeauty.isChecked = !mBinding.ctvBeauty.isChecked
            if (mBinding.ctvBeauty.isChecked) {
                mBinding.ctvBeauty.setText(R.string.show_multi_beauty_factory)
                mBinding.rgBeauty.isVisible = true
            } else {
                when (BeautyManager.beautyType) {
                    BeautyManager.BeautyType.SenseTime -> mBinding.ctvBeauty.setText(R.string.show_multi_beauty_sensetime)
                    BeautyManager.BeautyType.FaceUnity -> mBinding.ctvBeauty.setText(R.string.show_multi_beauty_faceunity)
                    BeautyManager.BeautyType.ByteDance -> mBinding.ctvBeauty.setText(R.string.show_multi_beauty_bytedance)
                    BeautyManager.BeautyType.Agora -> mBinding.ctvBeauty.setText(R.string.show_multi_beauty_agora)
                }
                mBinding.rgBeauty.isVisible = false
            }
        }
        mBinding.rgBeauty.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbSenseTime -> BeautyManager.beautyType = BeautyManager.BeautyType.SenseTime
                R.id.rbFaceUnity -> BeautyManager.beautyType = BeautyManager.BeautyType.FaceUnity
                R.id.rbByteDance -> BeautyManager.beautyType = BeautyManager.BeautyType.ByteDance
                R.id.rbAgora -> BeautyManager.beautyType = BeautyManager.BeautyType.Agora
            }
            // resetVirtualBackground()
            mBinding.ctvBeauty.performClick()
            updateControllerView(BeautyManager.beautyType)
        }
        updateControllerView(BeautyManager.beautyType)
    }



    private fun updateControllerView(beautyType: BeautyManager.BeautyType) {
        mBinding.controllerContainer.removeAllViews()
        val controllerView = when (beautyType) {
            BeautyManager.BeautyType.SenseTime -> SenseTimeControllerView(context)
            BeautyManager.BeautyType.FaceUnity -> FaceUnityControllerView(context)
            BeautyManager.BeautyType.ByteDance -> ByteDanceControllerView(context)
            BeautyManager.BeautyType.Agora -> AgoraControllerView(context)
        }
        setupControllerView(controllerView)
        mBinding.controllerContainer.addView(controllerView)
    }

    private fun setupControllerView(controllerView: BaseControllerView) {
        // 美颜开关
        controllerView.beautyOpenClickListener =
            OnClickListener { BeautyManager.enable = !BeautyManager.enable }

        // 虚拟背景配置
        controllerView.pageList = ArrayList(controllerView.pageList).apply {
            add(
                BaseControllerView.PageInfo(
                    R.string.show_beauty_group_virtual_bg,
                    listOf(
                        BaseControllerView.ItemInfo(
                            R.string.show_beauty_item_none,
                            R.mipmap.show_beauty_ic_none,
                            isSelected = RtcEngineInstance.virtualBackgroundSource.backgroundSourceType == VirtualBackgroundSource.BACKGROUND_COLOR,
                            onValueChanged = { _ ->
                                RtcEngineInstance.virtualBackgroundSource.backgroundSourceType =
                                    VirtualBackgroundSource.BACKGROUND_COLOR
                                RtcEngineInstance.rtcEngine.enableVirtualBackground(
                                    false,
                                    RtcEngineInstance.virtualBackgroundSource,
                                    RtcEngineInstance.virtualBackgroundSegmentation
                                )
                            }
                        ),
                        BaseControllerView.ItemInfo(
                            R.string.show_beauty_item_virtual_bg_blur,
                            R.mipmap.show_beauty_ic_virtual_bg_blur,
                            value = RtcEngineInstance.virtualBackgroundSegmentation.greenCapacity,
                            isSelected = RtcEngineInstance.virtualBackgroundSource.backgroundSourceType == VirtualBackgroundSource.BACKGROUND_BLUR,
                            onValueChanged = { value ->
                                RtcEngineInstance.virtualBackgroundSource.backgroundSourceType =
                                    VirtualBackgroundSource.BACKGROUND_BLUR
                                RtcEngineInstance.virtualBackgroundSegmentation.greenCapacity =
                                    value
                                RtcEngineInstance.rtcEngine.enableVirtualBackground(
                                    true,
                                    RtcEngineInstance.virtualBackgroundSource,
                                    RtcEngineInstance.virtualBackgroundSegmentation
                                )
                            }
                        ),
                        BaseControllerView.ItemInfo(
                            R.string.show_beauty_item_virtual_bg_mitao,
                            R.mipmap.show_beauty_ic_virtual_bg_mitao,
                            value = RtcEngineInstance.virtualBackgroundSegmentation.greenCapacity,
                            isSelected = RtcEngineInstance.virtualBackgroundSource.backgroundSourceType == VirtualBackgroundSource.BACKGROUND_IMG,
                            onValueChanged = { value ->
                                RtcEngineInstance.virtualBackgroundSource.backgroundSourceType =
                                    VirtualBackgroundSource.BACKGROUND_IMG
                                RtcEngineInstance.virtualBackgroundSource.source =
                                    FileUtils.copyFileFromAssets(
                                        context,
                                        "virtualbackgroud_mitao.jpg",
                                        context.externalCacheDir!!.absolutePath
                                    )
                                RtcEngineInstance.virtualBackgroundSegmentation.greenCapacity =
                                    value
                                RtcEngineInstance.rtcEngine.enableVirtualBackground(
                                    true,
                                    RtcEngineInstance.virtualBackgroundSource,
                                    RtcEngineInstance.virtualBackgroundSegmentation
                                )
                            }
                        )
                    )
                )
            )
        }

        val virtualBgBinding =
            ShowWidgetBeautyMultiDialogVirtualBgBinding.inflate(LayoutInflater.from(context))
        controllerView.viewBinding.topCustomView.addView(virtualBgBinding.root)
        virtualBgBinding.mSwitchMaterial.isChecked =
            RtcEngineInstance.virtualBackgroundSegmentation.modelType == SegmentationProperty.SEG_MODEL_GREEN
        virtualBgBinding.mSwitchMaterial.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AlertDialog.Builder(context, R.style.show_alert_dialog).apply {
                    setCancelable(false)
                    setTitle(R.string.show_tip)
                    setMessage(R.string.show_beauty_green_screen_tip)
                    setPositiveButton(R.string.show_setting_confirm) { dialog, _ ->
                        controllerView.viewBinding.slider.visibility = View.VISIBLE
                        changeVirtualBGMode(SegmentationProperty.SEG_MODEL_GREEN)
                        dialog.dismiss()
                    }
                    setNegativeButton(R.string.show_setting_cancel) { dialog, _ ->
                        virtualBgBinding.mSwitchMaterial.isChecked = false
                        dialog.dismiss()
                    }
                }.create().show()
                return@setOnCheckedChangeListener
            } else {
                controllerView.viewBinding.slider.visibility = View.INVISIBLE
                changeVirtualBGMode(SegmentationProperty.SEG_MODEL_AI)
            }
        }

        controllerView.onSelectedChangeListener = { pageIndex, itemIndex ->
            val pageInfo = controllerView.pageList[pageIndex]
            val itemInfo = pageInfo.itemList[itemIndex]
            if (pageInfo.name == R.string.show_beauty_group_virtual_bg) {
                controllerView.viewBinding.ivCompare.isVisible = false
                if (itemInfo.name == R.string.show_beauty_item_none) {
                    controllerView.viewBinding.topCustomView.isVisible = false
                    controllerView.viewBinding.slider.visibility = View.INVISIBLE
                } else {
                    controllerView.viewBinding.topCustomView.isVisible = true
                    controllerView.viewBinding.slider.visibility = if(virtualBgBinding.mSwitchMaterial.isChecked) View.VISIBLE else View.INVISIBLE
                }
            } else {
                controllerView.viewBinding.topCustomView.isVisible = false
                controllerView.viewBinding.ivCompare.isVisible = true
            }
        }
    }

    private fun changeVirtualBGMode(modelType: Int) {
        RtcEngineInstance.virtualBackgroundSegmentation.modelType = modelType
        RtcEngineInstance.rtcEngine.enableVirtualBackground(
            true,
            RtcEngineInstance.virtualBackgroundSource,
            RtcEngineInstance.virtualBackgroundSegmentation
        )
    }
}