//
//  VLKTVSettingView.h
//  VoiceOnLine
//

#import "VLBaseView.h"
@class VLKTVSettingModel;

typedef enum : NSUInteger {
    VLKTVValueDidChangedTypeEar = 0, // 耳返
    VLKTVValueDidChangedTypeMV,    // MV
    VLKTVValueDidChangedRiseFall,  // 升降调
    VLKTVValueDidChangedTypeSound, // 声音
    VLKTVValueDidChangedTypeAcc, // 伴奏
    VLKTVValueDidChangedTypeListItem // 列表
} VLKTVValueDidChangedType;

@protocol VLKTVSettingViewDelegate <NSObject>

- (void)settingViewSettingChanged:(VLKTVSettingModel *)setting valueDidChangedType:(VLKTVValueDidChangedType)type;

@end

@interface VLKTVSettingView : VLBaseView

- (instancetype)initWithSetting:(VLKTVSettingModel *)setting;

@property (nonatomic, weak) id <VLKTVSettingViewDelegate> delegate;

-(void)setIsEarOn:(BOOL)isEarOn;

@end

@interface VLKTVSettingModel : NSObject

#define kKindUnSelectedIdentifier 10000

/// 耳返
@property (nonatomic, assign) BOOL soundOn;
@property (nonatomic, assign) BOOL mvOn;
@property (nonatomic, assign) float soundValue;
@property (nonatomic, assign) float accValue;
@property (nonatomic, assign) NSInteger toneValue;

/// list选项
@property (nonatomic, assign) NSInteger kindIndex;

- (void)setDefaultProperties;

@end
