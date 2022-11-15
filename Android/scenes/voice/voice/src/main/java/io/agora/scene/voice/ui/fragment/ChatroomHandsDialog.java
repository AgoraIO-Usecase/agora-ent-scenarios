package io.agora.scene.voice.ui.fragment;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.Map;

import io.agora.scene.voice.R;
import io.agora.voice.baseui.BaseInitFragment;
import io.agora.voice.baseui.dialog.BaseSheetDialog;
import io.agora.voice.buddy.tool.LogTools;
import io.agora.voice.buddy.tool.DeviceTools;
import io.agora.scene.voice.databinding.VoiceRoomHandLayoutBinding;

public class ChatroomHandsDialog extends BaseSheetDialog<VoiceRoomHandLayoutBinding> {
    private int[] titles = {R.string.voice_room_raised_hands_title, R.string.voice_room_invite_hands_title};
    private ArrayList<BaseInitFragment> fragments = new ArrayList();
    private MaterialTextView title;
    private int index;
    private int mCount;
    private String roomId;
    private Bundle bundle = new Bundle();
    private ChatroomRaisedHandsFragment raisedHandsFragment;
    private ChatroomInviteHandsFragment inviteHandsFragment;
    private VoiceRoomHandLayoutBinding binding;

    private final String TAG = ChatroomHandsDialog.class.getSimpleName();

    @Nullable
    @Override
    protected VoiceRoomHandLayoutBinding getViewBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return VoiceRoomHandLayoutBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getBinding() != null)
            binding = getBinding();
        setOnApplyWindowInsets(binding.getRoot());
        initArgument();
        initView(savedInstanceState);
        initListener();
    }

    public static ChatroomHandsDialog getNewInstance() {
        return new ChatroomHandsDialog();
    }

    public void initArgument() {
        if (getArguments() != null && getArguments().containsKey("roomId"))
            roomId = getArguments().getString("roomId");
    }

    public void initView(Bundle savedInstanceState) {
        fragments.add(new ChatroomRaisedHandsFragment());
        fragments.add(new ChatroomInviteHandsFragment());
        setupWithViewPager();
    }

    public void initListener() {
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getCustomView() != null) {
                    LogTools.logD("onTabSelected：" + mCount, TAG);
                    index = tab.getPosition();
                    title = tab.getCustomView().findViewById(R.id.mtTabText);
                    ShapeableImageView tag_line = tab.getCustomView().findViewById(R.id.tab_bg);
                    ViewGroup.LayoutParams layoutParams = title.getLayoutParams();
                    layoutParams.height = (int) DeviceTools.dp2px(getActivity(), 26);
                    title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                    title.setGravity(Gravity.CENTER);
                    title.setTypeface(null, Typeface.BOLD);
                    String content = getString(titles[index]) + getString(R.string.voice_room_tab_layout_count, String.valueOf(mCount));
                    title.setText(content);
                    title.setTextColor(getResources().getColor(R.color.voice_dark_grey_color_040925));
                    tag_line.setBackgroundColor(getResources().getColor(R.color.voice_color_156ef3));
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (tab.getCustomView() != null) {
                    LogTools.logD("onTabUnselected：" + mCount, TAG);
                    MaterialTextView title = tab.getCustomView().findViewById(R.id.mtTabText);
                    ShapeableImageView tag_line = tab.getCustomView().findViewById(R.id.tab_bg);
                    title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                    title.setText(titles[tab.getPosition()]);
                    title.setTypeface(null, Typeface.NORMAL);
                    title.setTextColor(getResources().getColor(R.color.voice_color_979cbb));
                    tag_line.setBackgroundColor(getResources().getColor(R.color.voice_white));
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                LogTools.logD("onTabReselected：", TAG);
                title = tab.getCustomView().findViewById(R.id.mtTabText);
                title.setText(titles[tab.getPosition()]);
                title.setTextColor(getResources().getColor(R.color.voice_dark_grey_color_040925));
                title.setTypeface(null, Typeface.BOLD);
            }
        });
        binding.vpFragment.setCurrentItem(0);
        binding.tabLayout.selectTab(binding.tabLayout.getTabAt(0));
    }

    private void setupWithViewPager() {
        binding.vpFragment.setOffscreenPageLimit(ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT);
        // set adapter
        binding.vpFragment.setAdapter(new FragmentStateAdapter(getActivity().getSupportFragmentManager(), getLifecycle()) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                if (fragments.get(position) instanceof ChatroomRaisedHandsFragment) {
                    raisedHandsFragment = ((ChatroomRaisedHandsFragment) fragments.get(position));
                    raisedHandsFragment.setItemCountChangeListener(new ChatroomRaisedHandsFragment.itemCountListener() {
                        @Override
                        public void getItemCount(int count) {
                            mCount = count;
                            if (getActivity() != null) {
                                String content = requireActivity().getString(titles[index]) + getString(R.string.voice_room_tab_layout_count, String.valueOf(mCount));
                                LogTools.logD("getItemCount content1: " + content, TAG);
                                title.setText(content);
                            }
                        }
                    });
                } else if (fragments.get(position) instanceof ChatroomInviteHandsFragment) {
                    inviteHandsFragment = ((ChatroomInviteHandsFragment) fragments.get(position));
                    inviteHandsFragment.setItemCountChangeListener(new ChatroomInviteHandsFragment.itemCountListener() {
                        @Override
                        public void getItemCount(int count) {
                            mCount = count;
                            if (getActivity() != null) {
                                String content = requireActivity().getResources().getString(titles[index]) + getString(R.string.voice_room_tab_layout_count, String.valueOf(mCount));
                                LogTools.logD("getItemCount content2: " + content, TAG);
                                title.setText(content);
                            }
                        }
                    });
                }
                bundle.putString("roomId", roomId);
                fragments.get(position).setArguments(bundle);
                return fragments.get(position);
            }

            @Override
            public int getItemCount() {
                return titles.length;
            }

        });

        // set TabLayoutMediator
        TabLayoutMediator mediator = new TabLayoutMediator(binding.tabLayout, binding.vpFragment, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                tab.setCustomView(R.layout.voice_room_hands_tab_item);
                title = tab.getCustomView().findViewById(R.id.mtTabText);
                title.setText(titles[position]);
            }
        });
        // setup with viewpager2
        mediator.attach();
    }

    public void update(int index) {
        switch (index) {
            case 0:
                if (raisedHandsFragment != null)
                    raisedHandsFragment.reset();
                break;
            case 1:
                if (inviteHandsFragment != null)
                    inviteHandsFragment.reset();
                break;
        }
    }

    public void check(Map<String, String> map) {
        if (inviteHandsFragment != null)
            inviteHandsFragment.MicChanged(map);
    }

}
