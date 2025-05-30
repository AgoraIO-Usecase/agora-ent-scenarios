package com.agora.entfulldemo.home

import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.agora.entfulldemo.R
import com.agora.entfulldemo.databinding.AppActivityMainBinding
import com.alibaba.android.arouter.facade.annotation.Route
import com.tencent.bugly.crashreport.CrashReport
import io.agora.scene.base.BuildConfig
import io.agora.scene.base.PagePathConstant
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.base.utils.ThreadManager
import io.agora.scene.base.utils.TimeUtils
import io.agora.scene.widget.dialog.PermissionLeakDialog
import io.agora.scene.widget.dialog.SecurityNoticeDialog

/**
 * Main page container
 */
@Route(path = PagePathConstant.pageMainHome)
class MainActivity : BaseViewBindingActivity<AppActivityMainBinding>() {

    private lateinit var navController: NavController
    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainViewModel.fetchSceneConfig()

        SecurityNoticeDialog().show(supportFragmentManager, "SecurityNoticeDialog")

        ThreadManager.getInstance().runOnIOThread { TimeUtils.currentTimeMillis() }
    }

    override fun getViewBinding(inflater: LayoutInflater): AppActivityMainBinding {
        return AppActivityMainBinding.inflate(inflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        CrashReport.initCrashReport(applicationContext, "0e701c6bd0", BuildConfig.DEBUG)
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java).apply {
            setLifecycleOwner(this@MainActivity)
        }
        navController = findNavController(R.id.nav_host_fragment_activity_main)
        binding.navView.setupWithNavController(navController)
        binding.navView.itemIconTintList = null
    }

    override fun isCanExit(): Boolean = true

    override fun getPermissions() {
        (getFragment(HomeMineFragment::class.java) as? HomeMineFragment)?.openAlbum()
    }

    override fun onPermissionDined(permission: String) {
        super.onPermissionDined(permission)
        PermissionLeakDialog(this).show(permission, null) { launchAppSetting(permission) }
    }

    private fun getFragment(clazz: Class<*>): Fragment? {
        return supportFragmentManager.fragments.firstOrNull()?.let { navHostFragment ->
            (navHostFragment as? NavHostFragment)?.childFragmentManager?.fragments?.firstOrNull { fragment ->
                fragment.javaClass.isAssignableFrom(clazz)
            }
        }
    }
}