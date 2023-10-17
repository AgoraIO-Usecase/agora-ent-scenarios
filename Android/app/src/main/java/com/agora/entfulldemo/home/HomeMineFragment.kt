package com.agora.entfulldemo.home

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.provider.MediaStore
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.agora.entfulldemo.R
import com.agora.entfulldemo.databinding.AppFragmentHomeMineBinding
import com.agora.entfulldemo.home.constructor.URLStatics
import com.agora.entfulldemo.widget.dp
import io.agora.scene.base.BuildConfig
import io.agora.scene.base.Constant
import io.agora.scene.base.GlideApp
import io.agora.scene.base.PagePathConstant
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.base.component.BaseViewBindingFragment
import io.agora.scene.base.component.ISingleCallback
import io.agora.scene.base.component.OnButtonClickListener
import io.agora.scene.base.component.OnFastClickListener
import io.agora.scene.base.manager.PagePilotManager
import io.agora.scene.base.manager.UserManager
import io.agora.scene.base.utils.FileUtils
import io.agora.scene.base.utils.SPUtil
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.base.utils.UriUtils
import io.agora.scene.widget.dialog.CommonDialog
import io.agora.scene.widget.dialog.EditNameDialog
import io.agora.scene.widget.dialog.SelectPhotoFromDialog
import io.agora.scene.widget.utils.CenterCropRoundCornerTransform
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
    private var editNameDialog: EditNameDialog? = null
    private var debugModeDialog: CommonDialog? = null
    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): AppFragmentHomeMineBinding {
        return AppFragmentHomeMineBinding.inflate(inflater)
    }

    override fun initView() {
        mainViewModel.setLifecycleOwner(this)
    }

    @SuppressLint("SetTextI18n")
    override fun initListener() {
        mainViewModel.iSingleCallback = ISingleCallback { type: Int, o: Any? ->
            if (type == Constant.CALLBACK_TYPE_USER_INFO_CHANGE) {
                val user = UserManager.getInstance().user
                GlideApp.with(this).load(user.headUrl).error(R.mipmap.userimage)
                    .transform(CenterCropRoundCornerTransform(999))
                    .into(binding.ivUserAvatar)
                binding.tvUserPhone.text = user.mobile
                binding.etNickname.setText(user.name)
            } else if (type == Constant.CALLBACK_TYPE_USER_CANCEL_ACCOUNTS) {
                UserManager.getInstance().logout()
                requireActivity().finish()
                PagePilotManager.pageWelcome()
            }
        }
        binding.tvMineAccount.setOnClickListener(object :OnFastClickListener(){
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
        binding.tvDataSharing.setOnClickListener(object : OnFastClickListener() {

            override fun onClickJacking(view: View) {
                PagePilotManager.pageWebView(URLStatics.dataSharingURL)
            }
        })
        binding.tvAbout.setOnClickListener(object :OnFastClickListener(){
            override fun onClickJacking(view: View) {
                PagePilotManager.pageAboutUs()
            }
        })
        binding.etNickname.setOnClickListener { view: View? ->
            val cxt = context ?: return@setOnClickListener
            if (editNameDialog == null) {
                editNameDialog = EditNameDialog(cxt)
                editNameDialog?.iSingleCallback = ISingleCallback { type: Int, o: Any? ->
                    if (type == 0) {
                        mainViewModel.requestEditUserInfo(null, o as String?, null)
                    }
                }
            }
            editNameDialog?.show()
        }
        binding.ivUserAvatar.setOnClickListener { view: View? ->
            (requireActivity() as MainActivity).requestReadStoragePermission(
                true
            )
        }
        binding.tvDebugMode.setOnClickListener { v: View? -> showDebugModeCloseDialog() }
        if (AgoraApplication.the().isDebugModeOpen) {
            binding.tvDebugMode.visibility = View.VISIBLE
        }
    }

    var mTempPhotoPath: String? = null
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
        mainViewModel.requestUserInfo(UserManager.getInstance().user.userNo)
    }



    private fun showDebugModeCloseDialog() {
        if (debugModeDialog == null) {
            debugModeDialog = CommonDialog(requireContext())
            debugModeDialog?.setDialogTitle(getString(R.string.app_exit_debug))
            debugModeDialog?.setDescText(getString(R.string.app_exit_debug_tip))
            debugModeDialog?.setDialogBtnText(getString(R.string.cancel), getString(R.string.app_exit))
            debugModeDialog?.onButtonClickListener = object : OnButtonClickListener {
                override fun onLeftButtonClick() {}
                override fun onRightButtonClick() {
                    binding.tvDebugMode.visibility = View.GONE
                    AgoraApplication.the().enableDebugMode(false)
                    ToastUtils.showToast(R.string.app_debug_off)
                }
            }
        }
        debugModeDialog?.show()
    }


}