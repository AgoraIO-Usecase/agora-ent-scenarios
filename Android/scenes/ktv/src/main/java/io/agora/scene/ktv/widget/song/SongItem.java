package io.agora.scene.ktv.widget.song;

public class SongItem {
    // 歌曲信息
    public String songNo;// 歌曲的唯一标识
    public String songName;
    public String imageUrl; // 歌曲封面

    // 已点歌曲信息
    public String chooser; // 点歌人
    public boolean isChosen; // 是否已被点
    public boolean isChorus; // 是否是合唱

    public SongItem(String songNo, String songName, String imageUrl) {
        this(songNo, songName, imageUrl, "", false, false);
    }

    public SongItem(String songNo, String songName, String imageUrl, String chooser, boolean isChosen, boolean isChorus) {
        this.songNo = songNo;
        this.songName = songName;
        this.imageUrl = imageUrl;
        this.chooser = chooser;
        this.isChosen = isChosen;
        this.isChorus = isChorus;
    }

    // 用于存放原始数据
    private Object tag;

    public <T> void setTag(T tag) {
        this.tag = tag;
    }

    public <T> T getTag(Class<T> clazz){
        if(!clazz.isInstance(tag)){
            return null;
        }
        return (T) tag;
    }
}
