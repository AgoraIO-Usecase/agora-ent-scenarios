package io.agora.scene.ktv.widget.song;

/**
 * The type Song item.
 */
public class SongItem {
    /**
     * The Song no.
     */
// 歌曲信息
    public String songNo;// 歌曲的唯一标识
    /**
     * The Song name.
     */
    public String songName;
    /**
     * The Image url.
     */
    public String imageUrl; // 歌曲封面
    /**
     * The Singer.
     */
    public String singer; // 歌手名

    /**
     * The Chooser.
     */
// 已点歌曲信息
    public String chooser; // 点歌人
    /**
     * The Chooser id.
     */
    public String chooserId;
    /**
     * The Is chosen.
     */
    public boolean isChosen; // 是否已被点

    public Boolean loading = false; // 是否正在加载中

    /**
     * Instantiates a new Song item.
     *
     * @param songNo    the song no
     * @param songName  the song name
     * @param imageUrl  the image url
     * @param singer    the singer
     * @param chooserId the chooser id
     */
    public SongItem(String songNo, String songName, String imageUrl, String singer, String chooserId) {
        this(songNo, songName, imageUrl, singer, "", false, chooserId);
    }

    /**
     * Instantiates a new Song item.
     *
     * @param songNo    the song no
     * @param songName  the song name
     * @param imageUrl  the image url
     * @param singer    the singer
     * @param chooser   the chooser
     * @param isChosen  the is chosen
     * @param chooserId the chooser id
     */
    public SongItem(String songNo, String songName,
                    String imageUrl, String singer,
                    String chooser, boolean isChosen, String chooserId) {
        this.songNo = songNo;
        this.songName = songName;
        this.imageUrl = imageUrl;
        this.singer = singer;
        this.chooser = chooser;
        this.isChosen = isChosen;
        this.chooserId = chooserId;
    }

    // 用于存放原始数据
    private Object tag;

    /**
     * Sets tag.
     *
     * @param <T> the type parameter
     * @param tag the tag
     */
    public <T> void setTag(T tag) {
        this.tag = tag;
    }

    /**
     * Get tag t.
     *
     * @param <T>   the type parameter
     * @param clazz the clazz
     * @return the t
     */
    public <T> T getTag(Class<T> clazz){
        if(!clazz.isInstance(tag)){
            return null;
        }
        return (T) tag;
    }

    @Override
    public String toString() {
        return "SongItem{" +
                "songNo='" + songNo + '\'' +
                ", songName='" + songName + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", singer='" + singer + '\'' +
                ", chooser='" + chooser + '\'' +
                ", chooserId='" + chooserId + '\'' +
                ", isChosen=" + isChosen +
                ", tag=" + tag +
                '}';
    }
}
