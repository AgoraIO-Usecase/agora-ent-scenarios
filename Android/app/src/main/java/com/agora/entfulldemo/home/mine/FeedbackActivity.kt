package com.agora.entfulldemo.home.mine

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import com.agora.entfulldemo.R
import com.agora.entfulldemo.databinding.AppActivityFeedbackBinding
import com.agora.entfulldemo.databinding.AppItemFeedbackImageBinding
import com.agora.entfulldemo.databinding.AppItemFeedbackReasonBinding
import com.agora.entfulldemo.home.MainViewModel
import com.agora.entfulldemo.home.constructor.FeedbackModel
import com.agora.entfulldemo.home.mine.FeedbackActivity.Companion.DeleteImage
import com.agora.entfulldemo.widget.dp
import com.agora.entfulldemo.widget.image.GlideEngine
import com.agora.entfulldemo.widget.image.ImageFileCompressEngine
import com.agora.entfulldemo.widget.image.MeOnPreviewInterceptListener
import com.alibaba.android.arouter.facade.annotation.Route
import com.luck.picture.lib.basic.PictureSelectionModel
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.config.SelectModeConfig
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnPreviewInterceptListener
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.luck.picture.lib.style.PictureSelectorStyle
import io.agora.scene.base.GlideApp
import io.agora.scene.base.PagePathConstant
import io.agora.scene.base.component.BaseRecyclerViewAdapter
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.base.component.OnFastClickListener
import io.agora.scene.base.component.OnItemChildClickListener
import io.agora.scene.base.component.OnItemClickListener
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.widget.utils.CenterCropRoundCornerTransform
import io.agora.scene.widget.utils.UiUtils

@Route(path = PagePathConstant.pageFeedback)
class FeedbackActivity : BaseViewBindingActivity<AppActivityFeedbackBinding>() {

    companion object {
        private const val servicePhone = "400-632-6626"
        private const val maxImageSelectable = 3
        const val DeleteImage = "DeleteImage"
    }

    private val mainViewModel: MainViewModel by lazy {
        ViewModelProvider(this)[MainViewModel::class.java]
    }

    private val mFeedbackReasons: MutableList<FeedbackModel> by lazy {
        mutableListOf(
            FeedbackModel(getString(R.string.app_feedback_reason_crash), false),
            FeedbackModel(getString(R.string.app_feedback_reason_audio_lag), false),
            FeedbackModel(getString(R.string.app_feedback_reason_video_lag), false),
            FeedbackModel(getString(R.string.app_feedback_reason_communication_exception), false),
            FeedbackModel(getString(R.string.app_feedback_reason_functional_construction), true),
            FeedbackModel(getString(R.string.app_feedback_reason_other), false)
        )
    }

    private val mTempImageList by lazy { mutableListOf<String>() }

//    private var mFileProvider = com.agora.entfulldemo.BuildConfig.APPLICATION_ID + ".fileProvider"

    private val mReasonAdapter: BaseRecyclerViewAdapter<AppItemFeedbackReasonBinding, FeedbackModel, FeedbackReasonHolder>
            by lazy {
                BaseRecyclerViewAdapter(
                    mFeedbackReasons,
                    object : OnItemClickListener<FeedbackModel> {

                        override fun onItemClick(
                            feedbackModel: FeedbackModel, view: View, position: Int, viewType: Long
                        ) {
                            if (UiUtils.isFastClick(500)) return
                            feedbackModel.isSelect = !feedbackModel.isSelect
                            notifyReasonItemChanged(position)
                        }
                    },
                    FeedbackReasonHolder::class.java
                )
            }

    private fun notifyReasonItemChanged(position: Int) {
        mReasonAdapter.notifyItemChanged(position)
    }

    private val mImageAdapter: BaseRecyclerViewAdapter<AppItemFeedbackImageBinding, String, FeedbackImageHolder>
            by lazy {
                BaseRecyclerViewAdapter(
                    emptyList<String>(),
                    null,
                    object : OnItemChildClickListener<String> {
                        override fun onItemChildClick(
                            data: String?,
                            extData: Any?,
                            view: View,
                            position: Int,
                            itemViewType: Long
                        ) {
                            super.onItemChildClick(data, extData, view, position, itemViewType)
                            if (view.id == R.id.ivDelete) {
                                Log.d("zhangw", "onItemChildClick delete:$view,position:$position")
                                dealDeleteImage(position)
                            }

                        }
                    },
                    FeedbackImageHolder::class.java
                )
            }

    private fun dealDeleteImage(position: Int) {
        mSelectedMedia.removeAt(position)

        val selectPaths = mSelectedMedia.map { it.realPath }
        mImageAdapter.replaceItems(selectPaths)
        binding.ivAddFeedbackImage.isVisible = mImageAdapter.dataList.size < maxImageSelectable
    }

    override fun getViewBinding(inflater: LayoutInflater): AppActivityFeedbackBinding {
        return AppActivityFeedbackBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setOnApplyWindowInsetsListener(binding.root)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.rvFeedbackReason.adapter = mReasonAdapter
        binding.rvFeedbackImage.adapter = mImageAdapter
    }

    override fun initListener() {
        binding.etFeedbackReason.doAfterTextChanged {
            val length = it?.length ?: 0
            binding.tvReasonContentCount.text = "$length/200"
        }
        binding.ivAddFeedbackImage.setOnClickListener(object : OnFastClickListener() {
            override fun onClickJacking(view: View) {
                requestReadStoragePermission(true)
            }
        })
        binding.btnSubmit.setOnClickListener(object : OnFastClickListener() {
            override fun onClickJacking(view: View) {
                val selectReasons = checkReason()
                if (selectReasons.isEmpty()) {
                    ToastUtils.showToast(R.string.app_feedback_reason_empty_tips)
                    return
                }
                val reasonContent = binding.etFeedbackReason.text
                val uploadLog = binding.cvIAgree.isChecked
                // TODO: feedback api
                Log.d(
                    "zhangw", "reasons:$selectReasons\n content:$reasonContent\n images:$mTempImageList\n " +
                            "uploadLog:$uploadLog"
                )
                feedbackSuccessView()
            }
        })
        binding.tvServiceNumber.setOnClickListener(object : OnFastClickListener() {
            override fun onClickJacking(view: View) {
                val dialog = CallPhoneDialog().apply {
                    arguments = Bundle().apply {
                        putString(CallPhoneDialog.KEY_PHONE, servicePhone)
                        putString(
                            CallPhoneDialog.KEY_TITLE,
                            this@FeedbackActivity.getString(R.string.app_feedback_contact_the_customer_service)
                        )
                    }
                }
                dialog.onClickCallPhone = {
                    val intent = Intent(Intent.ACTION_DIAL)
                    val uri = Uri.parse("tel:$servicePhone")
                    intent.setData(uri)
                    startActivity(intent)
                }
                dialog.show(supportFragmentManager, "CallPhoneDialog")
            }
        })
    }


    private val mSelectorStyle = PictureSelectorStyle()
    private val mSelectedMedia = mutableListOf<LocalMedia>()
    private val mOnPreviewInterceptListener = MeOnPreviewInterceptListener()
    private fun startChooseImage() {
        val systemGalleryMode: PictureSelectionModel = PictureSelector.create(this)
            .openGallery(SelectMimeType.TYPE_IMAGE)
            .setSelectorUIStyle(mSelectorStyle)
            .setSelectionMode(SelectModeConfig.MULTIPLE)
            .setMaxSelectNum(maxImageSelectable)
            .setSelectedData(mSelectedMedia)
            .isMaxSelectEnabledMask(true)
            .isDisplayCamera(false)
            .setImageEngine(GlideEngine.createGlideEngine())
            .setCompressEngine(ImageFileCompressEngine())
//            .setCropEngine(null)
            .setSkipCropMimeType(*arrayOf<String>(PictureMimeType.ofGIF(), PictureMimeType.ofWEBP()))
            .isOriginalControl(false)
            .setPreviewInterceptListener(mOnPreviewInterceptListener)
//            .setSandboxFileEngine(MeSandboxFileEngine())
        systemGalleryMode.forResult(object : OnResultCallbackListener<LocalMedia> {
            override fun onResult(result: ArrayList<LocalMedia>) {
                Log.d("zhangw", "startChooseImage onResult:$result")
                mSelectedMedia.clear()
                mSelectedMedia.addAll(result)
                val selectPaths = result.map { it.realPath }
                mImageAdapter.replaceItems(selectPaths)
                binding.ivAddFeedbackImage.isVisible = mImageAdapter.dataList.size < maxImageSelectable
            }

            override fun onCancel() {
                Log.d("zhangw", "startChooseImage onCancel")
            }
        })
    }

    override fun getPermissions() {
        super.getPermissions()
        startChooseImage()
    }

    private fun checkReason(): List<FeedbackModel> {
        val selectReasons = mutableListOf<FeedbackModel>()
        mFeedbackReasons.forEach {
            if (it.isSelect) {
                selectReasons.add(it)
            }
        }
        return selectReasons
    }

    private fun feedbackSuccessView() {
        binding.groupFeedback.isVisible = false
        binding.layoutFeedbackSuccess.isVisible = true
        binding.tvServiceNumber.text = servicePhone
    }
}

class FeedbackReasonHolder constructor(mBinding: AppItemFeedbackReasonBinding) :
    BaseRecyclerViewAdapter.BaseViewHolder<AppItemFeedbackReasonBinding, FeedbackModel>(mBinding) {

    override fun binding(feedbackModel: FeedbackModel?, selectedIndex: Int) {
        feedbackModel ?: return
        val context = mBinding.root.context
        if (feedbackModel.isSelect) {
            mBinding.tvTabTitle.setTextColor(ResourcesCompat.getColor(context.resources, R.color.white, null))
            mBinding.tvTabTitle.setBackgroundResource(R.drawable.app_bg_button_303_solid_r8)
        } else {
            mBinding.tvTabTitle.setTextColor(
                ResourcesCompat.getColor(context.resources, R.color.def_text_grey_303, null)
            )
            mBinding.tvTabTitle.setBackgroundResource(R.drawable.app_bg_button_e9e_solid_r8)
        }
        mBinding.tvTabTitle.text = feedbackModel.reason
    }
}

class FeedbackImageHolder constructor(mBinding: AppItemFeedbackImageBinding) :
    BaseRecyclerViewAdapter.BaseViewHolder<AppItemFeedbackImageBinding, String>(mBinding) {

    override fun binding(imagePath: String?, selectedIndex: Int) {
        if (imagePath.isNullOrEmpty()) {
            mBinding.ivImage.setImageResource(0)
        } else {
            GlideApp.with(mBinding.root.context)
                .load(imagePath)
                .transform(CenterCropRoundCornerTransform(12.dp.toInt()))
                .into(mBinding.ivImage)
        }
        mBinding.ivDelete.setOnClickListener {
            onItemChildClick(DeleteImage, it)
        }
    }
}