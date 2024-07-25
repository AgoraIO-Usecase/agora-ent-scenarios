package io.agora.scene.show.widget

import AGResource
import AGResourceManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.divider.MaterialDividerItemDecoration
import io.agora.scene.base.GlideApp
import io.agora.scene.base.utils.dp
import io.agora.scene.show.R
import io.agora.scene.show.ShowLogger
import io.agora.scene.show.databinding.ShowAiPhotographerItemBinding
import io.agora.scene.show.databinding.ShowWidgetAiPhotographerDialogBinding
import io.agora.scene.show.photographer.AiPhotographerType
import io.agora.scene.widget.basic.BindingSingleAdapter
import io.agora.scene.widget.basic.BindingViewHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class AiPhotographerDialog constructor(context: Context) : BottomDarkDialog(context) {

    companion object {
        private const val TAG = "AiPhotographerDialog"

        // 资源列表
        private val URL_RESOURCE = "https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/ent/ai/manifestList"

        // meta 资源 uri
        private val URI_META_RESOURCES = "manifest/manifestAREffect"

        // 图片 资源 uri
        private val URI_IMAGE_RESOURCES = "manifest/manifestAREffectBgImage"

        // meta zip uri
        private val URI_META_AREFFECT = "DefaultPackageAndroid"

        // 背景 zip uri
        private val URI_BG_IMAGE = "AREffect/bgImage"
    }

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

    private val scope = CoroutineScope(Job() + Dispatchers.Main)

    // 资源目录
    private val resourceList = mutableListOf<AGResource>()

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
            resetAll(mAiPhotographerItemList)
        }
    }

    override fun onStart() {
        super.onStart()
        // 检测本地清单配置
        agResourceManager.checkManifestResource(URL_RESOURCE)
        agResourceManager.getManifest(URI_META_RESOURCES)?.url?.let {
            agResourceManager.checkFileResource(it)
        }
        agResourceManager.getManifest(URI_IMAGE_RESOURCES)?.url?.let {
            agResourceManager.checkFileResource(it)
        }
        scope.launch(Dispatchers.IO) {
            checkDownloadFromRemote()
            withContext(Dispatchers.Main) {
                checkFileDownload()
                mAiPhotographerAdapter.resetAll(mAiPhotographerItemList)
            }
        }
    }

    suspend fun checkDownloadFromRemote() {
        if (resourceList.isEmpty()) {
            // 下载资源目录 jso
            ShowLogger.d(TAG, "checkDownloadFromRemote 下载资源目录 start")
            agResourceManager.downloadManifestList(URL_RESOURCE,
                progressHandler = {},
                completionHandler = { agResourceList, err ->
                    resourceList.clear()
                    agResourceList?.let {
                        resourceList.addAll(it)
                    }

                    ShowLogger.d(TAG, "checkDownloadFromRemote 下载资源目录 end")
                })
        }
        val metaAreffect = resourceList.firstOrNull { it.uri == URI_META_AREFFECT }
        if (metaAreffect == null) {
            // AI摄影师资源清单
            resourceList.firstOrNull { it.uri == URI_META_RESOURCES }?.let { agResource ->
                agResourceManager.downloadManifest(agResource.url,
                    progressHandler = {},
                    completionHandler = { aGManifest, err ->
                        ShowLogger.d(TAG, "下载 ${agResource.uri} 资源 end")
                        aGManifest?.files?.find { it.uri == URI_META_AREFFECT }?.let {
                            resourceList.add(it)
                        }
                    })
            }
        }
        val bgImage = resourceList.firstOrNull { it.uri == URI_BG_IMAGE }
        if (bgImage == null) {
            // 背景图片清单
            resourceList.firstOrNull { it.uri == URI_IMAGE_RESOURCES }?.let { agResource ->
                agResourceManager.downloadManifest(agResource.url,
                    progressHandler = {},
                    completionHandler = { aGManifest, err ->
                        ShowLogger.d(TAG, "下载 ${agResource.uri} 资源 end")
                        aGManifest?.files?.find { it.uri == URI_BG_IMAGE }?.let {
                            resourceList.add(it)
                        }
                    })
            }
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
        val newResourceMd5 = resourceList.firstOrNull { it.uri == URI_META_AREFFECT }?.md5 ?: ""
        val newImageMd5 = resourceList.firstOrNull { it.uri == URI_BG_IMAGE }?.md5 ?: ""

        val metaResourcesMd5 = agResourceManager.getManifest(URI_META_AREFFECT)?.md5 ?: ""
        val metaImageMd5 = agResourceManager.getManifest(URI_BG_IMAGE)?.md5 ?: ""
        if (newResourceMd5.isNotEmpty() && newResourceMd5 == metaResourcesMd5) {
            mAiPhotographerItemList.forEach {
                when (it.itemId) {
                    AiPhotographerType.ITEM_ID_AI_EDGE_LIGHT,
                    AiPhotographerType.ITEM_ID_AI_LIGHTING_AD,
                    AiPhotographerType.ITEM_ID_AI_LIGHTING_3D,
                    AiPhotographerType.ITEM_ID_AI_AURORA -> {
                        it.status = DownloadStatus.Downloaded
                    }

                    AiPhotographerType.ITEM_ID_AI_LIGHTING_3D_VIRTUAL_BG -> {
                        val isImageDownload = newImageMd5.isNotEmpty() && newImageMd5 == metaImageMd5
                        if (isImageDownload) {
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


    private fun gotoDownload(itemId: Int) {
        // 更新下载状态
        updateLoading(itemId)
        mAiPhotographerAdapter.resetAll(mAiPhotographerItemList)
        scope.launch(Dispatchers.IO) {
            checkDownloadFromRemote()
            // AI摄影师资源zip
            resourceList.firstOrNull { it.uri == URI_META_AREFFECT }?.let { agResource ->
                downloadAndUnZipResource(agResource)
            }
            // 背景图片zip
            if (itemId == AiPhotographerType.ITEM_ID_AI_LIGHTING_3D_VIRTUAL_BG) {
                resourceList.firstOrNull { it.uri == URI_BG_IMAGE }?.let { agResource ->
                    downloadAndUnZipResource(agResource)
                }
            }
            withContext(Dispatchers.Main) {
                checkFileDownload()
                mAiPhotographerAdapter.resetAll(mAiPhotographerItemList)
            }
        }
    }

    // 下载 AI摄影师资源/虚拟背景图片 json
    private suspend fun downloadAndUnZipResource(agResource: AGResource) {
        ShowLogger.d(TAG, "下载 ${agResource.uri} 资源并解压 start")
        agResourceManager.downloadAndUnZipResource(
            agResource,
            progressHandler = { progress ->

            },
            completionHandler = { file: File?, err: Exception? ->
                ShowLogger.d(TAG, "downloaded filePath ${file?.path} err:$err")
            },
        )
    }
}