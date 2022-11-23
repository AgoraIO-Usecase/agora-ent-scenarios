package io.agora.scene.voice.imkit.bean;

import java.util.HashMap;
import java.util.Map;

import io.agora.scene.voice.imkit.custorm.CustomMsgType;


public class ChatMessageData {
    private String from;
    private String to;
    private String mMessageId;
    private String mContent;
    private String conversationId;
    //消息类型
    private String mType;
    //自定义消息类型
    private CustomMsgType customMsgType;
    private String mEvent;
    private Map<String,Object> mExt = new HashMap<>();
    private Map<String,String> mCustomParams = new HashMap<>();

    public String getFrom() {
        return from;
    }

    public void setForm(String from) {
        this.from = from;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getTo() {
        return to;
    }

    public void setContent(String mContent) {
        this.mContent = mContent;
    }

    public String getContent() {
        return mContent;
    }

    public void setMessageId(String mMessageId) {
        this.mMessageId = mMessageId;
    }

    public String getMessageId() {
        return mMessageId;
    }

    public void setExt(Map<String,Object> ext) {
        mExt.putAll(ext);
    }

    public Map<String,Object> getExt() {
        return mExt;
    }

    public void setType(String mType) {
        this.mType = mType;
    }

    public String getType() {
        return mType;
    }

    public void setEvent(String mEvent) {
        this.mEvent = mEvent;
    }

    public String getEvent() {
        return mEvent;
    }

    public void setCustomMsgType(CustomMsgType customMsgType) {
        this.customMsgType = customMsgType;
    }

    public CustomMsgType getCustomMsgType() {
        return customMsgType;
    }

    public void setCustomParams(Map<String, String> mCustomParams) {
        this.mCustomParams = mCustomParams;
    }

    public Map<String, String> getCustomParams() {
        return mCustomParams;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getConversationId() {
        return conversationId;
    }
}
