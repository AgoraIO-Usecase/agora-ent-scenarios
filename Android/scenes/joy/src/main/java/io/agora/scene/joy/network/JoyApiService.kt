package io.agora.scene.joy.network

import retrofit2.http.*

interface JoyApiService {
    @GET("/v1/apps/{app_id}/cloud-bullet-game/games")
    suspend fun getGames(
        @Path("app_id") id: String
    ): JoyApiResult<JoyGameResult>

    @POST("/v1/apps/{app_id}/cloud-bullet-game/gameid/{game_id}/start")
    suspend fun startGame(
        @Path("app_id") appId: String,
        @Path("game_id") gameId: String,
        @Body entity: JoyGameEntity
    ): JoyApiResult<JoyGameResult>

    @POST("/v1/apps/{app_id}/cloud-bullet-game/gameid/{game_id}/stop")
    suspend fun stopGame(
        @Path("app_id") appId: String,
        @Path("game_id") gameId: String,
        @Body entity: JoyGameEntity?
    ): JoyApiResult<JoyJsonModel.JoyEmpty>

    @GET("/v1/apps/{app_id}/cloud-bullet-game/gameid/{game_id}/status")
    suspend fun gameState(
        @Path("app_id") appId: String,
        @Path("game_id") gameId: String,
        @Query("task_id") taskId: String
    ): JoyApiResult<JoyGameResult>

    @GET("/v1/apps/{app_id}/cloud-bullet-game/gameid/{game_id}")
    suspend fun gamesDetails(
        @Path("app_id") appId: String,
        @Path("game_id") gameId: String
    ): JoyApiResult<JoyGameResult>

    @POST("/v1/apps/{app_id}/cloud-bullet-game/gameid/{game_id}/gift")
    suspend fun sendGift(
        @Path("app_id") appId: String,
        @Path("game_id") gameId: String,
        @Body entity: JoySendMessage
    ): JoyApiResult<JoyJsonModel.JoyEmpty>

    @POST("/v1/apps/{app_id}/cloud-bullet-game/gameid/{game_id}/comment")
    suspend fun gameComment(
        @Path("app_id") appId: String,
        @Path("game_id") gameId: String,
        @Body entity: JoySendMessage
    ): JoyApiResult<JoyJsonModel.JoyEmpty>

    @POST("/v1/apps/{app_id}/cloud-bullet-game/gameid/{game_id}/like")
    suspend fun gameLike(
        @Path("app_id") appId: String,
        @Path("game_id") gameId: String,
        @Body entity: JoySendMessage
    ): JoyApiResult<JoyJsonModel.JoyEmpty>

    @POST("/v1/apps/{app_id}/cloud-bullet-game/gameid/{game_id}/renew-token")
    suspend fun renewToken(
        @Path("app_id") appId: String,
        @Path("game_id") gameId: String,
        @Body entity: JoyGameEntity
    ): JoyApiResult<JoyJsonModel.JoyEmpty>
}