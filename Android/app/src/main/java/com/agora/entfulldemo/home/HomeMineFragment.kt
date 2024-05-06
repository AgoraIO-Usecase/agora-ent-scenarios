package com.agora.entfulldemo.home

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputFilter
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.agora.entfulldemo.databinding.AppFragmentHomeMineBinding
import com.agora.entfulldemo.home.constructor.URLStatics
import com.agora.entfulldemo.home.mine.AppDebugActivity
import com.bumptech.glide.request.RequestOptions
import io.agora.scene.base.BuildConfig
import io.agora.scene.base.Constant
import io.agora.scene.base.GlideApp
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.base.component.BaseViewBindingFragment
import io.agora.scene.base.component.OnButtonClickListener
import io.agora.scene.base.component.OnFastClickListener
import io.agora.scene.base.manager.PagePilotManager
import io.agora.scene.base.manager.UserManager
import io.agora.scene.base.utils.FileUtils
import io.agora.scene.base.utils.UriUtils
import io.agora.scene.widget.dialog.SelectPhotoFromDialog
import io.agora.scene.widget.utils.ImageCompressUtil
import java.io.File

class HomeMineFragment : BaseViewBindingFragment<AppFragmentHomeMineBinding>() {

    companion object {
        private const val CHOOSE_PHOTO = 100
        private const val TAKE_PHOTO = 101
    }

    private val mainViewModel: MainViewModel by lazy {
        ViewModelProvider(this)[MainViewModel::class.java]
    }

    private var selectPhotoFromDialog: SelectPhotoFromDialog? = null
    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): AppFragmentHomeMineBinding {
        return AppFragmentHomeMineBinding.inflate(inflater)
    }

    //防止多次回调
    private var lastKeyBoard = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(view) { v: View?, insets: WindowInsetsCompat ->
            val inset = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPaddingRelative(inset.left, inset.top, inset.right, 0)
            WindowInsetsCompat.CONSUMED
        }
        activity?.window?.let { window ->
            // 获取根布局可见区域的高度
            val initialWindowHeight = Rect().apply { window.decorView.getWindowVisibleDisplayFrame(this) }.height()
            view.viewTreeObserver.addOnGlobalLayoutListener {
                val tempWindow = activity?.window ?: return@addOnGlobalLayoutListener
                val currentWindowHeight =
                    Rect().apply { tempWindow.decorView.getWindowVisibleDisplayFrame(this) }.height()
                // 判断键盘高度来确定键盘的显示状态
                if (currentWindowHeight < initialWindowHeight) {
                    if (lastKeyBoard) return@addOnGlobalLayoutListener
                    lastKeyBoard = true
                    binding.etNickname.selectAll()
                    // 软键盘可见
                    Log.d("zhangw", "current: $currentWindowHeight, initial: $initialWindowHeight, show: true")
                    binding.ivEditNickname.isVisible = false

                } else {
                    if (!lastKeyBoard) return@addOnGlobalLayoutListener
                    lastKeyBoard = false
                    // 软键盘已收起
                    Log.d("zhangw", "current: $currentWindowHeight, initial: $initialWindowHeight, show: false")
                    binding.ivEditNickname.isVisible = true
                    binding.etNickname.clearComposingText()
                    binding.etNickname.clearFocus()
                    val newName = binding.etNickname.text.toString()
                    if (newName.isEmpty()) {
                        binding.etNickname.setText(UserManager.getInstance().user.name)
                    } else if (newName != UserManager.getInstance().user.name) {
                        mainViewModel.requestEditUserInfo(null, newName, null)
                    }
                }
            }
        }
    }

    override fun initView() {
        mainViewModel.setLifecycleOwner(this)
        mainViewModel.setISingleCallback { type, data ->
            if (type == Constant.CALLBACK_TYPE_USER_INFO_CHANGE || type == Constant.CALLBACK_TYPE_REQUEST_USER_INFO) {
                val user = UserManager.getInstance().user
                GlideApp.with(this)
                    .load(user.headUrl)
                    .placeholder(io.agora.scene.widget.R.mipmap.default_user_avatar)
                    .error(io.agora.scene.widget.R.mipmap.default_user_avatar)
                    .apply(RequestOptions.circleCropTransform())
                    .into(binding.ivUserAvatar)
                binding.tvUserPhone.text = hidePhoneNumber(user.mobile)
                binding.etNickname.setText(user.name)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.tvDebugMode.isVisible = AgoraApplication.the().isDebugModeOpen
    }

    @SuppressLint("SetTextI18n")
    override fun initListener() {
        binding.tvMineAccount.setOnClickListener(object : OnFastClickListener() {
            override fun onClickJacking(view: View) {
                PagePilotManager.pageMineAccount()
            }
        })
        binding.tvUserAgreement.setOnClickListener(object : OnFastClickListener() {
            override fun onClickJacking(view: View) {
                PagePilotManager.pageWebView(URLStatics.userAgreementURL)
            }
        })
        binding.tvPrivacyAgreement.setOnClickListener(object : OnFastClickListener() {
            override fun onClickJacking(view: View) {
                PagePilotManager.pageWebView(URLStatics.privacyAgreementURL)
            }
        })
        binding.tvCollectionChecklist.setOnClickListener(object : OnFastClickListener() {
            override fun onClickJacking(view: View) {
                val stringBuilder =
                    StringBuilder(URLStatics.collectionChecklistURL)
                        .append("?userNo=").append(UserManager.getInstance().user.userNo)
                        .append("&appId=").append(BuildConfig.AGORA_APP_ID)
                        .append("&projectId=")
                        .append("agora_ent_demo") //.append("&sceneId=").append("-1")
                        .append("&token=").append(UserManager.getInstance().user.token)
                PagePilotManager.pageWebView(stringBuilder.toString())
            }
        })
        binding.tvThirdDataSharing.setOnClickListener(object : OnFastClickListener() {

            override fun onClickJacking(view: View) {
                PagePilotManager.pageWebView(URLStatics.thirdDataSharingURL)
            }
        })
        binding.tvAbout.setOnClickListener(object : OnFastClickListener() {
            override fun onClickJacking(view: View) {
                PagePilotManager.pageAboutUs()
            }
        })
        binding.tvFeedback.setOnClickListener(object : OnFastClickListener() {
            override fun onClickJacking(view: View) {
                PagePilotManager.pageFeedback()
            }
        })
        binding.etNickname.setOnFocusChangeListener { v, hasFocus ->
            Log.d("zhangw", "etNickname setOnFocusChangeListener hasFocus $hasFocus")
        }
        binding.ivEditNickname.setOnClickListener {
            binding.etNickname.requestFocus()
            showKeyboard(binding.etNickname)
        }
        binding.ivUserAvatar.setOnClickListener { view: View? ->
            (requireActivity() as MainActivity).requestReadStoragePermission(
                true
            )
        }
        binding.tvDebugMode.setOnClickListener { v: View? ->
            activity?.let { activity->
                AppDebugActivity.startActivity(activity)
            }
        }
        binding.tvDebugMode.isVisible = AgoraApplication.the().isDebugModeOpen
        binding.etNickname.filters = arrayOf(mInputFilter)
    }

    private var mTempPhotoPath: String? = null
    private fun showSelectPhotoFromDialog() {
        val cxt = context ?: return
        if (selectPhotoFromDialog == null) {

            selectPhotoFromDialog = SelectPhotoFromDialog(cxt)
            selectPhotoFromDialog?.onButtonClickListener = object : OnButtonClickListener {
                override fun onLeftButtonClick() {
                    openAlbum()
                }

                override fun onRightButtonClick() {
                    takePhoto()
                }
            }
        }
        selectPhotoFromDialog?.show()
    }

    private fun hidePhoneNumber(phoneNumber: String): String {
        val prefix = phoneNumber.substring(0, 3)
        val suffix = phoneNumber.substring(7)
        return "$prefix****$suffix"
    }

    fun openAlbum() {
        val intentToPickPic = Intent(Intent.ACTION_PICK, null)
        intentToPickPic.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intentToPickPic.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        startActivityForResult(intentToPickPic, CHOOSE_PHOTO)
    }

    private fun takePhoto() {
        val cxt = context ?: return
        val intentToTakePhoto = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intentToTakePhoto.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val fileDir = File(
            FileUtils.getTempSDPath()
        )
        if (!fileDir.exists()) {
            fileDir.mkdirs()
        }
        val photoFile = File(fileDir, "photo.jpg")
        mTempPhotoPath = photoFile.absolutePath
        val imageUri = FileProvider.getUriForFile(
            cxt,
            com.agora.entfulldemo.BuildConfig.APPLICATION_ID + ".fileProvider",
            photoFile
        )
        intentToTakePhoto.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(intentToTakePhoto, TAKE_PHOTO)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CHOOSE_PHOTO) {
                val uri = data?.data ?: return
                val cxt = context ?: return
                val filePath = UriUtils.INSTANCE.getFilePathByUri(cxt, uri)
                if (!TextUtils.isEmpty(filePath)) {
                    setImage(filePath)
                }
            } else if (requestCode == TAKE_PHOTO) {
                setImage(mTempPhotoPath)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun setImage(filePath: String?) {
        if (filePath == null) return
        var path = ImageCompressUtil.displayPath(requireActivity(), filePath)
        if (TextUtils.isEmpty(path) || File(filePath).length() <= 150000) {
            path = filePath
        }
        mTempPhotoPath = path
        //        UserManager.getInstance().getUser().headUrl = mTempPhotoPath;
        mainViewModel.updatePhoto(File(mTempPhotoPath))
    }

    override fun requestData() {
        mainViewModel.requestUserInfo(UserManager.getInstance().user.userNo, true)
    }

    // 昵称限制 10 个字符
    private val maxLen = 10
    private val mInputFilter = InputFilter { source, start, end, dest, dstart, dend ->
        var dindex = 0
        var count = 0
        while (count <= maxLen && dindex < dest.length) {
            val c = dest[dindex++]
            count = if (c.code < 128) {
                count + 1
            } else {
                count + 2
            }
        }
        if (count > maxLen) {
            return@InputFilter dest.subSequence(0, dindex - 1)
        }
        var sindex = 0
        while (count <= maxLen && sindex < source.length) {
            val c = source[sindex++]
            count = if (c.code < 128) {
                count + 1
            } else {
                count + 2
            }
        }
        if (count > maxLen) {
            sindex--
        }
        source.subSequence(0, sindex)
    }
}