//
//  VLPopScoreView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@protocol VLSRPopScoreViewDelegate <NSObject>

@optional

- (void)popScoreViewDidClickConfirm;

@end

@interface VLSRPopScoreView : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLSRPopScoreViewDelegate>)delegate;

- (void)configScore:(int)score;

- (void)dismiss;


@end

NS_ASSUME_NONNULL_END
