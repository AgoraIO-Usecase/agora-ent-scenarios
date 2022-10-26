//
//  VLURLPathConfig.h
//  VoiceOnLine
//

#ifndef VLURLPathConfig_h
#define VLURLPathConfig_h

//通知的字符串
static NSString * const kExitRoomNotification = @"exitRoomNotification";
static NSString * const kDianGeSuccessNotification = @"DianGeSuccessNotification";
static NSString * const kMakeTopNotification = @"MakeTopNotification";
static NSString * const kDeleteSuccessNotification = @"DeleteSuccessNotification";
static NSString * const kUpdateSelSongArrayNotification = @"kUpdateSelSongArrayNotification";

#pragma mark - API
static NSString * const kURLPathUploadImage = @"/api-login/upload"; //上传图片
static NSString * const kURLPathDestroyUser = @"/api-login/users/cancellation"; //注销用户
static NSString * const kURLPathGetUserInfo = @"/api-login/users/getUserInfo"; //获取用户信息
static NSString * const kURLPathLogin = @"/api-login/users/login"; // 登录
static NSString * const kURLPathLogout = @"/api-login/users/logout"; // 退出登录 （接口文档未完成）
static NSString * const kURLPathUploadUserInfo = @"/api-login/users/update";  //修改用户信息
static NSString * const kURLPathVerifyCode = @"/api-login/users/verificationCode"; //发送验证码
static NSString * const kURLCreateRoom = @"/api-room/roomInfo/createRoom";
static NSString * const kURLGetRoolList = @"/api-room/roomInfo/roomList";
static NSString * const kURLGetSongsList = @"/api-room/songs/getListPage"; //获取歌曲列表
static NSString * const kURLChoosedSongs = @"/api-room/roomSong/haveOrderedList"; //已点歌曲列表
static NSString * const kURLSongDetail = @"/api-room/songs/getSongOnline";  //歌曲详情
static NSString * const kURLChooseSong = @"/api-room/roomSong/chooseSong";  //点歌
static NSString * const kURLGetInRoom = @"/api-room/roomInfo/getRoomInfo";  //进入房间
static NSString * const kURLRoomOnSeat = @"/api-room/roomInfo/onSeat";      //上麦
static NSString * const kURLRoomDropSeat = @"/api-room/roomInfo/outSeat";   //下麦
static NSString * const kURLRoomClose = @"/api-room/roomInfo/closeRoom";    //关闭房间
static NSString * const kURLRoomOut = @"/api-room/roomInfo/outRoom";        //退出房间
static NSString * const kURLRoomMakeSongTop = @"/api-room/roomSong/toDevelop"; //置顶歌曲
static NSString * const kURLDeleteSong = @"/api-room/roomSong/delSong";   //删除歌曲
static NSString * const kURLBeginSinging = @"/api-room/roomSong/begin";   //开始唱歌
static NSString * const kURLIfSetMute = @"/api-room/roomUsers/ifQuiet";   //是否静音
static NSString * const kURLIfOpenVido = @"/api-room/roomUsers/openCamera"; //是否开启摄像头
static NSString * const kURLUpdataRoom = @"/api-room/roomInfo/updateRoom";   //更新房间信息
static NSString * const kURLGetRTMToken = @"/api-room/users/getToken"; //获取RTM Token
static NSString * const kURLRoomJoinChorus = @"/api-room/roomSong/chorus"; // 发送加入合唱
static NSString * const kURLRoomSwitchSong = @"/api-room/roomSong/switchSong"; //切歌

#pragma mark - H5相关
static NSString * const kURLPathH5UserAgreement = @"https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/meta/demo/fulldemoStatic/privacy/service.html";
static NSString * const kURLPathH5Privacy = @"https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/meta/demo/fulldemoStatic/privacy/privacy.html";
static NSString * const kURLPathH5AboutUS = @"https://www.agora.io/cn/about-us/";

#endif /* VLURLPathConfig_h */
