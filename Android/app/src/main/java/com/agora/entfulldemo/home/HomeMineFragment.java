package com.agora.entfulldemo.home;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;

import com.agora.entfulldemo.BuildConfig;
import com.agora.entfulldemo.R;
import com.agora.entfulldemo.databinding.AppFragmentHomeMineBinding;
import com.agora.entfulldemo.home.mine.AboutUsActivity;

import java.io.File;

import io.agora.rtc2.RtcEngine;
import io.agora.scene.base.Constant;
import io.agora.scene.base.GlideApp;
import io.agora.scene.base.api.model.User;
import io.agora.scene.base.component.AgoraApplication;
import io.agora.scene.base.component.BaseViewBindingFragment;
import io.agora.scene.base.component.OnButtonClickListener;
import io.agora.scene.base.manager.PagePilotManager;
import io.agora.scene.base.manager.UserManager;
import io.agora.scene.base.utils.FileUtils;
import io.agora.scene.base.utils.SPUtil;
import io.agora.scene.base.utils.ToastUtils;
import io.agora.scene.base.utils.UriUtils;
import io.agora.scene.widget.dialog.CommonDialog;
import io.agora.scene.widget.dialog.EditNameDialog;
import io.agora.scene.widget.dialog.SelectPhotoFromDialog;
import io.agora.scene.widget.utils.CenterCropRoundCornerTransform;
import io.agora.scene.widget.utils.ImageCompressUtil;

public class HomeMineFragment extends BaseViewBindingFragment<AppFragmentHomeMineBinding> {
    private CommonDialog logoutDialog;
    private CommonDialog logoffAccountDialog;
    private MainViewModel mainViewModel;
    private SelectPhotoFromDialog selectPhotoFromDialog;
    private EditNameDialog editNameDialog;

    private CommonDialog debugModeDialog;
    private int counts = 0;
    private final long debugModeOpenTime = 2000;
    private long beginTime = 0;

    @NonNull
    @Override
    protected AppFragmentHomeMineBinding getViewBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return AppFragmentHomeMineBinding.inflate(inflater);
    }


    @Override
    public void initView() {
        String versionString = "20230530-" + io.agora.scene.base.BuildConfig.APP_VERSION_NAME + "-" + RtcEngine.getSdkVersion();
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        mainViewModel.setLifecycleOwner(this);
        getBinding().tvVersion.setText(getString(R.string.app_mine_current_version, versionString));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void initListener() {
        mainViewModel.setISingleCallback((type, o) -> {
            if (type == Constant.CALLBACK_TYPE_USER_INFO_CHANGE) {
                User user = UserManager.getInstance().getUser();
                GlideApp.with(this).load(user.headUrl).error(R.mipmap.userimage)
                        .transform(new CenterCropRoundCornerTransform(100))
                        .into(getBinding().ivUserAvatar);
                getBinding().tvUserID.setText(getString(R.string.id_is_, user.userNo));
                getBinding().tvUserMobile.setText(user.name);
            } else if (type == Constant.CALLBACK_TYPE_USER_CANCEL_ACCOUNTS) {
                UserManager.getInstance().logout();
                requireActivity().finish();
                PagePilotManager.pageWelcome();
            }
        });
        getBinding().tvUserAgreement.setOnClickListener(view -> {
            PagePilotManager.pageWebView("https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/meta/demo/fulldemoStatic/privacy/service.html");
        });
        getBinding().tvPrivacyAgreement.setOnClickListener(view -> {
            PagePilotManager.pageWebView("https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/meta/demo/fulldemoStatic/privacy/privacy.html");
        });

        getBinding().tvLogout.setOnClickListener(view -> {
            showLogoutDialog();
        });
        getBinding().tvLogoffAccount.setOnClickListener(view -> {
            showLogoffAccountDialog();
        });
        getBinding().tvAbout.setOnClickListener(view -> {
            startActivity(new Intent(getContext(), AboutUsActivity.class));
        });
        getBinding().vToEdit.setOnClickListener(view -> {
            if (editNameDialog == null) {
                editNameDialog = new EditNameDialog(getContext());
                editNameDialog.iSingleCallback = (type, o) -> {
                    if (type == 0) {
                        mainViewModel.requestEditUserInfo(null, (String) o, null);
                    }
                };
            }
            editNameDialog.show();
        });
        getBinding().ivUserAvatar.setOnClickListener(view -> {
            ((MainActivity) requireActivity()).requestReadStoragePermission(true);
        });
        getBinding().tvVersion.setOnClickListener(v -> {
            if (counts == 0) {
                beginTime = System.currentTimeMillis();
            }
            counts++;
            if (counts == 5) {
                if (System.currentTimeMillis() - beginTime > debugModeOpenTime) {
                    counts = 0;
                    return;
                }
                counts = 0;
                getBinding().tvDebugMode.setVisibility(View.VISIBLE);
                AgoraApplication.the().enableDebugMode(true);
                ToastUtils.showToast("Debug模式已打开");
            }
        });
        getBinding().tvDebugMode.setOnClickListener(v -> showDebugModeCloseDialog());
        if (AgoraApplication.the().isDebugModeOpen()) {
            getBinding().tvDebugMode.setVisibility(View.VISIBLE);
        }
    }


    private static final int CHOOSE_PHOTO = 100;
    private static final int TAKE_PHOTO = 101;
    String mTempPhotoPath = null;

    private void showSelectPhotoFromDialog() {
        if (selectPhotoFromDialog == null) {
            selectPhotoFromDialog = new SelectPhotoFromDialog(getContext());
            selectPhotoFromDialog.setOnButtonClickListener(new OnButtonClickListener() {
                @Override
                public void onLeftButtonClick() {
                    openAlbum();
                }

                @Override
                public void onRightButtonClick() {
                    takePhoto();
                }
            });
        }
        selectPhotoFromDialog.show();
    }

    public void openAlbum() {
        Intent intentToPickPic = new Intent(Intent.ACTION_PICK, null);
        intentToPickPic.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intentToPickPic.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intentToPickPic, CHOOSE_PHOTO);
    }

    private void takePhoto() {
        Intent intentToTakePhoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intentToTakePhoto.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        File fileDir = new File(FileUtils.getTempSDPath()
        );
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        File photoFile = new File(fileDir, "photo.jpg");
        mTempPhotoPath = photoFile.getAbsolutePath();
        Uri imageUri = FileProvider.getUriForFile(
                getContext(),
                BuildConfig.APPLICATION_ID + ".fileProvider",
                photoFile
        );
        intentToTakePhoto.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intentToTakePhoto, TAKE_PHOTO);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CHOOSE_PHOTO) {
                Uri uri = data.getData();
                if (uri != null) {
                    String filePath = UriUtils.INSTANCE.getFilePathByUri(getContext(), uri);
                    if (!TextUtils.isEmpty(filePath)) {
                        setImage(filePath);
                    }
                }
            } else if (requestCode == TAKE_PHOTO) {
                setImage(mTempPhotoPath);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setImage(String filePath) {
        if (filePath == null) return;
        String path = ImageCompressUtil.displayPath(requireActivity(), filePath);
        if (TextUtils.isEmpty(path) || new File(filePath).length() <= 150000) {
            path = filePath;
        }
        mTempPhotoPath = path;
//        UserManager.getInstance().getUser().headUrl = mTempPhotoPath;
        mainViewModel.updatePhoto(new File(mTempPhotoPath));
    }

    @Override
    public void requestData() {
        mainViewModel.requestUserInfo(UserManager.getInstance().getUser().userNo);
    }

    private void showLogoffAccountDialog() {
        if (logoffAccountDialog == null) {
            logoffAccountDialog = new CommonDialog(requireContext());
            logoffAccountDialog.setDialogTitle("确定注销账号？");
            logoffAccountDialog.setDescText("注销账号后，您将暂时无法使用该账号体验我们的服务，真的要注销吗？");
            logoffAccountDialog.setDialogBtnText(getString(R.string.app_logoff), getString(R.string.cancel));
            logoffAccountDialog.setOnButtonClickListener(new OnButtonClickListener() {
                @Override
                public void onLeftButtonClick() {
                    SPUtil.putBoolean(Constant.IS_AGREE, false);
                    mainViewModel.requestCancellation(UserManager.getInstance().getUser().userNo);
                }

                @Override
                public void onRightButtonClick() {
                    SPUtil.putBoolean(Constant.IS_AGREE, true);
                }
            });
        }
        logoffAccountDialog.show();
    }

    private void showLogoutDialog() {
        if (logoutDialog == null) {
            logoutDialog = new CommonDialog(requireContext());
            logoutDialog.setDialogTitle("确定退出登录吗？");
            logoutDialog.setDescText("退出登录后，我们还会继续保留您的账户数据，记得再来体验哦～");
            logoutDialog.setDialogBtnText(getString(R.string.app_exit), getString(R.string.cancel));
            logoutDialog.setOnButtonClickListener(new OnButtonClickListener() {
                @Override
                public void onLeftButtonClick() {
                    SPUtil.putBoolean(Constant.IS_AGREE, false);
                    UserManager.getInstance().logout();
                    requireActivity().finish();
                    PagePilotManager.pageWelcome();
                }

                @Override
                public void onRightButtonClick() {

                }
            });
        }
        logoutDialog.show();
    }

    private void showDebugModeCloseDialog() {
        if (debugModeDialog == null) {
            debugModeDialog = new CommonDialog(requireContext());
            debugModeDialog.setDialogTitle("确定退出Debug模式么？");
            debugModeDialog.setDescText("退出debug模式后， 设置页面将恢复成正常的设置页面哦～");
            debugModeDialog.setDialogBtnText(getString(R.string.cancel), getString(R.string.app_exit));
            debugModeDialog.setOnButtonClickListener(new OnButtonClickListener() {
                @Override
                public void onLeftButtonClick() {
                }

                @Override
                public void onRightButtonClick() {
                    counts = 0;
                    getBinding().tvDebugMode.setVisibility(View.GONE);
                    AgoraApplication.the().enableDebugMode(false);
                    ToastUtils.showToast("Debug模式已关闭");
                }
            });
        }
        debugModeDialog.show();
    }
}
