package io.agora.voice.network.http;

import android.text.TextUtils;
import android.util.Log;

public class VRRequestApi {
    VRRequestApi(){}
    public static VRRequestApi mInstance;
    private String BASE_URL = "";
    private final String BASE_ROOM = "/voice/room/%1$s";
    private final String BASE_MEMBERS = "/voice/room/%1$s/members";
    private final String BASE_MIC = "/voice/room/%1$s/mic";
    private final String LOGIN = "/user/login/device";
    private final String ROOM_LIST = "/voice/room/list?limit=%1$s";
    private final String CREATE_ROOM = "/voice/room/create";
    private final String ROOM_DETAILS = "/voice/room/%1$s";
    private final String FETCH_ROOM_MEMBERS = "/voice/room/%1$s/members/list?limit=%2$s";
    private final String JOIN_ROOM = "/join";
    private final String CHECK = "/validPassword";
    private final String LEAVE_ROOM = "/leave";
    private final String KICK_USER = "/kick";
    private final String BASE_GIFT = "/voice/room/%1$s/gift";
    private final String GIFT_LIST = "/list";
    private final String GIFT_ADD = "/add";
    private final String FETCH_APPLY_MEMBERS = "/voice/room/%1$s/mic/apply?limit=%2$s";
    private final String MIC_APPLY = "/apply";
    private final String MIC_CLOSE = "/close";
    private final String MIC_LEAVE = "/leave";
    private final String MIC_MUTE = "/mute";
    private final String MIC_EXCHANGE = "/exchange";
    private final String MIC_KICK = "/kick";
    private final String MIC_LOCK = "/lock";
    private final String MIC_INVITE = "/invite";
    private final String MIC_REJECT_INVITE = "/refuse";
    private final String MIC_AGREE = "/agree";

    public static VRRequestApi get() {
        if(mInstance == null) {
            synchronized (VRRequestApi.class) {
                if(mInstance == null) {
                    mInstance = new VRRequestApi();
                }
            }
        }
        return mInstance;
    }
    public void setBaseUrl(String baseUrl){
        BASE_URL = baseUrl;
    }

    public String login(){
        return BASE_URL+LOGIN;
    }

    public String createRoom(){
        return BASE_URL+CREATE_ROOM;
    }

    public String fetchRoomInfo(String roomId) {
        return BASE_URL + String.format(ROOM_DETAILS, roomId);
    }

    public String deleteRoom(String roomId){
        return BASE_URL + CREATE_ROOM;
    }

    public String modifyRoomInfo(String roomId){
        return BASE_URL + String.format(ROOM_DETAILS,roomId);
    }

    public String getRoomList(String cursor,int limit,int type){
        String api = BASE_URL + String.format(ROOM_LIST,limit);
        if (type != -1){
            api = api + "&type=" + type;
        }
        if (!TextUtils.isEmpty(cursor)){
            api = api + "&cursor=" + cursor;
        }
        Log.e("getRoomList","api: " + api);
        return api;
    }

    public String fetchRoomMembers(String roomId,String cursor,int limit){
        String api = BASE_URL+ String.format(FETCH_ROOM_MEMBERS,roomId,limit);
        if (!TextUtils.isEmpty(cursor)){
            api = api + "&cursor=" + cursor;
        }
        return api;
    }

    public String joinRoom(String roomId){
        Log.e("joinRoom","url: "+BASE_URL + String.format(BASE_MEMBERS,roomId) + JOIN_ROOM);
        return BASE_URL + String.format(BASE_MEMBERS,roomId) + JOIN_ROOM;
    }

    public String checkPassword(String roomId){
        return BASE_URL + String.format(BASE_ROOM,roomId) +CHECK;
    }

    public String leaveRoom(String roomId){
        return BASE_URL + String.format(BASE_MEMBERS,roomId) + LEAVE_ROOM;
    }

    public String kickUser(String roomId,String uid){
        return BASE_URL + String.format(BASE_MEMBERS,roomId) + KICK_USER + "&uid="+uid;
    }

    public String fetchApplyMembers(String roomId,String cursor,int limit){
        String api = BASE_URL + String.format(FETCH_APPLY_MEMBERS,roomId,limit);
        if (!TextUtils.isEmpty(cursor)){
            api = api + "&cursor=" + cursor;
        }
        Log.e("fetchApplyMembers","url: " + api);
        return api;
    }

    public String submitApply(String roomId){
        return BASE_URL + String.format(BASE_MIC,roomId) + MIC_APPLY;
    }

    public String cancelApply(String roomId){
        return BASE_URL + String.format(BASE_MIC,roomId) + MIC_APPLY;
    }

    public String fetchMicsInfo(String roomId){
        return BASE_URL + String.format(BASE_MIC,roomId);
    }

    public String closeMic(String roomId){
        return BASE_URL + String.format(BASE_MIC,roomId) + MIC_CLOSE;
    }

    public String cancelCloseMic(String roomId, int micIndex) {
        return BASE_URL + String.format(BASE_MIC, roomId) + MIC_CLOSE + "?mic_index=" + micIndex;
    }

    public String leaveMic(String roomId, int micIndex) {
        return BASE_URL + String.format(BASE_MIC, roomId) + MIC_LEAVE + "?mic_index=" + micIndex;
    }

    public String muteMic(String roomId){
        return BASE_URL + String.format(BASE_MIC,roomId) + MIC_MUTE;
    }

    public String unMuteMic(String roomId,int micIndex){
        return BASE_URL + String.format(BASE_MIC,roomId) + MIC_MUTE + "?mic_index=" + micIndex;
    }

    public String exchangeMic(String roomId){
        return BASE_URL + String.format(BASE_MIC,roomId) + MIC_EXCHANGE;
    }

    public String kickMic(String roomId){
        return BASE_URL + String.format(BASE_MIC,roomId) + MIC_KICK;
    }

    public String rejectMicInvitation(String roomId){
        return BASE_URL + String.format(BASE_MIC,roomId) + MIC_INVITE +  MIC_REJECT_INVITE;
    }

    public String agreeMicInvitation(String roomId){
        return BASE_URL + String.format(BASE_MIC,roomId) + MIC_INVITE + MIC_AGREE;
    }

    public String lockMic(String roomId){
        return BASE_URL + String.format(BASE_MIC,roomId) + MIC_LOCK;
    }

    public String unlockMic(String roomId, int micIndex) {
        return BASE_URL + String.format(BASE_MIC, roomId) + MIC_LOCK + "?mic_index=" + micIndex;
    }

    public String inviteUserToMic(String roomId){
        return BASE_URL + String.format(BASE_MIC,roomId) + MIC_INVITE;
    }

    public String rejectApplyInvitation(String roomId){
        return BASE_URL + String.format(BASE_MIC,roomId)+ MIC_APPLY + MIC_INVITE;
    }

    public String applyAgreeInvitation(String roomId){
        return BASE_URL + String.format(BASE_MIC,roomId)+ MIC_APPLY + MIC_AGREE;
    }


    public String fetchGiftContribute(String roomId){
        return BASE_URL + String.format(BASE_GIFT,roomId) + GIFT_LIST;
    }

    public String giftTo(String roomId){
        return BASE_URL + String.format(BASE_GIFT,roomId) + GIFT_ADD;
    }
}
