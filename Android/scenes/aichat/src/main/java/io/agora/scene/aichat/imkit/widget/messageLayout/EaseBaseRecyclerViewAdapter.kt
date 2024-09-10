package io.agora.scene.aichat.imkit.widget.messageLayout

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import io.agora.scene.aichat.R

abstract class EaseBaseAdapter<VH : RecyclerView.ViewHolder?> :
    RecyclerView.Adapter<VH>() {
    /**
     * Get the data item associated with the specified position in the data set.
     * @param position
     * @return
     */
    abstract fun getItem(position: Int): Any?
}

/**
 * As a base class of RecyclerView Adapter, there is a default blank layout
 * You can modify the default layout in the following two ways:
 * 1、Create a new ease_layout_default_no_data.xml overlay in app Layout.
 * 2、Inheriting EaseBaseRecyclerViewAdapter, rewrite getEmptyLayoutId () method, return to the layout of the custom.
 * @param <T>
</T> */
abstract class EaseBaseRecyclerViewAdapter<T> :
    EaseBaseAdapter<EaseBaseRecyclerViewAdapter.ViewHolder<T>>() {

    protected var mOnItemClickListener: ((view: View?, position: Int) -> Unit)? = null
    protected var mOnItemLongClickListener: ((view: View?, position: Int) -> Boolean)? = null
    protected var mItemSubViewListener: ((view: View?, position: Int) -> Unit)? = null
    var mContext: Context? = null
    var mData: MutableList<T>? = null
    private var isHideEmptyView = false
    private var emptyView: View? = null
    private var emptyViewId = 0
    private val emptyLayoutId: Int
        /**
         * Return the blank layout
         * @return
         */
        get() = R.layout.ease_layout_default_no_data

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<T> {
        mContext = parent.context
        if (viewType == VIEW_TYPE_EMPTY) {
            return getEmptyViewHolder(parent)
        }
        val holder = getViewHolder(parent, viewType)
        if (isItemClickEnable) {
            holder.itemView.setOnClickListener { v ->
                if (holder.bindingAdapterPosition >= 0) {
                    itemClickAction(v, holder.bindingAdapterPosition)
                }
            }
        }
        if (isItemLongClickEnable) {
            holder.itemView.setOnLongClickListener { v ->
                itemLongClickAction(
                    v,
                    holder.bindingAdapterPosition
                )
            }
        }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder<T>, position: Int) {
        holder.setAdapter(this)
        if (isEmptyViewType(position)) {
            holder.setEmptyData()
            return
        }
        if (mData.isNullOrEmpty()) {
            return
        }
        holder.setDataList(mData, position)
        val item = getItem(position)
        holder.setData(item, position)
    }

    /**
     * Check if it is an empty layout type
     * @param position
     * @return
     */
    private fun isEmptyViewType(position: Int): Boolean {
        val viewType = getItemViewType(position)
        return viewType == VIEW_TYPE_EMPTY
    }

    private fun itemLongClickAction(v: View?, position: Int): Boolean {
        return mOnItemLongClickListener?.invoke(v, position) ?: false
    }

    override fun getItemCount(): Int {
        return if (mData.isNullOrEmpty()) 1 else mData!!.size
    }

    /**
     * If you want to add more view type and use default empty view implementation, you should override [.getItemNotEmptyViewType]
     * @param position
     * @return
     */
    override fun getItemViewType(position: Int): Int {
        return if (mData.isNullOrEmpty()) {
            VIEW_TYPE_EMPTY
        } else getItemNotEmptyViewType(position)
    }

    /**
     * If you want to add more view type and use default empty view implementation, you should override the method
     * @param position
     * @return
     */
    open fun getItemNotEmptyViewType(position: Int): Int {
        return VIEW_TYPE_ITEM
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    val isItemClickEnable: Boolean
        /**
         * Check if item click can be used
         * Default is true
         * @return
         */
        get() = true
    val isItemLongClickEnable: Boolean
        /**
         * Check if long click can be used
         * Default is true
         * @return
         */
        get() = true

    /**
     * Click event
     * @param v
     * @param position
     */
    private fun itemClickAction(v: View?, position: Int) {
        mOnItemClickListener?.invoke(v, position)
    }

    /**
     * Returns the layout with null data
     * @param parent
     * @return
     */
    protected fun getEmptyViewHolder(parent: ViewGroup): ViewHolder<T> {
        var emptyView: View = getEmptyView(parent)
        this.emptyView?.let {
            emptyView = it
        }
        if (emptyViewId > 0) {
            emptyView = LayoutInflater.from(mContext).inflate(emptyViewId, parent, false)
        }
        if (isHideEmptyView) {
            emptyView = LayoutInflater.from(mContext)
                .inflate(R.layout.ease_layout_no_data_show_nothing, parent, false)
        }
        return object : ViewHolder<T>(emptyView) {
            override fun initView(itemView: View?) {}
            override fun setData(item: T?, position: Int) {}
        }
    }

    /**
     * Hide blank layout
     * @param hide
     */
    fun hideEmptyView(hide: Boolean) {
        isHideEmptyView = hide
        notifyDataSetChanged()
    }

    /**
     * Setting a blank layout
     * @param emptyView
     */
    fun setEmptyView(emptyView: View?) {
        this.emptyView = emptyView
        notifyDataSetChanged()
    }

    /**
     * Setting a blank layout
     * @param emptyViewId
     */
    fun setEmptyView(@LayoutRes emptyViewId: Int) {
        this.emptyViewId = emptyViewId
        notifyDataSetChanged()
    }

    /**
     * Getting a blank view
     * @param parent
     * @return
     */
    private fun getEmptyView(parent: ViewGroup): View {
        return LayoutInflater.from(mContext).inflate(emptyLayoutId, parent, false)
    }

    /**
     * Getting ViewHolder
     * @param parent
     * @param viewType
     * @return
     */
    abstract fun getViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<T>

    /**
     * Get the corresponding data according to position
     * @param position
     * @return
     */
    override fun getItem(position: Int): T? {
        mData?.let {
            return if (it.size > position) it[position] else null
        }
        return null
    }

    /**
     * Set data
     * @param data
     */
    fun setData(data: MutableList<T>?) {
        mData = data
        notifyDataSetChanged()
    }

    /**
     * Add a single piece of data
     * @param item
     */
    fun addData(item: T) {
        synchronized(EaseBaseRecyclerViewAdapter::class.java) {
            if (mData == null) {
                mData = ArrayList()
            }
            mData?.add(item)
        }
        notifyDataSetChanged()
    }


    /**
     * Add a single piece of data
     * @param position
     * @param item
     */
    fun addData(position: Int, item: T) {
        synchronized(EaseBaseRecyclerViewAdapter::class.java) {
            if (mData == null) {
                mData = ArrayList()
            }
            if (position >= 0) {
                mData?.add(position, item)
                notifyItemInserted(position)
            }
        }

    }

    /**
     * Add more data
     * @param data
     */
    fun addData(data: MutableList<T>?) {
        synchronized(EaseBaseRecyclerViewAdapter::class.java) {
            if (data.isNullOrEmpty()) {
                return
            }
            if (mData == null) {
                mData = data
            } else {
                mData?.addAll(data)
            }
        }
        notifyDataSetChanged()
    }

    /**
     * Add more data
     * @param position
     * @param data
     */
    fun addData(position: Int, data: MutableList<T>?) {
        synchronized(EaseBaseRecyclerViewAdapter::class.java) {
            if (data.isNullOrEmpty()) {
                return
            }
            if (mData == null) {
                mData = data
            } else {
                mData?.addAll(position, data)
            }
        }
        notifyDataSetChanged()
    }

    /**
     * Add more data
     * @param position
     * @param data
     * @param refresh
     */
    fun addData(position: Int, data: MutableList<T>?, refresh: Boolean) {
        synchronized(EaseBaseRecyclerViewAdapter::class.java) {
            if (data.isNullOrEmpty()) {
                return
            }
            if (mData == null) {
                mData = data
            } else {
                mData?.addAll(position, data)
            }
        }
        if (refresh) {
            notifyDataSetChanged()
        }
    }

    val data: List<T>?
        /**
         * Get data
         * @return
         */
        get() = mData

    /**
     * Clear data
     */
    fun clearData() {
        synchronized(EaseBaseRecyclerViewAdapter::class.java) {
            if (mData != null) {
                mData?.clear()
                notifyDataSetChanged()
            }
        }
    }

    /**
     * update position data
     */
    fun changeData(position: Int, item: T) {
        synchronized(EaseBaseRecyclerViewAdapter::class.java) {
            mData?.set(position, item)
        }
    }

    /**
     * set item click
     * @param listener
     */
    fun setOnItemClickListener(listener: ((view: View?, position: Int) -> Unit)) {
        mOnItemClickListener = listener
    }

    /**
     * set item long click
     * @param longClickListener
     */
    fun setOnItemLongClickListener(longClickListener: ((view: View?, position: Int) -> Boolean)) {
        mOnItemLongClickListener = longClickListener
    }

    /**
     * set item sub view click
     * @param itemSubViewListener
     */
    fun setOnItemSubViewClickListener(itemSubViewListener: ((view: View?, position: Int) -> Unit)) {
        this.mItemSubViewListener = itemSubViewListener
    }

    abstract class ViewHolder<T> constructor(itemView: View? = null, binding: ViewBinding? = null) :
        RecyclerView.ViewHolder(itemView ?: binding?.root!!) {
        /**
         * Get adapter
         * @return
         */
        var adapter: EaseBaseAdapter<ViewHolder<T>>? = null
            private set

        init {
            initView(itemView ?: binding?.root)
            initView(binding)
        }

        /**
         * Set data when viewType is VIEW_TYPE_EMPTY
         */
        fun setEmptyData() {}

        /**
         * Initialize the views
         * @param itemView
         */
        open fun initView(itemView: View?) {}

        /**
         * Initialize the views
         * @param viewBinding
         */
        open fun initView(viewBinding: ViewBinding?) {}

        /**
         * Set data
         * @param item
         * @param position
         */
        abstract fun setData(item: T?, position: Int)

        /**
         * @param id
         * @param <E>
         * @return
        </E> */
        fun <E : View?> findViewById(@IdRes id: Int): E {
            return itemView.findViewById(id)
        }

        /**
         * Set data to provide a data set
         * @param data
         * @param position
         */
        open fun setDataList(data: List<T>?, position: Int) {}

        /**
         * Set adapter
         * @param adapter
         */
        fun setAdapter(adapter: EaseBaseRecyclerViewAdapter<T>) {
            this.adapter = adapter
        }
    }

    companion object {
        const val VIEW_TYPE_EMPTY = -1
        const val VIEW_TYPE_ITEM = 0
    }
}