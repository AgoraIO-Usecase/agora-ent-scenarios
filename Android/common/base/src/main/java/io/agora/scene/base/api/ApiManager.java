package io.agora.scene.base.api;

import android.text.TextUtils;
import android.util.ArrayMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.agora.scene.base.BuildConfig;
import io.agora.scene.base.api.apiutils.GsonUtils;
import io.agora.scene.base.api.base.BaseResponse;
import io.agora.scene.base.api.common.NetConstants;
import io.agora.scene.base.api.model.User;
import io.agora.scene.base.bean.CommonBean;
import io.agora.scene.base.bean.MemberMusicModel;
import io.agora.scene.base.bean.PageModel;
import io.agora.scene.base.bean.RoomListModel;
import io.agora.scene.base.data.model.AgoraRoom;
import io.agora.scene.base.data.model.BaseMusicModel;
import io.agora.scene.base.data.model.KTVBaseResponse;
import io.agora.scene.base.data.model.MusicModelBase;
import io.agora.scene.base.manager.UserManager;
import io.reactivex.Observable;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

public class ApiManager {
    private Gson mGson;
    private final static long TIMEOUT = 5;
    private OkHttpClient httpClient;
    private ApiManagerService apiManagerService;
    public static String token;

    private ApiManager() {
        if (mGson == null) {
            mGson = new GsonBuilder().serializeNulls()
                    .disableHtmlEscaping()
                    .registerTypeAdapter(String.class, new GsonUtils.StringConverter()).create();
        }
        httpClient = new OkHttpClient.Builder().addInterceptor(chain -> {
                    Request.Builder builder = chain.request().newBuilder();
                    builder.addHeader(NetConstants.HEADER_APP_OS, "android");
                    builder.addHeader(NetConstants.HEADER_VERSION_NAME, BuildConfig.APP_VERSION_NAME);
                    builder.addHeader(NetConstants.HEADER_VERSION_CODE, String.valueOf(BuildConfig.APP_VERSION_CODE));
                    if (!TextUtils.isEmpty(token)) {
                        builder.addHeader(NetConstants.AUTHORIZATION, token);
                    } else {
                        if (UserManager.getInstance().getUser() != null) {
                            token = UserManager.getInstance().getUser().token;
                        }
                        if (!TextUtils.isEmpty(token)) {
                            builder.addHeader(NetConstants.AUTHORIZATION, token);
                        }
                    }
                    return chain.proceed(builder.build());
                }).addInterceptor(new LoggerInterceptor(null, true))
//                .addInterceptor(LoggerInterceptor(null, true))
                /*
            这里可以添加一个HttpLoggingInterceptor，因为Retrofit封装好了从Http请求到解析，
            出了bug很难找出来问题，添加HttpLoggingInterceptor拦截器方便调试接口
             */
//                .addInterceptor(HttpLoggingInterceptor()
//                        .setLevel(HttpLoggingInterceptor.Level.BODY))

                .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
                .build();


        Retrofit sRetrofit = new Retrofit.Builder()
                .baseUrl(UrlConstants.BASE_URL)
                .addConverterFactory(ResponseConverterFactory.Companion.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(httpClient)
                .build();
        apiManagerService = sRetrofit.create(ApiManagerService.class);
    }

    private static class SingletonHolder {
        private static final ApiManager INSTANCE = new ApiManager();
    }

    public static ApiManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public Observable<BaseResponse<String>> requestSendVerCode(String phone) {
        return apiManagerService.requestSendVerCode(phone).flatMap(it -> Observable.just(it));
    }

    /**
     * 登录
     *
     * @param phone 手机号
     * @param vCode 验证码
     */
    public Observable<BaseResponse<User>> requestLogin(String phone, String vCode) {
        return apiManagerService.requestLogin(phone, vCode).flatMap(it -> Observable.just(it));
    }

    /**
     * 获取用户信息
     *
     * @param userNo 用户id
     */
    public Observable<BaseResponse<User>> requestUserInfo(String userNo) {
        return apiManagerService.requestUserInfo(userNo).flatMap(it -> Observable.just(it));
    }

    /**
     *
     */
    public Observable<BaseResponse<CommonBean>> requestUploadPhoto(File file) {
        RequestBody fileBody = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part partFile = MultipartBody.Part.createFormData("file", file.getName(), fileBody);
        return apiManagerService.requestUploadPhoto(partFile).flatMap(it -> Observable.just(it));
    }

    /**
     * 注销用户
     *
     * @param userNo 用户id
     */
    public Observable<BaseResponse<String>> requestCancellationUser(String userNo) {
        return apiManagerService.requestCancellationUser(userNo).flatMap(it -> Observable.just(it));
    }

    /**
     * 创建房间
     *
     * @param isPrivate 是否是私密 1 是 0 否
     * @param name      房间名称
     * @param password  房间密码
     * @param userNo    用户id
     */
    public Observable<BaseResponse<String>> requestCreateRoom(int isPrivate, String name,
                                                              String password, String userNo,
                                                              String icon) {
        ArrayMap<String, String> params = new ArrayMap();
        params.put("isPrivate", String.valueOf(isPrivate));
        params.put("name", name);
        params.put("password", password);
        params.put("userNo", userNo);
        params.put("belCanto", "0");
        params.put("icon", icon);
        params.put("soundEffect", "0");
        return apiManagerService.requestCreateRoom(getRequestBody(params)).flatMap(it -> Observable.just(it));
    }

    /**
     * 上麦
     */
    public Observable<BaseResponse<AgoraRoom>> requestRoomHaveSeatRoomInfo(String roomNo, int seat, String userNo) {
        return apiManagerService.requestRoomHaveSeatRoomInfo(roomNo, seat, userNo).flatMap(it -> Observable.just(it));
    }

    /**
     * 下麦
     */
    public Observable<BaseResponse<AgoraRoom>> requestRoomLeaveSeatRoomInfo(String roomNo, String userNo) {
        return apiManagerService.requestRoomLeaveSeatRoomInfo(roomNo, userNo).flatMap(it -> Observable.just(it));
    }

    /**
     * 房间列表
     */
    public Observable<BaseResponse<RoomListModel>> requestRoomList(int current, int size) {
        ArrayMap<String, String> params = new ArrayMap();
        params.put("current", String.valueOf(current));
        params.put("size", String.valueOf(size));
        return apiManagerService.requestRoomList(getRequestBody(params)).flatMap(it -> Observable.just(it));
    }

    /**
     * 获取房间详情
     */
    public Observable<BaseResponse<AgoraRoom>> requestGetRoomInfo(String roomNo, String password) {
        return apiManagerService.requestGetRoomInfo(roomNo, password, UserManager.getInstance().getUser().userNo).flatMap(it -> Observable.just(it));
    }

    /**
     * 退出房间
     */
    public Observable<BaseResponse<String>> requestExitRoom(String roomNo) {
        return apiManagerService.requestExitRoom(roomNo, UserManager.getInstance().getUser().userNo).flatMap(it -> Observable.just(it));
    }

    /**
     * 关闭房间
     */
    public Observable<BaseResponse<String>> requestCloseRoom(String roomNo) {
        return apiManagerService.requestCloseRoom(roomNo, UserManager.getInstance().getUser().userNo).flatMap(it -> Observable.just(it));
    }

    /**
     * 关闭房间
     */
    public Observable<BaseResponse<CommonBean>> requestRTMToken(Long userId) {
        return apiManagerService.requestRTMToken(userId).flatMap(it -> Observable.just(it));
    }

    /**
     * 获取歌曲列表
     */
    public Observable<BaseResponse<BaseMusicModel>> requestGetSongsList(int current, int type) {
        return apiManagerService.requestGetSongsList(current, type).flatMap(it -> Observable.just(it));
    }

    /**
     * 搜索
     */
    public Observable<BaseResponse<BaseMusicModel>> requestSearchSong(PageModel pageModel) {
        return apiManagerService.requestSearchSong(RequestBody.create(
                        MediaType.parse("application/json;charset=UTF-8"),
                        GsonUtils.Companion.getGson().toJson(pageModel))).
                flatMap(it -> Observable.just(it));
    }

    /**
     * 已点
     */
    public Observable<BaseResponse<List<MemberMusicModel>>> requestGetSongsOrderedList(String roomNo) {
        return apiManagerService.requestGetSongsOrderedList(roomNo).flatMap(it -> Observable.just(it));
    }

    /**
     * 点歌
     */
    public Observable<BaseResponse<String>> requestChooseSong(String imageUrl,
                                                              int isChorus,
                                                              int score,
                                                              String singer,
                                                              String songName,
                                                              String songNo,
                                                              String songUrl,
                                                              String userNo,
                                                              String roomNo) {
        return apiManagerService.requestChooseSong(imageUrl, isChorus, score, singer, songName, songNo, songUrl, userNo, roomNo).flatMap(it -> Observable.just(it));
    }

    /**
     * 删歌
     */
    public Observable<BaseResponse<String>> requestDeleteSong(
            int sort,
            String songNo,
            String userNo,
            String roomNo) {
        return apiManagerService.requestDeleteSong(sort, songNo, userNo, roomNo).flatMap(it -> Observable.just(it));
    }

    /**
     * 顶置
     */
    public Observable<BaseResponse<String>> requestTopSong(
            int sort,
            String songNo,
            String userNo,
            String roomNo) {
        return apiManagerService.requestTopSong(sort, songNo, userNo, roomNo).flatMap(it -> Observable.just(it));
    }

    /**
     * 切歌
     */
    public Observable<BaseResponse<String>> requestSwitchSong(
            String userNo,
            String songNo,
            String roomNo) {
        return apiManagerService.requestSwitchSong(userNo, songNo, roomNo).flatMap(it -> Observable.just(it));
    }

    /**
     * 加入合唱
     */
    public Observable<BaseResponse<String>> requestJoinChorus(
            String songNo, String userNo,
            String roomNo) {
        return apiManagerService.requestJoinChorus(songNo, userNo, roomNo).flatMap(it -> Observable.just(it));
    }

    /**
     * 静音
     */
    public Observable<BaseResponse<String>> requestToggleMic(int status,
                                                             String userNo,
                                                             String roomNo) {
        return apiManagerService.toggleMic(status, userNo, roomNo).flatMap(it -> Observable.just(it));
    }

    /**
     * 修改
     */
    public Observable<BaseResponse<String>> requestRoomInfoEdit(
            String roomNo,
            String belCanto,
            String bgOption,
            String soundEffect) {
        ArrayMap<String, String> params = new ArrayMap();
        params.put("roomNo", roomNo);
        params.put("userNo", UserManager.getInstance().getUser().userNo);
        params.put("belCanto", belCanto);
        params.put("bgOption", bgOption);
        params.put("soundEffect", soundEffect);
        return apiManagerService.requestRoomInfoEdit(getRequestBody(params)).flatMap(it -> Observable.just(it));
    }

    /**
     * 修改
     */
    public Observable<BaseResponse<User>> requestUserUpdate(
            String headUrl,
            String name,
            String sex,
            String userNo) {
        ArrayMap<String, String> params = new ArrayMap();
        if (!TextUtils.isEmpty(headUrl)) {
            params.put("headUrl", headUrl);
        }
        if (!TextUtils.isEmpty(name)) {
            params.put("name", name);
        }
        if (!TextUtils.isEmpty(sex)) {
            params.put("sex", sex);
        }
        if (!TextUtils.isEmpty(userNo)) {
            params.put("userNo", userNo);
        }
        return apiManagerService.requestUserUpdate(getRequestBody(params)).flatMap(it -> Observable.just(it));
    }

    /**
     * 打开 关闭 摄像头
     */
    public Observable<BaseResponse<String>> requestOpenCamera(int status,
                                                              String userNo,
                                                              String roomNo) {
        return apiManagerService.requestOpenCamera(status, userNo, roomNo).flatMap(it -> Observable.just(it));
    }

    /**
     * 取消合唱
     */
    public Observable<BaseResponse<String>> requestRoomCancelChorus(
            String userNo,
            String songNo,
            String roomNo) {
        return apiManagerService.requestRoomCancelChorus(roomNo, songNo, userNo).flatMap(it -> Observable.just(it));
    }

    /**
     * 开唱
     */
    public Observable<BaseResponse<String>> requestRoomSongBegin(
            int sort,
            String userNo,
            String songNo,
            String roomNo) {
        return apiManagerService.requestRoomSongBegin(sort, userNo, songNo, roomNo).flatMap(it -> Observable.just(it));
    }

    /**
     * 唱完
     */
    public Observable<BaseResponse<String>> requestRoomSongOver(
            int sort,
            String userNo,
            String songNo,
            String roomNo) {
        return apiManagerService.requestRoomSongOver(sort, userNo, songNo, roomNo).flatMap(it -> Observable.just(it));
    }

    /**
     * 获取歌曲排行
     */
    public Observable<BaseResponse<String>> requestGetSongsRankList(int hotType) {
        return apiManagerService.requestGetSongsRankList(hotType).flatMap(it -> Observable.just(it));
    }

    /**
     * 获取歌曲详情
     */
    public Observable<KTVBaseResponse<MusicModelBase>> requestSongsDetail(String songCode) {
        return apiManagerService.requestSongsDetail(songCode, "0").flatMap(it -> Observable.just(it));
    }

    private RequestBody getRequestBody(ArrayMap<String, String> params) {
        return RequestBody.create(
                MediaType.parse("application/json;charset=UTF-8"),
                GsonUtils.Companion.getGson().toJson(params)
        );
    }
}
