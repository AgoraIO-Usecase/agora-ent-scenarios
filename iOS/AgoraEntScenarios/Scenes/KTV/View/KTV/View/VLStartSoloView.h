//
//  VLSoloSongView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@protocol VLStartSoloViewDelegate <NSObject>

@optional
- (void)onStartSoloBtn;
@end

@interface VLStartSoloView : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLStartSoloViewDelegate>)delegate;

@property (nonatomic, strong) UILabel *musicLabel;
@property (nonatomic, strong) UILabel *countDownLabel;

@end

NS_ASSUME_NONNULL_END
