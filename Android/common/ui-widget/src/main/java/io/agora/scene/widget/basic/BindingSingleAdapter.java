package io.agora.scene.widget.basic;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class BindingSingleAdapter<Data, Binding extends ViewBinding> extends RecyclerView.Adapter<BindingViewHolder<Binding>> {
    protected final List<Data> mDataList = new ArrayList<>();

    @NonNull
    @Override
    public final BindingViewHolder<Binding> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return createBindingViewHolder(getClass(), parent, 1);
    }

    public void insertFirst(Data item) {
        insert(0, item);
    }

    public void insertLast(Data item) {
        insert(getItemCount(), item);
    }

    public void insert(int index, Data item) {
        int itemCount = getItemCount();
        if (index < 0) {
            index = 0;
        }
        if (index > itemCount) {
            index = itemCount;
        }
        mDataList.add(index, item);
        notifyItemInserted(index);
    }

    public void resetAll(List<Data> list){
        if (list == null) {
            list = new ArrayList<>();
        }
        mDataList.clear();
        mDataList.addAll(list);
        notifyDataSetChanged();
    }

    public void insertAll(List<Data> list) {
        if (list == null || list.size() <= 0) {
            return;
        }
        int itemCount = getItemCount();
        mDataList.addAll(list);
        notifyItemRangeInserted(itemCount, list.size());
    }

    public void insertAll(Data[] list) {
        if (list == null) {
            return;
        }
        int itemCount = getItemCount();
        Collections.addAll(mDataList, list);
        notifyItemRangeInserted(itemCount, list.length);
    }

    public void remove(int index) {
        int itemCount = getItemCount();
        if (index < 0 || index > itemCount) {
            return;
        }
        mDataList.remove(index);
        notifyItemRemoved(index);
    }

    public void removeAll() {
        int itemCount = getItemCount();
        if (itemCount <= 0) {
            return;
        }
        mDataList.clear();
        notifyItemRangeRemoved(0, itemCount);
    }

    public Data getItem(int index){
        int itemCount = getItemCount();
        if (index < 0 || index > itemCount) {
            return null;
        }
        return mDataList.get(index);
    }

    @Override
    public final int getItemCount() {
        return mDataList.size();
    }

    @NonNull
    public static <Binding extends ViewBinding> BindingViewHolder<Binding> createBindingViewHolder(Class<?> aClass, @NonNull ViewGroup parent, int index) {
        Type genericSuperclass = aClass.getGenericSuperclass();
        Type[] actualTypeArguments;
        if (!(genericSuperclass instanceof ParameterizedType)) {
            return createBindingViewHolder(aClass.getSuperclass(), parent, index);
        } else {
            actualTypeArguments = ((ParameterizedType) genericSuperclass).getActualTypeArguments();
            if (actualTypeArguments.length < (index + 1)) {
                return createBindingViewHolder(aClass.getSuperclass(), parent, index);
            }
        }

        Class<Binding> c = (Class<Binding>) actualTypeArguments[index];
        Binding binding = null;
        try {
            binding = (Binding) c.getDeclaredMethod("inflate", LayoutInflater.class).invoke(null, LayoutInflater.from(parent.getContext()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new BindingViewHolder<>(binding);
    }

}
