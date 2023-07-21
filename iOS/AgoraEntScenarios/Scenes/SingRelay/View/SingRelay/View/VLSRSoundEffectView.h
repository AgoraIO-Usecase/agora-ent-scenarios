//
//  VLsoundEffectView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

typedef enum : NSUInteger {
    VLSRSoundEffectTypeHeFeng = 0, // 和风
    VLSRSoundEffectTypeXiaoDiao,   // 小调
    VLSRSoundEffectTypeDaDiao,      // 大调
    VLSRSoundEffectTypeNone
} VLSRSoundEffectType;

NS_ASSUME_NONNULL_BEGIN
@class VLSRSoundEffectView;
@protocol VLSRSoundEffectViewDelegate <NSObject>

@optional
- (void)soundEffectViewBackBtnActionWithView:(VLSRSoundEffectView*)view;
- (void)soundEffectItemClickAction:(VLSRSoundEffectType)effectType;

@end

@interface VLSRSoundEffectView : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLSRSoundEffectViewDelegate>)delegate;

@end

NS_ASSUME_NONNULL_END
