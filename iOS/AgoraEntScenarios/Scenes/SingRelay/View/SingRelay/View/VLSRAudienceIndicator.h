//
//  VLTouristOnLineView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN
@protocol VLSRAudienceIndicatorDelegate <NSObject>

@optional
- (void)requestOnlineAction;

@end

@interface VLSRAudienceIndicator : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLSRAudienceIndicatorDelegate>)delegate;

@end

NS_ASSUME_NONNULL_END
