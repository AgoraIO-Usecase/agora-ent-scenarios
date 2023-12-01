package io.agora.scene.joy.network

import retrofit2.http.*

interface JoyApiService {
    @POST("/toolbox/v1/cloud-bullet-game/games")
    suspend fun getGames(
        @Body entity: JoyGameEntity
    ): JoyApiResult<JoyGameResult>

    @POST("/v1/cloud-bullet-game/gameid/start")
    suspend fun startGame(
        @Body entity: JoyGameEntity
    ): JoyApiResult<JoyGameResult>

    @POST("/v1/cloud-bullet-game/gameid/stop")
    suspend fun stopGame(
        @Body entity: JoyGameEntity
    ): JoyApiResult<JoyJsonModel.JoyEmpty>

    @GET("/v1/cloud-bullet-game/gameid/status")
    suspend fun gameState(
        @Query("task_id") taskId: String
    ): JoyApiResult<JoyGameResult>

    @GET("/v1/cloud-bullet-game/gameid/{game_id}")
    suspend fun gamesDetails(
        @Path("app_id") appId: String,
        @Path("game_id") gameId: String
    ): JoyApiResult<JoyGameResult>

    @POST("/v1/cloud-bullet-game/gameid/gift")
    suspend fun sendGift(
        @Path("app_id") appId: String,
        @Path("game_id") gameId: String,
        @Body entity: JoySendMessage
    ): JoyApiResult<JoyJsonModel.JoyEmpty>

    @POST("/v1/cloud-bullet-game/gameid/comment")
    suspend fun gameComment(
        @Path("app_id") appId: String,
        @Path("game_id") gameId: String,
        @Body entity: JoySendMessage
    ): JoyApiResult<JoyJsonModel.JoyEmpty>

    @POST("/v1/cloud-bullet-game/gameid/like")
    suspend fun gameLike(
        @Path("app_id") appId: String,
        @Path("game_id") gameId: String,
        @Body entity: JoySendMessage
    ): JoyApiResult<JoyJsonModel.JoyEmpty>

    @POST("/v1/cloud-bullet-game/gameid/renew-token")
    suspend fun renewToken(
        @Path("app_id") appId: String,
        @Path("game_id") gameId: String,
        @Body entity: JoyGameEntity
    ): JoyApiResult<JoyJsonModel.JoyEmpty>
}