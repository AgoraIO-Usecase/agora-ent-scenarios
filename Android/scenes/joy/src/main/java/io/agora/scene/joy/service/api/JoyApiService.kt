package io.agora.scene.joy.service.api

import io.agora.scene.joy.service.base.JoyApiResult
import io.agora.scene.joy.service.base.JoyJsonModel
import retrofit2.http.*

interface JoyApiService {
    companion object {
        const val TAG = "JoyApiService"
    }

    @POST("/toolbox/v2/cloud-bullet-game/games")
    suspend fun getGames(
        @Body entity: JoyGameEntity
    ): JoyApiResult<JoyGameResult>

    @POST("/toolbox/v1/cloud-bullet-game/games/game")
    suspend fun gameDetails(
        @Body entity: JoyGameEntity
    ): JoyApiResult<JoyGameDetailResult>

    @POST("/toolbox/v1/cloud-bullet-game/games/start")
    suspend fun startGame(
        @Body entity: JoyGameEntity
    ): JoyApiResult<JoyGameResult>

    @POST("/toolbox/v1/cloud-bullet-game/games/stop")
    suspend fun stopGame(
        @Body entity: JoyGameEntity
    ): JoyApiResult<JoyJsonModel.JoyEmpty>

    @POST("/toolbox/v1/cloud-bullet-game/games/status")
    suspend fun gameState(
        @Body entity: JoyGameEntity
    ): JoyApiResult<JoyGameResult>

    @POST("/toolbox/v1/cloud-bullet-game/games/gift")
    suspend fun sendGift(
        @Body entity: JoySendMessage
    ): JoyApiResult<JoyJsonModel.JoyEmpty>

    @POST("/toolbox/v1/cloud-bullet-game/games/comment")
    suspend fun sendComment(
        @Body entity: JoySendMessage
    ): JoyApiResult<JoyJsonModel.JoyEmpty>

    @POST("/toolbox/v1/cloud-bullet-game/games/like")
    suspend fun sendLike(
        @Body entity: JoySendMessage
    ): JoyApiResult<JoyJsonModel.JoyEmpty>

    @POST("/toolbox/v1/cloud-bullet-game/games/renew-token")
    suspend fun renewToken(
        @Body entity: JoyGameEntity
    ): JoyApiResult<JoyJsonModel.JoyEmpty>

    @GET("/toolbox/v1/configs/{scenario}")
    suspend fun gameConfig(
        @Path("scenario") scenario: String
    ): JoyApiResult<JoyGameResult>
}

//{"code":0,"data"data:{"carousel":[{"index":1,"url":""},{"index":2,"url":""},{"index":3,"url":""}]},"msg":"success", "tip":"this is demo api, don't use in production!"}