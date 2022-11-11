//
//  VLBadNetWorkView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN
@class VLBadNetWorkView;
@protocol VLBadNetWorkViewDelegate <NSObject>

- (void)onVLBadNetworkView:(VLBadNetWorkView*)view dismiss:(id)sender;

@end

@interface VLBadNetWorkView : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLBadNetWorkViewDelegate>)delegate;

@end

NS_ASSUME_NONNULL_END
