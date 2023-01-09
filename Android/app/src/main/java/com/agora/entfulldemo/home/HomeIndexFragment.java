package com.agora.entfulldemo.home;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.agora.entfulldemo.databinding.AppFragmentHomeIndexBinding;
import com.agora.entfulldemo.databinding.AppItemHomeIndexBinding;
import com.agora.entfulldemo.databinding.AppItemHomeIndexBinding;
import com.agora.entfulldemo.home.constructor.ScenesConstructor;
import com.agora.entfulldemo.home.constructor.ScenesModel;
import com.agora.entfulldemo.home.holder.HomeIndexHolder;
import com.agora.entfulldemo.databinding.AppFragmentHomeIndexBinding;

import java.util.List;

import io.agora.scene.base.component.BaseRecyclerViewAdapter;
import io.agora.scene.base.component.BaseViewBindingFragment;
import io.agora.scene.base.component.OnItemClickListener;
import io.agora.scene.base.utils.ToastUtils;

public class HomeIndexFragment extends BaseViewBindingFragment<AppFragmentHomeIndexBinding> {

    @NonNull
    @Override
    protected AppFragmentHomeIndexBinding getViewBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return AppFragmentHomeIndexBinding.inflate(inflater);
    }

    @Override
    public void initView() {

        Context context = getContext();
        if (context != null) {
            List<ScenesModel> scenesModels = ScenesConstructor.buildData(context);
            BaseRecyclerViewAdapter<AppItemHomeIndexBinding, ScenesModel, HomeIndexHolder> homeIndexAdapter = new BaseRecyclerViewAdapter<>(scenesModels, new OnItemClickListener<ScenesModel>() {
                @Override
                public void onItemClick(@NonNull ScenesModel scenesModel, View view, int position, long viewType) {
                    if (scenesModel.getActive()) {
                        goScene(scenesModel);
                    }
                }
            }, HomeIndexHolder.class);
            getBinding().rvScenes.setAdapter(homeIndexAdapter);
        }
    }

    private void goScene(@NonNull ScenesModel scenesModel) {
        Intent intent = new Intent();
        intent.setClassName(requireContext(), scenesModel.getClazzName());
        try {
            startActivity(intent);
        } catch (Exception e) {
            ToastUtils.showToast(e.getMessage());
        }
    }

    @Override
    public void initListener() {
    }
}
