package com.agora.entfulldemo.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.agora.entfulldemo.databinding.FragmentHomeIndexBinding;
import com.alibaba.android.arouter.launcher.ARouter;

import io.agora.scene.base.PagePathConstant;
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
        getBinding().bgChatting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ARouter.getInstance()
                        .build(PagePathConstant.pageVoiceSplash)
                        .navigation();
            }
        });
    }
}
