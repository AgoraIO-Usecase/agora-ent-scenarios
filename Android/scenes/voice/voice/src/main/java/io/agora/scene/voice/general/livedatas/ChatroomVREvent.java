package io.agora.scene.voice.general.livedatas;


import java.io.Serializable;


/**
 * 语聊房event
 */
public class ChatroomVREvent implements Serializable {
    public boolean refresh;
    public String event;
    public TYPE type;
    public String message;

    public ChatroomVREvent() {}

    public ChatroomVREvent(String event, TYPE type, boolean refresh) {
        this.refresh = refresh;
        this.event = event;
        this.type = type;
    }

    public ChatroomVREvent(String event, TYPE type) {
        this.refresh = true;
        this.event = event;
        this.type = type;
    }

    public static ChatroomVREvent create(String event, TYPE type) {
        return new ChatroomVREvent(event, type);
    }

    public static ChatroomVREvent create(String event, TYPE type, String message) {
        ChatroomVREvent easeEvent = new ChatroomVREvent(event, type);
        easeEvent.message = message;
        return easeEvent;
    }

    public static ChatroomVREvent create(String event, TYPE type, boolean refresh) {
        return new ChatroomVREvent(event, type, refresh);
    }

    public boolean isMessageChange() {
        return type == TYPE.MESSAGE;
    }

    public boolean isChatRoomLeave() {
        return type == TYPE.CHAT_ROOM_LEAVE;
    }

    public boolean isNotifyChange() {
        return type == TYPE.NOTIFY;
    }

    public boolean isVRDataChange() {
        return type == TYPE.VR_DATA_CHANGE;
    }

    public enum TYPE {
       MESSAGE, NOTIFY,CHAT_ROOM_LEAVE,VR_DATA_CHANGE
    }
}
