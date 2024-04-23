package io.agora.rtmsyncmanager.model;

import androidx.annotation.IntDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@IntDef({
        AUIPlayStatus.idle,
        AUIPlayStatus.playing,
})
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.FIELD})
public @interface AUIPlayStatus {
    int idle = 0; // 未播放
    int playing = 1; // 播放中
}
