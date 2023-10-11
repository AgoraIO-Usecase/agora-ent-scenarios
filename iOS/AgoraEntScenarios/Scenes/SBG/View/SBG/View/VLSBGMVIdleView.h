//
//  VLNoBodyOnLineView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@protocol VLSBGMVIdleViewDelegate <NSObject>

@optional


@end

@interface VLSBGMVIdleView : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLSBGMVIdleViewDelegate>)delegate;

@end

NS_ASSUME_NONNULL_END
