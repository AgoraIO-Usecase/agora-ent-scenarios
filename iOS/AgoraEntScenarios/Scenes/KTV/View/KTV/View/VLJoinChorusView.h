//
//  VLRobMicrophoneView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN
@protocol VLJoinChorusViewDelegate <NSObject>

@optional

/// 合唱点击
- (void)onJoinChorusBtn;

@end

@interface VLJoinChorusView : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLJoinChorusViewDelegate>)delegate;

@property (nonatomic, copy) NSString* musicLabelText;
@property (nonatomic, strong) UILabel *countDownLabel;

@end

NS_ASSUME_NONNULL_END
