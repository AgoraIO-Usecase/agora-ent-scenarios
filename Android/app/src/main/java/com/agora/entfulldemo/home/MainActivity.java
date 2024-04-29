package com.agora.entfulldemo.home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.ActivityKt;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.BottomNavigationViewKt;

import com.agora.entfulldemo.R;
import com.agora.entfulldemo.databinding.AppActivityMainBinding;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.tencent.bugly.crashreport.CrashReport;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import io.agora.scene.base.BuildConfig;
import io.agora.scene.base.PagePathConstant;
import io.agora.scene.base.component.BaseViewBindingActivity;
import io.agora.scene.base.event.UserTokenErrorEvent;
import io.agora.scene.base.manager.PagePilotManager;
import io.agora.scene.widget.dialog.PermissionLeakDialog;

/**
 * 主页容器
 */
@Route(path = PagePathConstant.pageMainHome)
public class MainActivity extends BaseViewBindingActivity<AppActivityMainBinding> {

    private static final String KEY_CODE = "key_code";
    public static final int PARAMS_EXIT = 100;

    public static void startActivity(Activity activity, int code){
        Intent intent = new Intent(activity, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(KEY_CODE, code);
        activity.startActivity(intent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(@Nullable UserTokenErrorEvent event) {
        finishAffinity();
        PagePilotManager.pageWelcomeClear();
    }

    private NavController navController;
    /**
     * 主页接收消息
     */
    private MainViewModel mainViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent != null){
            int code = intent.getIntExtra(KEY_CODE,-1);
            if (code==PARAMS_EXIT){
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(0);
                    }
                }, 500);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected AppActivityMainBinding getViewBinding(@NonNull LayoutInflater inflater) {
        return AppActivityMainBinding.inflate(inflater);
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState) {
        CrashReport.initCrashReport(getApplicationContext(), "0e701c6bd0", BuildConfig.DEBUG);
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        mainViewModel.setLifecycleOwner(this);
        navController = ActivityKt.findNavController(this, R.id.nav_host_fragment_activity_main);
        BottomNavigationViewKt.setupWithNavController(getBinding().navView, navController);
    }

    @Override
    protected boolean isCanExit() {
        return true;
    }

    @Override
    public void initListener() {
        getBinding().navView.setItemIconTintList(null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void getPermissions() {
        Fragment fragment = getFragment(HomeMineFragment.class);
        if (fragment != null) {
            ((HomeMineFragment) fragment).openAlbum();
        }
    }

    @Override
    protected void onPermissionDined(String permission) {
        super.onPermissionDined(permission);
        new PermissionLeakDialog(this).show(permission, null, () -> launchAppSetting(permission));
    }

    public Fragment getFragment(Class<?> clazz) {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null && fragments.size() > 0) {
            NavHostFragment navHostFragment = (NavHostFragment) fragments.get(0);
            List<Fragment> childfragments = navHostFragment.getChildFragmentManager().getFragments();
            if (childfragments != null && childfragments.size() > 0) {
                for (int j = 0; j < childfragments.size(); j++) {
                    Fragment fragment = childfragments.get(j);
                    if (fragment.getClass().isAssignableFrom(clazz)) {
                        return fragment;
                    }
                }
            }
        }
        return null;
    }
}
