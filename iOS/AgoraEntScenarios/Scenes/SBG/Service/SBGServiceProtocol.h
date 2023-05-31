//
//  SBGServiceProtocol.h
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/10/19.
//

#import <Foundation/Foundation.h>
#import "SBGServiceModel.h"
#import "VLSBGRoomListModel.h"
#import "VLSBGRoomSelSongModel.h"
#import "VLLoginModel.h"
@import AgoraRtmKit;

typedef enum : NSUInteger {
    SBGServiceNetworkStatusConnecting = 0,
    SBGServiceNetworkStatusOpen,
    SBGServiceNetworkStatusFail,
    SBGServiceNetworkStatusClosed,
} SBGServiceNetworkStatus;


typedef enum : NSUInteger {
    SBGSubscribeCreated,      //创建
    SBGSubscribeDeleted,      //删除
    SBGSubscribeUpdated,      //更新
    SBGSubscribeFaild,      //failed
} SBGSubscribe;

NS_ASSUME_NONNULL_BEGIN

@protocol SBGServiceProtocol <NSObject>


/// room info

/// 获取房间列表
/// @param page <#page description#>
/// @param completion <#completion description#>
- (void)getRoomListWithPage:(NSUInteger)page
                 completion:(void(^)(NSError* _Nullable, NSArray<VLSBGRoomListModel*>* _Nullable))completion;

/// 创建房间
/// @param inputModel <#inputModel description#>
/// @param completion <#completion description#>
- (void)createRoomWithInput:(SBGCreateRoomInputModel*)inputModel
                 completion:(void (^)(NSError*_Nullable, SBGCreateRoomOutputModel*_Nullable))completion;

/// 加入房间
/// @param inputModel <#inputModel description#>
/// @param completion <#completion description#>
- (void)joinRoomWithInput:(SBGJoinRoomInputModel*)inputModel
               completion:(void (^)(NSError* _Nullable, SBGJoinRoomOutputModel*_Nullable))completion;

/// 离开房间
/// @param completion <#completion description#>
- (void)leaveRoomWithCompletion:(void(^)(NSError* _Nullable))completion;




// mic seat

/// 上麦
/// @param inputModel <#inputModel description#>
/// @param completion <#completion description#>
- (void)enterSeatWithInput:(SBGOnSeatInputModel*)inputModel
                completion:(void(^)(NSError* _Nullable))completion;

/// 下麦
/// @param inputModel <#inputModel description#>
/// @param completion <#completion description#>
- (void)leaveSeatWithInput:(SBGOutSeatInputModel*)inputModel
                completion:(void(^)(NSError* _Nullable))completion;

/// 设置麦位声音
/// @param muted YES: 关闭声音 NO: 开启声音
/// @param completion <#completion description#>
- (void)updateSeatAudioMuteStatusWithMuted:(BOOL)muted
                                completion:(void(^)(NSError* _Nullable))completion;

/// 打开麦位摄像头
/// @param muted YES: 关闭摄像头 NO: 开启摄像头
/// @param completion <#completion description#>
- (void)updateSeatVideoMuteStatusWithMuted:(BOOL)muted
                                completion:(void(^)(NSError* _Nullable))completion;






//choose songs

/// 删除选中歌曲
/// @param inputModel <#inputModel description#>
/// @param completion <#completion description#>
- (void)removeSongWithInput:(SBGRemoveSongInputModel*)inputModel
                 completion:(void(^)(NSError* _Nullable))completion;

/// 获取选择歌曲列表
/// @param completion <#completion description#>
- (void)getChoosedSongsListWithCompletion:(void(^)(NSError* _Nullable, NSArray<VLSBGRoomSelSongModel*>* _Nullable))completion;


/// 主唱告诉后台当前播放的歌曲
/// @param inputModel <#inputModel description#>
/// @param completion <#completion description#>
- (void)markSongDidPlayWithInput: (VLSBGRoomSelSongModel*)inputModel
                      completion:(void(^)(NSError* _Nullable))completion;

/// 点歌
/// @param inputModel <#inputModel description#>
/// @param completion <#completion description#>
- (void)chooseSongWithInput:(SBGChooseSongInputModel*)inputModel
                 completion:(void(^)(NSError* _Nullable))completion;

/// 置顶歌曲
/// @param inputModel <#inputModel description#>
/// @param completion <#completion description#>
- (void)pinSongWithInput:(SBGMakeSongTopInputModel*)inputModel
              completion:(void(^)(NSError* _Nullable))completion;


//lyrics

/// 加入合唱
/// @param inputModel <#inputModel description#>
/// @param completion <#completion description#>
- (void)joinChorusWithInput:(SBGJoinChorusInputModel*)inputModel
                 completion:(void(^)(NSError* _Nullable))completion;


/// 伴唱取消合唱
/// @param completion <#completion description#>
- (void)coSingerLeaveChorusWithCompletion:(void(^)(NSError* _Nullable))completion;

/// 当前歌曲合唱改为独唱
- (void)enterSoloMode;

/// 切换MV封面
/// @param inputModel <#inputModel description#>
/// @param completion <#completion description#>
- (void)changeMVCoverWithParams:(SBGChangeMVCoverInputModel*)inputModel
                completion:(void(^)(NSError* _Nullable))completion;

/// 更新得分
/// @param score <#totalVolume description#>
//- (void)updateSingingScoreWithScore:(double)score;


//subscribe

/// 订阅用户变化
/// @param changedBlock <#changedBlock description#>
- (void)subscribeUserListCountChangedWithBlock:(void(^)(NSUInteger))changedBlock;

/// 用户属性变化
/// @param changedBlock <#changedBlock description#>
- (void)subscribeUserChangedWithBlock:(void(^)(NSUInteger, VLLoginModel*))changedBlock;

/// 订阅麦位变化
/// @param changedBlock <#changedBlock description#>
- (void)subscribeSeatListChangedWithBlock:(void (^)(NSUInteger, VLSBGRoomSeatModel*))changedBlock;

/// 订阅房间状态变化
/// @param changedBlock <#changedBlock description#>
- (void)subscribeRoomStatusChangedWithBlock:(void (^)(NSUInteger, VLSBGRoomListModel*))changedBlock;

/// 订阅选中歌曲变化
/// @param changedBlock <#changedBlock description#>
- (void)subscribeChooseSongChangedWithBlock:(void (^)(NSUInteger, VLSBGRoomSelSongModel*, NSArray<VLSBGRoomSelSongModel*>*))changedBlock;

/// 订阅歌曲评分变化
/// @param changedBlock <#changedBlock description#>
//- (void)subscribeSingingScoreChangedWithBlock:(void(^)(double))changedBlock;


/// 订阅网络状态变化
/// @param changedBlock <#changedBlock description#>
- (void)subscribeNetworkStatusChangedWithBlock:(void(^)(SBGServiceNetworkStatus))changedBlock;



/// 订阅房间过期
/// @param changedBlock <#changedBlock description#>
- (void)subscribeRoomWillExpire:(void(^)(void))changedBlock;

/// 取消全部订阅
- (void)unsubscribeAll;

//查询抢唱状态
-(void)innerGetSingBattleGameInfo:(void (^)( NSError* _Nullable , SingBattleGameModel* __nullable))completion;

//新增抢唱状态
-(void)innerAddSingBattleGameInfo:(SingBattleGameModel*) model
                       completion:(void (^)(NSError* _Nullable))completion;

//修改抢唱状态
-(void)innerUpdateSingBattleGameInfo:(SingBattleGameModel*) model
                          completion:(void (^)(NSError* _Nullable))completion;

//订阅抢唱状态
- (void)innerSubscribeSingBattleGameInfoWithCompletion:(void(^)(SBGSubscribe status, SingBattleGameModel* _Nullable, NSError* _Nullable))completion;

- (void)updateChooseSongWithSongInfo:(VLSBGRoomSelSongModel *)songInfo
                           finished:(void (^)(NSError * _Nullable))finished;

@end

NS_ASSUME_NONNULL_END
