package io.agora.scene.widget.basic;

import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import java.lang.reflect.ParameterizedType;

public class BindingViewHolder<T extends ViewBinding> extends RecyclerView.ViewHolder {
    public final T binding;

    public BindingViewHolder(@NonNull T binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public T inflate(@NonNull LayoutInflater inflater) {
        try {
            Class<T> c = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
            if (c != null)
                return (T) c.getDeclaredMethod("inflate", LayoutInflater.class).invoke(null, inflater);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
