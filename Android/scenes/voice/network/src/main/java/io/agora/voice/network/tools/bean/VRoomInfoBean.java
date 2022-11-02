//YApi QuickType插件生成，具体参考文档:https://github.com/RmondJone/YapiQuickType

package io.agora.voice.network.tools.bean;

import java.io.Serializable;
import java.util.List;

import io.agora.voice.buddy.config.ConfigConstants;

public class VRoomInfoBean implements Serializable {
    private List<VRMicBean> mic_info;
    private VRoomDetail room;

    public List<VRMicBean> getMic_info() {
        return mic_info;
    }

    public void setMic_info(List<VRMicBean> mic_info) {
        this.mic_info = mic_info;
    }

    public VRoomDetail getRoom() {
        return room;
    }

    public void setRoom(VRoomDetail value) {
        this.room = value;
    }

    public static class VRoomDetail implements Serializable{
        private VMemberBean owner;
        private String room_id;
        private boolean is_private;
        private List<VRankingMemberBean> ranking_list;
        private boolean use_robot;
        private int click_count;
        private boolean allowed_free_join_mic;
        private int type;
        private String name;
        private String sound_effect;
        private String chatroom_id;
        private int member_count;
        private String channel_id;
        private int gift_amount;
        private int robot_volume;
        private String announcement;

        public VMemberBean getOwner() {
            return owner;
        }

        public void setOwner(VMemberBean value) {
            this.owner = value;
        }

        public String getRoom_id() {
            return room_id;
        }

        public void setRoom_id(String room_id) {
            this.room_id = room_id;
        }

        public boolean isIs_private() {
            return is_private;
        }

        public void setIs_private(boolean is_private) {
            this.is_private = is_private;
        }

        public List<VRankingMemberBean> getRanking_list() {
            return ranking_list;
        }

        public void setRanking_list(List<VRankingMemberBean> ranking_list) {
            this.ranking_list = ranking_list;
        }

        public boolean isUse_robot() {
            return use_robot;
        }

        public void setUse_robot(boolean use_robot) {
            this.use_robot = use_robot;
        }

        public int getClick_count() {
            return click_count;
        }

        public void setClick_count(int click_count) {
            this.click_count = click_count;
        }

        public boolean isAllowed_free_join_mic() {
            return allowed_free_join_mic;
        }

        public void setAllowed_free_join_mic(boolean allowed_free_join_mic) {
            this.allowed_free_join_mic = allowed_free_join_mic;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSound_effect() {
            return sound_effect;
        }

        public void setSound_effect(String sound_effect) {
            this.sound_effect = sound_effect;
        }

        public String getChatroom_id() {
            return chatroom_id;
        }

        public void setChatroom_id(String chatroom_id) {
            this.chatroom_id = chatroom_id;
        }

        public int getMember_count() {
            return member_count;
        }

        public void setMember_count(int member_count) {
            this.member_count = member_count;
        }

        public String getChannel_id() {
            return channel_id;
        }

        public void setChannel_id(String channel_id) {
            this.channel_id = channel_id;
        }

        public int getGift_amount() {
            return gift_amount;
        }

        public void setGift_amount(int gift_amount) {
            this.gift_amount = gift_amount;
        }

        public int getRobot_volume() {
            return robot_volume;
        }

        public void setRobot_volume(int robot_volume) {
            this.robot_volume = robot_volume;
        }

        public String getAnnouncement() {
            return announcement;
        }

        public void setAnnouncement(String announcement) {
            this.announcement = announcement;
        }

        public int getSoundSelection() {
            if (sound_effect.equals(ConfigConstants.SoundSelectionText.Social_Chat)) {
                return ConfigConstants.SoundSelection.Social_Chat;
            } else if (sound_effect.equals(ConfigConstants.SoundSelectionText.Karaoke)) {
                return ConfigConstants.SoundSelection.Karaoke;
            } else if (sound_effect.equals(ConfigConstants.SoundSelectionText.Gaming_Buddy)) {
                return ConfigConstants.SoundSelection.Gaming_Buddy;
            } else if (sound_effect.equals(ConfigConstants.SoundSelectionText.Professional_Broadcaster)) {
                return ConfigConstants.SoundSelection.Professional_Broadcaster;
            } else {
                return ConfigConstants.SoundSelection.Social_Chat;
            }
        }
    }
}
