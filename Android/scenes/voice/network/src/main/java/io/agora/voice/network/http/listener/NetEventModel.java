package io.agora.voice.network.http.listener;

/**
 * @author create by zhangwei03
 */
public class NetEventModel {

    public long fetch_duration; //请求发出到拿到数据，不包括本地排队时间
    public long dns_duration; //dns解析时间
    public long connect_duration; // 创建socket通道时间
    public long secure_duration; // ssl握手时间，connect_duration包含secure_duration
    public long request_duration; // writeBytes的时间
    public long response_duration; // readBytes的时间
    public long serve_duration; // 相当于responseStartDate - requestEndDate

}
