//
//  KTVServiceModel.h
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/10/19.
//

#import <Foundation/Foundation.h>
#import "VLRoomSeatModel.h"
#import "VLRoomSelSongModel.h"

NS_ASSUME_NONNULL_BEGIN

/// create room input parameter
@interface KTVCreateRoomInputModel : NSObject
@property (nonatomic, copy) NSString* belCanto;
@property (nonatomic, copy) NSString* icon;
@property (nonatomic, copy) NSNumber* isPrivate;
@property (nonatomic, copy) NSString* name;
@property (nonatomic, copy) NSString* password;
@property (nonatomic, copy) NSString* soundEffect;
//@property (nonatomic, copy) NSString* userNo;
@end

/// Create room output parameter
@interface KTVCreateRoomOutputModel : NSObject
@property (nonatomic, copy) NSString* name;
@property (nonatomic, copy) NSString* roomNo;
@property (nonatomic, strong) NSArray<VLRoomSeatModel*>* seatsArray;
@end

/// Join room input parameter
@interface KTVJoinRoomInputModel : NSObject
@property (nonatomic, copy) NSString* roomNo;
//@property (nonatomic, copy) NSString* userNo;
@property (nonatomic, copy) NSString* password;
@end

/// Join room output parameter
@interface KTVJoinRoomOutputModel : NSObject
@property (nonatomic, copy) NSString* creator;
@property (nonatomic, strong) NSArray<VLRoomSeatModel*>* seatsArray;
@end


/// change mv cover input parameter
@interface KTVChangeMVCoverInputModel : NSObject
//@property (nonatomic, copy) NSString* roomNo;
@property (nonatomic, assign) NSUInteger mvIndex;
//@property (nonatomic, copy) NSString* userNo;
@end

/// change mv cover output parameter
//@interface KTVChangeMVCoverOutputModel : NSObject
//@property (nonatomic, copy) NSString* name;
//@property (nonatomic, copy) NSString* roomNo;
//@property (nonatomic, strong) NSArray<VLRoomSeatModel*>* seatsArray;
//@end

/// on seat input parameter
@interface KTVOnSeatInputModel : NSObject
@property (nonatomic, assign) NSUInteger seatIndex;
@end

/// out seat input parameter
@interface KTVOutSeatInputModel : NSObject
@property (nonatomic, copy) NSString* userNo;
@property (nonatomic, copy) NSString* userId;
@property (nonatomic, copy) NSString* userName;
@property (nonatomic, copy) NSString* userHeadUrl;
@property (nonatomic, assign) NSInteger userOnSeat;
@end

/// remove song input parameter
@interface KTVRemoveSongInputModel : NSObject
@property (nonatomic, copy) NSString* songNo;
@property (nonatomic, assign) NSString* sort;
@property (nonatomic, copy, nullable) NSString* objectId;
@end

/// join chorus input parameter
@interface KTVJoinChorusInputModel : NSObject
@property (nonatomic, copy) NSString* isChorus;
@property (nonatomic, copy) NSString* songNo;
@end

@interface KTVChooseSongInputModel : NSObject
@property (nonatomic, assign) BOOL isChorus;
@property (nonatomic, copy) NSString* songName;
@property (nonatomic, copy) NSString* songNo;
//@property (nonatomic, copy) NSString* songUrl;
@property (nonatomic, copy) NSString* singer;
@property (nonatomic, copy) NSString* imageUrl;
@end

@interface KTVMakeSongTopInputModel : NSObject
@property (nonatomic, copy) NSString* songNo;
@property (nonatomic, assign) NSString* sort;
@property (nonatomic, copy) NSString* objectId;
@end

NS_ASSUME_NONNULL_END
