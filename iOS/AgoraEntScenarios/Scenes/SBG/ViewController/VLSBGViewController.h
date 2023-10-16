//
//  VLSBGViewController.h
//  VoiceOnLine
//

#import "BaseViewController.h"
#import "SBGServiceProtocol.h"

NS_ASSUME_NONNULL_BEGIN
@class VLSBGRoomListModel;
@class VLSBGRoomSeatModel;

@interface VLSBGViewController : BaseViewController

@property (nonatomic, strong) VLSBGRoomListModel *roomModel;
//麦位数组
@property (nonatomic, strong) NSArray <VLSBGRoomSeatModel *> *seatsArray;

@end

NS_ASSUME_NONNULL_END
