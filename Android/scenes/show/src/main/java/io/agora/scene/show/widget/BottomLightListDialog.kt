package io.agora.scene.show.widget

import android.content.Context
import android.view.LayoutInflater
import io.agora.scene.show.databinding.ShowWidgetBottomLightListDialogBinding
import io.agora.scene.show.databinding.ShowWidgetBottomLightListItemBinding
import io.agora.scene.widget.basic.BindingSingleAdapter
import io.agora.scene.widget.basic.BindingViewHolder


open class BottomLightListDialog(context: Context) : BottomLightDialog(context) {

    private val mBinding by lazy {
        ShowWidgetBottomLightListDialogBinding.inflate(
            LayoutInflater.from(
                context
            )
        )
    }

    private var onItemSelectedListener: ((BottomLightListDialog, Int)->Unit)? = null
    private var selectedItem = -1
    private val mAdapter by lazy {
        object : BindingSingleAdapter<String, ShowWidgetBottomLightListItemBinding>(){
            override fun onBindViewHolder(
                holder: BindingViewHolder<ShowWidgetBottomLightListItemBinding>,
                position: Int
            ) {
                val item = getItem(position) ?: return
                holder.binding.text.text = item
                holder.binding.text.isActivated = selectedItem == position
                holder.binding.text.setOnClickListener {
                    innerSetSelected(holder.adapterPosition)
                }
            }
        }
    }

    init {
        setBottomView(mBinding.root)
        mBinding.recyclerView.adapter = mAdapter
    }

    override fun setTitle(title: CharSequence?) {
        super.setTitle(title)
        mBinding.tvTitle.text = title
        mBinding.tvCancel.setOnClickListener {
            dismiss()
        }
    }

    fun setOnSelectedChangedListener(listener: (BottomLightListDialog, Int)->Unit){
        onItemSelectedListener = listener
    }

    fun setListData(data: List<String>){
        mAdapter.resetAll(data)
    }

    fun setSelectedPosition(position: Int){
        innerSetSelected(position)
    }

    fun getSelectedPosition() = selectedItem


    private fun innerSetSelected(position: Int){
        if(position == selectedItem){
            return
        }
        val oPosition = selectedItem
        selectedItem = position
        if(oPosition >= 0){
            mAdapter.notifyItemChanged(oPosition)
        }
        mAdapter.notifyItemChanged(selectedItem)
        onItemSelectedListener?.invoke(this, selectedItem)
    }





}