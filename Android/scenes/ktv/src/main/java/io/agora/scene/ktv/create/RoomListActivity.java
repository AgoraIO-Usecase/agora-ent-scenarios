package io.agora.scene.ktv.create;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import io.agora.scene.base.GlideApp;
import io.agora.scene.base.component.BaseViewBindingActivity;
import io.agora.scene.base.component.OnItemClickListener;
import io.agora.scene.ktv.R;
import io.agora.scene.ktv.databinding.KtvActivityRoomListBinding;
import io.agora.scene.ktv.live.RoomLivingActivity;
import io.agora.scene.ktv.service.KTVServiceProtocol;
import io.agora.scene.ktv.service.RoomListModel;
import io.agora.scene.widget.dialog.InputPasswordDialog;
import io.agora.scene.widget.utils.UiUtils;

/**
 * 房间列表
 */
public class RoomListActivity extends BaseViewBindingActivity<KtvActivityRoomListBinding> {
    private RoomListAdapter mAdapter;
    private RoomCreateViewModel roomCreateViewModel;
    private InputPasswordDialog inputPasswordDialog;
    private boolean isJoining = false;

    @Override
    protected KtvActivityRoomListBinding getViewBinding(@NonNull LayoutInflater inflater) {
        return KtvActivityRoomListBinding.inflate(inflater);
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
        mAdapter = new RoomListAdapter(null, this, new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull Object data, View view, int position, long viewType) {
                RoomListModel model = (RoomListModel)data;
                if (model.isPrivate()) {
                    showInputPwdDialog(model);
                } else {
                    if (!isJoining) {
                        isJoining = true;
                        roomCreateViewModel.joinRoom(model.getRoomNo(), null);
                    }
                }
            }
        });
        getBinding().rvRooms.setLayoutManager(new GridLayoutManager(this, 2));
        getBinding().rvRooms.setAdapter(mAdapter);
        getBinding().smartRefreshLayout.setEnableLoadMore(false);
        setOnApplyWindowInsetsListener(getBinding().getRoot());
    }

    @Override
    public void initListener() {
        getBinding().btnCreateRoom.setOnClickListener(view -> {
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
                getBinding().tvTips1.setVisibility(View.VISIBLE);
                getBinding().ivBgMobile.setVisibility(View.VISIBLE);
            } else {
                mAdapter.setDataList(vlRoomListModels);
                getBinding().rvRooms.setVisibility(View.VISIBLE);
                getBinding().tvTips1.setVisibility(View.GONE);
                getBinding().ivBgMobile.setVisibility(View.GONE);
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

    private class RoomListAdapter extends RecyclerView.Adapter<RoomListAdapter.ViewHolder> {
        private List<RoomListModel> mList;
        private Context mContext;
        private OnItemClickListener mListener;
        RoomListAdapter(List<RoomListModel> list, Context context, OnItemClickListener listener) {
            mList = list;
            mContext = context;
            mListener = listener;
        }

        public void setDataList(List<RoomListModel> list) {
            mList = list;
            notifyDataSetChanged();
        }
        @NonNull
        @Override
        public RoomListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.ktv_item_room_list, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Integer num = position % 5;
            int resId = getResources().getIdentifier("ktv_img_room_item_bg_" + num, "mipmap", getPackageName());
            holder.ivBackground.setImageResource(resId);
            RoomListModel data = mList.get(position);
            GlideApp.with(holder.ivAvatar.getContext()).load(data.getCreatorAvatar())
                    .into(holder.ivAvatar);
            holder.tvRoomName.setText(data.getName());
            holder.tvPersonNum.setText(holder.itemView.getContext().getString(R.string.ktv_people_count, data.getRoomPeopleNum()));
            holder.tvUserName.setText(data.getCreatorName());
            if (data.isPrivate()){
                holder.ivLock.setVisibility(View.VISIBLE);
            } else{
                holder.ivLock.setVisibility(View.GONE);
            }
            holder.itemView.setOnClickListener( view -> {
                mListener.onItemClick(data, view, position, getItemViewType(position));
            });
        }

        @Override
        public int getItemCount() {
            if (mList == null) {
                return 0;
            } else  {
                return mList.size();
            }
        }

        private class ViewHolder extends RecyclerView.ViewHolder {
            public ImageView ivBackground;
            public ImageView ivAvatar;
            public ImageView ivLock;
            public TextView tvRoomName;
            public TextView tvUserName;
            public TextView tvPersonNum;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                ivBackground = itemView.findViewById(R.id.ivBackground);
                ivAvatar = itemView.findViewById(R.id.ivAvatar);
                ivLock = itemView.findViewById(R.id.ivLock);
                tvRoomName = itemView.findViewById(R.id.tvRoomName);
                tvUserName = itemView.findViewById(R.id.tvUserName);
                tvPersonNum = itemView.findViewById(R.id.tvPersonNum);
            }
        }
    }
}
