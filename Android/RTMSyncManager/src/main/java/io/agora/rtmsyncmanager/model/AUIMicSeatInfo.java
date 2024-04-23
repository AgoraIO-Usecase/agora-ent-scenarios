package io.agora.rtmsyncmanager.model;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

public class AUIMicSeatInfo {

    @SerializedName("owner")
    public @Nullable AUIUserThumbnailInfo user;

    /** 麦位索引，可以不需要，根据麦位list可以计算出 */
    @SerializedName("micSeatNo")
    public int seatIndex = 0;
    /** 麦位状态 */
    @SerializedName("micSeatStatus")
    public @AUIMicSeatStatus int seatStatus = AUIMicSeatStatus.idle;
    /** 麦位禁用声音 */
    @SerializedName("isMuteAudio")
    public boolean muteAudio = false;

    /** 麦位禁用视频 */
    @SerializedName("isMuteVideo")
    public boolean muteVideo = false;

    @Override
    public String toString() {
        return "AUIMicSeatInfo{" +
                "user=" + user +
                ", seatIndex=" + seatIndex +
                ", seatStatus=" + seatStatus +
                ", muteAudio=" + muteAudio +
                ", muteVideo=" + muteVideo +
                '}';
    }
}
