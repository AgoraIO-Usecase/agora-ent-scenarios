package io.agora.scene.ktv.live.fragment.dialog;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.agora.scene.base.component.BaseRecyclerViewAdapter;
import io.agora.scene.base.component.BaseViewBindingFragment;
import io.agora.scene.base.component.OnItemClickListener;
import io.agora.scene.ktv.R;
import io.agora.scene.ktv.bean.EffectVoiceBean;
import io.agora.scene.ktv.databinding.FragmentEffectVoice2Binding;
import io.agora.scene.ktv.databinding.FragmentEffectVoiceBinding;
import io.agora.scene.ktv.databinding.KtvItemEffectvoiceBinding;
import io.agora.scene.ktv.live.holder.EffectVoiceHolder;
import io.agora.scene.ktv.live.holder.MVHolder;
import io.agora.scene.ktv.widget.MusicSettingBean;
import io.agora.scene.widget.DividerDecoration;

/**
 * ---------------------------------------------------------------------------------------------
 * 功能描述:
 * ---------------------------------------------------------------------------------------------
 * 时　　间: 2023/3/1
 * ---------------------------------------------------------------------------------------------
 * 代码创建: Leo
 * ---------------------------------------------------------------------------------------------
 * 代码备注:
 * ---------------------------------------------------------------------------------------------
 **/
public class EffectVoiceFragment2 extends BaseViewBindingFragment<FragmentEffectVoice2Binding> implements OnItemClickListener<EffectVoiceBean> {

    // TAG
    public static final String TAG = EffectVoiceFragment2.class.getSimpleName();
    // xxx
    private final MusicSettingBean mSetting;
    // RecyclerViewAdapter
    private BaseRecyclerViewAdapter<KtvItemEffectvoiceBinding, EffectVoiceBean, EffectVoiceHolder> adapter;

    public EffectVoiceFragment2(MusicSettingBean mSetting) {
        this.mSetting = mSetting;
    }

    @Override
    protected FragmentEffectVoice2Binding getViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentEffectVoice2Binding.inflate(inflater);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        List<EffectVoiceBean> list = new ArrayList<>();


        list.add(new EffectVoiceBean(0, R.color.red_e0, "原声"));
        list.add(new EffectVoiceBean(1, R.color.red_e0, "KTV"));
        list.add(new EffectVoiceBean(2, R.color.red_e0, "演唱会"));

        list.add(new EffectVoiceBean(3, R.color.red_e0, "录音棚"));
        list.add(new EffectVoiceBean(4, R.color.red_e0, "留声机"));
        list.add(new EffectVoiceBean(5, R.color.red_e0, "空旷"));

        list.add(new EffectVoiceBean(6, R.color.red_e0, "空灵"));
        list.add(new EffectVoiceBean(7, R.color.red_e0, "流行"));
        list.add(new EffectVoiceBean(8, R.color.red_e0, "R&B"));

        adapter = new BaseRecyclerViewAdapter<>(list, this, EffectVoiceHolder.class);
        getBinding().mRecyclerView.setAdapter(adapter);

        getBinding().mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        getBinding().mRecyclerView.addItemDecoration(new DividerDecoration(3));

        getBinding().mRecyclerView.setItemAnimator(null);


    }

    @Override
    public void onItemClick(@NonNull EffectVoiceBean data, View view, int position, long viewType) {
        OnItemClickListener.super.onItemClick(data, view, position, viewType);
        Log.e("liu0223", "onItemClick    " + position);

        for (int i = 0; i < adapter.dataList.size(); i++) {
            //EffectVoiceBean item = adapter.dataList.get(i);


            adapter.dataList.get(i).setSelect(i == position);
            adapter.notifyItemChanged(i);
        }





    }


}
