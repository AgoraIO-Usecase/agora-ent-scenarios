package io.agora.scene.show.widget

import AGResource
import AGResourceManager
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.divider.MaterialDividerItemDecoration
import io.agora.scene.base.GlideApp
import io.agora.scene.base.utils.dp
import io.agora.scene.show.R
import io.agora.scene.show.databinding.ShowAiPhotographerItemBinding
import io.agora.scene.show.databinding.ShowWidgetAiPhotographerDialogBinding
import io.agora.scene.show.photographer.AiPhotographerType
import io.agora.scene.widget.basic.BindingSingleAdapter
import io.agora.scene.widget.basic.BindingViewHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.internal.wait
import java.io.File

class AiPhotographerDialog constructor(context: Context) : BottomDarkDialog(context) {

    private val TAG = "AiPhotographerDialog"

    private val URL_RESOURCE =
        "https://fullapp.oss-cn-beijing.aliyuncs.com/ent-scenarios/resource/manifest/manifestList"

    private data class ItemInfo constructor(
        val itemId: Int,
        @DrawableRes val icon: Int,
        var status: DownloadStatus = DownloadStatus.None
    )

    private val agResourceManager by lazy {
        AGResourceManager(context)
    }

    private enum class DownloadStatus {
        None,
        Loading,
        Downloaded
    }

    private val mAiPhotographerItemList = arrayListOf(
        ItemInfo(AiPhotographerType.ITEM_ID_AI_RHYTHM, R.mipmap.show_ai_rhythm, DownloadStatus.Downloaded),
        ItemInfo(AiPhotographerType.ITEM_ID_AI_EDGE_LIGHT, R.mipmap.show_ai_edge_light),
        ItemInfo(AiPhotographerType.ITEM_ID_AI_SHADOW, R.mipmap.show_ai_shadow, DownloadStatus.Downloaded),
        ItemInfo(AiPhotographerType.ITEM_ID_AI_LIGHTING_AD, R.mipmap.show_ai_lighting_ad),
        ItemInfo(AiPhotographerType.ITEM_ID_AI_LIGHTING_3D, R.mipmap.show_ai_lighting_3d),
        ItemInfo(AiPhotographerType.ITEM_ID_AI_LIGHTING_3D_VIRTUAL_BG, R.mipmap.show_ai_lighting_3d_virtul_bg),
        ItemInfo(AiPhotographerType.ITEM_ID_AI_AURORA, R.mipmap.show_ai_aurora),
    )

    private var mAiSelectedIndex = -1
    private val mBinding by lazy {
        ShowWidgetAiPhotographerDialogBinding.inflate(LayoutInflater.from(context))
    }

    private val mAiPhotographerAdapter: BindingSingleAdapter<ItemInfo, ShowAiPhotographerItemBinding>

    var onItemSelectedListener: ((AiPhotographerDialog, itemId: Int) -> Unit)? = null

    val scope = CoroutineScope(Job() + Dispatchers.Main)

    init {
        setBottomView(mBinding.root)
        mAiPhotographerAdapter = object : BindingSingleAdapter<ItemInfo, ShowAiPhotographerItemBinding>() {
            override fun onBindViewHolder(holder: BindingViewHolder<ShowAiPhotographerItemBinding>, position: Int) {
                val item = getItem(position) ?: return
                holder.binding.ivOverlay.isActivated = position == mAiSelectedIndex
                holder.binding.ivIcon.apply {
                    GlideApp.with(this)
                        .load(item.icon)
                        .transform(RoundedCorners(2.dp.toInt()))
                        .into(this)
                }
                when (item.status) {
                    DownloadStatus.Downloaded -> {
                        holder.binding.ivDownload.visibility = View.INVISIBLE
                    }

                    DownloadStatus.Loading -> {
                        holder.binding.ivDownload.setImageResource(R.mipmap.show_ai_assets_loading)
                        holder.binding.ivDownload.visibility = View.VISIBLE
                    }

                    else -> {
                        holder.binding.ivDownload.setImageResource(R.mipmap.show_ai_assets_download)
                        holder.binding.ivDownload.visibility = View.VISIBLE
                    }
                }
                holder.binding.ivDownload.setOnClickListener {
                    if (item.status != DownloadStatus.None) return@setOnClickListener
                    gotoDownload(item.itemId)
                }
                holder.binding.root.setOnClickListener {
                    if (item.status != DownloadStatus.Downloaded) return@setOnClickListener
                    if (mAiSelectedIndex == holder.adapterPosition) {
                        val oSelectedIndex = mAiSelectedIndex
                        mAiSelectedIndex = -1
                        notifyItemChanged(oSelectedIndex)
                        onItemSelectedListener?.invoke(
                            this@AiPhotographerDialog,
                            AiPhotographerType.ITEM_ID_AI_PHOTOGRAPHER_NONE
                        )
                        return@setOnClickListener
                    }
                    val activate = !holder.binding.ivOverlay.isActivated
                    holder.binding.ivOverlay.isActivated = activate

                    updateAiPhotographerPosition(holder.adapterPosition)
                }
            }
        }.apply {
            mBinding.rvAiPhotographer.adapter = this
            mBinding.rvAiPhotographer.addItemDecoration(MaterialDividerItemDecoration(
                context,
                MaterialDividerItemDecoration.VERTICAL
            ).apply {
                dividerThickness = 24.dp.toInt()
                dividerColor = ContextCompat.getColor(context, android.R.color.transparent)
            })
            checkFileDownload()
            resetAll(mAiPhotographerItemList)
        }
    }

    private fun updateLoading(itemId: Int) {
        if (itemId == AiPhotographerType.ITEM_ID_AI_EDGE_LIGHT ||
            itemId == AiPhotographerType.ITEM_ID_AI_LIGHTING_AD ||
            itemId == AiPhotographerType.ITEM_ID_AI_LIGHTING_3D ||
            itemId == AiPhotographerType.ITEM_ID_AI_AURORA
        ) {
            mAiPhotographerItemList.forEach { item ->
                when (item.itemId) {
                    AiPhotographerType.ITEM_ID_AI_EDGE_LIGHT,
                    AiPhotographerType.ITEM_ID_AI_LIGHTING_AD,
                    AiPhotographerType.ITEM_ID_AI_LIGHTING_3D,
                    AiPhotographerType.ITEM_ID_AI_AURORA -> {
                        item.status = DownloadStatus.Loading
                    }
                }
            }
        } else if (itemId == AiPhotographerType.ITEM_ID_AI_LIGHTING_3D_VIRTUAL_BG) {
            mAiPhotographerItemList.forEach { item ->
                when (item.itemId) {
                    AiPhotographerType.ITEM_ID_AI_LIGHTING_3D_VIRTUAL_BG -> {
                        item.status = DownloadStatus.Loading
                    }
                }
            }
        }
    }

    private fun checkFileDownload() {
        val vtBgFile = File(context.getExternalFilesDir("assets"), "pano.jpg")
        val vtBgDownloaded = vtBgFile.exists()
        val file = File(context.getExternalFilesDir("assets"), "DefaultPackage")
        if (file.exists()) {
            mAiPhotographerItemList.forEach {
                when (it.itemId) {
                    AiPhotographerType.ITEM_ID_AI_EDGE_LIGHT,
                    AiPhotographerType.ITEM_ID_AI_LIGHTING_AD,
                    AiPhotographerType.ITEM_ID_AI_LIGHTING_3D,
                    AiPhotographerType.ITEM_ID_AI_AURORA -> {
                        it.status = DownloadStatus.Downloaded
                    }

                    AiPhotographerType.ITEM_ID_AI_LIGHTING_3D_VIRTUAL_BG -> {
                        if (vtBgDownloaded) {
                            it.status = DownloadStatus.Downloaded
                        }
                    }
                }
            }
        }
    }

    private fun updateAiPhotographerPosition(position: Int) {
        val item = mAiPhotographerItemList.getOrNull(position) ?: return
        val oSelectedIndex = mAiSelectedIndex
        mAiSelectedIndex = position

        mAiPhotographerAdapter.notifyItemChanged(oSelectedIndex)
        mAiPhotographerAdapter.notifyItemChanged(mAiSelectedIndex)

        onItemSelectedListener?.invoke(this@AiPhotographerDialog, item.itemId)
    }

    // 资源目录
    private val manifestResourceList = mutableListOf<AGResource>()

    private fun gotoDownload(itemId: Int) {
        // 更新下载状态
        updateLoading(itemId)
        mAiPhotographerAdapter.resetAll(mAiPhotographerItemList)
        scope.launch(Dispatchers.IO) {
            // 下载资源目录 json
            Log.d(TAG, "下载资源目录 start")
            agResourceManager.downloadManifestList(URL_RESOURCE, null, {},
                completionHandler = { agResourceList, err ->
                    manifestResourceList.clear()
                    agResourceList?.let {
                        manifestResourceList.addAll(it)
                    }
                    Log.d(TAG, "下载资源目录 end")
                })
            // AI摄影师资源
            manifestResourceList.find { it.uri == "manifest/manifestAREffect" }?.let { agResource ->
                downloadManifestFile(agResource)
            }
            // 背景图片
            if (itemId == AiPhotographerType.ITEM_ID_AI_LIGHTING_3D_VIRTUAL_BG) {
                manifestResourceList.find { it.uri == "manifest/manifestAREffectBgImage" }?.let { agResource ->
                    downloadManifestFile(agResource)
                }
            }
            withContext(Dispatchers.Main) {
                checkFileDownload()
                mAiPhotographerAdapter.resetAll(mAiPhotographerItemList)
            }
        }
    }

    private val tempUrl = "https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/ent/ai/AREffect.zip"

    // 下载 AI摄影师资源/虚拟背景图片 json
    private suspend fun downloadManifestFile(agResource: AGResource) {
        Log.d(TAG, "下载 ${agResource.uri} 资源 start")
        var agResourceFirst: AGResource? = null
        agResourceManager.downloadManifest(agResource.url, {},
            completionHandler = { aGManifest, err ->
                Log.d(TAG, "下载 ${agResource.uri} 资源 end")
                agResourceFirst = aGManifest?.files?.get(0) ?: return@downloadManifest
            })
        val resource = agResourceFirst ?: return
        if (resource.uri == "DefaultPackage") {
            resource.url = tempUrl
        }
        Log.d(TAG, "下载 ${resource.uri} 资源并解压 start")
        agResourceManager.downloadAndUnZipResource(resource, {},
            completionHandler = { file, exception ->
                Log.d(TAG, "下载 ${resource.uri} 资源并解压 end isSuccess:${file != null && exception == null}")
            })
    }
}