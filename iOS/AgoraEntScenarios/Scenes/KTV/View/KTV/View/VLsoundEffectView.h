//
//  VLsoundEffectView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

typedef enum : NSUInteger {
    VLKTVSoundEffectTypeHeFeng = 0, // 和风
    VLKTVSoundEffectTypeXiaoDiao,   // 小调
    VLKTVSoundEffectTypeDaDiao,      // 大调
    VLKTVSoundEffectTypeNone
} VLKTVSoundEffectType;

NS_ASSUME_NONNULL_BEGIN
@class VLsoundEffectView;
@protocol VLsoundEffectViewDelegate <NSObject>

@optional
- (void)soundEffectViewBackBtnActionWithView:(VLsoundEffectView*)view;
- (void)soundEffectItemClickAction:(VLKTVSoundEffectType)effectType;

@end

@interface VLsoundEffectView : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLsoundEffectViewDelegate>)delegate;

@end

NS_ASSUME_NONNULL_END
