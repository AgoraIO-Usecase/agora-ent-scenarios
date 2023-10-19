package com.agora.entfulldemo.home.mine

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import com.agora.entfulldemo.R
import com.agora.entfulldemo.databinding.AppActivityFeedbackBinding
import com.agora.entfulldemo.databinding.AppItemFeedbackReasonBinding
import com.agora.entfulldemo.home.MainViewModel
import com.agora.entfulldemo.home.constructor.FeedbackModel
import com.agora.entfulldemo.widget.dp
import com.alibaba.android.arouter.facade.annotation.Route
import io.agora.scene.base.GlideApp
import io.agora.scene.base.PagePathConstant
import io.agora.scene.base.component.BaseRecyclerViewAdapter
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.base.component.OnFastClickListener
import io.agora.scene.base.component.OnItemClickListener
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.base.utils.UriUtils
import io.agora.scene.widget.utils.CenterCropRoundCornerTransform
import io.agora.scene.widget.utils.ImageCompressUtil
import io.agora.scene.widget.utils.UiUtils
import java.io.File

@Route(path = PagePathConstant.pageFeedback)
class FeedbackActivity : BaseViewBindingActivity<AppActivityFeedbackBinding>() {

    companion object {
        private const val servicePhone = "400-632-6626"
        private const val CHOOSE_PHOTO1 = 1001
        private const val CHOOSE_PHOTO2 = 1002
        private const val CHOOSE_PHOTO3 = 1003
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

    private val mReasonAdapter: BaseRecyclerViewAdapter<AppItemFeedbackReasonBinding, FeedbackModel, FeedbackHolder>
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
                    FeedbackHolder::class.java
                )
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
    }

    override fun initListener() {
        binding.etFeedbackReason.doAfterTextChanged {
            val length = it?.length ?: 0
            binding.tvReasonContentCount.text = "$length/200"
        }
        binding.ivFeedback1.setOnClickListener(object : OnFastClickListener() {
            override fun onClickJacking(view: View) {
                openAlbum()
            }
        })
        binding.ivFeedback2.setOnClickListener(object : OnFastClickListener() {
            override fun onClickJacking(view: View) {
                openAlbum()
            }
        })
        binding.ivFeedback3.setOnClickListener(object : OnFastClickListener() {
            override fun onClickJacking(view: View) {
                openAlbum()
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
                Log.d("zhangw","reasons:$selectReasons\n content:$reasonContent\n images:$mTempImageList\n " +
                        "uploadLog:$uploadLog")
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

    private fun checkReason(): List<FeedbackModel> {
        val selectReasons = mutableListOf<FeedbackModel>()
        mFeedbackReasons.forEach {
            if (it.isSelect) {
                selectReasons.add(it)
            }
        }
        return selectReasons
    }

    private fun notifyReasonItemChanged(position: Int) {
        mReasonAdapter.notifyItemChanged(position)
    }

    fun openAlbum() {
        val intentToPickPic = Intent(Intent.ACTION_PICK, null)
        intentToPickPic.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intentToPickPic.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        startActivityForResult(intentToPickPic, CHOOSE_PHOTO1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CHOOSE_PHOTO1) {
                val uri = data?.data ?: return
                val filePath = UriUtils.INSTANCE.getFilePathByUri(this, uri)
                if (!TextUtils.isEmpty(filePath)) {
                    setImage(filePath)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun setImage(filePath: String?) {
        if (filePath == null) return
        var path = ImageCompressUtil.displayPath(this, filePath)
        if (TextUtils.isEmpty(path) || File(filePath).length() <= 1024 * 1000 * 5) {
            path = filePath
        } else {
            ToastUtils.showToast(R.string.app_feedback_image_limit)
            return
        }
        addReasonImage(path)
    }

    private fun addReasonImage(path: String) {
        mTempImageList.add(path)
        if (mTempImageList.size == 1) {
            binding.ivFeedback2.isVisible = true
            GlideApp.with(this)
                .load(path)
                .transform(CenterCropRoundCornerTransform(12.dp.toInt()))
                .into(binding.ivFeedback1)
        } else if (mTempImageList.size == 2) {
            binding.ivFeedback3.isVisible = true
            GlideApp.with(this)
                .load(path)
                .transform(CenterCropRoundCornerTransform(12.dp.toInt()))
                .into(binding.ivFeedback2)
        } else if (mTempImageList.size == 3) {
            GlideApp.with(this)
                .load(path)
                .transform(CenterCropRoundCornerTransform(12.dp.toInt()))
                .into(binding.ivFeedback3)
        }
    }

    private fun feedbackSuccessView() {
        binding.groupFeedback.isVisible = false
        binding.layoutFeedbackSuccess.isVisible = true
        binding.tvServiceNumber.text = servicePhone
    }
}

class FeedbackHolder constructor(mBinding: AppItemFeedbackReasonBinding) :
    BaseRecyclerViewAdapter.BaseViewHolder<AppItemFeedbackReasonBinding, FeedbackModel>(mBinding) {

    override fun binding(feedbackModel: FeedbackModel?, selectedIndex: Int) {
        feedbackModel ?: return
        val context = mBinding.root.context
        if (feedbackModel.isSelect) {
            mBinding.tvTabTitle.setTextColor(ResourcesCompat.getColor(context.resources, R.color.white, null))
            mBinding.tvTabTitle.setBackgroundResource(R.drawable.app_bg_button_303_solid_r8)
        } else {
            mBinding.tvTabTitle.setTextColor(
                ResourcesCompat.getColor(
                    context.resources,
                    R.color.def_text_grey_303,
                    null
                )
            )
            mBinding.tvTabTitle.setBackgroundResource(R.drawable.app_bg_button_e9e_solid_r8)
        }
        mBinding.tvTabTitle.text = feedbackModel.reason
    }
}