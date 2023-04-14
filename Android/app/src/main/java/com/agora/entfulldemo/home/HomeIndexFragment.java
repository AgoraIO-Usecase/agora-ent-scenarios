package com.agora.entfulldemo.home;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.agora.entfulldemo.R;
import com.agora.entfulldemo.databinding.AppFragmentHomeIndexBinding;
import com.agora.entfulldemo.databinding.AppItemHomeIndexBinding;
import com.agora.entfulldemo.databinding.AppItemHomeIndexBinding;
import com.agora.entfulldemo.home.constructor.ScenesConstructor;
import com.agora.entfulldemo.home.constructor.ScenesModel;
import com.agora.entfulldemo.home.holder.HomeIndexHolder;
import com.agora.entfulldemo.databinding.AppFragmentHomeIndexBinding;

import java.util.List;

import io.agora.scene.base.ReportApi;
import io.agora.scene.base.component.BaseRecyclerViewAdapter;
import io.agora.scene.base.component.BaseViewBindingFragment;
import io.agora.scene.base.component.OnItemClickListener;
import io.agora.scene.base.utils.ToastUtils;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

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
                        reportEnter(scenesModel);
                        goScene(scenesModel);
                    }
                }
            }, HomeIndexHolder.class);
            getBinding().rvScenes.setAdapter(homeIndexAdapter);
        }
    }

    private void reportEnter(@NonNull ScenesModel scenesModel){
        ReportApi.reportEnter(scenesModel.getScene(), new Function1<Boolean, Unit>() {
            @Override
            public Unit invoke(Boolean aBoolean) {
                return null;
            }
        },null);
    }

    private void goScene(@NonNull ScenesModel scenesModel) {
        Intent intent = new Intent();
        intent.setClassName(requireContext(), scenesModel.getClazzName());
        try {
            startActivity(intent);
        } catch (Exception e) {
            ToastUtils.showToast(R.string.app_coming_soon);
        }
    }

    @Override
    public void initListener() {
    }
}
