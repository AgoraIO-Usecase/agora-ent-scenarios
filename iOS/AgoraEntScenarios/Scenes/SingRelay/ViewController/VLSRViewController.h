//
//  VLKTVViewController.h
//  VoiceOnLine
//

#import "BaseViewController.h"

NS_ASSUME_NONNULL_BEGIN
@class VLSRRoomListModel;
@class VLSRRoomSeatModel;

@interface VLSRViewController : BaseViewController

@property (nonatomic, strong) VLSRRoomListModel *roomModel;
//麦位数组
@property (nonatomic, strong) NSArray <VLSRRoomSeatModel *> *seatsArray;

@end

NS_ASSUME_NONNULL_END
