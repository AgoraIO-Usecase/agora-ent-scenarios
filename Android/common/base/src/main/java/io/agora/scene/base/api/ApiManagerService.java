package io.agora.scene.base.api;

import java.util.Map;

import io.agora.scene.base.api.base.BaseResponse;
import io.agora.scene.base.api.model.User;
import io.agora.scene.base.bean.CommonBean;
import io.agora.scene.base.bean.FeedbackUploadResBean;
import io.reactivex.Observable;
import io.reactivex.Single;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface ApiManagerService {

    @GET(UrlConstants.REQUEST_SEND_V_CODE)
    Observable<BaseResponse<String>> requestSendVerCode(
            @Query("phone") String phone
    );

    @GET(UrlConstants.REQUEST_LOGIN)
    Observable<BaseResponse<User>> requestLogin(
            @Query("phone") String phone, @Query("code") String code
    );

    @GET(UrlConstants.REQUEST_USER_INFO)
    Observable<BaseResponse<User>> requestUserInfo(@Query("userNo") String userNo);

    @Multipart
    @POST(UrlConstants.REQUEST_USER_UPLOAD_PHOTO)
    Observable<BaseResponse<CommonBean>> requestUploadPhoto(@Part MultipartBody.Part body);

    @GET(UrlConstants.REQUEST_USER_CANCELLATION)
    Observable<BaseResponse<String>> requestCancellationUser(@Query("userNo") String userNo);

    @POST(UrlConstants.REQUEST_USER_UPDATE)
    Observable<BaseResponse<User>> requestUserUpdate(
            @Body RequestBody requestBody
    );

    @POST(UrlConstants.REQUEST_REPORT_DEVICE)
    Observable<BaseResponse<String>> requestReportDevice(@Query("userNo") String userNo,
                                                         @Query("sceneId") String sceneId,
                                                         @Query("appId") String appId,
                                                         @Query("projectId") String projectId,
                                                         @Body RequestBody requestBody);

    @POST(UrlConstants.REQUEST_REPORT_ACTION)
    Observable<BaseResponse<String>> requestReportAction(@Query("userNo") String userNo,
                                                     @Query("sceneId") String sceneId,
                                                     @Query("appId") String appId,
                                                     @Query("projectId") String projectId,
                                                     @Body RequestBody requestBody);

    @Multipart
    @POST(UrlConstants.REQUEST_UPLOAD_LOG)
    Observable<BaseResponse<CommonBean>> requestUploadLog(@Part MultipartBody.Part body);

    @POST(UrlConstants.REQUEST_FEEDBACK_UPLOAD)
    Observable<BaseResponse<FeedbackUploadResBean>> requestFeedbackUpload(@Body RequestBody requestBody);
}
