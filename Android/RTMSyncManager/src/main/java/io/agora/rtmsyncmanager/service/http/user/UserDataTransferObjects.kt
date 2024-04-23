package io.agora.auikit.service.http.user



data class CreateUserReq(
    val userName: String
)

data class CreateUserRsp(
    val userName: String,
    val accessToken: String,
    val userUuid : String,
    val appKey:String
)

data class KickUserReq(
    val operatorId:String,//操作者id 要求为房主
    val roomId : String,
    val uid:Int, //被踢用户id
)

data class KickUserRsp(
    val uid:Int
)