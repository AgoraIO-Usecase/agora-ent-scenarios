//
//  UIView+VL.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface UIView (VL)

- (void)vl_whenTouches:(NSUInteger)numberOfTouches tapped:(NSUInteger)numberOfTaps handler:(void (^)(void))block;

- (void)vl_whenTapped:(void (^)(void))block;

- (void)vl_whenDoubleTapped:(void (^)(void))block;

- (void)vl_eachSubview:(void (^)(UIView *subview))block;

/**
 圆角
 使用自动布局，需要在layoutsubviews 中使用
 @param radius 圆角尺寸
 @param corner 圆角位置
 */
- (void)vl_radius:(CGFloat)radius corner:(UIRectCorner)corner;


@end

NS_ASSUME_NONNULL_END
