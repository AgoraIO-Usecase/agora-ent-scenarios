package io.agora.scene.show.beauty.sensetime;

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
import static io.agora.scene.show.beauty.BeautyConstantsKt.ITEM_ID_EFFECT_CWEI;
import static io.agora.scene.show.beauty.BeautyConstantsKt.ITEM_ID_EFFECT_NONE;
import static io.agora.scene.show.beauty.BeautyConstantsKt.ITEM_ID_EFFECT_YUANQI;
import static io.agora.scene.show.beauty.BeautyConstantsKt.ITEM_ID_FILTER_CREAM;
import static io.agora.scene.show.beauty.BeautyConstantsKt.ITEM_ID_FILTER_MAKALONG;
import static io.agora.scene.show.beauty.BeautyConstantsKt.ITEM_ID_FILTER_NONE;
import static io.agora.scene.show.beauty.BeautyConstantsKt.ITEM_ID_STICKER_HUAHUA;
import static io.agora.scene.show.beauty.BeautyConstantsKt.ITEM_ID_STICKER_NONE;

import android.content.Context;
import android.graphics.Matrix;
import android.opengl.GLES11;
import android.opengl.GLES11Ext;

import com.sensetime.effects.STRenderer;
import com.sensetime.effects.utils.FileUtils;
import com.sensetime.stmobile.STCommonNative;
import com.sensetime.stmobile.params.STEffectBeautyType;

import java.io.File;
import java.nio.ByteBuffer;

import io.agora.base.TextureBufferHelper;
import io.agora.base.VideoFrame;
import io.agora.base.internal.video.EglBase;
import io.agora.base.internal.video.YuvHelper;
import io.agora.scene.show.beauty.BeautyCache;
import io.agora.scene.show.beauty.IBeautyProcessor;

public class BeautySenseTimeImpl extends IBeautyProcessor {

    private final Context mContext;
    private STRenderer mSTRenderer;


    private TextureBufferHelper textureBufferHelper;

    private boolean isFrontCamera = true;
    private EglBase.Context mEglBaseContext;
    private ByteBuffer mNV21Buffer;
    private byte[] mNV21ByteArray;

    private volatile boolean sdkIsInit = false;
    private volatile boolean isReleased = false;


    public BeautySenseTimeImpl(Context context) {
        mContext = context.getApplicationContext();
    }

    @Override
    public void release() {
        super.release();
        mEglBaseContext = null;
        isReleased = true;
        if (textureBufferHelper != null) {
            textureBufferHelper.invoke(() -> {
                unInitST();
                return null;
            });
            textureBufferHelper.dispose();
            textureBufferHelper = null;
        }
        sdkIsInit = false;
    }

    private void initST() {
        if (sdkIsInit) {
            return;
        }
        mSTRenderer = new STRenderer(mContext);
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
    public boolean onCaptureVideoFrame(VideoFrame videoFrame) {
        if (!isEnable() || isReleased) {
            return true;
        }

        VideoFrame.Buffer buffer = videoFrame.getBuffer();

        // 获取NV21数据
        VideoFrame.I420Buffer i420Buffer = buffer.toI420();
        int nv21Size = (buffer.getWidth() * buffer.getHeight() * 3 + 1) / 2;
        if (mNV21Buffer == null || mNV21Buffer.capacity() != nv21Size) {
            mNV21Buffer = ByteBuffer.allocateDirect(nv21Size);
            mNV21ByteArray = new byte[nv21Size];
        }
        mNV21Buffer.clear();
        mNV21Buffer.position(0);

        YuvHelper.I420ToNV12(i420Buffer.getDataY(), i420Buffer.getStrideY(),
                i420Buffer.getDataV(), i420Buffer.getStrideV(),
                i420Buffer.getDataU(), i420Buffer.getStrideV(),
                mNV21Buffer,
                buffer.getWidth(), buffer.getHeight());
        mNV21Buffer.position(0);
        mNV21Buffer.get(mNV21ByteArray);
        i420Buffer.release();


        int texture;
        Matrix transformMatrix;
        if (buffer instanceof VideoFrame.TextureBuffer) {
            VideoFrame.TextureBuffer textureBuffer = (VideoFrame.TextureBuffer) buffer;
            if (textureBufferHelper == null) {
                mEglBaseContext = textureBuffer.getEglBaseContext();
                textureBufferHelper = TextureBufferHelper.create("BeautyProcessor", mEglBaseContext);
                textureBufferHelper.invoke(() -> {
                    initST();
                    return null;
                });
            } else if (mEglBaseContext != textureBuffer.getEglBaseContext()) {
                textureBufferHelper.invoke(() -> {
                    unInitST();
                    return null;
                });
                textureBufferHelper.dispose();
                textureBufferHelper = null;
                return true;
            }
            texture = textureBufferHelper.invoke(() -> {
                int texFormat = textureBuffer.getType() == VideoFrame.TextureBuffer.Type.OES ? GLES11Ext.GL_TEXTURE_EXTERNAL_OES : GLES11.GL_TEXTURE_2D;

                return mSTRenderer.preProcess(
                        textureBuffer.getWidth(), textureBuffer.getHeight(), videoFrame.getRotation(),
                        mNV21ByteArray, STCommonNative.ST_PIX_FMT_NV21,
                        textureBuffer.getTextureId(), texFormat);
            });
            transformMatrix = textureBuffer.getTransformMatrix();

        } else {
            if (textureBufferHelper == null) {
                mEglBaseContext = null;
                textureBufferHelper = TextureBufferHelper.create("BeautyProcessor", null);
                textureBufferHelper.invoke(() -> {
                    initST();
                    return null;
                });
            } else if (mEglBaseContext != null) {
                textureBufferHelper.invoke(() -> {
                    unInitST();
                    return null;
                });
                textureBufferHelper.dispose();
                textureBufferHelper = null;
                return true;
            }

            texture = textureBufferHelper.invoke(() ->
                    mSTRenderer.preProcess(buffer.getWidth(), buffer.getHeight(), videoFrame.getRotation(), mNV21ByteArray, STCommonNative.ST_PIX_FMT_NV21));
            transformMatrix = new Matrix();
        }

        boolean isFront = videoFrame.getRotation() == 270;
        if (isFrontCamera != isFront) {
            isFrontCamera = isFront;
            return true;
        }

        if (texture < 0) {
            return true;
        }

        VideoFrame.TextureBuffer newBuffer = textureBufferHelper.wrapTextureBuffer(
                buffer.getWidth(),
                buffer.getHeight(),
                VideoFrame.TextureBuffer.Type.RGB,
                texture, transformMatrix);
        videoFrame.replaceBuffer(newBuffer, videoFrame.getRotation(), videoFrame.getTimestampNs());
        return true;
    }

    @Override
    protected void setFaceBeautifyAfterCached(int itemId, float intensity) {
        if (!sdkIsInit || isReleased) {
            return;
        }

        mSTRenderer.setBeautyMode(STEffectBeautyType.EFFECT_BEAUTY_BASE_FACE_SMOOTH, STEffectBeautyType.SMOOTH1_MODE);
        mSTRenderer.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_BASE_FACE_SMOOTH, BeautyCache.INSTANCE.getItemValue(ITEM_ID_BEAUTY_SMOOTH));
        mSTRenderer.setBeautyMode(STEffectBeautyType.EFFECT_BEAUTY_BASE_WHITTEN, STEffectBeautyType.WHITENING1_MODE);
        mSTRenderer.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_BASE_WHITTEN, BeautyCache.INSTANCE.getItemValue(ITEM_ID_BEAUTY_WHITEN));

        mSTRenderer.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_RESHAPE_NARROW_FACE, BeautyCache.INSTANCE.getItemValue(ITEM_ID_BEAUTY_OVERALL));
        mSTRenderer.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_RESHAPE_SHRINK_FACE, BeautyCache.INSTANCE.getItemValue(ITEM_ID_BEAUTY_CHEEKBONE));
        mSTRenderer.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_RESHAPE_SHRINK_JAW, BeautyCache.INSTANCE.getItemValue(ITEM_ID_BEAUTY_JAWBONE));
        mSTRenderer.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_RESHAPE_ENLARGE_EYE, BeautyCache.INSTANCE.getItemValue(ITEM_ID_BEAUTY_EYE));
        mSTRenderer.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_WHITE_TEETH, BeautyCache.INSTANCE.getItemValue(ITEM_ID_BEAUTY_TEETH));
        mSTRenderer.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_THINNER_HEAD, BeautyCache.INSTANCE.getItemValue(ITEM_ID_BEAUTY_FOREHEAD));
        mSTRenderer.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_NARROW_NOSE, BeautyCache.INSTANCE.getItemValue(ITEM_ID_BEAUTY_NOSE));
        mSTRenderer.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_MOUTH_SIZE, BeautyCache.INSTANCE.getItemValue(ITEM_ID_BEAUTY_MOUTH));
        mSTRenderer.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_CHIN_LENGTH, BeautyCache.INSTANCE.getItemValue(ITEM_ID_BEAUTY_CHIN));
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
            mSTRenderer.cleanStyle();
        }
    }

    @Override
    protected void setStickerAfterCached(int itemId) {
        if (!sdkIsInit || isReleased) {
            return;
        }

        if (itemId == ITEM_ID_STICKER_NONE) {
            mSTRenderer.removeStickers();
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
        mSTRenderer.setStyle(path, strength, strength);
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


}
