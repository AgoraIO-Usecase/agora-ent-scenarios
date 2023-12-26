//
//  VLSRSettingView.h
//  VoiceOnLine
//

#import "VLBaseView.h"
@class VLSRSettingModel;

typedef enum : NSUInteger {
    VLSRValueDidChangedTypeEar = 0,      // 耳返
    VLSRValueDidChangedTypeMV,           // MV
    VLSRValueDidChangedRiseFall,         // 升降调
    VLSRValueDidChangedTypeSound,        // 声音
    VLSRValueDidChangedTypeAcc,          // 伴奏
    VLSRValueDidChangedTypeRemoteValue,  //远端音量
    VLSRValueDidChangedTypeListItem      // 列表
} VLSRValueDidChangedType;

@protocol VLSRSettingViewDelegate <NSObject>

- (void)settingViewSettingChanged:(VLSRSettingModel *)setting valueDidChangedType:(VLSRValueDidChangedType)type;

@end

@interface VLSRSettingView : VLBaseView

- (instancetype)initWithSetting:(VLSRSettingModel *)setting;

@property (nonatomic, weak) id <VLSRSettingViewDelegate> delegate;

- (void)setIsEarOn:(BOOL)isEarOn;
- (void)setAccValue:(float)accValue;
-(void)setIspause:(BOOL)isPause;
@end

@interface VLSRSettingModel : NSObject

#define kKindUnSelectedIdentifier 10000

/// 耳返
@property (nonatomic, assign) BOOL soundOn;
@property (nonatomic, assign) BOOL mvOn;
@property (nonatomic, assign) float soundValue;
@property (nonatomic, assign) float accValue;
@property (nonatomic, assign) NSInteger toneValue;
@property (nonatomic, assign) int remoteVolume;


/// list选项
@property (nonatomic, assign) NSInteger kindIndex;

- (void)setDefaultProperties;

@end
