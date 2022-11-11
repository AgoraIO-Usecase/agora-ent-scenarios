package io.agora.scene.base.bean;


import java.io.File;
import java.io.Serializable;

import io.agora.scene.base.data.model.MusicModelNew;

public class MemberMusicModel {

    public enum Type implements Serializable {
        Default, MiGu;
    }

    public enum SingType implements Serializable {
        Single(0), Chorus(1);

        public int value;

        SingType(int value) {
            this.value = value;
        }

        public static SingType parse(int value) {
            if (value == 0) {
                return SingType.Single;
            } else if (value == 1) {
                return SingType.Chorus;
            }
            return SingType.Single;
        }
    }

    public enum UserStatus implements Serializable {
        Idle(0), Ready(1);

        public int value;

        UserStatus(int value) {
            this.value = value;
        }

        public static UserStatus parse(int value) {
            if (value == 0) {
                return UserStatus.Idle;
            } else if (value == 1) {
                return UserStatus.Ready;
            }
            return UserStatus.Idle;
        }
    }

    public String userNo;
    public String songNo;
    public String chorusNo;
    public boolean isChorus;
    public int sort;
    public String singer;
    public int isOriginal;
    // 1 已唱 0未唱 2 正在唱
    public int status;
    public String songUrl;
    public String imageUrl;
    public String songName;

    public String name;

    public boolean isJoin;

    public String poster;
    public String song;
    public String lyric;

    public File fileMusic;
    public File fileLrc;

    public Type musicType = Type.MiGu;

    private SingType type = SingType.Single;

    public void setType(SingType type) {
        this.type = type;
    }

    public SingType getType() {
        if (isChorus) {
            return SingType.Chorus;
        } else {
            return SingType.Single;
        }
    }

    public Long userbgId;
    public UserStatus userStatus;

    public String userId;
    public Long user1bgId;
    public UserStatus user1Status;

    public String applyUser1Id;

    public MemberMusicModel() {

    }

    public MemberMusicModel(MusicModelNew modelNew) {
        this.songNo = modelNew.songNo;
        this.name = modelNew.songName;
        this.singer = modelNew.singer;
        this.poster = modelNew.imageUrl;
    }
}
