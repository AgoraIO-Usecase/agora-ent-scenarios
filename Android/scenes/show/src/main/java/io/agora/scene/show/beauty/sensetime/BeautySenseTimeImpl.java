package io.agora.scene.show.beauty.sensetime;

import static io.agora.beauty.sensetime.SenseTimeBeautyAPIKt.createSenseTimeBeautyAPI;
import static io.agora.scene.show.beauty.BeautyConstantsKt.ITEM_ID_ADJUST_CLARITY;
import static io.agora.scene.show.beauty.BeautyConstantsKt.ITEM_ID_ADJUST_CONTRAST;
import static io.agora.scene.show.beauty.BeautyConstantsKt.ITEM_ID_ADJUST_SATURATION;
import static io.agora.scene.show.beauty.BeautyConstantsKt.ITEM_ID_ADJUST_SHARPEN;
import static io.agora.scene.show.beauty.BeautyConstantsKt.ITEM_ID_BEAUTY_BRIGHT_EYE;
import static io.agora.scene.show.beauty.BeautyConstantsKt.ITEM_ID_BEAUTY_CHEEKBONE;
import static io.agora.scene.show.beauty.BeautyConstantsKt.ITEM_ID_BEAUTY_CHIN;
import static io.agora.scene.show.beauty.BeautyConstantsKt.ITEM_ID_BEAUTY_EYE;
import static io.agora.scene.show.beauty.BeautyConstantsKt.ITEM_ID_BEAUTY_FOREHEAD;
import static io.agora.scene.show.beauty.BeautyConstantsKt.ITEM_ID_BEAUTY_JAWBONE;
import static io.agora.scene.show.beauty.BeautyConstantsKt.ITEM_ID_BEAUTY_MOUTH;
import static io.agora.scene.show.beauty.BeautyConstantsKt.ITEM_ID_BEAUTY_NOSE;
import static io.agora.scene.show.beauty.BeautyConstantsKt.ITEM_ID_BEAUTY_OVERALL;
import static io.agora.scene.show.beauty.BeautyConstantsKt.ITEM_ID_BEAUTY_REDDEN;
import static io.agora.scene.show.beauty.BeautyConstantsKt.ITEM_ID_BEAUTY_REMOVE_DARK_CIRCLES;
import static io.agora.scene.show.beauty.BeautyConstantsKt.ITEM_ID_BEAUTY_REMOVE_NASOLABIAL_FOLDS;
import static io.agora.scene.show.beauty.BeautyConstantsKt.ITEM_ID_BEAUTY_SMOOTH;
import static io.agora.scene.show.beauty.BeautyConstantsKt.ITEM_ID_BEAUTY_TEETH;
import static io.agora.scene.show.beauty.BeautyConstantsKt.ITEM_ID_BEAUTY_WHITEN;
import static io.agora.scene.show.beauty.BeautyConstantsKt.ITEM_ID_EFFECT_CWEI;
import static io.agora.scene.show.beauty.BeautyConstantsKt.ITEM_ID_EFFECT_NONE;
import static io.agora.scene.show.beauty.BeautyConstantsKt.ITEM_ID_EFFECT_YUANQI;
import static io.agora.scene.show.beauty.BeautyConstantsKt.ITEM_ID_FILTER_CREAM;
import static io.agora.scene.show.beauty.BeautyConstantsKt.ITEM_ID_FILTER_MAKALONG;
import static io.agora.scene.show.beauty.BeautyConstantsKt.ITEM_ID_FILTER_NONE;
import static io.agora.scene.show.beauty.BeautyConstantsKt.ITEM_ID_STICKER_HUAHUA;
import static io.agora.scene.show.beauty.BeautyConstantsKt.ITEM_ID_STICKER_NONE;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.sensetime.effects.STRenderKit;
import com.sensetime.effects.utils.FileUtils;
import com.sensetime.stmobile.params.STEffectBeautyType;

import java.io.File;

import io.agora.base.VideoFrame;
import io.agora.beauty.sensetime.CaptureMode;
import io.agora.beauty.sensetime.Config;
import io.agora.beauty.sensetime.ErrorCode;
import io.agora.beauty.sensetime.IEventCallback;
import io.agora.beauty.sensetime.SenseTimeBeautyAPI;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.video.IVideoFrameObserver;
import io.agora.scene.show.beauty.BeautyCache;
import io.agora.scene.show.beauty.IBeautyProcessor;

public class BeautySenseTimeImpl extends IBeautyProcessor {

    private final Context mContext;
    private STRenderKit mSTRenderer;
    private volatile boolean sdkIsInit = false;
    private volatile boolean isReleased = false;

    private volatile boolean shouldMirror = false;

    @Override
    public void initialize(@NonNull RtcEngine rtcEngine, @NonNull CaptureMode captureMode, boolean statsEnable, @NonNull IEventCallback eventCallback) {
        getSenseTimeBeautyAPI().initialize(new Config(rtcEngine, mSTRenderer, eventCallback, captureMode, 1000, statsEnable));
    }

    private SenseTimeBeautyAPI innerSenseTimeApi;

    @NonNull
    @Override
    public SenseTimeBeautyAPI getSenseTimeBeautyAPI() {
        if (innerSenseTimeApi == null) {
            innerSenseTimeApi = createSenseTimeBeautyAPI();
        }
        return innerSenseTimeApi;
    }

    public BeautySenseTimeImpl(Context context) {
        mContext = context.getApplicationContext();
        initST();
    }

    @Override
    public void release() {
        super.release();
        unInitST();
        if (innerSenseTimeApi != null) {
            innerSenseTimeApi.release();
            innerSenseTimeApi = null;
        }
        isReleased = true;
        sdkIsInit = false;
    }

    private void initST() {
        if (sdkIsInit) {
            return;
        }
        mSTRenderer = new STRenderKit(mContext, null);
        sdkIsInit = true;
        restore();
    }

    private void unInitST() {
        if (!sdkIsInit) {
            return;
        }
        mSTRenderer.release();
        sdkIsInit = false;
    }

    @Override
    protected void setFaceBeautifyAfterCached(int itemId, float intensity) {
        if (!sdkIsInit || isReleased) {
            return;
        }
        Log.d("hugo", "setFaceBeautifyAfterCached, itemId: " + itemId + " intensity: " + intensity);
        mSTRenderer.setBeautyMode(STEffectBeautyType.EFFECT_BEAUTY_BASE_FACE_SMOOTH, STEffectBeautyType.SMOOTH2_MODE);
        mSTRenderer.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_BASE_FACE_SMOOTH, BeautyCache.INSTANCE.getItemValue(ITEM_ID_BEAUTY_SMOOTH));
        mSTRenderer.setBeautyMode(STEffectBeautyType.EFFECT_BEAUTY_BASE_WHITTEN, STEffectBeautyType.WHITENING3_MODE);
        mSTRenderer.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_BASE_WHITTEN, BeautyCache.INSTANCE.getItemValue(ITEM_ID_BEAUTY_WHITEN));
        mSTRenderer.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_BASE_REDDEN, BeautyCache.INSTANCE.getItemValue(ITEM_ID_BEAUTY_REDDEN));

        mSTRenderer.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_THIN_FACE, BeautyCache.INSTANCE.getItemValue(ITEM_ID_BEAUTY_OVERALL));
        mSTRenderer.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_SHRINK_CHEEKBONE, BeautyCache.INSTANCE.getItemValue(ITEM_ID_BEAUTY_CHEEKBONE));
        mSTRenderer.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_SHRINK_JAWBONE, BeautyCache.INSTANCE.getItemValue(ITEM_ID_BEAUTY_JAWBONE));
        mSTRenderer.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_RESHAPE_ENLARGE_EYE, BeautyCache.INSTANCE.getItemValue(ITEM_ID_BEAUTY_EYE));
        mSTRenderer.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_WHITE_TEETH, BeautyCache.INSTANCE.getItemValue(ITEM_ID_BEAUTY_TEETH));
        mSTRenderer.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_HAIRLINE_HEIGHT, BeautyCache.INSTANCE.getItemValue(ITEM_ID_BEAUTY_FOREHEAD));
        mSTRenderer.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_NARROW_NOSE, BeautyCache.INSTANCE.getItemValue(ITEM_ID_BEAUTY_NOSE));
        mSTRenderer.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_MOUTH_SIZE, BeautyCache.INSTANCE.getItemValue(ITEM_ID_BEAUTY_MOUTH));
        mSTRenderer.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_CHIN_LENGTH, BeautyCache.INSTANCE.getItemValue(ITEM_ID_BEAUTY_CHIN));

        mSTRenderer.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_BRIGHT_EYE, BeautyCache.INSTANCE.getItemValue(ITEM_ID_BEAUTY_BRIGHT_EYE));
        mSTRenderer.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_REMOVE_DARK_CIRCLES, BeautyCache.INSTANCE.getItemValue(ITEM_ID_BEAUTY_REMOVE_DARK_CIRCLES));
        mSTRenderer.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_REMOVE_NASOLABIAL_FOLDS, BeautyCache.INSTANCE.getItemValue(ITEM_ID_BEAUTY_REMOVE_NASOLABIAL_FOLDS));


        mSTRenderer.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_TONE_CLEAR, BeautyCache.INSTANCE.getItemValue(ITEM_ID_ADJUST_CLARITY));
        mSTRenderer.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_TONE_SHARPEN, BeautyCache.INSTANCE.getItemValue(ITEM_ID_ADJUST_SHARPEN));
        mSTRenderer.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_TONE_SATURATION, BeautyCache.INSTANCE.getItemValue(ITEM_ID_ADJUST_SATURATION));
        mSTRenderer.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_TONE_CONTRAST, BeautyCache.INSTANCE.getItemValue(ITEM_ID_ADJUST_CONTRAST));

    }

    @Override
    protected void setFilterAfterCached(int itemId, float intensity) {
        if (!sdkIsInit || isReleased) {
            return;
        }
        if (itemId == ITEM_ID_FILTER_NONE) {
            mSTRenderer.setFilterStyle("", "", "");
        } else if (itemId == ITEM_ID_FILTER_CREAM) {
            setFilterItem("filter_portrait" + File.separator + "filter_style_babypink_1.5.0_v2_origin_20221130_20230228.model", intensity);
        } else if (itemId == ITEM_ID_FILTER_MAKALONG) {
            setFilterItem("filter_portrait" + File.separator + "filter_style_ol_1.5.0_v2_origin_20221130_20230228.model", intensity);
        }
    }


    @Override
    protected void setEffectAfterCached(int itemId, float intensity) {
        if (!sdkIsInit || isReleased) {
            return;
        }
        if (itemId == ITEM_ID_EFFECT_YUANQI) {
            setStyleItem("style_lightly" + File.separator + "qise.zip", intensity);
        } else if (itemId == ITEM_ID_EFFECT_CWEI) {
            setStyleItem("style_lightly" + File.separator + "wanneng.zip", intensity);
        } else if (itemId == ITEM_ID_EFFECT_NONE) {
            //mSTRenderer.cleanStyle();
        }
    }

    @Override
    protected void setStickerAfterCached(int itemId) {
        if (!sdkIsInit || isReleased) {
            return;
        }

        if (itemId == ITEM_ID_STICKER_NONE) {
            //mSTRenderer.removeStickers();
        } else if (itemId == ITEM_ID_STICKER_HUAHUA) {
            setStickerItem("sticker_face_shape" + File.separator + "lianxingface.zip");
        }
    }


    private void setStyleItem(String stylePath, float strength) {
        String[] split = stylePath.split(File.separator);
        String className = split[0];
        String fileName = split[1];
        String path = FileUtils.getFilePath(mContext, className + File.separator + fileName);
        FileUtils.copyFileIfNeed(mContext, fileName, className);
        //mSTRenderer.setStyle(path, strength, strength);
    }

    private void setFilterItem(String filterPath, float strength) {
        String[] split = filterPath.split(File.separator);
        String className = split[0];
        String fileName = split[1];
        String filterName = split[1].split("_")[2].split("\\.")[0];
        String path = FileUtils.getFilePath(mContext, className + File.separator + fileName);
        FileUtils.copyFileIfNeed(mContext, fileName, className);
        mSTRenderer.setFilterStyle(className, filterName, path);
        mSTRenderer.setFilterStrength(strength);
    }

    private void setStickerItem(String path) {
        String[] split = path.split(File.separator);
        String className = split[0];
        String fileName = split[1];
        String _path = FileUtils.getFilePath(mContext, className + File.separator + fileName);
        FileUtils.copyFileIfNeed(mContext, fileName, className);
        if (mSTRenderer != null) {
            mSTRenderer.changeSticker(_path);
        }
    }

    private void setMakeUpItem(int type, String typePath, float strength) {
        if (typePath != null) {
            String[] split = typePath.split(File.separator);
            String className = split[0];
            String fileName = split[1];
            String _path = FileUtils.getFilePath(mContext, className + File.separator + fileName);
            FileUtils.copyFileIfNeed(mContext, fileName, className);
            mSTRenderer.setMakeupForType(type, _path);
            mSTRenderer.setMakeupStrength(type, strength);
        } else {
            mSTRenderer.removeMakeupByType(type);
        }
    }

    @Override
    public boolean onCaptureVideoFrame(int type, VideoFrame videoFrame) {
        if (videoFrame == null) return false;
        shouldMirror = false;
        int ret = getSenseTimeBeautyAPI().onFrame(videoFrame);
        if (ret == ErrorCode.ERROR_OK.getValue()) {
            return true;
        } else if (ret == ErrorCode.ERROR_FRAME_SKIPPED.getValue()) {
            return false;
        } else {
            shouldMirror = videoFrame.getSourceType() == VideoFrame.SourceType.kFrontCamera;
            return true;
        }
    }

    @Override
    public boolean onPreEncodeVideoFrame(int type, VideoFrame videoFrame) {
        return false;
    }

    @Override
    public boolean onMediaPlayerVideoFrame(VideoFrame videoFrame, int mediaPlayerId) {
        return false;
    }

    @Override
    public boolean onRenderVideoFrame(String channelId, int uid, VideoFrame videoFrame) {
        return false;
    }

    @Override
    public int getVideoFrameProcessMode() {
        return IVideoFrameObserver.PROCESS_MODE_READ_WRITE;
    }

    @Override
    public int getVideoFormatPreference() {
        return IVideoFrameObserver.VIDEO_PIXEL_DEFAULT;
    }

    @Override
    public boolean getRotationApplied() {
        return false;
    }


    @Override
    public boolean getMirrorApplied() {
        return shouldMirror;
    }

    @Override
    public int getObservedFramePosition() {
        return IVideoFrameObserver.POSITION_POST_CAPTURER;
    }
}
