//
//  VLBadNetWorkView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN
@class VLSBGBadNetWorkView;
@protocol VLSBGBadNetWorkViewDelegate <NSObject>
@optional
- (void)onVLBadNetworkView:(VLSBGBadNetWorkView*)view dismiss:(id)sender;

@end

@interface VLSBGBadNetWorkView : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLSBGBadNetWorkViewDelegate>)delegate;

@end

NS_ASSUME_NONNULL_END
