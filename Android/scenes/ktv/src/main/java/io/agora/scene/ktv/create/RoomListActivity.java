package io.agora.scene.ktv.create;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.alibaba.android.arouter.facade.annotation.Route;

import io.agora.scene.base.KtvConstant;
import io.agora.scene.base.PagePathConstant;
import io.agora.scene.base.component.BaseRecyclerViewAdapter;
import io.agora.scene.base.component.BaseViewBindingActivity;
import io.agora.scene.base.component.OnItemClickListener;
import io.agora.scene.base.manager.PagePilotManager;
import io.agora.scene.base.manager.UserManager;
import io.agora.scene.base.utils.SPUtil;
import io.agora.scene.base.utils.ToastUtils;
import io.agora.scene.ktv.create.holder.RoomHolder;
import io.agora.scene.ktv.databinding.ActivityRoomListBinding;
import io.agora.scene.ktv.databinding.ItemRoomListBinding;
import io.agora.scene.ktv.live.RoomLivingActivity;
import io.agora.scene.ktv.service.VLRoomListModel;
import io.agora.scene.widget.dialog.InputPasswordDialog;

/**
 * 房间列表
 */
@Route(path = PagePathConstant.pageRoomList)
public class RoomListActivity extends BaseViewBindingActivity<ActivityRoomListBinding> {
    private BaseRecyclerViewAdapter<ItemRoomListBinding, VLRoomListModel, RoomHolder> mAdapter;
    private RoomCreateViewModel roomCreateViewModel;
    private InputPasswordDialog inputPasswordDialog;

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

    private void loadRoomList() {
        SPUtil.putBoolean(KtvConstant.IS_AGREE, true);
        roomCreateViewModel.loadRooms();
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState) {
        roomCreateViewModel = new ViewModelProvider(this).get(RoomCreateViewModel.class);
        mAdapter = new BaseRecyclerViewAdapter<>(null, new OnItemClickListener<VLRoomListModel>() {
            @Override
            public void onItemClick(@NonNull VLRoomListModel data, View view, int position, long viewType) {
                if (data.isPrivate() && !UserManager.getInstance().getUser().userNo.equals(data.getCreatorNo())) {
                    showInputPwdDialog(data);
                } else {
                    // RoomManager.getInstance().setAgoraRoom(data);
                    roomCreateViewModel.joinRoom(data.getRoomNo(), null);
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
            PagePilotManager.pageCreateRoomStep1();
        });
        getBinding().btnCreateRoom2.setOnClickListener(view -> {
            PagePilotManager.pageCreateRoomStep1();
        });
        roomCreateViewModel.roomModelList.observe(this, vlRoomListModels -> {
            hideLoadingView();
            getBinding().smartRefreshLayout.finishRefresh();
            if (vlRoomListModels == null) {
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
            if (ktvJoinRoomOutputModel == null) {
                ToastUtils.showToast("密码不正确");
                setDarkStatusIcon(isBlackDarkStatus());
            } else {
                RoomLivingActivity.launch(RoomListActivity.this, ktvJoinRoomOutputModel);
            }
        });
        getBinding().smartRefreshLayout.setOnRefreshListener(refreshLayout -> {
            loadRoomList();
        });
    }

    private void showInputPwdDialog(VLRoomListModel data) {
        if (inputPasswordDialog == null) {
            inputPasswordDialog = new InputPasswordDialog(this);
        }
        inputPasswordDialog.clearContent();
        inputPasswordDialog.iSingleCallback = (type, o) -> {
            data.setPassword((String) o);
            // RoomManager.getInstance().setAgoraRoom(data);
            roomCreateViewModel.joinRoom(data.getRoomNo(), (String) o);
        };
        inputPasswordDialog.show();
    }
}
