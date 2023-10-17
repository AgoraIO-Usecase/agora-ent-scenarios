package com.agora.entfulldemo.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.agora.entfulldemo.R
import com.agora.entfulldemo.databinding.AppFragmentHomeIndexSubBinding
import com.agora.entfulldemo.databinding.AppItemHomeIndexSubBinding
import com.agora.entfulldemo.home.constructor.HomeSceneModel
import com.agora.entfulldemo.home.constructor.HomeScenesType
import com.agora.entfulldemo.home.constructor.ScenesConstructor
import io.agora.scene.base.ReportApi.reportEnter
import io.agora.scene.base.component.BaseRecyclerViewAdapter
import io.agora.scene.base.component.BaseViewBindingFragment
import io.agora.scene.base.component.OnItemClickListener
import io.agora.scene.base.manager.UserManager
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.widget.utils.UiUtils

class HomeIndexSubFragment : BaseViewBindingFragment<AppFragmentHomeIndexSubBinding>() {

    companion object {
        const val Key_Scene_Type = "key_scene_type"

        fun newInstance(homeScenesType: HomeScenesType) = HomeIndexSubFragment().apply {
            arguments = Bundle().apply {
                putString(Key_Scene_Type, homeScenesType.name)
            }
        }
    }

    private var mHomeScenesType: HomeScenesType = HomeScenesType.Full

    private val mainViewModel: MainViewModel by lazy {
        ViewModelProvider(this)[MainViewModel::class.java]
    }

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): AppFragmentHomeIndexSubBinding {
        return AppFragmentHomeIndexSubBinding.inflate(inflater)
    }

    override fun initView() {
        val scenesType = arguments?.getString(Key_Scene_Type, HomeScenesType.Full.name) ?: HomeScenesType.Full.name
        mHomeScenesType = HomeScenesType.valueOf(scenesType)
        mainViewModel.setLifecycleOwner(this)
        UserManager.getInstance().user?.let { user ->
            mainViewModel.requestReportDevice(user.userNo, "")
        }

        val cxt = context ?: return
        val scenesModels = ScenesConstructor.buildScene(cxt, mHomeScenesType)
        val homeIndexAdapter = BaseRecyclerViewAdapter(scenesModels, object : OnItemClickListener<HomeSceneModel?> {
            override fun onItemClick(scenesModel: HomeSceneModel, view: View, position: Int, viewType: Long) {
                if (UiUtils.isFastClick(2000)) return
                if (scenesModel.active) {
                    reportEnter(scenesModel)
                    UserManager.getInstance().user?.let { user ->
                        mainViewModel.requestReportDevice(user.userNo, "")
                        mainViewModel.requestReportAction(user.userNo, scenesModel.scene.name)
                    }
                    goScene(scenesModel)
                }
            }
        }, HomeIndexSubHolder::class.java)
        binding.rvScenes.adapter = homeIndexAdapter

        binding.rvScenes.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                Log.d("zhangw", "$mHomeScenesType onScrollStateChanged $newState")
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                Log.d("zhangw", "onScrolled $mHomeScenesType onScrolled $dx $dy")
            }
        })
    }

    private fun reportEnter(scenesModel: HomeSceneModel) {
        reportEnter(scenesModel.scene.name, { aBoolean: Boolean? -> null }, null)
    }

    private fun goScene(scenesModel: HomeSceneModel) {
        val intent = Intent()
        intent.setClassName(requireContext(), scenesModel.clazzName)
        try {
            startActivity(intent)
        } catch (e: Exception) {
            ToastUtils.showToast(R.string.app_coming_soon)
        }
    }
}

class HomeIndexSubHolder constructor(mBinding: AppItemHomeIndexSubBinding) :
    BaseRecyclerViewAdapter.BaseViewHolder<AppItemHomeIndexSubBinding, HomeSceneModel?>(mBinding) {

    override fun binding(scensModel: HomeSceneModel?, selectedIndex: Int) {
        scensModel ?: return
        if (scensModel.title.isEmpty()) {
            mBinding.tvBigSceneTitle.isVisible = false
        } else {
            mBinding.tvBigSceneTitle.isVisible = true
            mBinding.tvBigSceneTitle.text = scensModel.title
        }
        mBinding.tvScenesName.text = scensModel.name
        mBinding.tvScenesTips.text = if (scensModel.active) scensModel.tip else
            mBinding.root.context.getString(R.string.app_coming_soon)
        mBinding.ivScenesBg.setImageResource(scensModel.background)
    }
}