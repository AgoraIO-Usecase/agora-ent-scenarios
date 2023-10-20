//
//  VLKTVSettingView.h
//  VoiceOnLine
//

#import "VLBaseView.h"
@class VLKTVSettingModel;

typedef enum : NSUInteger {
    DHCVLKTVValueDidChangedTypeEar = 0,      // 耳返
    DHCVLKTVValueDidChangedTypeMV,           // MV
    DHCVLKTVValueDidChangedRiseFall,         // 升降调
    DHCVLKTVValueDidChangedTypeSound,        // 声音
    DHCVLKTVValueDidChangedTypeAcc,          // 伴奏
    DHCVLKTVValueDidChangedTypeRemoteValue,  //远端音量
    DHCVLKTVValueDidChangedTypeListItem,      // 列表
    DHCVLKTVValueDidChangedTypeIMMode
} DHCVLKTVValueDidChangedType;

@protocol DHCVLKTVSettingViewDelegate <NSObject>

- (void)settingViewSettingChanged:(VLKTVSettingModel *)setting valueDidChangedType:(DHCVLKTVValueDidChangedType)type;
- (void)settingViewEffectChoosed:(NSInteger)effectIndex;
@end

@interface DHCVLKTVSettingView : VLBaseView

- (instancetype)initWithSetting:(VLKTVSettingModel *)setting;

@property (nonatomic, weak) id <DHCVLKTVSettingViewDelegate> delegate;

- (void)setIsEarOn:(BOOL)isEarOn;
- (void)setAccValue:(float)accValue;
-(void)setIspause:(BOOL)isPause;
-(void)setSelectEffect:(NSInteger)index;
-(void)setIMMode:(BOOL)flag;
@end

@interface VLKTVSettingModel : NSObject

#define kKindUnSelectedIdentifier 10000

/// 耳返
@property (nonatomic, assign) BOOL soundOn;
@property (nonatomic, assign) BOOL mvOn;
@property (nonatomic, assign) float soundValue;
@property (nonatomic, assign) float accValue;
@property (nonatomic, assign) float remoteValue;
@property (nonatomic, assign) NSInteger toneValue;
@property (nonatomic, assign) int remoteVolume;
@property (nonatomic, assign) NSInteger selectEffect;
@property (nonatomic, assign) int imMode;

/// list选项
@property (nonatomic, assign) NSInteger kindIndex;

- (void)setDefaultProperties;

@end
