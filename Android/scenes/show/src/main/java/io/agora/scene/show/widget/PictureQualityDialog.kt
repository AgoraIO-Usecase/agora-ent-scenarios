package io.agora.scene.show.widget

import android.content.Context
import android.util.Size
import android.view.LayoutInflater
import androidx.annotation.IntDef
import androidx.annotation.StringRes
import io.agora.scene.show.R
import io.agora.scene.show.databinding.ShowWidgetPictureQualityDialogBinding
import io.agora.scene.show.databinding.ShowWidgetPictureQualityDialogItemBinding
import io.agora.scene.widget.basic.BindingSingleAdapter
import io.agora.scene.widget.basic.BindingViewHolder

class PictureQualityDialog(context: Context) : BottomDarkDialog(context) {

    companion object {
        const val QUALITY_INDEX_1080P = 0
        const val QUALITY_INDEX_720P = 1
        const val QUALITY_INDEX_540P = 2
        const val QUALITY_INDEX_360P = 3
        const val QUALITY_INDEX_270P = 4
        const val QUALITY_INDEX_180P = 5

        @IntDef(
            QUALITY_INDEX_1080P,
            QUALITY_INDEX_720P,
            QUALITY_INDEX_540P,
            QUALITY_INDEX_360P,
            QUALITY_INDEX_270P,
            QUALITY_INDEX_180P
        )
        @Retention(AnnotationRetention.RUNTIME)
        public annotation class QualityIndex

        private val QualityItemList = arrayListOf(
            QualityItem(QUALITY_INDEX_1080P, R.string.show_picture_quality_1080p, Size(1080, 1920)),
            QualityItem(QUALITY_INDEX_720P, R.string.show_picture_quality_720p, Size(720, 1280)),
            QualityItem(QUALITY_INDEX_540P, R.string.show_picture_quality_540p, Size(540, 960)),
            QualityItem(QUALITY_INDEX_360P, R.string.show_picture_quality_360p, Size(360, 640)),
            QualityItem(QUALITY_INDEX_270P, R.string.show_picture_quality_270p, Size(270, 480)),
            QualityItem(QUALITY_INDEX_180P, R.string.show_picture_quality_180p, Size(180, 320)),
        )

        private var cacheSelectedIndex = QUALITY_INDEX_720P

        fun getCacheQualityResolution() = QualityItemList[cacheSelectedIndex].size
    }

    private data class QualityItem(@QualityIndex val qualityIndex: Int, @StringRes val name: Int, val size: Size)


    private val mBinding by lazy {
        ShowWidgetPictureQualityDialogBinding.inflate(
            LayoutInflater.from(
                context
            )
        )
    }

    private var currSelectedIndex = cacheSelectedIndex
    private var onQualitySelectListener: ((PictureQualityDialog, Int, Size) -> Unit)? = null

    private val mAdapter by lazy {
        object : BindingSingleAdapter<QualityItem, ShowWidgetPictureQualityDialogItemBinding>() {
            override fun onBindViewHolder(
                holder: BindingViewHolder<ShowWidgetPictureQualityDialogItemBinding>,
                position: Int
            ) {
                val item = getItem(position) ?: return
                val selected = currSelectedIndex == position

                holder.binding.text.isActivated = selected
                holder.binding.text.text = context.getString(item.name)
                holder.binding.root.setOnClickListener {
                    updateSelectPosition(holder.adapterPosition)
                }
            }
        }
    }

    init {
        setBottomView(mBinding.root)
        mBinding.recycleView.adapter = mAdapter
        mAdapter.resetAll(QualityItemList)
    }

    fun setSelectQuality(width: Int, height: Int){
        val index = QualityItemList.indexOfFirst { it.size.width == width && it.size.height == height }
        updateSelectPosition(index)
    }

    fun setOnQualitySelectListener(listener: ((PictureQualityDialog, Int, Size) -> Unit)) {
        onQualitySelectListener = listener
    }

    private fun updateSelectPosition(selectPosition: Int) {
        val item = QualityItemList.getOrNull(selectPosition) ?: return
        if (currSelectedIndex == selectPosition) {
            return
        }
        val oIndex = currSelectedIndex
        currSelectedIndex = selectPosition

        mAdapter.notifyItemChanged(oIndex)
        mAdapter.notifyItemChanged(currSelectedIndex)

        onQualitySelectListener?.invoke(this, item.qualityIndex, Size(item.size.width, item.size.height))
    }

}