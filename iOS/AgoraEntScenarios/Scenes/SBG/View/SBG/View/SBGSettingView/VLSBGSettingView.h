//
//  VLSBGSettingView.h
//  VoiceOnLine
//

#import "VLBaseView.h"
@class VLSBGSettingModel;

typedef enum : NSUInteger {
    VLSBGValueDidChangedTypeEar = 0,      // 耳返
    VLSBGValueDidChangedTypeMV,           // MV
    VLSBGValueDidChangedRiseFall,         // 升降调
    VLSBGValueDidChangedTypeSound,        // 声音
    VLSBGValueDidChangedTypeAcc,          // 伴奏
    VLSBGValueDidChangedTypeRemoteValue,  //远端音量
    VLSBGValueDidChangedTypeListItem      // 列表
} VLSBGValueDidChangedType;

@protocol VLSBGSettingViewDelegate <NSObject>

- (void)settingViewSettingChanged:(VLSBGSettingModel *)setting valueDidChangedType:(VLSBGValueDidChangedType)type;

@end

@interface VLSBGSettingView : VLBaseView

- (instancetype)initWithSetting:(VLSBGSettingModel *)setting;

@property (nonatomic, weak) id <VLSBGSettingViewDelegate> delegate;

- (void)setIsEarOn:(BOOL)isEarOn;
- (void)setAccValue:(float)accValue;
-(void)setIspause:(BOOL)isPause;
@end

@interface VLSBGSettingModel : NSObject

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
