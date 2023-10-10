//
//  VLTouristOnLineView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN
@protocol VLSBGAudienceIndicatorDelegate <NSObject>

@optional
- (void)requestOnlineAction;

@end

@interface VLSBGAudienceIndicator : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLSBGAudienceIndicatorDelegate>)delegate;
-(void)setTipHidden:(BOOL)isHidden;
@end

NS_ASSUME_NONNULL_END
