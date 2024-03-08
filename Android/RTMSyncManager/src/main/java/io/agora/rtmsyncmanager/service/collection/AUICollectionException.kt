package io.agora.rtmsyncmanager.service.collection


class AUICollectionException private constructor(val code: Int, override val message: String) :
    Exception() {


    enum class ErrorCode(val value: Int, val message: String) {
        unknown(100, "unknown error"),      //未知错误
        updateTypeNotFound(101, "update type not found"),
        removeMetaDataFail(102, "remove metaData fail"),
        invalidPayloadType(103, "invalid payload type"),
        filterNotFound(104, "filter result not found"),
        encodeToJsonStringFail(105, "encode to json string fail"),
        calculateMapFail(106, "calculate map fail"),
        recvErrorReceipt(107, "receipt error"),
        unsupportedAction(108, "action unsupported");

        fun toException(msg: String? = null): AUICollectionException {
            return AUICollectionException(value, msg ?: message)
        }
    }
}