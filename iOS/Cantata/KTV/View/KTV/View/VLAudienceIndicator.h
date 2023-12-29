//
//  VLTouristOnLineView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN
@protocol VLAudienceIndicatorDelegate <NSObject>

@optional
- (void)requestOnlineAction;

@end

@interface VLAudienceIndicator : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLAudienceIndicatorDelegate>)delegate;

@end

NS_ASSUME_NONNULL_END
