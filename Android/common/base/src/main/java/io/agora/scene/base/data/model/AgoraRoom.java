package io.agora.scene.base.data.model;


import java.util.List;

import io.agora.scene.base.R;

public class AgoraRoom {
    /**
     * id
     */
    public String id;
    /**
     * 房间名称
     */
    public String name;
    /**
     * 是否是私密 0 否 1 是
     */
    public int isPrivate;
    /**
     * 密码
     */
    public String password;
    /**
     * 创建人
     */
    public String creatorNo;
    /**
     * 房间号码
     */
    public String roomNo;
    /**
     * 是否合唱  无用
     */
    public int isChorus;
    /**
     * 背景图
     */
    public String bgOption;
    /**
     * 声音效果
     */
    public String soundEffect;
    /**
     * 美声 唱法
     */
    public String belCanto;
    public String icon;

    public String agoraRTMToken;
    public String agoraRTCToken;
    public String agoraPlayerRTCToken;

    public String createdAt;
    public String updatedAt;
    public String deletedAt;
    /**
     * 状态
     */
    public int status;
    /**
     * 房间人数
     */
    public int roomPeopleNum;

    public List<AgoraMember> roomUserInfoDTOList;
    public List<MusicModelNew> roomSongInfoDTOS;

    public int getCoverRes() {
        if ("1".equals(icon)) {
            return R.mipmap.icon_room_cover1;
        } else if ("2".equals(icon)) {
            return R.mipmap.icon_room_cover2;
        } else if ("3".equals(icon)) {
            return R.mipmap.icon_room_cover3;
        } else if ("4".equals(icon)) {
            return R.mipmap.icon_room_cover4;
        } else if ("5".equals(icon)) {
            return R.mipmap.icon_room_cover5;
        } else if ("6".equals(icon)) {
            return R.mipmap.icon_room_cover6;
        } else if ("7".equals(icon)) {
            return R.mipmap.icon_room_cover7;
        } else if ("8".equals(icon)) {
            return R.mipmap.icon_room_cover8;
        } else if ("9".equals(icon)) {
            return R.mipmap.icon_room_cover9;
        }
        return R.mipmap.icon_room_cover1;
    }
}
