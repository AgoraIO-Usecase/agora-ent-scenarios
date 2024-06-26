package io.agora.scene.ktv.singbattle.widget.song;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

import java.util.List;

import io.agora.scene.base.GlideApp;
import io.agora.scene.base.component.BaseViewBindingFragment;
import io.agora.scene.base.manager.UserManager;
import io.agora.scene.base.utils.UiUtil;
import io.agora.scene.ktv.singbattle.R;
import io.agora.scene.ktv.singbattle.databinding.KtvSingbattleFragmentSongOrderListBinding;
import io.agora.scene.ktv.singbattle.databinding.KtvSingbattleItemChoosedSongListBinding;
import io.agora.scene.widget.basic.BindingSingleAdapter;
import io.agora.scene.widget.basic.BindingViewHolder;

/**
 * 已点歌单列表
 */
public final class SongChosenFragment extends BaseViewBindingFragment<KtvSingbattleFragmentSongOrderListBinding> {

    private boolean controllable = false;
    private Listener listener;

    private final BindingSingleAdapter<SongItem, KtvSingbattleItemChoosedSongListBinding> mAdapter = new BindingSingleAdapter<SongItem, KtvSingbattleItemChoosedSongListBinding>() {
        @Override
        public void onBindViewHolder(@NonNull BindingViewHolder<KtvSingbattleItemChoosedSongListBinding> holder, int position) {
            SongItem item = getItem(position);
            KtvSingbattleItemChoosedSongListBinding binding = holder.binding;
            if (item != null) {
                binding.tvNo.setText(String.valueOf(position + 1));
                binding.tvMusicName.setText(item.songName);
                binding.tvChooser.setText(item.chooser);
                GlideApp.with(binding.ivCover).load(item.imageUrl)
                        .fallback(R.mipmap.ktv_ic_song_default)
                        .error(R.mipmap.ktv_ic_song_default)
                        .transform(new RoundedCorners(UiUtil.dp2px(10)))
                        .into(binding.ivCover);
                if (position == 0 && controllable) {
                    binding.ivToDel.setOnClickListener(v -> {
                        onSongDeleteClicked(item);
                    });
                    binding.tvSing.setVisibility(View.GONE);
                    binding.ivSinging.setVisibility(View.GONE);
                    binding.ivToDel.setVisibility(View.VISIBLE);
                    binding.ivToTop.setVisibility(View.GONE);
                } else if (controllable) {
                    binding.ivToDel.setOnClickListener(v -> {
                        onSongDeleteClicked(item);
                    });
                    binding.ivToTop.setOnClickListener(v -> {
                        onSongTopClicked(item);
                    });
                    binding.tvSing.setVisibility(View.GONE);
                    binding.ivSinging.setVisibility(View.GONE);
                    binding.ivToDel.setVisibility(View.VISIBLE);
                    binding.ivToTop.setVisibility(View.VISIBLE);
                } else {
                    binding.tvSing.setVisibility(View.GONE);
                    binding.ivSinging.setVisibility(View.GONE);
                    binding.ivToTop.setVisibility(View.GONE);

                    if (item.chooserId.equals(UserManager.getInstance().getUser().id.toString())) {
                        binding.ivToDel.setOnClickListener(v -> {
                            onSongDeleteClicked(item);
                        });
                        binding.ivToDel.setVisibility(View.VISIBLE);
                    } else {
                        binding.ivToDel.setVisibility(View.GONE);
                    }
                }

                binding.tvChorus.setText(getString(R.string.ktv_singbattle_song_ordering_person));
            }
        }
    };

    @NonNull
    @Override
    protected KtvSingbattleFragmentSongOrderListBinding getViewBinding(@NonNull LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup) {
        return KtvSingbattleFragmentSongOrderListBinding.inflate(layoutInflater);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    @Override
    public void initView() {
        getBinding().list.setAdapter(mAdapter);
    }

    void setControllable(boolean controllable) {
        this.controllable = controllable;
    }

    void resetSongList(List<SongItem> songs) {
        mAdapter.resetAll(songs);
    }

    void addSongItem(SongItem song){
        mAdapter.insertLast(song);
    }

    void deleteSongItem(SongItem song){
        for (int i = 0; i < mAdapter.getItemCount(); i++) {
            SongItem item = mAdapter.getItem(i);
            if(song.songNo.equals(item.songNo)){
                mAdapter.remove(i);
                break;
            }
        }
    }

    void topUpSongItem(SongItem song){
        for (int i = 0; i < mAdapter.getItemCount(); i++) {
            SongItem item = mAdapter.getItem(i);
            if (song.songNo.equals(item.songNo)) {
                if (i > 1) {
                    mAdapter.remove(i);
                    mAdapter.insert(1, item);
                }
                break;
            }
        }
    }

    int getSongSize(){
        return mAdapter.getItemCount();
    }


    public void setListener(Listener listener) {
        this.listener = listener;
    }

    private void onSongDeleteClicked(SongItem song){
        if(listener != null){
            listener.onSongDeleteClicked(song);
        }
    }

    private void onSongTopClicked(SongItem song){
        if(listener != null){
            listener.onSongTopClicked(song);
        }
    }


    interface Listener {
        void onSongDeleteClicked(SongItem song);
        void onSongTopClicked(SongItem song);
    }
}
