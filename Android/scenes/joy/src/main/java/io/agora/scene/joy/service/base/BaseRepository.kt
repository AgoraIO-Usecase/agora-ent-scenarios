package io.agora.scene.joy.service.base

import kotlin.reflect.KClass

open class BaseRepository {

    companion object {
        private const val TAG = "BaseRepository"
    }

    /**
     * Common method for repository data requests,
     * Sets the value of baseResp.dataState in different states, and finally notifies the UI of the dataState status
     * @param block API request method
     * @param stateLiveData Each request passes in the corresponding LiveData, mainly responsible for network status monitoring
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
            // Start requesting data
            val invoke = block.invoke()
            // Copy the result to baseResp
            baseResp = invoke
            if (baseResp.code == 0) {
                // Request successful, check if data is empty
                baseResp.dataState = if (baseResp.data == null) {
                    if (kClass == JoyJsonModel.JoyEmpty::class) {
                        // Empty type
                        DataState.STATE_SUCCESS
                    } else {
                        DataState.STATE_EMPTY
                    }
                } else if (baseResp.data is List<*> && (baseResp.data as List<*>).isEmpty()) {
                    // List is empty
                    DataState.STATE_EMPTY
                } else {
                    DataState.STATE_SUCCESS
                }
            } else {
                // Server request error
                baseResp.dataState = DataState.STATE_FAILED
            }
        } catch (e: Exception) {
            // Non-backend error, caught exception
            baseResp.dataState = DataState.STATE_ERROR
            baseResp.msg = e.message
        } finally {
            stateLiveData.postValue(baseResp)
        }
    }
}