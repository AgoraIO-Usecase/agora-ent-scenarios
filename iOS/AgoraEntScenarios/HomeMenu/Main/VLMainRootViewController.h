//
//  VLMainRootViewController.h
//  VoiceOnLine
//

#import "QMUIKit.h"
#import "CYLTabBarController.h"
NS_ASSUME_NONNULL_BEGIN

@interface VLMainRootViewController : QMUINavigationController

- (CYLTabBarController *)createNewTabBarWithContext:(NSString *__nullable)context;

@end

NS_ASSUME_NONNULL_END
