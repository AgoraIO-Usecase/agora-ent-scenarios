package io.agora.scene.aichat.create

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class QuickAdapter<B : ViewBinding, T>(
    private val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> B,
    private val datas: List<T>
) :
    RecyclerView.Adapter<QuickVH<B>>() {

    //        设置条目点击事件监听
    var onItemClickListener: ((List<T>, Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuickVH<B> {
        val binding = bindingInflater(LayoutInflater.from(parent.context), parent, false)
        return QuickVH(binding)
    }

    override fun onBindViewHolder(holder: QuickVH<B>, position: Int) {
        onBind(holder.binding, datas, position)
        holder.binding.root.setOnClickListener {
            onItemClickListener?.invoke(datas, position)
        }
    }


    override fun getItemCount(): Int {
        return datas.size
    }


    abstract fun onBind(binding: B, datas: List<T>, position: Int)


}


class QuickVH<B : ViewBinding>(val binding: B) : RecyclerView.ViewHolder(binding.root)
