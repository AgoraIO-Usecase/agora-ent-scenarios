//
//  VLNoBodyOnLineView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@protocol VLKTVMVIdleViewDelegate <NSObject>

@optional


@end

@interface VLKTVMVIdleView : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLKTVMVIdleViewDelegate>)delegate;

@end

NS_ASSUME_NONNULL_END
