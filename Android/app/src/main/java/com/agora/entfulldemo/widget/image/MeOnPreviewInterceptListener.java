package com.agora.entfulldemo.widget.image;

import android.content.Context;

import androidx.fragment.app.FragmentActivity;

import com.luck.picture.lib.basic.FragmentInjectManager;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.interfaces.OnPreviewInterceptListener;

import java.util.ArrayList;

public class MeOnPreviewInterceptListener  implements OnPreviewInterceptListener {

    @Override
    public void onPreview(Context context, int position, int totalNum, int page, long currentBucketId, String currentAlbumName, boolean isShowCamera, ArrayList<LocalMedia> data, boolean isBottomPreview) {
        CustomPreviewFragment previewFragment = CustomPreviewFragment.newInstance();
        previewFragment.setInternalPreviewData(isBottomPreview, currentAlbumName, isShowCamera,
                position, totalNum, page, currentBucketId, data);
        FragmentInjectManager.injectFragment((FragmentActivity) context, CustomPreviewFragment.TAG, previewFragment);
    }
}
