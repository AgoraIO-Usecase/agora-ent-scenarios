package com.agora.entfulldemo.home;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agora.entfulldemo.R;
import com.agora.entfulldemo.databinding.AppFragmentHomeIndexNewBinding;
import com.agora.entfulldemo.databinding.AppItemHomeIndexBinding;
import com.agora.entfulldemo.home.constructor.ScenesConstructor;
import com.agora.entfulldemo.home.constructor.ScenesModel;
import com.agora.entfulldemo.home.holder.HomeIndexHolder;
import com.alibaba.android.arouter.launcher.ARouter;
import com.google.android.material.divider.MaterialDividerItemDecoration;

import java.util.List;

import io.agora.rtc2.internal.DeviceUtils;
import io.agora.scene.base.PagePathConstant;
import io.agora.scene.base.component.BaseRecyclerViewAdapter;
import io.agora.scene.base.component.BaseViewBindingFragment;
import io.agora.scene.base.component.OnItemClickListener;
import io.agora.scene.base.manager.PagePilotManager;
import io.agora.scene.base.utils.KTVUtil;

public class HomeIndexFragment extends BaseViewBindingFragment<AppFragmentHomeIndexNewBinding> {

    @NonNull
    @Override
    protected AppFragmentHomeIndexNewBinding getViewBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return AppFragmentHomeIndexNewBinding.inflate(inflater);
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
            MaterialDividerItemDecoration dividerItemDecorationV = new MaterialDividerItemDecoration(context, MaterialDividerItemDecoration.VERTICAL);
            dividerItemDecorationV.setDividerThickness(KTVUtil.dp2px(8));
            dividerItemDecorationV.setDividerColor(Color.TRANSPARENT);
            int padding = KTVUtil.dp2px(8);
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
//            getBinding().rvScenes.addItemDecoration(dividerItemDecorationV);
            getBinding().rvScenes.addItemDecoration(itemDecoration);
            getBinding().rvScenes.setAdapter(homeIndexAdapter);
        }
    }

    private void goScene(@NonNull ScenesModel scenesModel) {
        switch (scenesModel.getType()) {
            case Ktv_Online:
                PagePilotManager.pageRoomList();
                break;
            case Voice_Chat:
                ARouter.getInstance()
                        .build(PagePathConstant.pageVoiceChat)
                        .navigation();
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
//        getBinding().bgGoKTV.setOnClickListener(view -> PagePilotManager.pageRoomList());
//        getBinding().bgChatting.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ARouter.getInstance()
//                        .build(PagePathConstant.pageVoiceSplash)
//                        .navigation();
//            }
//        });
    }
}
