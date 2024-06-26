package io.agora.onetoone.signalClient

import io.agora.onetoone.AGError

/*
 * 信令回调协议
 */
interface ISignalClientListener {
    /**
     * 收到消息的回调
     * @param message 消息内容
     */
    fun onMessageReceive(message: String)

    /**
     * 信令日志回调
     * @param message 日志消息内容
     * @param logLevel 日志优先级
     */
    fun debugInfo(message: String, logLevel: Int)
}

/*
 * 信令抽象协议, 可以使用自己实现的信息通道
 */
interface ISignalClient {
    /**
     * CallApi往信令系统发消息
     * @param userId 目标用户id
     * @param message 消息对象
     * @param completion 完成回调
     */
    fun sendMessage(userId: String, message: String, completion: ((AGError?)-> Unit)?)

    /**
     * 注册信令系统回调
     * @param listener ISignalClientListener对象
     */
    fun addListener(listener: ISignalClientListener)

    /**
     * 移除信令系统回调
     * @param listener ISignalClientListener对象
     */
    fun removeListener(listener: ISignalClientListener)
}