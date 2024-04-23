//
//  VLKTVSettingView.h
//  VoiceOnLine
//

#import "VLBaseView.h"
@class VLKTVSettingModel;

typedef enum : NSUInteger {
    VLKTVValueDidChangedTypeEar = 0,      // 耳返
    VLKTVValueDidChangedTypeSoundCard = 1,      // 声卡
    VLKTVValueDidChangedTypeMV,           // MV
    VLKTVValueDidChangedRiseFall,         // 升降调
    VLKTVValueDidChangedTypeSound,        // 声音
    VLKTVValueDidChangedTypeAcc,          // 伴奏
    VLKTVValueDidChangedTypeRemoteValue,  //远端音量
    VLKTVValueDidChangedTypeListItem,      // 列表
    VLKTVValueDidChangedTypeLrc, //歌词等级
    VLKTVValueDidChangedTypeVqs, //音质
    VLKTVValueDidChangedTypeAns, //降噪
    VLKTVValueDidChangedTypebro, //专业主播
    VLKTVValueDidChangedTypeaiaec, //AIAec
    VLKTVValueDidChangedTypeDelay,
    VLKTVValueDidChangedTypeAecLevel,
    VLKTVValueDidChangedTypeenableMultipath,
} VLKTVValueDidChangedType;

@protocol VLKTVSettingViewDelegate <NSObject>
- (void)settingViewSettingChanged:(VLKTVSettingModel *)setting valueDidChangedType:(VLKTVValueDidChangedType)type;
- (void)settingViewSettingChanged:(VLKTVSettingModel *)setting effectChoosed:(NSInteger)effectIndex;
@end

@interface VLKTVSettingView : VLBaseView

- (instancetype)initWithSetting:(VLKTVSettingModel *)setting;

@property (nonatomic, weak) id <VLKTVSettingViewDelegate> delegate;

- (void)setIsEarOn:(BOOL)isEarOn;
- (void)setAccValue:(float)accValue;
-(void)setIspause:(BOOL)isPause;
-(void)setSelectEffect:(NSInteger)index;
-(void)setUseSoundCard:(BOOL)useSoundCard;
-(void)setChorusStatus:(BOOL)status;
-(void)setAEC:(BOOL)enable level:(NSInteger)level;
@end

@interface VLKTVSettingModel : NSObject

#define kKindUnSelectedIdentifier 10000

/// 耳返
@property (nonatomic, assign) BOOL soundOn;
@property (nonatomic, assign) BOOL mvOn;
@property (nonatomic, assign) BOOL soundCardOn;
@property (nonatomic, assign) float soundValue;
@property (nonatomic, assign) float accValue;
@property (nonatomic, assign) float remoteValue;
@property (nonatomic, assign) NSInteger toneValue;
@property (nonatomic, assign) int remoteVolume;
@property (nonatomic, assign) NSInteger selectEffect;
@property (nonatomic, assign) NSInteger lrcLevel;
@property (nonatomic, assign) NSInteger vqs;
@property (nonatomic, assign) NSInteger ans;
@property (nonatomic, assign) BOOL isPerBro;//专业主播
@property (nonatomic, assign) BOOL isDelay;//低延迟
@property (nonatomic, assign) BOOL enableAec;
@property (nonatomic, assign) NSInteger aecLevel;
@property (nonatomic, assign) BOOL enableMultipath;
/// list选项
@property (nonatomic, assign) NSInteger kindIndex;

- (void)setDefaultProperties;

@end
