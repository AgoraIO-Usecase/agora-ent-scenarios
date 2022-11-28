package io.agora.scene.show.widget

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.IntDef
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
    }

    private val mBinding by lazy {
        ShowWidgetPictureQualityDialogBinding.inflate(
            LayoutInflater.from(
                context
            )
        )
    }

    private var currSelectedIndex = 0
    private var onQualitySelectListener: ((PictureQualityDialog, Int) -> Unit)? = null

    private val mAdapter by lazy {
        object : BindingSingleAdapter<String, ShowWidgetPictureQualityDialogItemBinding>() {
            override fun onBindViewHolder(
                holder: BindingViewHolder<ShowWidgetPictureQualityDialogItemBinding>,
                position: Int
            ) {
                val selected = currSelectedIndex == position
                val item = getItem(position)

                holder.binding.text.isActivated = selected
                holder.binding.text.text = item
                holder.binding.root.setOnClickListener {
                    setQuality(holder.adapterPosition)
                }
            }
        }
    }

    init {
        setBottomView(mBinding.root)
        mBinding.recycleView.adapter = mAdapter
        mAdapter.resetAll(
            listOf(
                context.getString(R.string.show_picture_quality_1080p),
                context.getString(R.string.show_picture_quality_720p),
                context.getString(R.string.show_picture_quality_540p),
                context.getString(R.string.show_picture_quality_360p),
                context.getString(R.string.show_picture_quality_270p),
                context.getString(R.string.show_picture_quality_180p)
            )
        )
    }

    fun setQuality(@QualityIndex qualityIndex: Int) {
        if (currSelectedIndex == qualityIndex) {
            return
        }
        val oIndex = currSelectedIndex
        currSelectedIndex = qualityIndex

        mAdapter.notifyItemChanged(oIndex)
        mAdapter.notifyItemChanged(currSelectedIndex)

        onQualitySelectListener?.invoke(this, currSelectedIndex)
    }

    fun getQuality() = currSelectedIndex

    fun setOnQualitySelectListener(listener: ((PictureQualityDialog, Int) -> Unit)){
        onQualitySelectListener = listener
    }

}