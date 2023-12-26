//
//  VLKTVViewController.h
//  VoiceOnLine
//

#import "VLBaseViewController.h"

NS_ASSUME_NONNULL_BEGIN
@class VLRoomListModel;
@class VLRoomSeatModel;

@interface VLKTVViewController : VLBaseViewController

@property (nonatomic, strong) VLRoomListModel *roomModel;
//麦位数组
@property (nonatomic, strong) NSArray <VLRoomSeatModel *> *seatsArray;

@end

NS_ASSUME_NONNULL_END
