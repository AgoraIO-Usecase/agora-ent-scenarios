package io.agora.scene.base.component;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewbinding.ViewBinding;

import io.agora.scene.base.R;
import io.agora.scene.base.utils.ToastUtils;
import kotlin.jvm.internal.Intrinsics;

/**
 * 带load的baseActivity
 * 由kotlin转换而来
 */
public abstract class BaseViewBindingActivity<T extends ViewBinding> extends BaseBindingActivity<T> {
    private View loadingView;
    /**
     * 退出标记位
     */
    private boolean isExit = false;

    private String appSettingInput;
    private ActivityResultLauncher<String> appSettingLauncher = registerForActivityResult(new ActivityResultContract<String, Boolean>() {

        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, String input) {
            appSettingInput = input;
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            return intent;
        }

        @Override
        public Boolean parseResult(int resultCode, @Nullable Intent intent) {
            return ActivityCompat.checkSelfPermission(BaseViewBindingActivity.this, appSettingInput) == PackageManager.PERMISSION_GRANTED;
        }
    }, result -> {
        checkPermission();
    });

    private void addLoadingView() {
        if (this.loadingView == null) {
            this.loadingView = LayoutInflater.from(this).inflate(R.layout.view_base_loading, null, false);
            ((ViewGroup) this.getBinding().getRoot()).addView(this.loadingView, new LayoutParams(-1, -1));
        }
        this.loadingView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            getWindow().setAttributes(params);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

//        new Handler().postDelayed(() -> {
//            if (isFinishing()) {
//                return;
//            }
//            // 检测是否要动态申请相应的权限
//            int reqIndex = requestNextPermission();
//            if (reqIndex < 0) {
//                getPermissions();
//            }
//        }, 200);
    }

    ///////////////////////////////////////////////////////////////////////////
    //////////////////////// Methods of Permission ////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    protected PermissionItem[] mPermissionArray;

    public static final int PERM_REQID_RECORD_AUDIO = 0x1001;
    public static final int PERM_REQID_CAMERA = 0x1002;
    public static final int PERM_REQID_RDSTORAGE = 0x1003;
    public static final int PERM_REQID_WRSTORAGE = 0x1004;
    public static final int PERM_REQID_MGSTORAGE = 0x1005;
    public static final int PERM_REQID_WIFISTATE = 0x1006;
    public static final int PERM_REQID_FINELOCAL = 0x1007;

    private void checkPermission() {
        int reqIndex = requestNextPermission();
        if (reqIndex < 0) {
            getPermissions();
        }
    }

    public void requestReadStoragePermission() {
        if (VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mPermissionArray = new PermissionItem[1];
            for (PermissionItem item : mPermissionArray) {
                item.granted = true;
            }
        } else if (VERSION.SDK_INT < Build.VERSION_CODES.R) {
            mPermissionArray = new PermissionItem[1];
            mPermissionArray[0] = new PermissionItem(Manifest.permission.READ_EXTERNAL_STORAGE, PERM_REQID_RDSTORAGE);
            for (PermissionItem item : mPermissionArray) {
                item.granted = (ContextCompat.checkSelfPermission(this, item.permissionName) == PackageManager.PERMISSION_GRANTED);
            }
        } else {
            mPermissionArray = new PermissionItem[1];
            mPermissionArray[0] = new PermissionItem(Manifest.permission.READ_EXTERNAL_STORAGE, PERM_REQID_RDSTORAGE);
            for (PermissionItem item : mPermissionArray) {
                item.granted = (ContextCompat.checkSelfPermission(this, item.permissionName) == PackageManager.PERMISSION_GRANTED);
            }
        }
        checkPermission();
    }

    protected void requestCameraPermission() {
        if (VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mPermissionArray = new PermissionItem[1];
            for (PermissionItem item : mPermissionArray) {
                item.granted = true;
            }

        } else if (VERSION.SDK_INT < Build.VERSION_CODES.R) {
            mPermissionArray = new PermissionItem[1];
            mPermissionArray[0] = new PermissionItem(Manifest.permission.CAMERA, PERM_REQID_CAMERA);
            for (PermissionItem item : mPermissionArray) {
                item.granted = (ContextCompat.checkSelfPermission(this, item.permissionName) == PackageManager.PERMISSION_GRANTED);
            }
        } else {
            mPermissionArray = new PermissionItem[1];
            mPermissionArray[0] = new PermissionItem(Manifest.permission.CAMERA, PERM_REQID_CAMERA);
            for (PermissionItem item : mPermissionArray) {
                item.granted = (ContextCompat.checkSelfPermission(this, item.permissionName) == PackageManager.PERMISSION_GRANTED);
            }
        }
        checkPermission();
    }

    protected void requestRecordPermission() {
        if (VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mPermissionArray = new PermissionItem[1];
            for (PermissionItem item : mPermissionArray) {
                item.granted = true;
            }

        } else if (VERSION.SDK_INT < Build.VERSION_CODES.R) {
            mPermissionArray = new PermissionItem[1];
            mPermissionArray[0] = new PermissionItem(Manifest.permission.RECORD_AUDIO, PERM_REQID_RECORD_AUDIO);
            for (PermissionItem item : mPermissionArray) {
                item.granted = (ContextCompat.checkSelfPermission(this, item.permissionName) == PackageManager.PERMISSION_GRANTED);
            }
        } else {
            mPermissionArray = new PermissionItem[1];
            mPermissionArray[0] = new PermissionItem(Manifest.permission.RECORD_AUDIO, PERM_REQID_RECORD_AUDIO);
            for (PermissionItem item : mPermissionArray) {
                item.granted = (ContextCompat.checkSelfPermission(this, item.permissionName) == PackageManager.PERMISSION_GRANTED);
            }
        }
        checkPermission();
    }

    /*
     * @brief 进行下一个需要的权限申请
     * @param None
     * @return 申请权限的索引, -1表示所有权限都有了，不再需要申请
     */
    protected int requestNextPermission() {
        if (mPermissionArray == null) return -1;
        for (int i = 0; i < mPermissionArray.length; i++) {
            if (!mPermissionArray[i].granted) {
                // 请求相应的权限i
                String permission = mPermissionArray[i].permissionName;
                int requestCode = mPermissionArray[i].requestId;
                boolean shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, permission);
                if (shouldShowRationale) {
                    doOnPermissionsDenied(requestCode);
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
                }
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            setPermGrantedByReqId(requestCode);
        } else { // 拒绝了该权限
            doOnPermissionsDenied(requestCode);
            return;
        }

        // 检测是否要动态申请相应的权限
        int reqIndex = requestNextPermission();
        if (reqIndex < 0) {
            getPermissions();
        }
    }

    /*
     * @brief 根据requestId 标记相应的 PermissionItem 权限已经获得
     * @param reqId :  request Id
     * @return 相应的索引, -1表示没有找到 request Id 对应的项
     */
    protected int setPermGrantedByReqId(int reqId) {
        if(mPermissionArray==null) return -1;
        for (int i = 0; i < mPermissionArray.length; i++) {
            if (mPermissionArray[i].requestId == reqId) {
                mPermissionArray[i].granted = true;
                return i;
            }
        }

        return -1;
    }


    @SuppressLint({"AutoDispose"})
    public void requestAppPermissions(@NonNull String... permissions) {
        Intrinsics.checkNotNullParameter(permissions, "permissions");
        if (VERSION.SDK_INT >= 23) {
            boolean hasAll = true;
            String[] var5 = permissions;
            int var6 = permissions.length;
            for (int var4 = 0; var4 < var6; ++var4) {
                String p = var5[var4];
                int i = ContextCompat.checkSelfPermission(this, p);
                if (i != 0) {
                    ActivityCompat.requestPermissions(this, permissions, 12);
                    hasAll = false;
                    break;
                }
            }
            if (hasAll) {
                this.getAlonePermissions();
            }
        } else {
            this.getAlonePermissions();
        }

    }

    private void doOnPermissionsDenied(int requestCode){
        ToastUtils.showToast(R.string.permission_leak_tip);
        if(mPermissionArray==null) return;
        String permission = null;
        for (int i = 0; i < mPermissionArray.length; i++) {
            if (mPermissionArray[i].requestId == requestCode) {
                permission = mPermissionArray[i].permissionName;
                break;
            }
        }
        if(permission == null){
            return;
        }
        onPermissionDined(permission);
    }

    protected void launchAppSetting(String permission){
        appSettingLauncher.launch(permission);
    }

    protected void onPermissionDined(String permission){

    }

    public void getPermissions() {

    }

    public void getAlonePermissions() {
    }

    @Nullable
    public View getLayoutView() {
        return super.getLayoutView();
    }

    public final void showLoadingView() {
        getWindow().getDecorView().post(this::addLoadingView);
        getWindow().getDecorView().postDelayed(() -> hideLoadingView(), 5000);
    }

    public final void hideLoadingView() {
        if (this.loadingView == null) {
            return;
        }
        getWindow().getDecorView().post(() -> {
            if (this.loadingView != null) {
                this.loadingView.setVisibility(View.GONE);
            }
        });
    }

    /**
     * 是否可执行退出 由子类控制
     */
    protected boolean isCanExit() {
        return false;
    }


    public final void hideInput() {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(INPUT_METHOD_SERVICE);
        View v = this.getWindow().peekDecorView();
        if (v != null) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    public final void showInput(EditText et) {
        et.requestFocus();
        InputMethodManager imm = (InputMethodManager) this.getSystemService(INPUT_METHOD_SERVICE);
        imm.showSoftInput((View) et, 1);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && isCanExit()) {
            if (loadingView != null && loadingView.getVisibility() == View.VISIBLE) {
                loadingView.setVisibility(View.GONE);
                return true;
            }
            exitAPP();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void exitAPP() {
        if (!isExit) {
            isExit = true;
            ToastUtils.showToast(R.string.try_again_to_exit);
            new Handler(Looper.getMainLooper()).postDelayed(() -> isExit = false, 2000);
        } else {
            // RTMManager.getInstance().mRtmClient.release();
            finish();
            System.exit(0);
        }
    }

    @Override

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
//        if (hasFocus) {
//            getWindow().getDecorView().setSystemUiVisibility(
//                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION//| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);//| View.SYSTEM_UI_FLAG_FULLSCREEN
//        }
    }
}
