//
//  VLNoBodyOnLineView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@protocol VLSRMVIdleViewDelegate <NSObject>

@optional


@end

@interface VLSRMVIdleView : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLSRMVIdleViewDelegate>)delegate;

@end

NS_ASSUME_NONNULL_END
