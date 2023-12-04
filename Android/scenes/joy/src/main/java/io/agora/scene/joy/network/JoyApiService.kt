package io.agora.scene.joy.network

import retrofit2.http.*

interface JoyApiService {
    @POST("/toolbox/v1/cloud-bullet-game/games")
    suspend fun getGames(
        @Body entity: JoyGameEntity
    ): JoyApiResult<JoyGameResult>

    @POST("/toolbox/v1/cloud-bullet-game/games/game")
    suspend fun gamesDetails(
        @Body entity: JoyGameEntity
    ): JoyApiResult<JoyGameResult>

    @POST("/toolbox/v1/cloud-bullet-game/games/start")
    suspend fun startGame(
        @Body entity: JoyGameEntity
    ): JoyApiResult<JoyGameResult>

    @POST("/toolbox/v1/cloud-bullet-game/games/stop")
    suspend fun stopGame(
        @Body entity: JoyGameEntity
    ): JoyApiResult<JoyJsonModel.JoyEmpty>

    @GET("/toolbox/v1/cloud-bullet-game/games/status")
    suspend fun gameState(
        @Query("task_id") taskId: String
    ): JoyApiResult<JoyGameResult>

    @POST("/toolbox/v1/cloud-bullet-game/games/gift")
    suspend fun sendGift(
        @Path("app_id") appId: String,
        @Path("game_id") gameId: String,
        @Body entity: JoySendMessage
    ): JoyApiResult<JoyJsonModel.JoyEmpty>

    @POST("/toolbox/v1/cloud-bullet-game/games/comment")
    suspend fun gameComment(
        @Path("app_id") appId: String,
        @Path("game_id") gameId: String,
        @Body entity: JoySendMessage
    ): JoyApiResult<JoyJsonModel.JoyEmpty>

    @POST("/toolbox/v1/cloud-bullet-game/games/like")
    suspend fun gameLike(
        @Path("app_id") appId: String,
        @Path("game_id") gameId: String,
        @Body entity: JoySendMessage
    ): JoyApiResult<JoyJsonModel.JoyEmpty>

    @POST("/toolbox/v1/cloud-bullet-game/games/renew-token")
    suspend fun renewToken(
        @Path("app_id") appId: String,
        @Path("game_id") gameId: String,
        @Body entity: JoyGameEntity
    ): JoyApiResult<JoyJsonModel.JoyEmpty>
}