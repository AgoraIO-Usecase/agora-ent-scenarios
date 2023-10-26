//
//  VLCreateRoomViewController.h
//  VoiceOnLine
//

#import "BaseViewController.h"
#import "AgoraEntScenarios-Swift.h"

NS_ASSUME_NONNULL_BEGIN

typedef void(^CreateRoomBlock)(CGFloat);
typedef void(^CreateRoomVCBlock)(UIViewController *);
@interface VLCreateRoomViewController : BaseViewController
@property (nonatomic, copy) CreateRoomBlock createRoomBlock;
@property (nonatomic, copy) CreateRoomVCBlock createRoomVCBlock;
@end

NS_ASSUME_NONNULL_END
