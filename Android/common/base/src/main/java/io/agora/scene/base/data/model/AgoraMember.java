package io.agora.scene.base.data.model;

public class AgoraMember {

    public enum Role {
        Listener(0), Owner(1), Speaker(2);
        private int value;

        Role(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static Role parse(int value) {
            if (value == 0) {
                return Role.Listener;
            } else if (value == 1) {
                return Role.Owner;
            } else if (value == 2) {
                return Role.Speaker;
            }
            return Role.Listener;
        }
    }

    public Long id;
    public AgoraRoom roomId;
    public String userNo;
    public Role role = Role.Listener;
    public int isSelfMuted = 0;
    //0 显示头像 1显示视频
    public int isVideoMuted = 0;
    //坐位号
    public int onSeat = 1;

    public boolean isMaster;

    public String headUrl;

    public String name;

    public Long getStreamId() {
        return id;
    }
}
