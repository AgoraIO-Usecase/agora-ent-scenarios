package io.agora.scene.pure1v1.rtt

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.divider.MaterialDividerItemDecoration
import io.agora.scene.base.utils.dp
import io.agora.scene.pure1v1.R
import io.agora.scene.pure1v1.databinding.Pure1v1RttSelectLanguageDialogBinding
import io.agora.scene.pure1v1.databinding.Pure1v1RttSelectLanguageItemBinding

class PureRttSelectLanguageDialog : BottomSheetDialog {

    companion object {
        const val SourceLanguageType = 0
        const val TargetLanguageType = 1
    }

    private val mBinding by lazy {
        Pure1v1RttSelectLanguageDialogBinding.inflate(LayoutInflater.from(context))
    }

    private var mAdapter: RttLanguageAdapter? = null

    private var type = SourceLanguageType

    constructor(context: Context, type: Int) : this(context, type, R.style.Pure1v1_bottom_full_dialog)

    constructor(context: Context, type: Int, theme: Int) : super(context, theme) {
        super.setContentView(mBinding.root)
        this.type = type
        initView()
    }

    override fun onStart() {
        super.onStart()
        val bottomSheet = findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        val container = findViewById<View>(com.google.android.material.R.id.container)
        bottomSheet?.let {
            it.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        }
        behavior.isDraggable = false
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        container?.let { view ->
            ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
                val inset = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPaddingRelative(inset.left, 0, inset.right, inset.bottom)
                WindowInsetsCompat.CONSUMED
            }
        }
    }

    private fun initView() {
        val list = PureRttManager.mRttLanguages.map { it.split(":")[1].trim() }
        mAdapter = RttLanguageAdapter(list, type)
        mBinding.rvLanguage.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        mBinding.rvLanguage.adapter = mAdapter
        mBinding.rvLanguage.addItemDecoration(
            MaterialDividerItemDecoration(
                context,
                MaterialDividerItemDecoration.VERTICAL
            ).apply {
                dividerThickness = 1.dp.toInt()
                dividerColor = ResourcesCompat.getColor(context.resources, R.color.def_diver_grey_F2F, null)
            })
        if (type == SourceLanguageType) {
            mBinding.mtBottomSheetTitle.text = context.getString(R.string.pure1v1_source_language)
        } else {
            mBinding.mtBottomSheetTitle.text = context.getString(R.string.pure1v1_target_language)
        }
        mBinding.tvClose.setOnClickListener { dismiss() }
    }

    inner class RttLanguageAdapter(private var mList: List<String>, val type: Int) :
        RecyclerView.Adapter<RttLanguageAdapter.LanguageHolder>() {

        inner class LanguageHolder(val binding: Pure1v1RttSelectLanguageItemBinding) :
            RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageHolder {
            val binding =
                Pure1v1RttSelectLanguageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return LanguageHolder(binding)
        }

        override fun getItemCount(): Int {
            return mList.size
        }

        override fun onBindViewHolder(holder: LanguageHolder, position: Int) {
            holder.binding.tvLanguage.text = mList[position]

            val context = holder.itemView.context
            val color = if (type == SourceLanguageType && PureRttManager.selectedSourceLanguageIndex == position) {
                ResourcesCompat.getColor(context.resources, R.color.bg_color_blue_31, null)
            } else if (type == TargetLanguageType && PureRttManager.selectedTargetLanguageIndex == position) {
                ResourcesCompat.getColor(context.resources, R.color.bg_color_blue_31, null)
            } else {
                ResourcesCompat.getColor(context.resources, R.color.def_text_color_3c4, null)
            }
            holder.binding.tvLanguage.setTextColor(color)

            holder.binding.root.setOnClickListener {
                if (type == SourceLanguageType) {
                    PureRttManager.selectedSourceLanguageIndex = position
                    dismiss()
                } else {
                    PureRttManager.selectedTargetLanguageIndex = position
                    dismiss()
                }
            }
        }
    }
}
