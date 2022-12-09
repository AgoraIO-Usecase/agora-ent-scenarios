//
//  KTVServiceProtocol.h
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/10/19.
//

#import <Foundation/Foundation.h>
#import "KTVServiceModel.h"
#import "VLRoomListModel.h"
@import AgoraRtmKit;

typedef enum : NSUInteger {
    KTVServiceNetworkStatusConnecting = 0,
    KTVServiceNetworkStatusOpen,
    KTVServiceNetworkStatusFail,
    KTVServiceNetworkStatusClosed,
} KTVServiceNetworkStatus;


typedef enum : NSUInteger {
    KTVSubscribeCreated,      //创建
    KTVSubscribeDeleted,      //删除
    KTVSubscribeUpdated,      //更新
} KTVSubscribe;

NS_ASSUME_NONNULL_BEGIN

@protocol KTVServiceProtocol <NSObject>


/// room info

/// 获取房间列表
/// @param page <#page description#>
/// @param completion <#completion description#>
- (void)getRoomListWithPage:(NSUInteger)page
                 completion:(void(^)(NSError* _Nullable, NSArray<VLRoomListModel*>* _Nullable))completion;

/// 创建房间
/// @param inputModel <#inputModel description#>
/// @param completion <#completion description#>
- (void)createRoomWithInput:(KTVCreateRoomInputModel*)inputModel
                 completion:(void (^)(NSError*_Nullable, KTVCreateRoomOutputModel*_Nullable))completion;

/// 加入房间
/// @param inputModel <#inputModel description#>
/// @param completion <#completion description#>
- (void)joinRoomWithInput:(KTVJoinRoomInputModel*)inputModel
               completion:(void (^)(NSError* _Nullable, KTVJoinRoomOutputModel*_Nullable))completion;

/// 离开房间
/// @param completion <#completion description#>
- (void)leaveRoomWithCompletion:(void(^)(NSError* _Nullable))completion;




// mic seat

/// 上麦
/// @param inputModel <#inputModel description#>
/// @param completion <#completion description#>
- (void)enterSeatWithInput:(KTVOnSeatInputModel*)inputModel
                completion:(void(^)(NSError* _Nullable))completion;

/// 下麦
/// @param inputModel <#inputModel description#>
/// @param completion <#completion description#>
- (void)leaveSeatWithInput:(KTVOutSeatInputModel*)inputModel
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
- (void)removeSongWithInput:(KTVRemoveSongInputModel*)inputModel
                 completion:(void(^)(NSError* _Nullable))completion;

/// 获取选择歌曲列表
/// @param completion <#completion description#>
- (void)getChoosedSongsListWithCompletion:(void(^)(NSError* _Nullable, NSArray<VLRoomSelSongModel*>* _Nullable))completion;


/// 主唱告诉后台当前播放的歌曲
/// @param inputModel <#inputModel description#>
/// @param completion <#completion description#>
- (void)markSongDidPlayWithInput: (VLRoomSelSongModel*)inputModel
                      completion:(void(^)(NSError* _Nullable))completion;

/// 点歌
/// @param inputModel <#inputModel description#>
/// @param completion <#completion description#>
- (void)chooseSongWithInput:(KTVChooseSongInputModel*)inputModel
                 completion:(void(^)(NSError* _Nullable))completion;

/// 置顶歌曲
/// @param inputModel <#inputModel description#>
/// @param completion <#completion description#>
- (void)pinSongWithInput:(KTVMakeSongTopInputModel*)inputModel
              completion:(void(^)(NSError* _Nullable))completion;


//lyrics

/// 加入合唱
/// @param inputModel <#inputModel description#>
/// @param completion <#completion description#>
- (void)joinChorusWithInput:(KTVJoinChorusInputModel*)inputModel
                 completion:(void(^)(NSError* _Nullable))completion;

/// 当前歌曲合唱改为独唱
- (void)enterSoloMode;

/// 切换MV封面
/// @param inputModel <#inputModel description#>
/// @param completion <#completion description#>
- (void)changeMVCoverWithParams:(KTVChangeMVCoverInputModel*)inputModel
                completion:(void(^)(NSError* _Nullable))completion;

/// 更新得分
/// @param score <#totalVolume description#>
//- (void)updateSingingScoreWithScore:(double)score;


//subscribe

/// 订阅用户变化
/// @param changedBlock <#changedBlock description#>
- (void)subscribeUserListCountChangedWithBlock:(void(^)(NSUInteger))changedBlock;

/// 订阅麦位变化
/// @param changedBlock <#changedBlock description#>
- (void)subscribeSeatListChangedWithBlock:(void (^)(NSUInteger, VLRoomSeatModel*))changedBlock;

/// 订阅房间状态变化
/// @param changedBlock <#changedBlock description#>
- (void)subscribeRoomStatusChangedWithBlock:(void (^)(NSUInteger, VLRoomListModel*))changedBlock;

/// 订阅选中歌曲变化
/// @param changedBlock <#changedBlock description#>
- (void)subscribeChooseSongChangedWithBlock:(void (^)(NSUInteger, VLRoomSelSongModel*))changedBlock;

/// 订阅歌曲评分变化
/// @param changedBlock <#changedBlock description#>
//- (void)subscribeSingingScoreChangedWithBlock:(void(^)(double))changedBlock;


/// 订阅网络状态变化
/// @param changedBlock <#changedBlock description#>
- (void)subscribeNetworkStatusChangedWithBlock:(void(^)(KTVServiceNetworkStatus))changedBlock;



/// 订阅房间过期
/// @param changedBlock <#changedBlock description#>
- (void)subscribeRoomWillExpire:(void(^)(void))changedBlock;

/// 取消全部订阅
- (void)unsubscribeAll;
@end

NS_ASSUME_NONNULL_END
