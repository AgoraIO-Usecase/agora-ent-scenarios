//
//  VLsoundEffectView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

typedef enum : NSUInteger {
    VLSBGSoundEffectTypeHeFeng = 0, // 和风
    VLSBGSoundEffectTypeXiaoDiao,   // 小调
    VLSBGSoundEffectTypeDaDiao,      // 大调
    VLSBGSoundEffectTypeNone
} VLSBGSoundEffectType;

NS_ASSUME_NONNULL_BEGIN
@class VLSBGSoundEffectView;
@protocol VLSBGsoundEffectViewDelegate <NSObject>

@optional
- (void)soundEffectViewBackBtnActionWithView:(VLSBGSoundEffectView*)view;
- (void)soundEffectItemClickAction:(VLSBGSoundEffectType)effectType;

@end

@interface VLSBGSoundEffectView : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLSBGsoundEffectViewDelegate>)delegate;

@end

NS_ASSUME_NONNULL_END
