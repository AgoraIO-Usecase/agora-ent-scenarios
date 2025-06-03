package com.agora.entfulldemo.widget.image;

import android.os.Bundle;

import com.luck.picture.lib.PictureSelectorPreviewFragment;
import com.luck.picture.lib.adapter.PicturePreviewAdapter;

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
