package io.agora.scene.base.api;

import java.util.List;

import io.agora.scene.base.api.base.BaseResponse;
import io.agora.scene.base.api.model.User;
import io.agora.scene.base.bean.CommonBean;
import io.agora.scene.base.bean.MemberMusicModel;
import io.agora.scene.base.bean.RoomListModel;
import io.agora.scene.base.data.model.AgoraRoom;
import io.agora.scene.base.data.model.BaseMusicModel;
import io.agora.scene.base.data.model.KTVBaseResponse;
import io.agora.scene.base.data.model.MusicModelBase;
import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

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

    @POST(UrlConstants.REQUEST_ROOM_LIST)
    Observable<BaseResponse<RoomListModel>> requestRoomList(
            @Body RequestBody requestBody
    );

    @POST(UrlConstants.REQUEST_CREATE_ROOM)
    Observable<BaseResponse<String>> requestCreateRoom(
            @Body RequestBody requestBody
    );

    @GET(UrlConstants.REQUEST_GET_ROOM_INFO)
    Observable<BaseResponse<AgoraRoom>> requestGetRoomInfo(
            @Query("roomNo") String roomNo, @Query("password") String password, @Query("userNo") String userNo
    );

    @GET(UrlConstants.REQUEST_OUT_ROOM)
    Observable<BaseResponse<String>> requestExitRoom(
            @Query("roomNo") String roomNo, @Query("userNo") String userNo
    );

    @GET(UrlConstants.REQUEST_CLOSE_ROOM)
    Observable<BaseResponse<String>> requestCloseRoom(
            @Query("roomNo") String roomNo, @Query("userNo") String userNo
    );

    @GET(UrlConstants.REQUEST_ROOM_RTM_TOKEN)
    Observable<BaseResponse<CommonBean>> requestRTMToken(
            @Query("userId") Long userId
    );

    @GET(UrlConstants.REQUEST_ROOM_HAVE_SEAT)
    Observable<BaseResponse<AgoraRoom>> requestRoomHaveSeatRoomInfo(
            @Query("roomNo") String roomNo, @Query("seat") int seat, @Query("userNo") String userNo
    );

    @GET(UrlConstants.REQUEST_ROOM_ROOM_CANCEL_CHORUS)
    Observable<BaseResponse<String>> requestRoomCancelChorus(
            @Query("roomNo") String roomNo, @Query("songNo") String songNo, @Query("userNo") String userNo
    );

    @POST(UrlConstants.REQUEST_ROOM_UPDATE_ROOM)
    Observable<BaseResponse<String>> requestRoomInfoEdit(
            @Body RequestBody requestBody
    );

    @POST(UrlConstants.REQUEST_USER_UPDATE)
    Observable<BaseResponse<User>> requestUserUpdate(
            @Body RequestBody requestBody
    );

    @GET(UrlConstants.REQUEST_ROOM_SONG_BEGIN)
    Observable<BaseResponse<String>> requestRoomSongBegin(
            @Query("sort") int sort,
            @Query("userNo") String userNo,
            @Query("songNo") String songNo,
            @Query("roomNo") String roomNo
    );

    @GET(UrlConstants.REQUEST_ROOM_SONG_OVER)
    Observable<BaseResponse<String>> requestRoomSongOver(
            @Query("seat") int seat,
            @Query("userNo") String userNo,
            @Query("songNo") String songNo,
            @Query("roomNo") String roomNo
    );

    @GET(UrlConstants.REQUEST_ROOM_LEAVE_SEAT)
    Observable<BaseResponse<AgoraRoom>> requestRoomLeaveSeatRoomInfo(
            @Query("roomNo") String roomNo, @Query("userNo") String userNo
    );

    @GET(UrlConstants.REQUEST_GET_SONGS_LIST)
    Observable<BaseResponse<BaseMusicModel>> requestGetSongsList(
            @Query("current") int current, @Query("type") int type
    );

    @POST(UrlConstants.REQUEST_GET_SONGS_LIST_POST)
    Observable<BaseResponse<BaseMusicModel>> requestSearchSong(
            @Body RequestBody requestBody
//            @Query("name") String name, @Query("current") int current, @Query("size") int size
    );

    @GET(UrlConstants.REQUEST_GET_HAVE_ORDERED_LIST)
    Observable<BaseResponse<List<MemberMusicModel>>> requestGetSongsOrderedList(
            @Query("roomNo") String roomNo
    );

    /**
     * @param imageUrl 图片
     * @param isChorus 是否合唱 0 不合唱 1合唱
     * @param score    唱完打分
     * @param singer   作者
     * @param songName
     * @param songNo
     * @param songUrl
     * @param userNo
     * @param roomNo
     */
    @GET(UrlConstants.REQUEST_GET_CHOOSE_SONG)
    Observable<BaseResponse<String>> requestChooseSong(
            @Query("imageUrl") String imageUrl,
            @Query("isChorus") int isChorus,
            @Query("score") int score,
            @Query("singer") String singer,
            @Query("songName") String songName,
            @Query("songNo") String songNo,
            @Query("songUrl") String songUrl,
            @Query("userNo") String userNo,
            @Query("roomNo") String roomNo
    );

    /**
     * @param songNo
     * @param userNo
     * @param roomNo
     */
    @GET(UrlConstants.REQUEST_GET_DELETE_SONG)
    Observable<BaseResponse<String>> requestDeleteSong(
            @Query("sort") int sort,
            @Query("songNo") String songNo,
            @Query("userNo") String userNo,
            @Query("roomNo") String roomNo
    );

    /**
     * @param songNo
     * @param userNo
     * @param roomNo
     */
    @GET(UrlConstants.REQUEST_GET_TO_TOP_SONG)
    Observable<BaseResponse<String>> requestTopSong(
            @Query("sort") int sort,
            @Query("songNo") String songNo,
            @Query("userNo") String userNo,
            @Query("roomNo") String roomNo
    );

    /**
     * @param songNo
     * @param roomNo
     */
    @GET(UrlConstants.REQUEST_GET_SWITCH_SONG)
    Observable<BaseResponse<String>> requestSwitchSong(
            @Query("userNo") String userNo,
            @Query("songNo") String songNo,
            @Query("roomNo") String roomNo
    );

    @GET(UrlConstants.REQUEST_GET_JOIN_CHORUS)
    Observable<BaseResponse<String>> requestJoinChorus(
            @Query("songNo") String songNo,
            @Query("userNo") String userNo,
            @Query("roomNo") String roomNo
    );

    @GET(UrlConstants.REQUEST_USER_CHANGE_IF_QUIET)
    Observable<BaseResponse<String>> toggleMic(
            @Query("setStatus") int status,
            @Query("userNo") String userNo,
            @Query("roomNo") String roomNo
    );

    @GET(UrlConstants.REQUEST_USER_CHANGE_OPEN_CAMERA)
    Observable<BaseResponse<String>> requestOpenCamera(
            @Query("setStatus") int status,
            @Query("userNo") String userNo,
            @Query("roomNo") String roomNo
    );

    @GET(UrlConstants.REQUEST_GET_SONGS_SONG_HOT)
    Observable<BaseResponse<String>> requestGetSongsRankList(
            @Query("hotType") int hotType
    );

    @GET(UrlConstants.REQUEST_GET_SONG_ON_LINE)
    Observable<KTVBaseResponse<MusicModelBase>> requestSongsDetail(
            @Query("songCode") String songCode, @Query("lyricType") String lyricType
    );

    /**
     * 下载文件
     */
    @Streaming
    @GET
    Observable<ResponseBody> downloadOtaFileLoad(@Url String fileUrl);
}
