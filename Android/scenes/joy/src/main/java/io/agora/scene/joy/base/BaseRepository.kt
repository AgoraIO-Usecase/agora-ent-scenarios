package io.agora.scene.joy.base

import kotlin.reflect.KClass

open class BaseRepository {

    companion object {
        private const val TAG = "BaseRepository"
    }

    /**
     * repo 请求数据的公共方法，
     * 在不同状态下先设置 baseResp.dataState的值，最后将dataState 的状态通知给UI
     * @param block api的请求方法
     * @param stateLiveData 每个请求传入相应的LiveData，主要负责网络状态的监听
     */
    suspend inline fun <reified T : Any> executeResp(
        block: suspend () -> JoyApiResult<T>,
        stateLiveData: StateLiveData<T>,
        kClass: KClass<*>? = T::class
    ) {
        var baseResp = JoyApiResult<T>()
        try {
            baseResp.dataState = DataState.STATE_LOADING
            stateLiveData.postValue(baseResp)
            //开始请求数据
            val invoke = block.invoke()
            //将结果复制给baseResp
            baseResp = invoke
            if (baseResp.code == 0) {
                //请求成功，判断数据是否为空，
                baseResp.dataState = if (baseResp.data == null) {
                    if (kClass == JoyJsonModel.JoyEmpty::class) {
                        // 空类型
                        DataState.STATE_SUCCESS
                    } else {
                        DataState.STATE_EMPTY
                    }
                } else if (baseResp.data is List<*> && (baseResp.data as List<*>).isEmpty()) {
                    // 列表为空
                    DataState.STATE_EMPTY
                } else {
                    DataState.STATE_SUCCESS
                }
            } else {
                //服务器请求错误
                baseResp.dataState = DataState.STATE_FAILED
            }
        } catch (e: Exception) {
            //非后台返回错误，捕获到的异常
            baseResp.dataState = DataState.STATE_ERROR
            baseResp.msg = e.message
        } finally {
            stateLiveData.postValue(baseResp)
        }
    }
}