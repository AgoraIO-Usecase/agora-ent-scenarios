package io.agora.scene.base.api;


import io.agora.scene.base.BuildConfig;

public class UrlConstants {
    //    public static final String BASE_URL = "http://124.220.20.196:8006";
    public static final String BASE_URL = BuildConfig.SERVER_HOST;
    public static final String BASE_REQUEST_LOGIN_BY_TOKEN = "/api-login";//
    public static final String BASE_REQUEST_ROOM_BY_TOKEN = "/api-room";//

    //发送验证码
    public static final String REQUEST_SEND_V_CODE = BASE_REQUEST_LOGIN_BY_TOKEN + "/users/verificationCode";

    //登录
    public static final String REQUEST_LOGIN = BASE_REQUEST_LOGIN_BY_TOKEN + "/users/login";

    //获取用户信息
    public static final String REQUEST_USER_INFO = BASE_REQUEST_LOGIN_BY_TOKEN + "/users/getUserInfo";

    //注销用户
    public static final String REQUEST_USER_CANCELLATION = BASE_REQUEST_LOGIN_BY_TOKEN + "/users/cancellation";

    //上传头像
    public static final String REQUEST_USER_UPLOAD_PHOTO = BASE_REQUEST_LOGIN_BY_TOKEN + "/upload";

    //创建房间
    public static final String REQUEST_CREATE_ROOM = BASE_REQUEST_ROOM_BY_TOKEN + "/roomInfo/createRoom";

    //获取房间详情
    public static final String REQUEST_GET_ROOM_INFO = BASE_REQUEST_ROOM_BY_TOKEN + "/roomInfo/getRoomInfo";

    //退出房间
    public static final String REQUEST_OUT_ROOM = BASE_REQUEST_ROOM_BY_TOKEN + "/roomInfo/outRoom";

    //关闭房间
    public static final String REQUEST_CLOSE_ROOM = BASE_REQUEST_ROOM_BY_TOKEN + "/roomInfo/closeRoom";

    //获取歌曲列表
    public static final String REQUEST_GET_SONGS_LIST = BASE_REQUEST_ROOM_BY_TOKEN + "/songs/getListPage";
    //获取歌曲列表
    public static final String REQUEST_GET_SONGS_LIST_POST = BASE_REQUEST_ROOM_BY_TOKEN + "/songs/getListPagePost";

    //已点列表
    public static final String REQUEST_GET_HAVE_ORDERED_LIST = BASE_REQUEST_ROOM_BY_TOKEN + "/roomSong/haveOrderedList";

    //点歌
    public static final String REQUEST_GET_CHOOSE_SONG = BASE_REQUEST_ROOM_BY_TOKEN + "/roomSong/chooseSong";

    //删歌
    public static final String REQUEST_GET_DELETE_SONG = BASE_REQUEST_ROOM_BY_TOKEN + "/roomSong/delSong";

    //置顶
    public static final String REQUEST_GET_TO_TOP_SONG = BASE_REQUEST_ROOM_BY_TOKEN + "/roomSong/toDevelop";

    //切歌
    public static final String REQUEST_GET_SWITCH_SONG = BASE_REQUEST_ROOM_BY_TOKEN + "/roomSong/switchSong";

    //静音
    public static final String REQUEST_USER_CHANGE_IF_QUIET = BASE_REQUEST_ROOM_BY_TOKEN + "/roomUsers/ifQuiet";

    //开摄像头
    public static final String REQUEST_USER_CHANGE_OPEN_CAMERA = BASE_REQUEST_ROOM_BY_TOKEN + "/roomUsers/openCamera";

    //加入合唱
    public static final String REQUEST_GET_JOIN_CHORUS = BASE_REQUEST_ROOM_BY_TOKEN + "/roomSong/chorus";

    //修改用户信息
    public static final String REQUEST_USER_UPDATE = BASE_REQUEST_LOGIN_BY_TOKEN + "/users/update";

    //获取歌曲列表
    public static final String REQUEST_GET_SONGS_SONG_HOT = BASE_REQUEST_ROOM_BY_TOKEN + "/songs/songHot";

    //获取歌曲详情
    public static final String REQUEST_GET_SONG_ON_LINE = BASE_REQUEST_ROOM_BY_TOKEN + "/songs/getSongOnline";

    //房间列表
    public static final String REQUEST_ROOM_LIST = BASE_REQUEST_ROOM_BY_TOKEN + "/roomInfo/roomList";

    //上麦
    public static final String REQUEST_ROOM_HAVE_SEAT = BASE_REQUEST_ROOM_BY_TOKEN + "/roomInfo/onSeat";

    //下麦
    public static final String REQUEST_ROOM_LEAVE_SEAT = BASE_REQUEST_ROOM_BY_TOKEN + "/roomInfo/outSeat";

    //开始唱
    public static final String REQUEST_ROOM_SONG_BEGIN = BASE_REQUEST_ROOM_BY_TOKEN + "/roomSong/begin";

    //唱完
    public static final String REQUEST_ROOM_SONG_OVER = BASE_REQUEST_ROOM_BY_TOKEN + "/roomSong/over";

    //修改背景
    public static final String REQUEST_ROOM_UPDATE_ROOM = BASE_REQUEST_ROOM_BY_TOKEN + "/roomInfo/updateRoom";

    //取消合唱
    public static final String REQUEST_ROOM_ROOM_CANCEL_CHORUS = BASE_REQUEST_ROOM_BY_TOKEN + "/roomSong/cancelChorus";
    //获取RTM token
    public static final String REQUEST_ROOM_RTM_TOKEN = BASE_REQUEST_ROOM_BY_TOKEN + "/users/getToken";
}
