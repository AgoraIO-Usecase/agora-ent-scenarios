package com.agora.entfulldemo.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.viewbinding.ViewBinding
import com.agora.entfulldemo.R
import com.agora.entfulldemo.databinding.AppFragmentHomeIndexSubBinding
import com.agora.entfulldemo.databinding.AppItemHomeHeadSubBinding
import com.agora.entfulldemo.databinding.AppItemHomeIndexSubBinding
import com.agora.entfulldemo.home.constructor.HomeSceneModel
import com.agora.entfulldemo.home.constructor.HomeScenesType
import com.agora.entfulldemo.home.constructor.ScenesConstructor
import io.agora.scene.base.ReportApi.reportEnter
import io.agora.scene.base.component.BaseRecyclerViewAdapter
import io.agora.scene.base.component.BaseViewBindingFragment
import io.agora.scene.base.component.OnItemClickListener
import io.agora.scene.base.manager.UserManager
import io.agora.scene.widget.dialog.checkRealName
import io.agora.scene.widget.toast.CustomToast
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
        if (mHomeScenesType == HomeScenesType.Full) {
            setupFullSceneAdapter()
            return
        }
        val scenesModels = ScenesConstructor.buildScene(cxt, mHomeScenesType)
        val homeIndexAdapter = BaseRecyclerViewAdapter(scenesModels, object : OnItemClickListener<HomeSceneModel?> {
            override fun onItemClick(scenesModel: HomeSceneModel, view: View, position: Int, viewType: Long) {
                onItemClickScene(scenesModel)
            }
        }, HomeIndexSubHolder::class.java)
        binding.rvScenes.adapter = homeIndexAdapter
    }

    private fun onItemClickScene(scenesModel: HomeSceneModel) {
        if (UiUtils.isFastClick(1000)) return
        if (scenesModel.active) {
            reportEnter(scenesModel)
            UserManager.getInstance().user?.let { user ->
                mainViewModel.requestReportDevice(user.userNo, "")
                mainViewModel.requestReportAction(user.userNo, scenesModel.scene.name)
            }
            goScene(scenesModel)
        }
    }

    // Setup adapter for full scene mode
    private fun setupFullSceneAdapter() {
        val cxt = context ?: return

        // Voice section
        val voiceHeadAdapter = HomeHeadAdapter(
            mutableListOf(cxt.getString(R.string.app_home_scene_voice)), HomeHeadSubHolder::class.java
        )
        val voiceScenesModels = ScenesConstructor.buildScene(cxt, HomeScenesType.Voice)
        val voiceAdapter = BaseRecyclerViewAdapter(voiceScenesModels, object : OnItemClickListener<HomeSceneModel?> {
            override fun onItemClick(scenesModel: HomeSceneModel, view: View, position: Int, viewType: Long) {
                onItemClickScene(scenesModel)
            }
        }, HomeIndexSubHolder::class.java)

        // Live streaming section
        val liveHeadAdapter = HomeHeadAdapter(
            mutableListOf(cxt.getString(R.string.app_home_scene_live)), HomeHeadSubHolder::class.java
        )
        val liveScenesModels = ScenesConstructor.buildScene(cxt, HomeScenesType.Live)
        val liveAdapter = BaseRecyclerViewAdapter(liveScenesModels, object : OnItemClickListener<HomeSceneModel?> {
            override fun onItemClick(scenesModel: HomeSceneModel, view: View, position: Int, viewType: Long) {
                onItemClickScene(scenesModel)
            }
        }, HomeIndexSubHolder::class.java)

        // KTV section
        val ktvHeadAdapter = HomeHeadAdapter(
            mutableListOf(cxt.getString(R.string.app_home_scene_ktv)), HomeHeadSubHolder::class.java
        )
        val ktvScenesModels = ScenesConstructor.buildScene(cxt, HomeScenesType.KTV)
        val ktvAdapter = BaseRecyclerViewAdapter(ktvScenesModels, object : OnItemClickListener<HomeSceneModel?> {
            override fun onItemClick(scenesModel: HomeSceneModel, view: View, position: Int, viewType: Long) {
                onItemClickScene(scenesModel)
            }
        }, HomeIndexSubHolder::class.java)

        // Game section
        val joyHeadAdapter = HomeHeadAdapter(
            mutableListOf(cxt.getString(R.string.app_home_scene_game)), HomeHeadSubHolder::class.java
        )
        val joyScenesModels = ScenesConstructor.buildScene(cxt, HomeScenesType.Game)
        val joyAdapter = BaseRecyclerViewAdapter(joyScenesModels, object : OnItemClickListener<HomeSceneModel?> {
            override fun onItemClick(scenesModel: HomeSceneModel, view: View, position: Int, viewType: Long) {
                onItemClickScene(scenesModel)
            }
        }, HomeIndexSubHolder::class.java)

        // AIGC section (commented out)
//        val aigcHeadAdapter = HomeHeadAdapter(
//            mutableListOf(cxt.getString(R.string.app_home_scene_aigc)), HomeHeadSubHolder::class.java
//        )
//        val aigcScenesModels = ScenesConstructor.buildScene(cxt, HomeScenesType.AIGC)
//        val aigcAdapter = BaseRecyclerViewAdapter(aigcScenesModels, object : OnItemClickListener<HomeSceneModel?> {
//            override fun onItemClick(scenesModel: HomeSceneModel, view: View, position: Int, viewType: Long) {
//                onItemClickScene(scenesModel)
//            }
//        }, HomeIndexSubHolder::class.java)

        // Configure and set the concat adapter
        val config = ConcatAdapter.Config.Builder().setIsolateViewTypes(true).build()

        val concatAdapter = ConcatAdapter(
            config,
            voiceHeadAdapter, voiceAdapter,
            liveHeadAdapter, liveAdapter,
            ktvHeadAdapter, ktvAdapter,
            joyHeadAdapter, joyAdapter,
//            aigcHeadAdapter, aigcAdapter
        )
        binding.rvScenes.adapter = concatAdapter
    }

    private fun reportEnter(scenesModel: HomeSceneModel) {
        reportEnter(scenesModel.scene.name, { aBoolean: Boolean? -> null }, null)
    }

    private fun goScene(scenesModel: HomeSceneModel) {
        activity?.apply {
            if (checkRealName()) {
                val intent = Intent()
                intent.setClassName(this, scenesModel.clazzName)
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    CustomToast.show(R.string.app_coming_soon)
                }
            }
        }
    }
}

// ViewHolder for scene items
class HomeIndexSubHolder constructor(mBinding: AppItemHomeIndexSubBinding) :
    BaseRecyclerViewAdapter.BaseViewHolder<AppItemHomeIndexSubBinding, HomeSceneModel?>(mBinding) {

    override fun binding(scensModel: HomeSceneModel?, selectedIndex: Int) {
        scensModel ?: return
        mBinding.tvScenesName.text = scensModel.name
        mBinding.tvScenesTips.text = if (scensModel.active) scensModel.tip else
            mBinding.root.context.getString(R.string.app_coming_soon)
        mBinding.ivScenesBg.setImageResource(scensModel.background)
    }
}

// Adapter for section headers
class HomeHeadAdapter<B : ViewBinding, T, H : BaseRecyclerViewAdapter.BaseViewHolder<B, T>>(
    dataList: List<T>,
    viewHolderClass: Class<H>
) : BaseRecyclerViewAdapter<B,T,H>(dataList, viewHolderClass) {
    override fun onViewAttachedToWindow(holder: H) {
        super.onViewAttachedToWindow(holder)
        (holder.itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams).isFullSpan = true
    }
}

// ViewHolder for section headers
class HomeHeadSubHolder constructor(mBinding: AppItemHomeHeadSubBinding) :
    BaseRecyclerViewAdapter.BaseViewHolder<AppItemHomeHeadSubBinding, String>(mBinding) {
    override fun binding(data: String?, selectedIndex: Int) {
        data ?: return
        mBinding.tvBigSceneTitle.text = data
    }
}