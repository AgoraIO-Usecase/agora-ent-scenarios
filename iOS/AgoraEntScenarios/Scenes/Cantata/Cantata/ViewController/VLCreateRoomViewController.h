//
//  VLCreateRoomViewController.h
//  VoiceOnLine
//

#import "VLBaseViewController.h"

NS_ASSUME_NONNULL_BEGIN

typedef void(^DHCCreateRoomBlock)(CGFloat);
typedef void(^DHCCreateRoomVCBlock)(UIViewController *);
@interface VLCreateRoomViewController : VLBaseViewController
@property (nonatomic, copy) DHCCreateRoomBlock createRoomBlock;
@property (nonatomic, copy) DHCCreateRoomVCBlock createRoomVCBlock;
@end

NS_ASSUME_NONNULL_END
