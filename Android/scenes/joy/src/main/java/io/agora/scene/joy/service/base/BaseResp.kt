package io.agora.scene.joy.service.base

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
    @SerializedName("err_no")
    var code: Int? = null,
    @Expose
    @SerializedName("logid")
    var logid: String? = null,
    @Expose
    @SerializedName("err_msg")
    var errMsg: String? = null,
)

enum class DataState {
    STATE_CREATE,    // Create
    STATE_LOADING,   // Loading
    STATE_SUCCESS,   // Success
    STATE_COMPLETED, // Completed
    STATE_EMPTY,     // Data is null
    STATE_FAILED,    // API request successful but server returned error
    STATE_ERROR,     // Request failed
    STATE_UNKNOWN    // Unknown
}