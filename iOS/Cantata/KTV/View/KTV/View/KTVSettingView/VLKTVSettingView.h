//
//  VLKTVSettingView.h
//  VoiceOnLine
//

#import "VLBaseView.h"
@class VLKTVSettingModel;

typedef NS_ENUM(NSInteger, VLKTVValueDidChangedType) {
    VLKTVValueDidChangedTypeEar = 0,      // 耳返
    VLKTVValueDidChangedTypeMV,           // MV
    VLKTVValueDidChangedRiseFall,         // 升降调
    VLKTVValueDidChangedTypeSound,        // 声音
    VLKTVValueDidChangedTypeAcc,          // 伴奏
    VLKTVValueDidChangedTypeRemoteValue,  //远端音量
    VLKTVValueDidChangedTypeListItem,     // 列表
    VLKTVValueDidChangedTypeIMMode //沉浸模式
} ;

@protocol VLKTVSettingViewDelegate <NSObject>

- (void)settingViewSettingChanged:(VLKTVSettingModel *)setting valueDidChangedType:(VLKTVValueDidChangedType)type;

@end

@interface VLKTVSettingView : VLBaseView

- (instancetype)initWithSetting:(VLKTVSettingModel *)setting;

@property (nonatomic, weak) id <VLKTVSettingViewDelegate> delegate;

- (void)setIsEarOn:(BOOL)isEarOn;
- (void)setAccValue:(float)accValue;
-(void)setIspause:(BOOL)isPause;
-(void)setIMMode:(int)mode;
@end

@interface VLKTVSettingModel : NSObject

#define kKindUnSelectedIdentifier 10000

/// 耳返
@property (nonatomic, assign) BOOL soundOn;
@property (nonatomic, assign) BOOL mvOn;
@property (nonatomic, assign) float soundValue;
@property (nonatomic, assign) float accValue;
@property (nonatomic, assign) NSInteger toneValue;
@property (nonatomic, assign) int remoteVolume;
@property (nonatomic, assign) int imMode;


/// list选项
@property (nonatomic, assign) NSInteger kindIndex;

- (void)setDefaultProperties;

@end
