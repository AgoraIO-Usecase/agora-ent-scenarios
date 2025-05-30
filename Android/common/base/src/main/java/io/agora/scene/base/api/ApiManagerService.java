package io.agora.scene.base.api;

import io.agora.scene.base.api.base.BaseResponse;
import io.agora.scene.base.api.model.User;
import io.agora.scene.base.bean.CommonBean;
import io.agora.scene.base.bean.FeedbackUploadResBean;
import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface ApiManagerService {

    @GET("/api-login/users/verificationCode")
    Observable<BaseResponse<String>> requestSendVerCode(
            @Query("phone") String phone
    );

    @GET("/api-login/users/login")
    Observable<BaseResponse<User>> requestLogin(
            @Query("phone") String phone, @Query("code") String code
    );

    @GET("/api-login/users/getUserInfo")
    Observable<BaseResponse<User>> requestUserInfo(@Query("userNo") String userNo);

    @Multipart
    @POST("/api-login/upload")
    Observable<BaseResponse<CommonBean>> requestUploadPhoto(@Part MultipartBody.Part body);

    @GET("/api-login/users/cancellation")
    Observable<BaseResponse<String>> requestCancellationUser(@Query("userNo") String userNo);

    @POST("/api-login/users/update")
    Observable<BaseResponse<User>> requestUserUpdate(
            @Body RequestBody requestBody
    );

    @POST("/api-login/report/device")
    Observable<BaseResponse<String>> requestReportDevice(@Query("userNo") String userNo,
                                                         @Query("sceneId") String sceneId,
                                                         @Query("appId") String appId,
                                                         @Query("projectId") String projectId,
                                                         @Body RequestBody requestBody);

    @POST("/api-login/report/action")
    Observable<BaseResponse<String>> requestReportAction(@Query("userNo") String userNo,
                                                     @Query("sceneId") String sceneId,
                                                     @Query("appId") String appId,
                                                     @Query("projectId") String projectId,
                                                     @Body RequestBody requestBody);

    @Multipart
    @POST("/api-login/upload/log")
    Observable<BaseResponse<CommonBean>> requestUploadLog(@Part MultipartBody.Part body);

    @POST("/api-login/feedback/upload")
    Observable<BaseResponse<FeedbackUploadResBean>> requestFeedbackUpload(@Body RequestBody requestBody);

    @POST("/api-login/users/realNameAuth")
    Observable<BaseResponse<Void>> requestRealNameAuth(@Body RequestBody requestBody);
}
