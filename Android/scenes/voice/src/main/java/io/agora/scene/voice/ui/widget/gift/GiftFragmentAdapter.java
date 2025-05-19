package io.agora.scene.voice.ui.widget.gift;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

import io.agora.scene.voice.model.GiftBean;

public class GiftFragmentAdapter extends FragmentStateAdapter {
    private OnVpFragmentItemListener listener;
    private List<GiftBean> list;

    public GiftFragmentAdapter(@NonNull FragmentActivity fragment) {
        super(fragment);
        list =  GiftRepository.getDefaultGifts(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        LiveGiftListFragment fragment = new LiveGiftListFragment();
        Bundle args = new Bundle();
        args.putInt("position",position);
        fragment.setArguments(args);
        fragment.setOnItemSelectClickListener(new OnConfirmClickListener() {
            @Override
            public void onConfirmClick(View view, Object bean) {
                if(listener != null) {
                    listener.onVpFragmentItem(position, bean);
                }
            }

            @Override
            public void onFirstItem(GiftBean firstBean) {
                if(listener != null) {
                    listener.onFirstData(firstBean);
                }
            }
        });
        return fragment;
    }

    @Override
    public int getItemCount() {
        return Math.round((list.size()/4)+0.5f);
    }

    public void setOnVpFragmentItemListener(OnVpFragmentItemListener listener) {
        this.listener = listener;
    }

    public interface OnVpFragmentItemListener {
        void onVpFragmentItem(int position, Object bean);
        void onFirstData(GiftBean bean);
    }
}
