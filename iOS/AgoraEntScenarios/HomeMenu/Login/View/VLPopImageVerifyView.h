//
//  VLPopImageVerifyView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@protocol VLPopImageVerifyViewDelegate <NSObject>

@optional
- (void)closeBtnAction;

- (void)slideSuccessAction;

@end

@interface VLPopImageVerifyView : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLPopImageVerifyViewDelegate>)delegate;

@end

NS_ASSUME_NONNULL_END
