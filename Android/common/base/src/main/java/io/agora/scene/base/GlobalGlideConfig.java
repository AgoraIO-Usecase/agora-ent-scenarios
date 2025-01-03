package io.agora.scene.base;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.module.AppGlideModule;

@GlideModule
public class GlobalGlideConfig extends AppGlideModule {
    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        super.applyOptions(context, builder);
        // Set memory cache size to 20MB
        long memoryCacheSizeBytes = 1024 * 1024 * 20;
        builder.setMemoryCache(new LruResourceCache(memoryCacheSizeBytes));
        // Set bitmap pool size to 30MB
        long bitmapPoolSizeBytes = 1024 * 1024 * 30;
        builder.setBitmapPool(new LruBitmapPool(bitmapPoolSizeBytes));
        builder.setLogLevel(Log.ERROR);
    }
}
