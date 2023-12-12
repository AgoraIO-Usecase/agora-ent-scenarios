package io.agora.scene.joy.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;
import android.view.View;

public class VideoTextureView extends TextureView {

    private static final String TAG = "VideoTextureView";

    public int currentVideoWidth;
    public int currentVideoHeight;

    public VideoTextureView(Context context) {
        super(context);
        currentVideoWidth = 0;
        currentVideoHeight = 0;
    }

    public VideoTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        currentVideoWidth = 0;
        currentVideoHeight = 0;
    }

    public void setVideoSize(int currentVideoWidth, int currentVideoHeight) {
        if (this.currentVideoWidth != currentVideoWidth || this.currentVideoHeight != currentVideoHeight) {
            this.currentVideoWidth = currentVideoWidth;
            this.currentVideoHeight = currentVideoHeight;
            Log.e(TAG, "Video , width" + currentVideoWidth + ",height" + currentVideoHeight);
            requestLayout();
        }
    }

    @Override
    public void setRotation(float rotation) {
        if (rotation != getRotation()) {
            super.setRotation(rotation);
            requestLayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int viewRotation = (int) getRotation();
        int videoWidth = currentVideoWidth;
        int videoHeight = currentVideoHeight;

        int parentHeight = ((View) getParent()).getMeasuredHeight();
        int parentWidth = ((View) getParent()).getMeasuredWidth();
        if (parentWidth != 0 && parentHeight != 0 && videoWidth != 0 && videoHeight != 0) {
//            if (IjkVideoPlayer.VIDEO_IMAGE_DISPLAY_TYPE == IjkVideoPlayer.VIDEO_IMAGE_DISPLAY_TYPE_FILL_PARENT) {
//                if (viewRotation == 90 || viewRotation == 270) {
//                    int tempSize = parentWidth;
//                    parentWidth = parentHeight;
//                    parentHeight = tempSize;
//                }
//
//                videoHeight = videoWidth * parentHeight / parentWidth;
//            }
        }

        if (viewRotation == 90 || viewRotation == 270) {
            int tempMeasureSpec = widthMeasureSpec;
            widthMeasureSpec = heightMeasureSpec;
            heightMeasureSpec = tempMeasureSpec;
        }

        int width = getDefaultSize(videoWidth, widthMeasureSpec);
        int height = getDefaultSize(videoHeight, heightMeasureSpec);

        if (videoWidth > 0 && videoHeight > 0) {
            int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
            int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
            int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

            if (widthSpecMode == MeasureSpec.EXACTLY && heightSpecMode == MeasureSpec.EXACTLY) {
                // the size is fixed
                width = widthSpecSize;
                height = heightSpecSize;
                // for compatibility, we adjust size based on aspect ratio
                if (videoWidth * height < width * videoHeight) {
                    width = height * videoWidth / videoHeight;
                } else if (videoWidth * height > width * videoHeight) {
                    height = width * videoHeight / videoWidth;
                }
            } else if (widthSpecMode == MeasureSpec.EXACTLY) {
                // only the width is fixed, adjust the height to match aspect ratio if
                // possible
                width = widthSpecSize;
                height = width * videoHeight / videoWidth;
                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    height = heightSpecSize;
                    width = height * videoWidth / videoHeight;
                }
            } else if (heightSpecMode == MeasureSpec.EXACTLY) {
                // only the height is fixed, adjust the width to match aspect ratio if
                // possible
                height = heightSpecSize;
                width = height * videoWidth / videoHeight;
                if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    width = widthSpecSize;
                    height = width * videoHeight / videoWidth;
                }
            } else {
                // neither the width nor the height are fixed, try to use actual video size
                width = videoWidth;
                height = videoHeight;
                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // too tall, decrease both width and height
                    height = heightSpecSize;
                    width = height * videoWidth / videoHeight;
                }
                if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // too wide, decrease both width and height
                    width = widthSpecSize;
                    height = width * videoHeight / videoWidth;
                }
            }
        }

        if (parentWidth != 0 && parentHeight != 0 && videoWidth != 0 && videoHeight != 0) {
//            if (IjkVideoPlayer.VIDEO_IMAGE_DISPLAY_TYPE == IjkVideoPlayer.VIDEO_IMAGE_DISPLAY_TYPE_ORIGINAL) {
//                height = videoHeight;
//                width = videoWidth;
//            } else if (IjkVideoPlayer.VIDEO_IMAGE_DISPLAY_TYPE == IjkVideoPlayer.VIDEO_IMAGE_DISPLAY_TYPE_FILL_CROP) {
//                if (viewRotation == 90 || viewRotation == 270) {
//                    int tempSize = parentWidth;
//                    parentWidth = parentHeight;
//                    parentHeight = tempSize;
//                }
//
//                if (((double) videoHeight / videoWidth) > ((double) parentHeight / parentWidth)) {
//                    height = (int) (((double) parentWidth / (double) width * (double) height));
//                    width = parentWidth;
//                } else if (((double) videoHeight / videoWidth) < ((double) parentHeight / parentWidth)) {
//                    width = (int) (((double) parentHeight / (double) height * (double) width));
//                    height = parentHeight;
//                }
//            }
        }


        Log.e(TAG, "View , width" + width + ",height" + height);

        setMeasuredDimension(width, height);
    }
}
