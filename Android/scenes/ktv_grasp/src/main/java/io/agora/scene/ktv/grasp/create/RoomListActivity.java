package io.agora.scene.ktv.grasp.create;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.alibaba.android.arouter.facade.annotation.Route;

import io.agora.scene.base.PagePathConstant;
import io.agora.scene.base.component.BaseRecyclerViewAdapter;
import io.agora.scene.base.component.BaseViewBindingActivity;
import io.agora.scene.base.component.OnItemClickListener;
import io.agora.scene.base.utils.ToastUtils;
import io.agora.scene.ktv.grasp.create.holder.RoomHolder;
import io.agora.scene.ktv.grasp.databinding.ActivityRoomListBinding;
import io.agora.scene.ktv.grasp.databinding.ItemRoomListBinding;
import io.agora.scene.ktv.grasp.live.RoomLivingActivity;
import io.agora.scene.ktv.grasp.service.KTVServiceProtocol;
import io.agora.scene.ktv.grasp.service.RoomListModel;
import io.agora.scene.widget.dialog.InputPasswordDialog;
import io.agora.scene.widget.utils.UiUtils;

/**
 * 房间列表
 */
//@Route(path = PagePathConstant.pageKTVRoomList)
public class RoomListActivity extends BaseViewBindingActivity<ActivityRoomListBinding> {
    private BaseRecyclerViewAdapter<ItemRoomListBinding, RoomListModel, RoomHolder> mAdapter;
    private RoomCreateViewModel roomCreateViewModel;
    private InputPasswordDialog inputPasswordDialog;
    private boolean isJoining = false;

    @Override
    protected ActivityRoomListBinding getViewBinding(@NonNull LayoutInflater inflater) {
        return ActivityRoomListBinding.inflate(inflater);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setDarkStatusIcon(isBlackDarkStatus());
        loadRoomList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        KTVServiceProtocol.Companion.getImplInstance().reset();
    }

    private void loadRoomList() {
        roomCreateViewModel.loadRooms();
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState) {
        roomCreateViewModel = new ViewModelProvider(this).get(RoomCreateViewModel.class);
        mAdapter = new BaseRecyclerViewAdapter<>(null, new OnItemClickListener<RoomListModel>() {
            @Override
            public void onItemClick(@NonNull RoomListModel data, View view, int position, long viewType) {
                if (data.isPrivate()) {
                    showInputPwdDialog(data);
                } else {
                    // RoomManager.getInstance().setAgoraRoom(data);
                    if (!isJoining) {
                        isJoining = true;
                        roomCreateViewModel.joinRoom(data.getRoomNo(), null);
                    }
                }
            }
        }, RoomHolder.class);
        getBinding().rvRooms.setLayoutManager(new GridLayoutManager(this, 2));
        getBinding().rvRooms.setAdapter(mAdapter);
        getBinding().smartRefreshLayout.setEnableLoadMore(false);
    }

    @Override
    public void initListener() {
        getBinding().btnCreateRoom.setOnClickListener(view -> {
            if (UiUtils.isFastClick(2000)) {
                return;
            }
            startActivity(new Intent(this, RoomCreateActivity.class));
        });
        getBinding().btnCreateRoom2.setOnClickListener(view -> {
            if (UiUtils.isFastClick(2000)) {
                return;
            }
            startActivity(new Intent(this, RoomCreateActivity.class));
        });
        roomCreateViewModel.roomModelList.observe(this, vlRoomListModels -> {
            hideLoadingView();
            getBinding().smartRefreshLayout.finishRefresh();
            if (vlRoomListModels == null || vlRoomListModels.isEmpty()) {
                getBinding().rvRooms.setVisibility(View.GONE);
                getBinding().btnCreateRoom.setVisibility(View.GONE);
                getBinding().tvTips1.setVisibility(View.VISIBLE);
                getBinding().ivBgMobile.setVisibility(View.VISIBLE);
                getBinding().btnCreateRoom2.setVisibility(View.VISIBLE);
            } else {
                mAdapter.setDataList(vlRoomListModels);
                getBinding().rvRooms.setVisibility(View.VISIBLE);
                getBinding().btnCreateRoom.setVisibility(View.VISIBLE);
                getBinding().tvTips1.setVisibility(View.GONE);
                getBinding().ivBgMobile.setVisibility(View.GONE);
                getBinding().btnCreateRoom2.setVisibility(View.GONE);
            }
        });
        roomCreateViewModel.joinRoomResult.observe(this, ktvJoinRoomOutputModel -> {
            isJoining = false;
            if (ktvJoinRoomOutputModel == null) {
                setDarkStatusIcon(isBlackDarkStatus());
            } else {
                RoomLivingActivity.launch(RoomListActivity.this, ktvJoinRoomOutputModel);
            }
        });
        getBinding().smartRefreshLayout.setOnRefreshListener(refreshLayout -> {
            loadRoomList();
        });
    }

    private void showInputPwdDialog(RoomListModel data) {
        if (inputPasswordDialog == null) {
            inputPasswordDialog = new InputPasswordDialog(this);
        }
        inputPasswordDialog.clearContent();
        inputPasswordDialog.iSingleCallback = (type, o) -> {
            roomCreateViewModel.joinRoom(data.getRoomNo(), (String) o);
        };
        inputPasswordDialog.show();
    }
}
