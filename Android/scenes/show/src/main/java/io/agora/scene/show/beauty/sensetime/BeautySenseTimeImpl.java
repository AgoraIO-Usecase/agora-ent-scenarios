package io.agora.scene.show.beauty.sensetime;

import static io.agora.beautyapi.sensetime.SenseTimeBeautyAPIKt.createSenseTimeBeautyAPI;
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

import androidx.annotation.NonNull;

import com.softsugar.stmobile.STMobileEffectNative;
import com.softsugar.stmobile.params.STEffectBeautyType;

import java.io.File;

import io.agora.beautyapi.sensetime.CameraConfig;
import io.agora.beautyapi.sensetime.CaptureMode;
import io.agora.beautyapi.sensetime.Config;
import io.agora.beautyapi.sensetime.IEventCallback;
import io.agora.beautyapi.sensetime.STHandlers;
import io.agora.beautyapi.sensetime.SenseTimeBeautyAPI;
import io.agora.rtc2.RtcEngine;
import io.agora.scene.show.beauty.BeautyCache;
import io.agora.scene.show.beauty.IBeautyProcessor;

public class BeautySenseTimeImpl extends IBeautyProcessor {

    private final Context mContext;
    private volatile boolean sdkIsInit = false;
    private volatile boolean isReleased = false;

    @Override
    public void initialize(@NonNull RtcEngine rtcEngine, @NonNull CaptureMode captureMode, boolean statsEnable, @NonNull IEventCallback eventCallback) {
        getSenseTimeBeautyAPI().initialize(new Config(
                mContext,
                rtcEngine,
                new STHandlers(SenseTimeBeautySDK.getMobileEffectNative(), SenseTimeBeautySDK.getHumanActionNative()),
                eventCallback,
                captureMode,
                1000,
                statsEnable,
                new CameraConfig()));
    }

    private volatile SenseTimeBeautyAPI innerSenseTimeApi;

    @NonNull
    @Override
    public synchronized SenseTimeBeautyAPI getSenseTimeBeautyAPI() {
        if (innerSenseTimeApi == null) {
            innerSenseTimeApi = createSenseTimeBeautyAPI();
        }
        return innerSenseTimeApi;
    }

    public BeautySenseTimeImpl(Context context) {
        mContext = context.getApplicationContext();
        if (!sdkIsInit) {
            SenseTimeBeautySDK.initBeautySDK(mContext);
            SenseTimeBeautySDK.initMobileEffect(context);
            sdkIsInit = true;
            restore();
        }
    }

    @Override
    public boolean setBeautyEnable(boolean beautyEnable) {
        super.setBeautyEnable(beautyEnable);
        if (SenseTimeBeautySDK.isLicenseCheckSuccess()) {
            getSenseTimeBeautyAPI().enable(beautyEnable);
            return true;
        }
        return false;
    }

    @Override
    public void release() {
        super.release();
        if (innerSenseTimeApi != null) {
            innerSenseTimeApi.release();
            innerSenseTimeApi = null;
        }
        if (sdkIsInit) {
            SenseTimeBeautySDK.unInitMobileEffect();
            sdkIsInit = false;
        }
        isReleased = true;
    }

    @Override
    protected void setFaceBeautifyAfterCached(int itemId, float intensity) {
        if (!sdkIsInit || isReleased) {
            return;
        }
        getSenseTimeBeautyAPI().runOnProcessThread(() -> {
            STMobileEffectNative effectNative = SenseTimeBeautySDK.getMobileEffectNative();
            effectNative.setBeautyMode(STEffectBeautyType.EFFECT_BEAUTY_BASE_FACE_SMOOTH, STEffectBeautyType.SMOOTH2_MODE);
            effectNative.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_BASE_FACE_SMOOTH, BeautyCache.INSTANCE.getItemValue(ITEM_ID_BEAUTY_SMOOTH));
            effectNative.setBeautyMode(STEffectBeautyType.EFFECT_BEAUTY_BASE_WHITTEN, STEffectBeautyType.WHITENING3_MODE);
            effectNative.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_BASE_WHITTEN, BeautyCache.INSTANCE.getItemValue(ITEM_ID_BEAUTY_WHITEN));
            effectNative.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_BASE_REDDEN, BeautyCache.INSTANCE.getItemValue(ITEM_ID_BEAUTY_REDDEN));

            effectNative.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_THIN_FACE, BeautyCache.INSTANCE.getItemValue(ITEM_ID_BEAUTY_OVERALL));
            effectNative.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_SHRINK_CHEEKBONE, BeautyCache.INSTANCE.getItemValue(ITEM_ID_BEAUTY_CHEEKBONE));
            effectNative.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_SHRINK_JAWBONE, BeautyCache.INSTANCE.getItemValue(ITEM_ID_BEAUTY_JAWBONE));
            effectNative.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_RESHAPE_ENLARGE_EYE, BeautyCache.INSTANCE.getItemValue(ITEM_ID_BEAUTY_EYE));
            effectNative.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_WHITE_TEETH, BeautyCache.INSTANCE.getItemValue(ITEM_ID_BEAUTY_TEETH));
            effectNative.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_HAIRLINE_HEIGHT, BeautyCache.INSTANCE.getItemValue(ITEM_ID_BEAUTY_FOREHEAD));
            effectNative.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_NARROW_NOSE, BeautyCache.INSTANCE.getItemValue(ITEM_ID_BEAUTY_NOSE));
            effectNative.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_MOUTH_SIZE, BeautyCache.INSTANCE.getItemValue(ITEM_ID_BEAUTY_MOUTH));
            effectNative.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_CHIN_LENGTH, BeautyCache.INSTANCE.getItemValue(ITEM_ID_BEAUTY_CHIN));

            effectNative.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_BRIGHT_EYE, BeautyCache.INSTANCE.getItemValue(ITEM_ID_BEAUTY_BRIGHT_EYE));
            effectNative.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_REMOVE_DARK_CIRCLES, BeautyCache.INSTANCE.getItemValue(ITEM_ID_BEAUTY_REMOVE_DARK_CIRCLES));
            effectNative.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_REMOVE_NASOLABIAL_FOLDS, BeautyCache.INSTANCE.getItemValue(ITEM_ID_BEAUTY_REMOVE_NASOLABIAL_FOLDS));


            effectNative.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_TONE_CLEAR, BeautyCache.INSTANCE.getItemValue(ITEM_ID_ADJUST_CLARITY));
            effectNative.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_TONE_SHARPEN, BeautyCache.INSTANCE.getItemValue(ITEM_ID_ADJUST_SHARPEN));
            effectNative.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_TONE_SATURATION, BeautyCache.INSTANCE.getItemValue(ITEM_ID_ADJUST_SATURATION));
            effectNative.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_TONE_CONTRAST, BeautyCache.INSTANCE.getItemValue(ITEM_ID_ADJUST_CONTRAST));

        });

    }

    @Override
    protected void setFilterAfterCached(int itemId, float intensity) {
        if (!sdkIsInit || isReleased) {
            return;
        }
        getSenseTimeBeautyAPI().runOnProcessThread(() -> {
            if (itemId == ITEM_ID_FILTER_NONE) {
                SenseTimeBeautySDK.setFilter(mContext, "", 0);
            } else if (itemId == ITEM_ID_FILTER_CREAM) {
                SenseTimeBeautySDK.setFilter(mContext, "filter_portrait" + File.separator + "filter_style_babypink.model", intensity);
            } else if (itemId == ITEM_ID_FILTER_MAKALONG) {
                SenseTimeBeautySDK.setFilter(mContext, "filter_portrait" + File.separator + "filter_style_ol.model", intensity);
            }
        });
    }


    @Override
    protected void setEffectAfterCached(int itemId, float intensity) {
        if (!sdkIsInit || isReleased) {
            return;
        }
        getSenseTimeBeautyAPI().runOnProcessThread(() -> {
            if (itemId == ITEM_ID_EFFECT_YUANQI) {
                SenseTimeBeautySDK.setStyle(mContext, "style_lightly" + File.separator + "qise.zip", intensity);
            } else if (itemId == ITEM_ID_EFFECT_CWEI) {
                SenseTimeBeautySDK.setStyle(mContext, "style_lightly" + File.separator + "wanneng.zip", intensity);
            } else if (itemId == ITEM_ID_EFFECT_NONE) {
                SenseTimeBeautySDK.setStyle(mContext, "", 0);
            }
        });
    }

    @Override
    protected void setStickerAfterCached(int itemId) {
        if (!sdkIsInit || isReleased) {
            return;
        }
        getSenseTimeBeautyAPI().runOnProcessThread(() -> {
            if (itemId == ITEM_ID_STICKER_NONE) {
                SenseTimeBeautySDK.setSticker(mContext, "");
            } else if (itemId == ITEM_ID_STICKER_HUAHUA) {
                SenseTimeBeautySDK.setSticker(mContext, "sticker_face_shape" + File.separator + "ShangBanLe.zip");
            }
        });
    }


}
