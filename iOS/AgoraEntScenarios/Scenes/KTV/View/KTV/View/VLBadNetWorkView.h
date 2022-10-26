//
//  VLBadNetWorkView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN
@protocol VLBadNetWorkViewDelegate <NSObject>

@optional
- (void)knowBtnClickAction;

@end

@interface VLBadNetWorkView : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLBadNetWorkViewDelegate>)delegate;

@end

NS_ASSUME_NONNULL_END
