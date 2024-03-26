package io.agora.rtmsyncmanager.service.callback;

import androidx.annotation.NonNull;

public class AUIException extends Exception{
    public static final int ERROR_CODE_UNKNOWN = -1; //未知错误
    public static final int ERROR_CODE_RTC = -2; //rtc错误
    public static final int ERROR_CODE_RTM = -3; //rtm错误
    public static final int ERROR_CODE_RTM_PRESENCE = -4; //rtm presence错误
    public static final int ERROR_CODE_HTTP = -5; //http错误
    public static final int ERROR_CODE_NETWORK_PARSE = -6; //http响应解析错误
    public static final int ERROR_CODE_TOKEN = -7; //找不到对应房间token信息
    public static final int ERROR_CODE_SEAT_NOT_IDLE = -8; //麦位不空闲
    public static final int ERROR_CODE_SEAT_ALREADY_ENTER = -9; //已经上麦过了
    public static final int ERROR_CODE_SEAT_NOT_ENTER = -10; //观众未上麦
    public static final int ERROR_CODE_SONG_ALREADY_EXIST = -11; //歌曲已经选择过了
    public static final int ERROR_CODE_SONG_NOT_EXIST = -12; //歌曲已经选择过了
    public static final int ERROR_CODE_CHORISTER_ALREADY_EXIST = -13;
    public static final int ERROR_CODE_CHORISTER_NOT_EXIST = -14;
    public static final int ERROR_CODE_PERMISSION_LEAK = -15;
    public static final int ERROR_CODE_ROOM_EXITED = -16;
    public static final int ERROR_CODE_RTM_COLLECTION = -17;

    public final int code;


    public AUIException(int code, String message){
        super(message);
        this.code = code;
    }


    @NonNull
    @Override
    public String toString() {
        return "AUIException{" +
                "code=" + code +
                "message=" + getMessage() +
                "}";
    }
}
