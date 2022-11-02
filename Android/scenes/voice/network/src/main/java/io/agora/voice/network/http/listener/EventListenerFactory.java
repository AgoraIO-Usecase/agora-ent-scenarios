//package io.agora.voice.network.http.listener;
//
//import okhttp3.Call;
//import okhttp3.EventListener;
//
///**
// * @author create by zhangwei03
// */
//public class EventListenerFactory implements EventListener.Factory {
//    @Override
//    public EventListener create(Call call) {
//        NetEventModel tag = call.request().tag(NetEventModel.class);
//        return tag != null ? new NetEventListener(tag) : EventListener.NONE;
//    }
//}
