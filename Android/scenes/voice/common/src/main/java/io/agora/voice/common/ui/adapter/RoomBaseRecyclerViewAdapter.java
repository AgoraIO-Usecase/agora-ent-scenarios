package io.agora.voice.common.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import io.agora.voice.common.R;
import io.agora.voice.common.ui.adapter.listener.OnAdapterItemClickListener;
import io.agora.voice.common.ui.adapter.listener.OnItemLongClickListener;

import java.util.ArrayList;
import java.util.List;


/**
 * As a base class of RecyclerView Adapter, there is a default blank layout
 * You can modify the default layout in the following two ways:
 * 1、Create a new ease_layout_default_no_data.xml overlay in app Layout.
 * 2、Inheriting EaseBaseRecyclerViewAdapter, rewrite getEmptyLayoutId () method, return to the layout of the custom.
 * @param <T>
 */
public abstract class RoomBaseRecyclerViewAdapter<T> extends RecyclerView.Adapter<RoomBaseRecyclerViewAdapter.ViewHolder<T>> {
    public static final int VIEW_TYPE_EMPTY = -1;
    public static final int VIEW_TYPE_ITEM = 0;
    protected OnAdapterItemClickListener mOnItemClickListener;
    protected OnItemLongClickListener mOnItemLongClickListener;
    protected OnItemSubViewClickListener mItemSubViewListener;
    public Context mContext;
    public List<T> mData;
    private boolean hideEmptyView;
    private View emptyView;
    private int emptyViewId;

    @NonNull
    @Override
    public ViewHolder<T> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        if(viewType == VIEW_TYPE_EMPTY) {
            return getEmptyViewHolder(parent);
        }
        ViewHolder<T> holder = getViewHolder(parent, viewType);
        if(isItemClickEnable()) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemClickAction(v, holder.getBindingAdapterPosition());
                }
            });
        }
        if(isItemLongClickEnable()) {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return itemLongClickAction(v, holder.getBindingAdapterPosition());
                }
            });
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder<T> holder, final int position) {
        holder.setAdapter(this);
        if(isEmptyViewType(position)) {
            holder.setEmptyData();
            return;
        }
        if(mData == null || mData.isEmpty()) {
            return;
        }
        T item = getItem(position);
        holder.setData(item, position);
        holder.setDataList(mData, position);
    }

    /**
     * Check if it is an empty layout type
     * @param position
     * @return
     */
    public boolean isEmptyViewType(int position) {
        int viewType = getItemViewType(position);
        return viewType == VIEW_TYPE_EMPTY;
    }

    public boolean itemLongClickAction(View v, int position) {
        if(mOnItemLongClickListener != null) {
            return mOnItemLongClickListener.onItemLongClick(v, position);
        }
        return false;
    }

    @Override
    public int getItemCount() {
        return (mData == null || mData.isEmpty()) ? 1 : mData.size();
    }

    /**
     * If you want to add more view type and use default empty view implementation, you should override {@link #getItemNotEmptyViewType(int)}
     * @param position
     * @return
     */
    @Override
    public int getItemViewType(int position) {
        return (mData == null || mData.isEmpty()) ? VIEW_TYPE_EMPTY : getItemNotEmptyViewType(position);
    }

    /**
     * If you want to add more view type and use default empty view implementation, you should override the method
     * @param position
     * @return
     */
    public int getItemNotEmptyViewType(int position) {
        return VIEW_TYPE_ITEM;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Check if item click can be used
     * Default is true
     * @return
     */
    public boolean isItemClickEnable() {
        return true;
    }

    /**
     * Check if long click can be used
     * Default is true
     * @return
     */
    public boolean isItemLongClickEnable() {
        return true;
    }


    /**
     * Click event
     * @param v
     * @param position
     */
    public void itemClickAction(View v, int position) {
        if(mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(v, position);
        }
    }

    /**
     * Returns the layout with null data
     * @param parent
     * @return
     */
    protected ViewHolder<T> getEmptyViewHolder(ViewGroup parent) {
        View emptyView = getEmptyView(parent);
        if(this.emptyView != null) {
            emptyView = this.emptyView;
        }
        if(this.emptyViewId > 0) {
            emptyView = LayoutInflater.from(mContext).inflate(this.emptyViewId, parent, false);
        }
        if(hideEmptyView) {
            emptyView = LayoutInflater.from(mContext).inflate(R.layout.voice_layout_no_data_show_nothing, parent, false);
        }
        return new ViewHolder<T>(emptyView) {

            @Override
            public void initView(View itemView) {

            }

            @Override
            public void setData(T item, int position) {

            }
        };
    }

    /**
     * Hide blank layout
     * @param hide
     */
    public void hideEmptyView(boolean hide) {
        hideEmptyView = hide;
        notifyDataSetChanged();
    }

    /**
     * Setting a blank layout
     * @param emptyView
     */
    public void setEmptyView(View emptyView) {
        this.emptyView = emptyView;
        notifyDataSetChanged();
    }

    /**
     * Setting a blank layout
     * @param emptyViewId
     */
    public void setEmptyView(@LayoutRes int emptyViewId) {
        this.emptyViewId = emptyViewId;
        notifyDataSetChanged();
    }

    /**
     * Getting a blank view
     * @param parent
     * @return
     */
    private View getEmptyView(ViewGroup parent) {
        return LayoutInflater.from(mContext).inflate(getEmptyLayoutId(), parent, false);
    }

    /**
     * Getting ViewHolder
     * @param parent
     * @param viewType
     * @return
     */
    public abstract ViewHolder<T> getViewHolder(ViewGroup parent, int viewType);

    /**
     * Get the corresponding data according to position
     * @param position
     * @return
     */
    public T getItem(int position) {
        return mData == null ? null : mData.get(position);
    }

    /**
     * Set data
     * @param data
     */
    public void setData(List<T> data) {
        this.mData = data;
        notifyDataSetChanged();
    }

    /**
     * Add a single piece of data
     * @param item
     */
    public void addData(T item) {
        synchronized (RoomBaseRecyclerViewAdapter.class) {
            if(this.mData == null) {
                this.mData = new ArrayList<>();
            }
            this.mData.add(item);
        }
        notifyDataSetChanged();
    }

    /**
     * Add more data
     * @param data
     */
    public void addData(List<T> data) {
        synchronized (RoomBaseRecyclerViewAdapter.class) {
            if(data == null || data.isEmpty()) {
                return;
            }
            if(this.mData == null) {
                this.mData = data;
            }else {
                this.mData.addAll(data);
            }
        }
        notifyDataSetChanged();
    }

    /**
     * Add more data
     * @param position
     * @param data
     */
    public void addData(int position, List<T> data) {
        synchronized (RoomBaseRecyclerViewAdapter.class) {
            if(data == null || data.isEmpty()) {
                return;
            }
            if(this.mData == null) {
                this.mData = data;
            }else {
                this.mData.addAll(position, data);
            }
        }
        notifyDataSetChanged();
    }

    /**
     * Add more data
     * @param position
     * @param data
     * @param refresh
     */
    public void addData(int position, List<T> data, boolean refresh) {
        synchronized (RoomBaseRecyclerViewAdapter.class) {
            if(data == null || data.isEmpty()) {
                return;
            }
            if(this.mData == null) {
                this.mData = data;
            }else {
                this.mData.addAll(position, data);
            }
        }
        if(refresh) {
            notifyDataSetChanged();
        }
    }

    /**
     * Get data
     * @return
     */
    public List<T> getData() {
        return mData;
    }

    /**
     * Clear data
     */
    public void clearData() {
        if(mData != null) {
            mData.clear();
            notifyDataSetChanged();
        }
    }

    /**
     * set item click
     * @param listener
     */
    public void setOnItemClickListener(OnAdapterItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    /**
     * set item long click
     * @param longClickListener
     */
    public void setOnItemLongClickListener(OnItemLongClickListener longClickListener) {
        mOnItemLongClickListener = longClickListener;
    }

    /**
     * set item sub view click
     * @param mItemSubViewListener
     */
    public void setOnItemSubViewClickListener(OnItemSubViewClickListener mItemSubViewListener) {
        this.mItemSubViewListener = mItemSubViewListener;
    }

    public abstract static class ViewHolder<T> extends RecyclerView.ViewHolder {
        private RecyclerView.Adapter<ViewHolder<T>> adapter;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            initView(itemView);
        }

        /**
         * Set data when viewType is VIEW_TYPE_EMPTY
         */
        public void setEmptyData(){}

        /**
         * Initialize the views
         * @param itemView
         */
        public void initView(View itemView){}

        /**
         * Set data
         * @param item
         * @param position
         */
        public abstract void setData(T item, int position);

        /**
         * @param id
         * @param <E>
         * @return
         */
        public  <E extends View> E findViewById(@IdRes int id) {
            return this.itemView.findViewById(id);
        }

        /**
         * Set data to provide a data set
         * @param data
         * @param position
         */
        public void setDataList(List<T> data, int position) { }

        /**
         * Set adapter
         * @param adapter
         */
        private void setAdapter(RoomBaseRecyclerViewAdapter<T> adapter) {
            this.adapter = adapter;
        }

        /**
         * Get adapter
         * @return
         */
        public RecyclerView.Adapter<ViewHolder<T>> getAdapter() {
            return adapter;
        }
    }

    /**
     * Return the blank layout
     * @return
     */
    public int getEmptyLayoutId() {
        return R.layout.voice_layout_no_data_show_nothing;
    }

    /**
     * item sub view interface
     */
    public interface OnItemSubViewClickListener {
        void onItemSubViewClick(View view, int position);
    }
}
