//
//  VLRobMicrophoneView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN
@protocol VLRobMicrophoneViewDelegate <NSObject>

@optional

/// 合唱点击
- (void)robViewChorusAction;

@end

@interface VLRobMicrophoneView : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLRobMicrophoneViewDelegate>)delegate;

@property (nonatomic, strong) UILabel *musicLabel;
@property (nonatomic, strong) UILabel *countDownLabel;

@end

NS_ASSUME_NONNULL_END
