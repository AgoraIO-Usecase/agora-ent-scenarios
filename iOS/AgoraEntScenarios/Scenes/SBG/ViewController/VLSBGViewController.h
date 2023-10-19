//
//  VLSBGViewController.h
//  VoiceOnLine
//

#import "VLBaseViewController.h"
#import "SBGServiceProtocol.h"

NS_ASSUME_NONNULL_BEGIN
@class VLSBGRoomListModel;
@class VLSBGRoomSeatModel;

@interface VLSBGViewController : VLBaseViewController

@property (nonatomic, strong) VLSBGRoomListModel *roomModel;
//麦位数组
@property (nonatomic, strong) NSArray <VLSBGRoomSeatModel *> *seatsArray;

@end

NS_ASSUME_NONNULL_END
