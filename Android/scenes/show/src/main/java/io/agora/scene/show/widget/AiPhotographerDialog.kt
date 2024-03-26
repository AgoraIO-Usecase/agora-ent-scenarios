package io.agora.scene.show.widget

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.google.gson.reflect.TypeToken
import io.agora.scene.base.GlideApp
import io.agora.scene.base.api.apiutils.GsonUtils
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.base.utils.dp
import io.agora.scene.show.R
import io.agora.scene.show.databinding.ShowAiPhotographerItemBinding
import io.agora.scene.show.databinding.ShowWidgetAiPhotographerDialogBinding
import io.agora.scene.show.photographer.AiPhotographerType
import io.agora.scene.show.photographer.ManifestFileModel
import io.agora.scene.show.photographer.ManifestModel
import io.agora.scene.show.utils.DownloadUtils
import io.agora.scene.show.utils.FileUtils
import io.agora.scene.widget.basic.BindingSingleAdapter
import io.agora.scene.widget.basic.BindingViewHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.reflect.Type


class AiPhotographerDialog constructor(context: Context) : BottomDarkDialog(context) {

    private val URL_RESOURCE =
        "https://fullapp.oss-cn-beijing.aliyuncs.com/ent-scenarios/resource/manifest/manifestList"

    private data class ItemInfo constructor(
        val itemId: Int,
        @DrawableRes val icon: Int,
        var status: DownloadStatus = DownloadStatus.None
    )

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

    private fun checkFileDownload() {
        val vtBgFile = File(context.getExternalFilesDir("assets/metaFiles"), "pano.jpg")
        val vtBgDownloaded = vtBgFile.exists()
        val file = File(context.getExternalFilesDir("assets/metaAssets"), "DefaultPackage")
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
    private val manifestList = mutableListOf<ManifestModel>()

    private fun gotoDownload(itemId: Int) {
        val scope = CoroutineScope(Job() + Dispatchers.Main)
        scope.launch {
            val file = File(context.getExternalFilesDir("assets"), URL_RESOURCE.substringAfterLast("/"))
            if (file.exists()) {
                val manifests = FileUtils.readJsonFromFile(file)
                val type: Type = object : TypeToken<List<ManifestModel>>() {}.type
                val list = GsonUtils.gson.fromJson<List<ManifestModel>>(manifests, type)
                manifestList.addAll(list)
            }
            if (manifestList.isEmpty()) {
                // 下载资源目录
                async {
                    downloadManifestList()
                }.await()
            }
            manifestList.find { it.uri == "manifest/manifestAREffect" }?.let { manifestModel ->
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
                mAiPhotographerAdapter.resetAll(mAiPhotographerItemList)
                async {
                    downloadManifestFile(manifestModel)
                }.await()
            }
            if (itemId == AiPhotographerType.ITEM_ID_AI_LIGHTING_3D_VIRTUAL_BG) {
                manifestList.find { it.uri == "manifest/manifestAREffectBgImage" }?.let { manifestModel ->
                    mAiPhotographerItemList.forEach { item ->
                        when (item.itemId) {
                            AiPhotographerType.ITEM_ID_AI_LIGHTING_3D_VIRTUAL_BG -> {
                                item.status = DownloadStatus.Loading
                            }
                        }
                    }
                    mAiPhotographerAdapter.resetAll(mAiPhotographerItemList)
                    async {
                        downloadManifestFile(manifestModel)
                    }.await()
                }
            }
        }
    }

    // 下载资源目录 json
    private suspend fun downloadManifestList() {
        DownloadUtils.instance.download(context, URL_RESOURCE, object : DownloadUtils.FileDownloadCallback {})
        try {
            val file = File(context.getExternalFilesDir("assets"), URL_RESOURCE.substringAfterLast("/"))
            val manifests = FileUtils.readJsonFromFile(file)
            val type: Type = object : TypeToken<List<ManifestModel>>() {}.type
            val list = GsonUtils.gson.fromJson<List<ManifestModel>>(manifests, type)
            manifestList.addAll(list)
            Log.d("zhangww", "manifestList:${manifestList.size}")
        } catch (e: Exception) {
            Log.e("zhangww", "downloadManifestList:$e")
        }
    }

    // 下载 AI摄影师资源/虚拟背景图片 json
    private suspend fun downloadManifestFile(manifestModel: ManifestModel) {
        DownloadUtils.instance.download(context, manifestModel.url, object : DownloadUtils.FileDownloadCallback {})
        try {
            val file = File(context.getExternalFilesDir("assets"), manifestModel.url.substringAfterLast("/"))
            val manifestFiles = FileUtils.readJsonFromFile(file)
            val type: Type = object : TypeToken<ManifestFileModel>() {}.type
            val manifestFileModel = GsonUtils.gson.fromJson<ManifestFileModel>(manifestFiles, type)
            if (manifestModel.uri == "manifest/manifestAREffect") {
                val newManifestModel = manifestFileModel.files[0]
                processFile(newManifestModel)
                Log.d("zhangww", "manifestAREffect:$newManifestModel")
            } else if (manifestModel.uri == "manifest/manifestAREffectBgImage") {
                val newManifestModel = manifestFileModel.files[0]
                processFile(newManifestModel)
                Log.d("zhangww", "manifestAREffectBgImage:$newManifestModel")
            }
            withContext(Dispatchers.Main) {
                checkFileDownload()
                mAiPhotographerAdapter.resetAll(mAiPhotographerItemList)
            }
        } catch (e: Exception) {
            Log.e("zhangww", "downloadManifestFile:$e")
        }
    }

    private suspend fun processFile(manifestModel: ManifestModel) {
        val destDirPath = when (manifestModel.uri) {
            "DefaultPackage" -> {
                AgoraApplication.the().getExternalFilesDir("assets/metaAssets").toString()
            }

            "AREffect/bgImage" -> {
                AgoraApplication.the().getExternalFilesDir("assets/metaFiles").toString()
            }

            else -> {
                AgoraApplication.the().getExternalFilesDir("assets").toString()
            }
        }
        // TODO: 切换 json 中地址
        val url = if (manifestModel.uri == "DefaultPackage") {
            "https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/ent/ai/AREffect.zip"
        } else {
            manifestModel.url
        }
        DownloadUtils.instance.processFile(
            context,
            url,
            destDirPath,
            object : DownloadUtils.FileDownloadCallback {})
    }
}