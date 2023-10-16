//
//  VLPopScoreView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@protocol VLSBGPopScoreViewDelegate <NSObject>

@optional

- (void)popScoreViewDidClickConfirm;

@end

@interface VLSBGPopScoreView : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLSBGPopScoreViewDelegate>)delegate;

- (void)configScore:(int)score;

- (void)dismiss;


@end

NS_ASSUME_NONNULL_END
