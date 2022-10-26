//
//  VLPopScoreView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@protocol VLPopScoreViewDelegate <NSObject>

@optional

- (void)popScoreViewDidClickConfirm;

@end

@interface VLPopScoreView : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLPopScoreViewDelegate>)delegate;

- (void)configScore:(int)score;

- (void)dismiss;


@end

NS_ASSUME_NONNULL_END
