//
//  VLBadNetWorkView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN
@class VLSRBadNetWorkView;
@protocol VLSRBadNetWorkViewDelegate <NSObject>
@optional
- (void)onVLBadNetworkView:(VLSRBadNetWorkView*)view dismiss:(id)sender;

@end

@interface VLSRBadNetWorkView : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLSRBadNetWorkViewDelegate>)delegate;

@end

NS_ASSUME_NONNULL_END
