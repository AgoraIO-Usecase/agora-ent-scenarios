package com.agora.entfulldemo.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.agora.entfulldemo.databinding.AppFragmentHomeIndexBinding;

import io.agora.scene.base.component.BaseViewBindingFragment;
import io.agora.scene.base.manager.PagePilotManager;

public class HomeIndexFragment extends BaseViewBindingFragment<AppFragmentHomeIndexBinding> {

    @NonNull
    @Override
    protected AppFragmentHomeIndexBinding getViewBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return AppFragmentHomeIndexBinding.inflate(inflater);
    }

    @Override
    public void initView() {
    }

    @Override
    public void initListener() {
        getBinding().bgGoKTV.setOnClickListener(view -> PagePilotManager.pageKTVRoomList());
    }
}
