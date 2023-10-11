//
//  SBGServiceModel.h
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/10/19.
//

#import <Foundation/Foundation.h>
#import "VLSBGRoomSeatModel.h"
#import "VLSBGRoomSelSongModel.h"

NS_ASSUME_NONNULL_BEGIN

/// create room input parameter
@interface SBGCreateRoomInputModel : NSObject
@property (nonatomic, copy) NSString* belCanto;
@property (nonatomic, copy) NSString* icon;
@property (nonatomic, copy) NSNumber* isPrivate;
@property (nonatomic, copy) NSString* name;
@property (nonatomic, copy) NSString* password;
@property (nonatomic, copy) NSString* soundEffect;
//@property (nonatomic, copy) NSString* userNo;
@end

/// Create room output parameter
@interface SBGCreateRoomOutputModel : NSObject
@property (nonatomic, copy) NSString* name;
@property (nonatomic, copy) NSString* roomNo;
@property (nonatomic, strong) NSArray<VLSBGRoomSeatModel*>* seatsArray;
@end

/// Join room input parameter
@interface SBGJoinRoomInputModel : NSObject
@property (nonatomic, copy) NSString* roomNo;
//@property (nonatomic, copy) NSString* userNo;
@property (nonatomic, copy) NSString* password;
@end

/// Join room output parameter
@interface SBGJoinRoomOutputModel : NSObject
@property (nonatomic, copy) NSString* creatorNo;
@property (nonatomic, strong) NSArray<VLSBGRoomSeatModel*>* seatsArray;
@end


/// change mv cover input parameter
@interface SBGChangeMVCoverInputModel : NSObject
//@property (nonatomic, copy) NSString* roomNo;
@property (nonatomic, assign) NSUInteger mvIndex;
//@property (nonatomic, copy) NSString* userNo;
@end

/// change mv cover output parameter
//@interface SBGChangeMVCoverOutputModel : NSObject
//@property (nonatomic, copy) NSString* name;
//@property (nonatomic, copy) NSString* roomNo;
//@property (nonatomic, strong) NSArray<VLRoomSeatModel*>* seatsArray;
//@end

/// on seat input parameter
@interface SBGOnSeatInputModel : NSObject
@property (nonatomic, assign) NSUInteger seatIndex;
@end

/// out seat input parameter
@interface SBGOutSeatInputModel : NSObject
@property (nonatomic, copy) NSString* userNo;
@property (nonatomic, copy) NSString* userId;
@property (nonatomic, copy) NSString* userName;
@property (nonatomic, copy) NSString* userHeadUrl;
@property (nonatomic, assign) NSInteger seatIndex;
@end

/// remove song input parameter
@interface SBGRemoveSongInputModel : NSObject
@property (nonatomic, copy) NSString* songNo;
@property (nonatomic, copy, nullable) NSString* objectId;
@end

/// join chorus input parameter
@interface SBGJoinChorusInputModel : NSObject
@property (nonatomic, assign) BOOL isChorus;
@property (nonatomic, copy) NSString* songNo;
@end

@interface SBGChooseSongInputModel : NSObject
@property (nonatomic, assign) BOOL isChorus;
@property (nonatomic, copy) NSString* songName;
@property (nonatomic, copy) NSString* songNo;
//@property (nonatomic, copy) NSString* songUrl;
@property (nonatomic, copy) NSString* singer;
@property (nonatomic, copy) NSString* imageUrl;
@end

@interface SBGMakeSongTopInputModel : NSObject
@property (nonatomic, copy) NSString* songNo;
@property (nonatomic, assign) NSString* sort;
@property (nonatomic, copy) NSString* objectId;
@end

typedef NS_ENUM(NSInteger, SingBattleGameStatus) {
    SingBattleGameStatusIdle = 0,
    SingBattleGameStatusWaiting = 1,// 等待中
    SingBattleGameStatusStarted = 2, // 已开始
    SingBattleGameStatusEnded = 3 // 已结束
};

@interface SingBattleGameModel : NSObject
@property (nonatomic, assign) SingBattleGameStatus status;
/// for sync manager
@property (nonatomic, copy, nullable) NSString* objectId;
@property (nonatomic, strong) NSString *name;
@property (nonatomic, copy) NSDictionary *rank;
@end

@interface RankModel : NSObject
@property (nonatomic, copy) NSString *userName;
@property (nonatomic, assign) NSInteger songNum;
@property (nonatomic, assign) NSInteger score;
@property (nonatomic, copy) NSString *poster;
@end

@interface SubRankModel : RankModel
@property (nonatomic, copy) NSString *userId;
@property (nonatomic, assign) NSInteger index;
@property (nonatomic, assign) NSInteger count;
@end

NS_ASSUME_NONNULL_END
