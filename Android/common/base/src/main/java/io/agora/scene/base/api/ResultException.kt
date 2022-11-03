package io.agora.scene.base.api

internal class ResultException constructor(
    errCode: Int,
    msg: String
) : Exception(errCode.toString() + msg) {
    var errCode: Int? = errCode
}
