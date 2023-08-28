//
//  VLBaseView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface VLBaseView : UIView

@property (nonatomic, strong) UIView *containerView;
- (void)initSubViews;
- (void)addSubViewConstraints;

@end

NS_ASSUME_NONNULL_END
