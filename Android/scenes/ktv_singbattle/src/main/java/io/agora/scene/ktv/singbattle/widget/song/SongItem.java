package io.agora.scene.ktv.singbattle.widget.song;

/**
 * The type Song item.
 */
public class SongItem {
    /**
     * The Song no.
     */
    public String songNo;// Song unique identifier
    /**
     * The Song name.
     */
    public String songName;
    /**
     * The Image url.
     */
    public String imageUrl; 
    /**
     * The Singer.
     */
    public String singer; 

    /**
     * The Chooser.
     */
    public String chooser;
    /**
     * The Chooser id.
     */
    public String chooserId;
    /**
     * The Is chosen.
     */
    public boolean isChosen;

    public Boolean loading = false;

    public boolean enable = true;

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
