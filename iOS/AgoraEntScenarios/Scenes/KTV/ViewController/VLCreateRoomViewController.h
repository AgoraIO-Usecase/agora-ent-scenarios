//
//  VLCreateRoomViewController.h
//  VoiceOnLine
//

#import "VLBaseViewController.h"
#import "AgoraEntScenarios-Swift.h"

NS_ASSUME_NONNULL_BEGIN

typedef void(^CreateRoomBlock)(CGFloat);
typedef void(^CreateRoomVCBlock)(UIViewController *);
@interface VLCreateRoomViewController : UIViewController
@property (nonatomic, copy) CreateRoomBlock createRoomBlock;
@property (nonatomic, copy) CreateRoomVCBlock createRoomVCBlock;

@end

NS_ASSUME_NONNULL_END
