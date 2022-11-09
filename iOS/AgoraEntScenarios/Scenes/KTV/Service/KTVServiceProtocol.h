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
    VLSendMessageTypeOnSeat = 0,         // 上麦
    VLSendMessageTypeDropSeat = 1,       // 下麦
    VLSendMessageTypeChooseSong = 2,     // 点歌
    VLSendMessageTypeChangeSong = 3,     // 切歌
    VLSendMessageTypeCloseRoom = 4,      // 关闭房间
    VLSendMessageTypeChangeMVBg = 5,     // 切换MV背景

    VLSendMessageTypeAudioMute= 9,       // 静音
    VLSendMessageTypeVideoIfOpen = 10,    // 摄像头
    VLSendMessageTypeTellSingerSomeBodyJoin = 11,     //通知主唱有人加入合唱
    VLSendMessageTypeTellJoinUID = 12, //通知合唱者 主唱UID
    VLSendMessageTypeSoloSong = 13,  //独唱
    VLSendMessageTypeSeeScore = 14,   //观众看到分数
    
    VLSendMessageAuditFail = 20,
} VLSendMessageType;


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
- (void)onSeatWithInput:(KTVOnSeatInputModel*)inputModel
             completion:(void(^)(NSError* _Nullable))completion;

/// 下麦
/// @param inputModel <#inputModel description#>
/// @param completion <#completion description#>
- (void)outSeatWithInput:(KTVOutSeatInputModel*)inputModel
              completion:(void(^)(NSError* _Nullable))completion;

/// 设置麦位声音
/// @param openStatus YES: 开启声音 NO: 关闭声音
/// @param completion <#completion description#>
- (void)openAudioStatusWithStatus:(BOOL)openStatus
                       completion:(void(^)(NSError* _Nullable))completion;

/// 打开麦位摄像头
/// @param openStatus YES: 开启摄像头 NO: 关闭摄像头
/// @param completion <#completion description#>
- (void)openVideoStatusWithStatus: (BOOL)openStatus
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
- (void)makeSongTopWithInput:(KTVMakeSongTopInputModel*)inputModel
                  completion:(void(^)(NSError* _Nullable))completion;




//lyrics

/// 加入合唱
/// @param inputModel <#inputModel description#>
/// @param completion <#completion description#>
- (void)joinChorusWithInput:(KTVJoinChorusInputModel*)inputModel
                 completion:(void(^)(NSError* _Nullable))completion;

/// 当前歌曲合唱改为独唱
- (void)becomeSolo;

/// 切换MV封面
/// @param inputModel <#inputModel description#>
/// @param completion <#completion description#>
- (void)changeMVCoverWithInput:(KTVChangeMVCoverInputModel*)inputModel
                completion:(void(^)(NSError* _Nullable))completion;

/// 更新得分
/// @param totalVolume <#totalVolume description#>
- (void)updateSingingScoreWithTotalVolume:(double)totalVolume;


//subscribe

/// 订阅用户变化
/// @param changedBlock <#changedBlock description#>
- (void)subscribeUserListCountWithChanged:(void(^)(NSUInteger))changedBlock;

/// 订阅麦位变化
/// @param changedBlock <#changedBlock description#>
- (void)subscribeSeatListWithChanged:(void (^)(NSUInteger, VLRoomSeatModel*))changedBlock;

/// 订阅房间状态变化
/// @param changedBlock <#changedBlock description#>
- (void)subscribeRoomStatusWithChanged:(void (^)(NSUInteger, VLRoomListModel*))changedBlock;

/// 订阅选中歌曲变化
/// @param changedBlock <#changedBlock description#>
- (void)subscribeChooseSongWithChanged:(void (^)(NSUInteger, VLRoomSelSongModel*))changedBlock;
@end

NS_ASSUME_NONNULL_END
