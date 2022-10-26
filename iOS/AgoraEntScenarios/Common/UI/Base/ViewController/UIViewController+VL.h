//
//  UIViewController+VL.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface UIViewController (VL)

+ (void)popGestureClose:(UIViewController *)VC;
+ (void)popGestureOpen:(UIViewController *)VC;

@end

NS_ASSUME_NONNULL_END
