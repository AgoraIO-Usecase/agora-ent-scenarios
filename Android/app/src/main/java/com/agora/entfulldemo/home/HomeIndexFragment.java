package com.agora.entfulldemo.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.agora.entfulldemo.databinding.FragmentHomeIndexBinding;

import io.agora.scene.base.component.BaseViewBindingFragment;
import io.agora.scene.base.manager.PagePilotManager;

public class HomeIndexFragment extends BaseViewBindingFragment<FragmentHomeIndexBinding> {

    @NonNull
    @Override
    protected FragmentHomeIndexBinding getViewBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentHomeIndexBinding.inflate(inflater);
    }

    @Override
    public void initView() {
    }

    @Override
    public void initListener() {
        getBinding().bgGoKTV.setOnClickListener(view -> PagePilotManager.pageRoomList());
    }
}
