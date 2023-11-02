package com.agora.entfulldemo.widget.image;

import android.os.Bundle;

import com.luck.picture.lib.PictureSelectorPreviewFragment;
import com.luck.picture.lib.adapter.PicturePreviewAdapter;

/**
 * @author：luck
 * @date：2022/2/21 4:15 下午
 * @describe：CustomPreviewFragment
 */
public class CustomPreviewFragment extends PictureSelectorPreviewFragment {

    public static CustomPreviewFragment newInstance() {
        CustomPreviewFragment fragment = new CustomPreviewFragment();
        fragment.setArguments(new Bundle());
        return fragment;
    }

    @Override
    public String getFragmentTag() {
        return CustomPreviewFragment.class.getSimpleName();
    }

    @Override
    protected PicturePreviewAdapter createAdapter() {
        return new PicturePreviewAdapter();
    }
}
