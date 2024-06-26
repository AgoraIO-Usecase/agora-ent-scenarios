package io.agora.scene.ktv.singbattle.widget.song;

public class SongItem {
    // 歌曲信息
    public String songNo;// 歌曲的唯一标识
    public String songName;
    public String imageUrl; // 歌曲封面
    public String singer; // 歌手名

    // 已点歌曲信息
    public String chooser; // 点歌人
    public String chooserId;
    public boolean isChosen; // 是否已被点
    public boolean enable = true;

    public SongItem(String songNo, String songName, String imageUrl, String singer, String chooserId) {
        this(songNo, songName, imageUrl, singer, "", false, chooserId);
    }

    public SongItem(String songNo, String songName,
                    String imageUrl, String singer,
                    String chooser, boolean isChosen, String chooserId) {
        this.songNo = songNo;
        this.songName = songName;
        this.imageUrl = imageUrl;
        this.singer = singer;
        this.chooser = chooser;
        this.chooserId = chooserId;
        this.isChosen = isChosen;
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
