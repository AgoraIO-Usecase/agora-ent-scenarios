//
//  VLSoloSongView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@protocol VLSoloSongViewDelegate <NSObject>

@optional
- (void)soloBtnClickAction;
@end

@interface VLSoloSongView : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLSoloSongViewDelegate>)delegate;

@property (nonatomic, strong) UILabel *musicLabel;
@property (nonatomic, strong) UILabel *countDownLabel;

@end

NS_ASSUME_NONNULL_END
