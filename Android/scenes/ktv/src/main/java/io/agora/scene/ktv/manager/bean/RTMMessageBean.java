package io.agora.scene.ktv.manager.bean;


import io.agora.scene.ktv.manager.RoomManager;

public class RTMMessageBean {
    //消息类型
    public String messageType;
    //头像
    public String headUrl;
    //名称
    public String name;
    //名称
    public Long id;
    //坐位号
    public int onSeat;
    //静音状态 0 非静音 1 静音
    public int isSelfMuted;
    //摄像头开启关闭 0 未开启 1 开启
    public int isVideoMuted;
    //用户id
    public String userNo;
    //歌曲id
    public String songNo;
    //歌曲名称
    public String songName;
    //房间名称
    public String roomNo = RoomManager.mRoom.roomNo;
    //uid 多人时使用
    public Long bgUid;
    //背景图 需要 顺序从0开始 以效果图顺序排序
    public String bgOption;
    //0 安卓 1 ios
    public int platform = 0;
    //打分用 人声音调（Hz）。取值范围为 [0.0,4000.0]
    public double pitch = 0;

}
