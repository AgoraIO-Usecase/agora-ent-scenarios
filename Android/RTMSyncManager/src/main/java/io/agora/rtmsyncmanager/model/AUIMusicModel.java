package io.agora.rtmsyncmanager.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AUIMusicModel {
    public @NonNull String songCode = ""; // 歌曲id，mcc则对应songCode
    public @NonNull String name = ""; // 歌曲名称
    public @NonNull String singer = ""; // 演唱者
    public @NonNull String poster = ""; // 歌曲封面海报
    public @NonNull String releaseTime = ""; // 发布时间
    public int duration = 0; // 歌曲长度，单位秒
    public @Nullable String musicUrl; // 歌曲url，mcc则为空
    public @Nullable String lrcUrl; // 歌词url，mcc则为空
}
