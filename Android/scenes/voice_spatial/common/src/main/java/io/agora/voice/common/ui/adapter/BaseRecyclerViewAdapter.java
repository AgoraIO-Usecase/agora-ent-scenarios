package io.agora.voice.common.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Size;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import io.agora.voice.common.ui.adapter.listener.OnItemChildClickListener;
import io.agora.voice.common.ui.adapter.listener.OnItemClickListener;
import io.agora.voice.common.utils.BaseUiTool;

/**
 * 基础RecyclerView adapter
 */
public class BaseRecyclerViewAdapter<B extends ViewBinding, T, H extends BaseRecyclerViewAdapter.BaseViewHolder<B, T>> extends RecyclerView.Adapter<H> {

    public @NonNull
    List<T> dataList;

    private final OnItemClickListener<T> mOnItemClickListener;
    private OnItemChildClickListener<T> mOnItemChildClickListener;
    public int selectedIndex = -1;

    private Class<B> bindingClass;
    private final Class<H> viewHolderClass;

    public BaseRecyclerViewAdapter(@Nullable List<T> dataList, Class<H> viewHolderClass) {
        this(dataList, null, viewHolderClass);
    }


    public BaseRecyclerViewAdapter(@Nullable List<T> dataList, @Nullable OnItemClickListener<T> listener, Class<H> viewHolderClass) {
        this(dataList, listener, null, viewHolderClass);
    }

    public BaseRecyclerViewAdapter(@Nullable List<T> dataList, @Nullable OnItemClickListener<T> listener, @Nullable OnItemChildClickListener<T> itemChildListener, Class<H> viewHolderClass) {
        this.viewHolderClass = viewHolderClass;
        if (dataList == null) {
            this.dataList = new ArrayList<>();
        } else {
            this.dataList = new ArrayList<>(dataList);
        }

        this.mOnItemClickListener = listener;
        this.mOnItemChildClickListener = itemChildListener;
    }

    @Nullable
    private H createHolder(B mBinding) {
        ensureBindingClass();
        try {
            Constructor<H> constructor = viewHolderClass.getConstructor(bindingClass);
            return constructor.newInstance(mBinding);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @NonNull
    @Override
    public H onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        B mBinding = getViewBindingByReflect(LayoutInflater.from(parent.getContext()), parent);
        H holder = createHolder(mBinding);

        assert holder != null;

        if (mOnItemClickListener != null) {
            holder.mListener = (view, position, itemViewType) -> {
                T itemData = getItemData(position);
                if (itemData == null)
                    mOnItemClickListener.onItemClick(view, position, viewType);
                else
                    mOnItemClickListener.onItemClick(itemData, view, position, viewType);
            };
        }
        if (mOnItemChildClickListener != null) {
            holder.mChildListener = (view, extData, position, itemViewType) -> {
                T itemData = getItemData(position);
                mOnItemChildClickListener.onItemChildClick(itemData, extData, view, position, viewType);
            };
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull H holder, int position) {
        if (dataList.size() > 0){
            T data = dataList.get(position);
            holder.binding(data, selectedIndex);
        }
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    @Nullable
    public T getItemData(int position) {

        if (position < 0 || dataList.size() <= position) {
            return null;
        }

        return dataList.get(position);
    }

    public B getViewBindingByReflect(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        ensureBindingClass();
        try {
            return BaseUiTool.getViewBinding(bindingClass, inflater, container);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //<editor-fold desc="CURD">
    public boolean contains(@NonNull T data) {
        return dataList.contains(data);
    }

    public int indexOf(@NonNull T data) {
        return dataList.indexOf(data);
    }

    public void submitListWithDiffCallback(@NonNull BaseDiffCallback<T> callback) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(callback);
        this.dataList = callback.getNewList();
        diffResult.dispatchUpdatesTo(this);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void submitListAndPurge(@NonNull List<T> dataList) {
        this.dataList.clear();
        this.dataList = dataList;
        notifyDataSetChanged();
    }

    public void addItems(@NonNull List<T> dataList) {
        int index = this.dataList.size();
        this.dataList.addAll(dataList);
        notifyItemRangeChanged(index, this.dataList.size() - index);
    }

    public void addItem(@NonNull T data) {

        int index = dataList.indexOf(data);
        if (index < 0) {
            dataList.add(data);
            notifyItemInserted(dataList.size() - 1);
        } else {
            dataList.set(index, data);
            notifyItemChanged(index);
        }
    }

    public void addItem(@NonNull T data, int index) {
        dataList.add(index, data);
        notifyItemRangeChanged(index, dataList.size() - index);
    }

    public void update(int index, @NonNull T data) {
        dataList.set(index, data);
        notifyItemChanged(index);
    }

    public void clear() {
        int formalCount = dataList.size();
        dataList.clear();
        notifyItemRangeRemoved(0, formalCount);
    }

    public void deleteItem(@Size(min = 0) int pos) {
        if (0 <= pos && pos < dataList.size()) {
            dataList.remove(pos);
            notifyItemRemoved(pos);
        }
    }

    public void deleteItem(@NonNull T data) {
        int index = dataList.indexOf(data);
        if (0 <= index && index < dataList.size()) {
            dataList.remove(data);
            notifyItemRemoved(index);
        }
    }

    //</editor-fold>
    public void ensureBindingClass() {
        if (bindingClass == null)
            bindingClass = BaseUiTool.getGenericClass(viewHolderClass, 0);
    }

    public static abstract class BaseViewHolder<B extends ViewBinding, T> extends RecyclerView.ViewHolder {
        public OnHolderItemClickListener mListener;
        public OnHolderItemChildClickListener mChildListener;
        public final B mBinding;

        public BaseViewHolder(@NonNull B mBinding) {
            super(mBinding.getRoot());
            this.mBinding = mBinding;
            mBinding.getRoot().setOnClickListener(this::onItemClick);
        }

        public void onItemClick(View view) {
            if (mListener != null) {
                mListener.onItemClick(view, getBindingAdapterPosition(), getItemViewType());
            }
        }

        public void onItemChildClick(Object extData, View view) {
            if (mChildListener != null) {
                mChildListener.onItemChildClick(view, extData, getBindingAdapterPosition(), getItemViewType());
            }
        }

        interface OnHolderItemClickListener {
            void onItemClick(View view, int position, int itemViewType);
        }

        interface OnHolderItemChildClickListener {
            void onItemChildClick(View view, Object extData, int position, int itemViewType);
        }

        public abstract void binding(@Nullable T data, int selectedIndex);

        public Context getContext() {
            return itemView.getContext();
        }
    }
}
