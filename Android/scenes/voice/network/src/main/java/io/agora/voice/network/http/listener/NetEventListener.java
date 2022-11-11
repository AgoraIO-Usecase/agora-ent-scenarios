//package io.agora.voice.network.http.listener;
//
//import java.io.IOException;
//import java.net.InetAddress;
//import java.net.InetSocketAddress;
//import java.net.Proxy;
//import java.util.List;
//
//import io.agora.voice.buddy.tool.LogToolsKt;
//import okhttp3.Call;
//import okhttp3.EventListener;
//import okhttp3.Handshake;
//import okhttp3.Protocol;
//import okhttp3.Request;
//import okhttp3.Response;
//
///**
// * @author create by zhangwei03
// */
//public class NetEventListener extends EventListener {
//    private long callStart;
//    //建立连接
//    private long dnsStart;
//    private long connectStart;
//    private long secureConnectStart;
//
//    //连接已经建立
//    private long requestStart;
//    private long responseStart;
//
//    public long MAX_TIME = 30000;
//
//    private NetEventModel model;
//
//    public NetEventListener(NetEventModel model) {
//        this.model = model;
//    }
//
//    public NetEventModel getModel() {
//        return model;
//    }
//
//    @Override
//    public void callStart(Call call) {
//        callStart = System.currentTimeMillis();
//        recordEventLog(call,"callStart",0);
//    }
//
//    @Override
//    public void dnsStart(Call call, String domainName) {
//        dnsStart = System.currentTimeMillis();
//    }
//
//    @Override
//    public void dnsEnd(Call call, String domainName, List<InetAddress> inetAddressList) {
//        model.dns_duration = System.currentTimeMillis() - dnsStart;
//
//        if (model.dns_duration >= MAX_TIME) {
//            model.dns_duration = 0;
//        }
//        recordEventLog(call,"dnsEnd",model.dns_duration);
//    }
//
//    @Override
//    public void connectStart(Call call, InetSocketAddress inetSocketAddress, Proxy proxy) {
//        connectStart = System.currentTimeMillis();
//    }
//
//    @Override
//    public void secureConnectStart(Call call) {
//        secureConnectStart = System.currentTimeMillis();
//    }
//
//    @Override
//    public void secureConnectEnd(Call call, Handshake handshake) {
//        model.secure_duration = System.currentTimeMillis() - secureConnectStart;
//
//        if (model.secure_duration >= MAX_TIME) {
//            model.secure_duration = 0;
//        }
//        recordEventLog(call,"secureConnectEnd",model.secure_duration);
//    }
//
//    @Override
//    public void connectEnd(Call call, InetSocketAddress inetSocketAddress, Proxy proxy, Protocol protocol) {
//        model.connect_duration = System.currentTimeMillis() - connectStart;
//
//        if (model.connect_duration >= MAX_TIME) {
//            model.connect_duration = 0;
//        }
//
//        //因为connectionAcquired可能会有多次，那么请求从此处开始计时
//        requestStart =  System.currentTimeMillis();
//        model.request_duration = 0;
//    }
//
//    @Override
//    public void connectFailed(Call call, InetSocketAddress inetSocketAddress, Proxy proxy, Protocol protocol, IOException ioe) {
//        model.connect_duration = System.currentTimeMillis() - connectStart;
//        recordEventLog(call,"connectFailed",model.connect_duration);
//        if (model.connect_duration >= MAX_TIME) {
//            model.connect_duration = 0;
//        }
//    }
//
//    @Override
//    public void requestHeadersEnd(Call call, Request request) {
//        model.request_duration = System.currentTimeMillis() - requestStart;
//        recordEventLog(call,"requestHeadersEnd",model.request_duration);
//        if (model.request_duration >= MAX_TIME) {
//            model.request_duration = 0;
//        }
//    }
//
//    @Override
//    public void requestBodyEnd(Call call, long byteCount) {
//        model.request_duration = System.currentTimeMillis() - requestStart;
//        recordEventLog(call,"requestBodyEnd",model.request_duration);
//        if (model.request_duration >= MAX_TIME) {
//            model.request_duration = 0;
//        }
//
//    }
//
//    @Override
//    public void responseHeadersStart(Call call) {
//        responseStart = System.currentTimeMillis();
//        model.response_duration = 0;
//    }
//
//    @Override
//    public void responseHeadersEnd(Call call, Response response) {
//        model.response_duration = System.currentTimeMillis() - responseStart;
//        recordEventLog(call,"responseHeadersEnd",model.response_duration);
//        if (model.response_duration >= MAX_TIME) {
//            model.response_duration = 0;
//        }
//
//    }
//
//    @Override
//    public void responseBodyStart(Call call) {
//        if (responseStart == 0) {
//            responseStart = System.currentTimeMillis();
//        }
//    }
//
//    @Override
//    public void responseBodyEnd(Call call, long byteCount) {
//        model.response_duration = System.currentTimeMillis() - responseStart;
//        recordEventLog(call,"responseBodyEnd",model.response_duration);
//        if (model.response_duration >= MAX_TIME) {
//            model.response_duration = 0;
//        }
//
//        model.serve_duration = responseStart - (requestStart + model.request_duration);
//        recordEventLog(call,"responseBodyEnd",model.serve_duration);
//        if (model.serve_duration >= MAX_TIME) {
//            model.serve_duration = 0;
//        }
//    }
//
//    @Override
//    public void callEnd(Call call) {
//        model.fetch_duration = System.currentTimeMillis() - callStart;
//        recordEventLog(call,"callEnd",model.fetch_duration);
//        if (model.fetch_duration >= MAX_TIME) {
//            model.fetch_duration = 0;
//        }
//
//    }
//
//    @Override
//    public void callFailed(Call call, IOException ioe) {
//        model.fetch_duration = System.currentTimeMillis() - callStart;
//        recordEventLog(call,"callFailed",model.fetch_duration);
//        if (model.fetch_duration >= MAX_TIME) {
//            model.fetch_duration = 0;
//        }
//    }
//
//    private void recordEventLog(Call call,String name, long timestamp) {
//        try{
//            String msg = call.request().url().uri().getPath() + "，"+ name+ ":" + timestamp;
//            LogToolsKt.logE(msg, "NetEventListener");
//        }catch (Exception e){
//
//        }
//    }
//}
