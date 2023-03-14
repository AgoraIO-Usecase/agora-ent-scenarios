package com.agora.entfulldemo.home;

import android.content.Context;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.agora.entfulldemo.databinding.AppFragmentHomeIndexBinding;
import com.agora.entfulldemo.databinding.AppItemHomeIndexBinding;
import com.agora.entfulldemo.home.constructor.ScenesConstructor;
import com.agora.entfulldemo.home.constructor.ScenesModel;
import com.agora.entfulldemo.home.holder.HomeIndexHolder;
import com.alibaba.android.arouter.launcher.ARouter;

import java.util.List;

import io.agora.scene.base.PagePathConstant;
import io.agora.scene.base.component.BaseRecyclerViewAdapter;
import io.agora.scene.base.component.BaseViewBindingFragment;
import io.agora.scene.base.component.OnItemClickListener;
import io.agora.scene.base.manager.PagePilotManager;
import io.agora.scene.base.utils.UiUtil;

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
            int padding = UiUtil.dp2px(8);
            RecyclerView.ItemDecoration itemDecoration = new RecyclerView.ItemDecoration() {
                @Override
                public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                    super.getItemOffsets(outRect, view, parent, state);
                    outRect.top = padding;
                    outRect.bottom = padding;
                    outRect.left = padding;
                    outRect.right = padding;
                }
            };
            getBinding().rvScenes.addItemDecoration(itemDecoration);
            getBinding().rvScenes.setAdapter(homeIndexAdapter);
        }
    }

    private void goScene(@NonNull ScenesModel scenesModel) {
        switch (scenesModel.getType()) {
            case Ktv_Online:
                PagePilotManager.pageKTVRoomList();
                break;
            case Voice_Chat:
                break;
            case Meta_Live:
                break;
            case Meta_Chat:
                break;
            case Games:
                break;
            default:
                break;
        }
    }

    @Override
    public void initListener() {
    }
}
