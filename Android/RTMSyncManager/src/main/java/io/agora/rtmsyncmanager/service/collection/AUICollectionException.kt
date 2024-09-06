package io.agora.rtmsyncmanager.service.collection

fun fromValue(value: Int): AUICollectionException.ErrorCode? {
    return enumValues<AUICollectionException.ErrorCode>().firstOrNull { it.value == value }
}

class AUICollectionException private constructor(val code: Int, override val message: String) :
    Exception() {

    enum class ErrorCode(val value: Int, val message: String) {
        rtm(0, "rtm error"),
        unknown(100, "unknown error"),
        updateTypeNotFound(101, "update type not found"),
        removeMetaDataFail(102, "remove metaData fail"),
        invalidPayloadType(103, "invalid payload type"),
        filterNotFound(104, "filter result not found"),
        encodeToJsonStringFail(105, "encode to json string fail"),
        calculateMapFail(106, "calculate map fail"),
        recvErrorReceipt(107, "receipt error"),
        unsupportedAction(108, "action unsupported"),
        calculateMapOutOfRange(111, "calculate map out of range"),
        filterFoundSame(112, "filter result found the same value");

        fun toException(code: Int? = null, msg: String? = null): AUICollectionException {
            return AUICollectionException(code ?: value, msg ?: message)
        }
    }
}