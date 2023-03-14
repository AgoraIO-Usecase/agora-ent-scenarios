package io.agora.voice.common.ui.adapter;

import androidx.recyclerview.widget.DiffUtil;

import java.util.List;

public abstract class BaseDiffCallback<T> extends DiffUtil.Callback {
    private final List<T> oldList;
    private final List<T> newList;

    public BaseDiffCallback(List<T> oldList, List<T> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList == null ? 0 : oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList == null ? 0 : newList.size();
    }

    public List<T> getOldList() {
        return oldList;
    }

    public List<T> getNewList() {
        return newList;
    }
}