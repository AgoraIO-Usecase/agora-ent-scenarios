package com.agora.entfulldemo.home.mine

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.os.HandlerThread
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.agora.entfulldemo.R
import com.agora.entfulldemo.databinding.AppActivityFeedbackBinding
import com.agora.entfulldemo.databinding.AppItemFeedbackImageBinding
import com.agora.entfulldemo.databinding.AppItemFeedbackReasonBinding
import com.agora.entfulldemo.home.constructor.FeedbackModel
import io.agora.scene.base.utils.dp
import com.agora.entfulldemo.widget.image.GlideEngine
import com.agora.entfulldemo.widget.image.ImageFileCompressEngine
import com.agora.entfulldemo.widget.image.MeOnPreviewInterceptListener
import com.alibaba.android.arouter.facade.annotation.Route
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.luck.picture.lib.basic.IBridgeViewLifecycle
import com.luck.picture.lib.basic.PictureSelectionModel
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.config.SelectModeConfig
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnExternalPreviewEventListener
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.luck.picture.lib.style.PictureSelectorStyle
import io.agora.scene.base.GlideApp
import io.agora.scene.base.PagePathConstant
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.base.component.OnFastClickListener
import io.agora.scene.base.utils.FileUtils
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.base.utils.ZipUtils
import io.agora.scene.base.utils.ZipUtils.ZipCallback
import io.agora.scene.widget.dialog.PermissionLeakDialog
import java.io.File
import java.util.Collections

@Route(path = PagePathConstant.pageFeedback)
class FeedbackActivity : BaseViewBindingActivity<AppActivityFeedbackBinding>() {

    companion object {
        private const val servicePhone = "400-632-6626"
        private const val maxImageSelectable = 3
        private val logFolder = AgoraApplication.the().getExternalFilesDir("")!!.absolutePath
        private val logFileWriteThread by lazy {
            HandlerThread("AgoraFeedback.$logFolder").apply {
                start()
            }
        }
        private const val rtcSdkPrefix = "agorasdk"
        private const val rtcApiPrefix = "agoraapi"
        private const val rtmSdkPrefix = "agorartmsdk"
        private const val commonBaseMiddle = "commonbase"
        private const val commonUIMiddle = "commonui"
    }

    private val mFeedbackViewModel: FeedbackViewModel by lazy {
        ViewModelProvider(this)[FeedbackViewModel::class.java]
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


    private val mReasonAdapter: FeedbackReasonAdapter by lazy {
        FeedbackReasonAdapter(this, mFeedbackReasons)
    }

    private val mFeedbackImages: MutableList<String> = mutableListOf()

    private val mImageAdapter: FeedbackImageAdapter by lazy {
        FeedbackImageAdapter(this, mFeedbackImages,
            mOnItemClick = { path, position ->
                Log.d("zhangw", "mOnItemClick :$path,position:$position")
                startPreviewImage(position)
            },
            mOnItemLongClick = { path, position, holder ->
                Log.d("zhangw", "mOnItemLongClick :$path,position:$position")
//                mItemTouchHelper.startDrag(holder)
            })
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
        // 绑定拖拽事件
//        mItemTouchHelper.attachToRecyclerView(binding.rvFeedbackImage)
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

                checkReason()
                if (mSelectReasons.isEmpty()) {
                    ToastUtils.showToast(R.string.app_feedback_reason_empty_tips)
                    return
                }
                val reasonContent = binding.etFeedbackReason.text
                val uploadLog = binding.cvUploadLog.isChecked
                // TODO: feedback api
                Log.d(
                    "zhangw", "reasons:$mSelectReasons\n" +
                            " content:$reasonContent\n " +
                            "images:$mFeedbackImages\n " +
                            "uploadLog:$uploadLog"
                )

                mUploadImageUrls.clear()
                showLoadingView()
                uploadImages()
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
                    intent.data = uri
                    startActivity(intent)
                }
                dialog.show(supportFragmentManager, "CallPhoneDialog")
            }
        })
    }

    private val mSelectReasons: MutableList<String> by lazy {
        mutableListOf()
    }
    private val mUploadImageUrls: MutableList<String> by lazy {
        mutableListOf()
    }
    private var mUploadLogUrl: String = ""

    // 1. upload images
    private fun uploadImages() {
        val imagePath = mFeedbackImages.removeFirstOrNull()
        if (imagePath.isNullOrEmpty()) {
            uploadLog()
        } else {
            val file = File(imagePath)
            mFeedbackViewModel.updatePhoto(file, completion = { error, url ->
                if (error == null) { //success
                    mUploadImageUrls.add(url)
                    uploadImages()
                } else {
                    uploadImages()
                    Log.e("tag", "${error.message}")
                }
            })
        }
    }

    // 2. upload sdk log
    private fun uploadLog() {
        mUploadLogUrl = ""
        val uploadLog = binding.cvUploadLog.isChecked
        if (uploadLog) {
            val sdkLogZipPath = logFolder + File.separator + "agoraSdkLog.zip"

            val sdkPaths = getAgoraSDKPaths()
            val scenePaths = getScenePaths()
            val logPaths = mutableListOf<String>().apply {
                addAll(sdkPaths)
                addAll(scenePaths)
            }
            ZipUtils.compressFiles(logPaths, sdkLogZipPath, object : ZipCallback {
                override fun onFileZipped(destinationFilePath: String) {
                    mFeedbackViewModel.requestUploadLog(File(destinationFilePath), completion = { error, url ->
                        if (error == null) { // success
                            mUploadLogUrl = url
                            Log.d("zhangw","upload log success: $mUploadLogUrl")
                        } else {
                            Log.e("zhangw", "upload log failed:${error.message}")
                        }
                        FileUtils.deleteFile(sdkLogZipPath)
                        requestFeedbackApi()
                    })
                }

                override fun onError(e: java.lang.Exception?) {
                    requestFeedbackApi()
                }
            })
        } else {
            requestFeedbackApi()
        }
    }

    private fun getAgoraSDKPaths(): List<String> {
        val paths = mutableListOf<String>()
        File(logFolder).listFiles()?.forEach { file ->
            if (file.isFile) {
                if (file.name.startsWith(rtcSdkPrefix) ||
                    file.name.startsWith(rtcApiPrefix) ||
                    file.name.startsWith(rtmSdkPrefix)
                ) {
                    paths.add(file.path)
                }
            }
        }
        return paths
    }

    private fun getScenePaths(): List<String> {
        val paths = mutableListOf<String>()
        File(logFolder + File.separator + "ent").listFiles()?.forEach { file ->
            if (file.isFile) {
                if (!file.name.contains(commonBaseMiddle) &&
                    !file.name.contains(commonUIMiddle)
                ) {
                    paths.add(file.path)
                }
            }
        }
        return paths
    }

    // 3. request feedback api
    private fun requestFeedbackApi() {
        val screenshotURLS = mutableMapOf<String, String>()
        if (mUploadImageUrls.isNotEmpty()) {
            for (i in 0 until mUploadImageUrls.size) {
                screenshotURLS["${i + 1}"] = mUploadImageUrls[i]
            }
        }
        val tags = mSelectReasons.toTypedArray()
        val description = binding.etFeedbackReason.text.toString()
        mFeedbackViewModel.requestFeedbackUpload(screenshotURLS, tags, description, mUploadLogUrl,
            completion = { error, feedbackRes ->
                if (error == null) {
                    feedbackSuccessView()
                }
                hideLoadingView()
            })
    }

    private val mSelectorStyle = PictureSelectorStyle()
    private val mSelectedMedia = ArrayList<LocalMedia>()
    private val mOnPreviewInterceptListener = MeOnPreviewInterceptListener()
    private val mImageEngine: GlideEngine by lazy {
        GlideEngine.createGlideEngine()
    }

    private fun startChooseImage() {
        val systemGalleryMode: PictureSelectionModel = PictureSelector.create(this)
            .openGallery(SelectMimeType.TYPE_IMAGE)
            .setSelectorUIStyle(mSelectorStyle)
            .setSelectionMode(SelectModeConfig.MULTIPLE)
            .isPreviewFullScreenMode(true)
            .setMaxSelectNum(maxImageSelectable)
            .setSelectedData(mSelectedMedia)
            .isMaxSelectEnabledMask(true)
            .isDisplayCamera(false)
            .setImageEngine(mImageEngine)
            .setCompressEngine(ImageFileCompressEngine())
//            .setCropEngine(null)
            .setSkipCropMimeType(*arrayOf<String>(PictureMimeType.ofGIF(), PictureMimeType.ofWEBP()))
            .isOriginalControl(false)
            .setPreviewInterceptListener(mOnPreviewInterceptListener)
            .isPreviewZoomEffect(true)
//            .setSandboxFileEngine(MeSandboxFileEngine())
        systemGalleryMode.forResult(object : OnResultCallbackListener<LocalMedia> {
            override fun onResult(result: ArrayList<LocalMedia>) {
                Log.d("zhangw", "startChooseImage onResult:$result")
                mSelectedMedia.clear()
                mSelectedMedia.addAll(result)
                mFeedbackImages.clear()
                mFeedbackImages.addAll(result.map { it.realPath })
                mImageAdapter.notifyDataSetChanged()
                binding.ivAddFeedbackImage.isVisible = mFeedbackImages.size < maxImageSelectable
            }

            override fun onCancel() {
                Log.d("zhangw", "startChooseImage onCancel")
            }
        })
    }

    private fun startPreviewImage(position: Int) {
        // 预览图片
        PictureSelector.create(this)
            .openPreview()
            .setImageEngine(mImageEngine)
            .setSelectorUIStyle(mSelectorStyle)
            .isPreviewFullScreenMode(true)
//            .isPreviewZoomEffect(true, binding.rvFeedbackImage)
            .setAttachViewLifecycle(object : IBridgeViewLifecycle {
                override fun onViewCreated(fragment: Fragment, view: View, savedInstanceState: Bundle?) {

                }

                override fun onDestroy(fragment: Fragment) {

                }
            })
            .setExternalPreviewEventListener(MyExternalPreviewEventListener())
            .startActivityPreview(position, true, mSelectedMedia)
    }

    override fun getPermissions() {
        super.getPermissions()
        startChooseImage()
    }

    override fun onPermissionDined(permission: String?) {
        super.onPermissionDined(permission)
        PermissionLeakDialog(this).show(permission, null) { launchAppSetting(permission) }
    }

    private fun checkReason(): List<String> {
        mSelectReasons.clear()
        mFeedbackReasons.forEach {
            if (it.isSelect) {
                mSelectReasons.add(it.reason)
            }
        }
        return mSelectReasons
    }

    private fun feedbackSuccessView() {
        binding.groupFeedback.isVisible = false
        binding.layoutFeedbackSuccess.isVisible = true
        binding.tvServiceNumber.text = servicePhone
    }

    private inner class MyExternalPreviewEventListener : OnExternalPreviewEventListener {
        override fun onPreviewDelete(position: Int) {
            val imagePath = mFeedbackImages[position]
            mFeedbackImages.removeAt(position)
            mImageAdapter.notifyItemRemoved(position)
            mSelectedMedia.removeIf { it.realPath == imagePath }
            binding.ivAddFeedbackImage.isVisible = mFeedbackImages.size < maxImageSelectable
        }

        override fun onLongPressDownload(context: Context, media: LocalMedia): Boolean {
            return false
        }
    }

    private var mNeedScaleBig = true
    private var mNeedScaleSmall = false
    private val mItemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.Callback() {
        override fun isLongPressDragEnabled(): Boolean {
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
            viewHolder.itemView.alpha = 0.7f
            return makeMovementFlags(
                ItemTouchHelper.DOWN or ItemTouchHelper.UP
                        or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT, 0
            )
        }

        override fun onMove(
            recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder
        ): Boolean {
            try {
                //得到item原来的position
                val fromPosition = viewHolder.absoluteAdapterPosition
                //得到目标position
                val toPosition = target.absoluteAdapterPosition
                val itemViewType = target.itemViewType
                if (fromPosition < toPosition) {
                    for (i in fromPosition until toPosition) {
                        Collections.swap(mImageAdapter.getData(), i, i + 1)
                    }
                } else {
                    for (i in fromPosition downTo toPosition + 1) {
                        Collections.swap(mImageAdapter.getData(), i, i - 1)
                    }
                }
                mImageAdapter.notifyItemMoved(fromPosition, toPosition)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return true
        }

        override fun onChildDraw(
            c: Canvas, recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder, dx: Float, dy: Float, actionState: Int, isCurrentlyActive: Boolean
        ) {
            if (mNeedScaleBig) {
                mNeedScaleBig = false
                val animatorSet = AnimatorSet()
                animatorSet.playTogether(
                    ObjectAnimator.ofFloat(viewHolder.itemView, "scaleX", 1.0f, 1.2f),
                    ObjectAnimator.ofFloat(viewHolder.itemView, "scaleY", 1.0f, 1.2f)
                )
                animatorSet.duration = 50
                animatorSet.interpolator = LinearInterpolator()
                animatorSet.start()
                animatorSet.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        mNeedScaleSmall = true
                    }
                })
            }
            super.onChildDraw(c, recyclerView, viewHolder, dx, dy, actionState, isCurrentlyActive)
        }

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {

            super.onSelectedChanged(viewHolder, actionState)
        }

        override fun getAnimationDuration(
            recyclerView: RecyclerView,
            animationType: Int,
            animateDx: Float,
            animateDy: Float
        ): Long {
            return super.getAnimationDuration(recyclerView, animationType, animateDx, animateDy)
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            viewHolder.itemView.alpha = 1.0f
            if (mNeedScaleSmall) {
                mNeedScaleSmall = false
                val animatorSet = AnimatorSet()
                animatorSet.playTogether(
                    ObjectAnimator.ofFloat(viewHolder.itemView, "scaleX", 1.2f, 1.0f),
                    ObjectAnimator.ofFloat(viewHolder.itemView, "scaleY", 1.2f, 1.0f)
                )
                animatorSet.interpolator = LinearInterpolator()
                animatorSet.duration = 50
                animatorSet.start()
                animatorSet.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        mNeedScaleBig = true
                    }
                })
            }
            super.clearView(recyclerView, viewHolder)
            mImageAdapter.notifyItemChanged(viewHolder.absoluteAdapterPosition)
        }
    })
}

class FeedbackReasonAdapter constructor(
    private val mContext: Context,
    private val mFeedbackModels: List<FeedbackModel>,
) :
    RecyclerView.Adapter<FeedbackReasonAdapter.FeedbackReasonViewHolder>() {

    inner class FeedbackReasonViewHolder(val binding: AppItemFeedbackReasonBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedbackReasonViewHolder {
        return FeedbackReasonViewHolder(
            AppItemFeedbackReasonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return mFeedbackModels.size
    }

    override fun onBindViewHolder(holder: FeedbackReasonViewHolder, position: Int) {
        val feedbackModel = mFeedbackModels[position]
        if (feedbackModel.isSelect) {
            holder.binding.tvTabTitle.setTextColor(ResourcesCompat.getColor(mContext.resources, R.color.white, null))
            holder.binding.tvTabTitle.setBackgroundResource(R.drawable.app_bg_button_303_solid_r8)
        } else {
            holder.binding.tvTabTitle.setTextColor(
                ResourcesCompat.getColor(mContext.resources, R.color.def_text_grey_303, null)
            )
            holder.binding.tvTabTitle.setBackgroundResource(R.drawable.app_bg_button_e9e_solid_r8)
        }
        holder.binding.tvTabTitle.text = feedbackModel.reason
        holder.binding.root.setOnClickListener(object : OnFastClickListener() {
            override fun onClickJacking(view: View) {
                feedbackModel.isSelect = !feedbackModel.isSelect
                notifyItemChanged(holder.absoluteAdapterPosition)
            }
        })
    }
}


class FeedbackImageAdapter constructor(
    private val mContext: Context,
    private val mFeedbackImages: List<String>,
    private val mOnItemClick: ((String, Int) -> Unit),
    private val mOnItemLongClick: ((String, Int, FeedbackImageViewHolder) -> Unit),
) :
    RecyclerView.Adapter<FeedbackImageAdapter.FeedbackImageViewHolder>() {

    inner class FeedbackImageViewHolder(val binding: AppItemFeedbackImageBinding) :
        RecyclerView.ViewHolder(binding.root)

    fun getData(): List<String> {
        return mFeedbackImages
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedbackImageViewHolder {
        return FeedbackImageViewHolder(
            AppItemFeedbackImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return mFeedbackImages.size
    }

    override fun onBindViewHolder(holder: FeedbackImageViewHolder, position: Int) {
        val imagePath = mFeedbackImages[position]
        if (imagePath.isEmpty()) {
            holder.binding.ivImage.setImageResource(0)
        } else {
            GlideApp.with(mContext)
                .load(imagePath)
                .transform(RoundedCorners(12.dp.toInt()))
                .into(holder.binding.ivImage)
        }
        holder.binding.root.setOnClickListener {
            mOnItemClick.invoke(imagePath, holder.absoluteAdapterPosition)
        }
        holder.binding.root.setOnLongClickListener {
            mOnItemLongClick.invoke(imagePath, holder.absoluteAdapterPosition, holder)
            true
        }
    }
}