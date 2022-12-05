package io.agora.scene.show.beauty.bytedance;

import static io.agora.scene.show.beauty.BeautyConstantsKt.ITEM_ID_BEAUTY_CHEEKBONE;
import static io.agora.scene.show.beauty.BeautyConstantsKt.ITEM_ID_BEAUTY_CHIN;
import static io.agora.scene.show.beauty.BeautyConstantsKt.ITEM_ID_BEAUTY_EYE;
import static io.agora.scene.show.beauty.BeautyConstantsKt.ITEM_ID_BEAUTY_FOREHEAD;
import static io.agora.scene.show.beauty.BeautyConstantsKt.ITEM_ID_BEAUTY_JAWBONE;
import static io.agora.scene.show.beauty.BeautyConstantsKt.ITEM_ID_BEAUTY_MOUTH;
import static io.agora.scene.show.beauty.BeautyConstantsKt.ITEM_ID_BEAUTY_NOSE;
import static io.agora.scene.show.beauty.BeautyConstantsKt.ITEM_ID_BEAUTY_OVERALL;
import static io.agora.scene.show.beauty.BeautyConstantsKt.ITEM_ID_BEAUTY_SMOOTH;
import static io.agora.scene.show.beauty.BeautyConstantsKt.ITEM_ID_BEAUTY_TEETH;
import static io.agora.scene.show.beauty.BeautyConstantsKt.ITEM_ID_BEAUTY_WHITEN;
import static io.agora.scene.show.beauty.BeautyConstantsKt.ITEM_ID_EFFECT_BAIXI;
import static io.agora.scene.show.beauty.BeautyConstantsKt.ITEM_ID_EFFECT_CWEI;
import static io.agora.scene.show.beauty.BeautyConstantsKt.ITEM_ID_EFFECT_TIANMEI;
import static io.agora.scene.show.beauty.BeautyConstantsKt.ITEM_ID_EFFECT_YUANQI;
import static io.agora.scene.show.beauty.BeautyConstantsKt.ITEM_ID_FILTER_CREAM;
import static io.agora.scene.show.beauty.BeautyConstantsKt.ITEM_ID_FILTER_MAKALONG;
import static io.agora.scene.show.beauty.BeautyConstantsKt.ITEM_ID_FILTER_NONE;
import static io.agora.scene.show.beauty.BeautyConstantsKt.ITEM_ID_FILTER_OXGEN;
import static io.agora.scene.show.beauty.BeautyConstantsKt.ITEM_ID_FILTER_Po9;
import static io.agora.scene.show.beauty.BeautyConstantsKt.ITEM_ID_FILTER_WUYU;
import static io.agora.scene.show.beauty.BeautyConstantsKt.ITEM_ID_STICKER_BITI;
import static io.agora.scene.show.beauty.BeautyConstantsKt.ITEM_ID_STICKER_NONE;
import static io.agora.scene.show.beauty.BeautyConstantsKt.ITEM_ID_STICKER_ZHOUNIAN;

import android.content.Context;

import com.bytedance.labcv.core.effect.EffectManager;
import com.bytedance.labcv.core.util.ImageUtil;
import com.bytedance.labcv.effectsdk.BytedEffectConstants;

import io.agora.base.TextureBufferHelper;
import io.agora.base.VideoFrame;
import io.agora.scene.show.beauty.BeautyCache;
import io.agora.scene.show.beauty.IBeautyProcessor;

public class BeautyByteDanceImpl extends IBeautyProcessor {

    private final String BEAUTY_NODE = "beauty_Android_lite";
    private final String BEAUTY_4ITEMS_NODE = "beauty_4Items";
    private final String RESHARP_LITE_NODE = "reshape_lite";
    private final String STYLE_BAIXI_NODE = "style_makeup/baixi";
    private final String STYLE_TIANMEI_NODE = "style_makeup/tianmei";
    private final String STYLE_CWEI_NODE = "style_makeup/cwei";
    private final String STYLE_YUANQI_NODE = "style_makeup/yuanqi";



    private final Context mContext;
    private volatile boolean isReleased = false;

    private EffectManager mEffectManager;
    private ImageUtil mImageUtil;

    // sdk 初始化标记，仅用于用来标记SDK的初始化。
    private volatile boolean resourceReady = false;
    private volatile boolean sdkIsInit = false;

    private final ResourcesHelper resourcesHelper;

    private TextureBufferHelper textureBufferHelper;

    private boolean isFrontCamera = true;

    public BeautyByteDanceImpl(Context context) {
        mContext = context;
        resourcesHelper = new ResourcesHelper(context);
        resourcesHelper.init(() -> resourceReady = true);
    }

    /**
     * EffectManager的初始化，包括各种资源路径配置
     */
    private void cvSdkInit() {
        mEffectManager = new EffectManager(mContext, resourcesHelper, resourcesHelper.getLicensePath());
        mImageUtil = new ImageUtil();
    }

    private void cvSdkUnInit() {
        if(mEffectManager != null){
            mEffectManager.destroy();
            mImageUtil.release();
            mEffectManager = null;
            mImageUtil = null;
            sdkIsInit = false;
        }
    }

    private void configSdkDefault() {
        if (sdkIsInit) {
            return;
        }
        // 必须在gl 线程中运行。
        mEffectManager.init();
        mEffectManager.setComposeNodes(new String[]{BEAUTY_NODE, RESHARP_LITE_NODE, BEAUTY_4ITEMS_NODE, STYLE_BAIXI_NODE, STYLE_TIANMEI_NODE, STYLE_CWEI_NODE, STYLE_YUANQI_NODE});
        sdkIsInit = true;

        restore();
        updateNodeByCache();
    }

    @Override
    public boolean onCaptureVideoFrame(VideoFrame videoFrame) {
        if(!isEnable()){
            return true;
        }

        VideoFrame.Buffer buffer = videoFrame.getBuffer();
        if(!(buffer instanceof VideoFrame.TextureBuffer)){
            return true;
        }



        VideoFrame.TextureBuffer textureBuffer = (VideoFrame.TextureBuffer) buffer;
        if (textureBufferHelper == null) {
            textureBufferHelper = TextureBufferHelper.create("BeautyProcessor", textureBuffer.getEglBaseContext());
            textureBufferHelper.invoke(() -> {
                cvSdkInit();
                return null;
            });
        }
        int texture = textureBufferHelper.invoke(() -> process(textureBuffer.getTextureId(), textureBuffer.getWidth(), textureBuffer.getHeight()));

        boolean isFront = videoFrame.getRotation() == 270;
        if (isFrontCamera != isFront) {
            isFrontCamera = isFront;
            return true;
        }

        if (texture < 0) {
            return true;
        }

        VideoFrame.TextureBuffer newBuffer = textureBufferHelper.wrapTextureBuffer(
                textureBuffer.getWidth(),
                textureBuffer.getHeight(),
                VideoFrame.TextureBuffer.Type.RGB,
                texture, textureBuffer.getTransformMatrix());
        videoFrame.replaceBuffer(newBuffer, videoFrame.getRotation(), videoFrame.getTimestampNs());
        return true;
    }

    public int process(int oesTexId, int width, int height) {
        if (isReleased) {
            return -1;
        }
        if (!resourceReady) {
            return -1;
        }
        configSdkDefault();
        // 是否为前置摄像头
        mEffectManager.setCameraPosition(isFrontCamera);
        // 生成目标承载纹理
        int dstTexture = mImageUtil.prepareTexture(width, height);
        // OES 纹理转2D纹理
        int texture2d = mImageUtil.transferTextureToTexture(oesTexId,
                BytedEffectConstants.TextureFormat.Texture_Oes,
                BytedEffectConstants.TextureFormat.Texure2D,
                width, height, new ImageUtil.Transition());
        // CV SDK 特效处理
        boolean process = mEffectManager.process(texture2d, dstTexture, width, height,
                BytedEffectConstants.Rotation.CLOCKWISE_ROTATE_0,
                System.currentTimeMillis());
        if (!process) {
            return -1;
        }

        return dstTexture;
    }

    private void updateNodeByCache() {
        mEffectManager.updateComposerNodeIntensity(BEAUTY_NODE, "smooth", BeautyCache.INSTANCE.getItemValue(ITEM_ID_BEAUTY_SMOOTH));// 磨皮
        mEffectManager.updateComposerNodeIntensity(BEAUTY_NODE, "whiten", BeautyCache.INSTANCE.getItemValue(ITEM_ID_BEAUTY_WHITEN));// 美白
        mEffectManager.updateComposerNodeIntensity(RESHARP_LITE_NODE, "Internal_Deform_Overall", BeautyCache.INSTANCE.getItemValue(ITEM_ID_BEAUTY_OVERALL));//瘦脸
        mEffectManager.updateComposerNodeIntensity(RESHARP_LITE_NODE, "Internal_Deform_Zoom_Cheekbone", BeautyCache.INSTANCE.getItemValue(ITEM_ID_BEAUTY_CHEEKBONE));//瘦颧骨
        mEffectManager.updateComposerNodeIntensity(RESHARP_LITE_NODE, "Internal_Deform_Zoom_Jawbone", BeautyCache.INSTANCE.getItemValue(ITEM_ID_BEAUTY_JAWBONE));//下颌骨
        mEffectManager.updateComposerNodeIntensity(RESHARP_LITE_NODE, "Internal_Deform_Eye", BeautyCache.INSTANCE.getItemValue(ITEM_ID_BEAUTY_EYE));//大眼
        mEffectManager.updateComposerNodeIntensity(BEAUTY_4ITEMS_NODE, "BEF_BEAUTY_WHITEN_TEETH", BeautyCache.INSTANCE.getItemValue(ITEM_ID_BEAUTY_TEETH));//美牙
        mEffectManager.updateComposerNodeIntensity(RESHARP_LITE_NODE, "Internal_Deform_Forehead", BeautyCache.INSTANCE.getItemValue(ITEM_ID_BEAUTY_FOREHEAD));//额头
        mEffectManager.updateComposerNodeIntensity(RESHARP_LITE_NODE, "Internal_Deform_Nose", BeautyCache.INSTANCE.getItemValue(ITEM_ID_BEAUTY_NOSE));//瘦鼻
        mEffectManager.updateComposerNodeIntensity(RESHARP_LITE_NODE, "Internal_Deform_ZoomMouth", BeautyCache.INSTANCE.getItemValue(ITEM_ID_BEAUTY_MOUTH));//嘴形
        mEffectManager.updateComposerNodeIntensity(RESHARP_LITE_NODE, "Internal_Deform_Chin", BeautyCache.INSTANCE.getItemValue(ITEM_ID_BEAUTY_CHIN));//下巴

        mEffectManager.updateComposerNodeIntensity(STYLE_BAIXI_NODE, "Filter_ALL", BeautyCache.INSTANCE.getItemValue(ITEM_ID_EFFECT_BAIXI));
        mEffectManager.updateComposerNodeIntensity(STYLE_BAIXI_NODE, "Makeup_ALL", BeautyCache.INSTANCE.getItemValue(ITEM_ID_EFFECT_BAIXI));
        mEffectManager.updateComposerNodeIntensity(STYLE_TIANMEI_NODE, "Filter_ALL", BeautyCache.INSTANCE.getItemValue(ITEM_ID_EFFECT_TIANMEI));
        mEffectManager.updateComposerNodeIntensity(STYLE_TIANMEI_NODE, "Makeup_ALL", BeautyCache.INSTANCE.getItemValue(ITEM_ID_EFFECT_TIANMEI));
        mEffectManager.updateComposerNodeIntensity(STYLE_CWEI_NODE, "Filter_ALL", BeautyCache.INSTANCE.getItemValue(ITEM_ID_EFFECT_CWEI));
        mEffectManager.updateComposerNodeIntensity(STYLE_CWEI_NODE, "Makeup_ALL", BeautyCache.INSTANCE.getItemValue(ITEM_ID_EFFECT_CWEI));
        mEffectManager.updateComposerNodeIntensity(STYLE_YUANQI_NODE, "Filter_ALL", BeautyCache.INSTANCE.getItemValue(ITEM_ID_EFFECT_YUANQI));
        mEffectManager.updateComposerNodeIntensity(STYLE_YUANQI_NODE, "Makeup_ALL", BeautyCache.INSTANCE.getItemValue(ITEM_ID_EFFECT_YUANQI));
    }

    @Override
    public void release() {
        super.release();
        isReleased = true;
        sdkIsInit = false;
        resourceReady = false;
        resourcesHelper.release();
        if(textureBufferHelper != null){
            textureBufferHelper.invoke(() -> {
                cvSdkUnInit();
                return null;
            });
            textureBufferHelper.dispose();
            textureBufferHelper = null;
        }
    }

    @Override
    protected void setFaceBeautifyAfterCached(int itemId, float intensity) {
        if (isReleased) {
            return;
        }
        updateNodeByCache();
    }



    @Override
    protected void setEffectAfterCached(int itemId, float intensity) {
        if (isReleased) {
            return;
        }
        updateNodeByCache();
    }

    @Override
    protected void setFilterAfterCached(int itemId, float intensity) {
        if (isReleased) {
            return;
        }
        if(itemId == ITEM_ID_FILTER_NONE){
            mEffectManager.setFilterAbs(null);
        }
        else if(itemId == ITEM_ID_FILTER_CREAM){
            mEffectManager.setFilter("Filter_01_38");
            mEffectManager.updateFilterIntensity(intensity);
        }
        else if(itemId == ITEM_ID_FILTER_MAKALONG){
            mEffectManager.setFilter("Filter_02_14");
            mEffectManager.updateFilterIntensity(intensity);
        }
        else if(itemId == ITEM_ID_FILTER_OXGEN){
            mEffectManager.setFilter("Filter_03_20");
            mEffectManager.updateFilterIntensity(intensity);
        }
        else if(itemId == ITEM_ID_FILTER_WUYU){
            mEffectManager.setFilter("Filter_04_12");
            mEffectManager.updateFilterIntensity(intensity);
        }
        else if(itemId == ITEM_ID_FILTER_Po9){
            mEffectManager.setFilter("Filter_05_10");
            mEffectManager.updateFilterIntensity(intensity);
        }
    }



    @Override
    protected void setStickerAfterCached(int itemId) {
        if (isReleased) {
            return;
        }
        if(itemId == ITEM_ID_STICKER_NONE){
            mEffectManager.setSticker(null);
        }
        else if(itemId == ITEM_ID_STICKER_BITI){
            mEffectManager.setSticker("maobing");
        }
        else if(itemId == ITEM_ID_STICKER_ZHOUNIAN){
            mEffectManager.setSticker("zhangshangyouxiji");
        }
    }
}
