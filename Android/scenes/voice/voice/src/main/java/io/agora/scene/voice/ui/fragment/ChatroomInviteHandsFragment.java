package io.agora.scene.voice.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.agora.voice.baseui.BaseListFragment;
import io.agora.voice.baseui.adapter.RoomBaseRecyclerViewAdapter;
import io.agora.voice.baseui.general.callback.OnResourceParseCallback;
import io.agora.voice.buddy.tool.LogTools;
import io.agora.voice.buddy.tool.ThreadManager;
import io.agora.voice.buddy.tool.ToastTools;
import io.agora.scene.voice.R;
import io.agora.scene.voice.general.net.ChatroomHttpManager;
import io.agora.scene.voice.model.RoomInviteViewModel;
import io.agora.scene.voice.ui.adapter.ChatroomInviteAdapter;
import io.agora.voice.network.tools.VRValueCallBack;
import io.agora.voice.network.tools.bean.VMemberBean;
import io.agora.voice.network.tools.bean.VRoomUserBean;

public class ChatroomInviteHandsFragment extends BaseListFragment<VMemberBean> implements ChatroomInviteAdapter.onActionListener, SwipeRefreshLayout.OnRefreshListener {
    private RecyclerView recyclerView;
    private SwipeRefreshLayout refreshLayout;
    private RoomInviteViewModel handsViewModel;
    private List<VMemberBean> dataList = new ArrayList<>();
    private ChatroomInviteAdapter adapter;
    private int pageSize = 10;
    private String cursor = "";
    private itemCountListener listener;
    private String roomId;
    private static final String TAG = "ChatroomInviteHandsFragment";
    private Map<String, Boolean> map = new HashMap<>();
    private boolean isRefreshing = false;
    private boolean isLoadingNextPage = false;
    private View emptyView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        emptyView = getLayoutInflater().inflate(R.layout.voice_no_data_layout, container, false);
        TextView textView = emptyView.findViewById(R.id.content_item);
        textView.setText(getString(R.string.voice_empty_invite_hands));
        LinearLayoutCompat.LayoutParams params = new LinearLayoutCompat.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        emptyView.setLayoutParams(params);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.voice_fragment_hands_list_layout;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        recyclerView = findViewById(R.id.list);
        refreshLayout = findViewById(R.id.swipeLayout);
        adapter = (ChatroomInviteAdapter) mListAdapter;
        if (emptyView == null) {
            adapter.setEmptyView(R.layout.voice_no_data_layout);
        } else {
            adapter.setEmptyView(emptyView);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        reset();
    }

    @Override
    public void initArgument() {
        super.initArgument();
        if (getArguments() != null && getArguments().containsKey("roomId"))
            roomId = getArguments().getString("roomId");
    }

    @Override
    protected void initViewModel() {
        super.initViewModel();
        handsViewModel = new ViewModelProvider(this).get(RoomInviteViewModel.class);
        handsViewModel.getInviteObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<VRoomUserBean>() {
                @Override
                public void onSuccess(@Nullable VRoomUserBean data) {
                    int total = 0;
                    if (data != null) {
                        cursor = data.getCursor();
                        dataList.clear();

                        if (adapter.getData() != null && adapter.getData().size() > 0) {
                            dataList.addAll(adapter.getData());
                            total = dataList.size();
                            LogTools.logE("total1:" + total, TAG);
                            for (VMemberBean member : adapter.getData()) {
                                if (member.getMic_index() != -1) {
                                    dataList.remove(member);
                                    total = total - 1;
                                }
                            }
                        } else {
                            dataList.addAll(data.getMembers());
                            total = dataList.size();
                            LogTools.logE("total2:" + +total, TAG);
                            if (data.getMembers().size() > 0) {
                                for (VMemberBean member : data.getMembers()) {
                                    if (member.getMic_index() != -1) {
                                        dataList.remove(member);
                                        total = total - 1;
                                    }
                                }
                            }
                        }
                        if (isRefreshing) {
                            adapter.setData(dataList);
                        } else {
                            adapter.addData(dataList);
                        }
                        if (null != listener)
                            listener.getItemCount(total);
                        finishRefresh();
                        isRefreshing = false;
                        if (adapter.getData() != null && adapter.getData().size() > 0) {
                            for (VMemberBean datum : adapter.getData()) {
                                if (map.containsKey(datum.getUid())) {
                                    adapter.setInvited(map);
                                }
                            }
                        }
                    }
                }
            });
        });
    }

    @Override
    protected void initListener() {
        super.initListener();
        adapter.setOnActionListener(this);
        refreshLayout.setOnRefreshListener(this);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                int lastVisibleItemPosition = lm.findLastVisibleItemPosition();
                int totalCount = lm.getItemCount();
                if (lastVisibleItemPosition == totalCount - 1 && !isLoadingNextPage && !isRefreshing) {
                    // 在前面addLoadItem后，itemCount已经变化
                    // 增加一层判断，确保用户是滑到了正在加载的地方，才加载更多
                    int findLastVisibleItemPosition = lm.findLastVisibleItemPosition();
                    if (findLastVisibleItemPosition == lm.getItemCount() - 1) {
                        ThreadManager.getInstance().runOnMainThread(new Runnable() {
                            @Override
                            public void run() {
                                isLoadingNextPage = true;
                                if (!TextUtils.isEmpty(cursor)) {
                                    pullData();
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    private void pullData() {
        ThreadManager.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                handsViewModel.getInviteList(getActivity(), roomId, pageSize, cursor);
                isLoadingNextPage = false;
            }
        });
    }

    @Override
    protected void initData() {
        super.initData();
    }

    @Override
    protected RecyclerView initRecyclerView() {
        return findViewById(R.id.list);
    }

    @Override
    protected RoomBaseRecyclerViewAdapter<VMemberBean> initAdapter() {
        RoomBaseRecyclerViewAdapter adapter = new ChatroomInviteAdapter();
        return adapter;
    }

    @Override
    public void onItemClick(View view, int position) {

    }

    @Override
    public void onRefresh() {
        reset();
    }

    protected void finishRefresh() {
        if (refreshLayout != null && refreshLayout.isRefreshing()) {
            refreshLayout.setRefreshing(false);
        }
    }

    public void reset() {
        cursor = "";
        isRefreshing = true;
        handsViewModel.getInviteList(getActivity(), roomId, pageSize, cursor);
    }

    @Override
    public void onItemActionClick(View view, int position, String uid) {
        ChatroomHttpManager.getInstance(getActivity()).invitationMic(roomId, uid, new VRValueCallBack<Boolean>() {
            @Override
            public void onSuccess(Boolean var1) {
                LogTools.logE("onActionClick Invite onSuccess " + uid, TAG);
                ThreadManager.getInstance().runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        Activity activity = getActivity();
                        if (activity != null) {
                            ToastTools.show(activity, activity.getString(R.string.voice_chatroom_host_invitation_sent), Toast.LENGTH_SHORT);
                        }
                        map.put(uid, true);
                        adapter.setInvited(map);
                    }
                });
            }

            @Override
            public void onError(int code, String desc) {
                LogTools.logE("onActionClick Invite onError " + code + " " + desc, TAG);
                Activity activity = getActivity();
                if (activity != null) {
                    ToastTools.show(activity, activity.getString(R.string.voice_room_send_invited_fail), Toast.LENGTH_SHORT);
                }
            }
        });
    }

    public interface itemCountListener {
        void getItemCount(int count);
    }

    public void setItemCountChangeListener(itemCountListener listener) {
        this.listener = listener;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        map.clear();
    }

    public void MicChanged(Map<String, String> data) {
        if (adapter.getData() != null && adapter.getData().size() > 0) {
            dataList.addAll(adapter.getData());
            for (String key : data.keySet()) {
                for (VMemberBean datum : adapter.getData()) {
                    if (String.valueOf(data.get(key)).equals(datum.getUid())) {
                        reset();
                        return;
                    }
                }
            }
        }
    }
}
