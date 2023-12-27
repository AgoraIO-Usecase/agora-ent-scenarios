package io.agora.scene.joy.base

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.agora.scene.base.api.apiutils.GsonUtils
import java.io.Serializable

interface JoyJsonModel : Serializable {

    class JoyEmpty : JoyJsonModel
}

data class JoyApiResult<T> constructor(
    @Expose
    @SerializedName("msg")
    var msg: String? = null,
    @Expose
    @SerializedName("code")
    var code: Int = 0,
    @Expose
    @SerializedName("data")
    var data: T? = null,
    @Expose
    @SerializedName("tip")
    var tip: String? = null,
    @Expose
    @SerializedName("errMsg")
    var errMsg: String? = null,

    var dataState: DataState? = null
) {
    val isSucceed: Boolean
        get() = code == 200 || code == 0

    val errorMessage: ErrorMessage?
        get() {
            try {
                return GsonUtils.gson.fromJson(errMsg, ErrorMessage::class.java)
            } catch (e: Exception) {
                return null
            }
        }
}

data class ErrorMessage constructor(
    @Expose
    @SerializedName("code")
    var code: Int? = null,
    @Expose
    @SerializedName("err")
    var err: String? = null,
    @Expose
    @SerializedName("err_msg")
    var errMsg: String? = null,
)

enum class DataState {
    STATE_CREATE,//创建
    STATE_LOADING,//加载中
    STATE_SUCCESS,//成功
    STATE_COMPLETED,//完成
    STATE_EMPTY,//数据为null
    STATE_FAILED,//接口请求成功但是服务器返回error
    STATE_ERROR,//请求失败
    STATE_UNKNOWN//未知
}