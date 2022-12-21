package io.agora.scene.show.beauty.sensetime;

import android.content.Context;

import com.sensetime.effects.STRenderer;

import io.agora.base.VideoFrame;
import io.agora.scene.show.beauty.IBeautyProcessor;

public class BeautySenseTimeImpl extends IBeautyProcessor {
    private final STRenderer mSTRenderer;
    private final Context mContext;

    public BeautySenseTimeImpl(Context context){
        mContext = context.getApplicationContext();
        mSTRenderer = new STRenderer(mContext);
    }

    @Override
    public boolean onCaptureVideoFrame(VideoFrame videoFrame) {
        return false;
    }


    @Override
    protected void setFaceBeautifyAfterCached(int itemId, float intensity) {

    }

    @Override
    protected void setFilterAfterCached(int itemId, float intensity) {

    }

    @Override
    protected void setEffectAfterCached(int itemId, float intensity) {

    }

    @Override
    protected void setStickerAfterCached(int itemId) {

    }


}
