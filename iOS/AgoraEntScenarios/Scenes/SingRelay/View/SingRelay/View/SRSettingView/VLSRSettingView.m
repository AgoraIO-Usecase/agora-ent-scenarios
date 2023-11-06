//
//  VLSRSettingView.m
//  VoiceOnLine
//

#import "VLSRSettingView.h"
#import "VLSRSwitcherView.h"
#import "VLSRTonesView.h"
#import "VLSRSliderView.h"
#import "VLSRKindsView.h"
#import "VLSRRemoteVolumeView.h"
#import "VLFontUtils.h"
#import "AESMacro.h"
@import Masonry;

@interface VLSRSettingView() <
VLSRSwitcherViewDelegate,
VLSRSliderViewDelegate,
VLSRKindsViewDelegate,
VLSRTonesViewDelegate,
VLSRRemoteVolumeViewDelegate
>

@property (nonatomic, strong) UILabel *titleLabel;
@property (nonatomic, strong) VLSRSwitcherView *soundSwitcher;
@property (nonatomic, strong) VLSRTonesView *tonesView;
@property (nonatomic, strong) VLSRSliderView *soundSlider;
@property (nonatomic, strong) VLSRSliderView *accSlider;
@property (nonatomic, strong) VLSRKindsView *kindsView;
@property (nonatomic, strong) VLSRRemoteVolumeView* remoteVolumeView;
@property (nonatomic, strong, readonly) VLSRSettingModel *setting;

@end

@implementation VLSRSettingView

- (instancetype)initWithSetting:(VLSRSettingModel *)setting {
    if (self = [super init]) {
        [self configData:setting];
        [self initSubViews];
        [self addSubViewConstraints];
        self.soundSlider.value = 1.0;
        self.accSlider.value = 0.5;
    }
    return self;
}

- (void)configData:(VLSRSettingModel *)setting {
    if (!setting) {
        _setting = [[VLSRSettingModel alloc] init];
        [self.setting setDefaultProperties];
    } else {
        _setting = setting;
    }
    
    self.soundSwitcher.on = self.setting.soundOn;
    self.soundSlider.value = self.setting.soundValue;
    self.accSlider.value = self.setting.accValue;
}

- (void)initSubViews {
    [self addSubview:self.titleLabel];
    [self addSubview:self.soundSwitcher];
//    [self addSubview:self.tonesView];
    [self addSubview:self.soundSlider];
    [self addSubview:self.accSlider];
    [self addSubview:self.remoteVolumeView];
    [self.remoteVolumeView setHidden:YES];
//    [self addSubview:self.kindsView];
}

- (void)addSubViewConstraints {
    [self.titleLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.mas_equalTo(20);
        make.centerX.mas_equalTo(self);
    }];
    
    [self.soundSwitcher mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.right.mas_equalTo(self);
        make.top.mas_equalTo(self.titleLabel.mas_bottom).offset(16);
    }];
    
//    [self.tonesView mas_makeConstraints:^(MASConstraintMaker *make) {
//        make.left.right.mas_equalTo(self);
//        make.top.mas_equalTo(self.soundSwitcher.mas_bottom).offset(25);
//        make.height.mas_equalTo(26);
//    }];
    
    [self.soundSlider mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.right.mas_equalTo(self);
        make.top.mas_equalTo(self.soundSwitcher.mas_bottom).offset(25);
        make.height.mas_equalTo(22);
    }];
    
    [self.accSlider mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.right.mas_equalTo(self);
        make.top.mas_equalTo(self.soundSlider.mas_bottom).offset(25);
        make.height.mas_equalTo(22);
    }];
    
    [self.remoteVolumeView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.right.mas_equalTo(self);
        make.top.mas_equalTo(self.accSlider.mas_bottom).offset(25);
        make.height.mas_equalTo(22);
    }];
    
//    [self.kindsView mas_makeConstraints:^(MASConstraintMaker *make) {
//        make.top.mas_equalTo(self.remoteVolumeView.mas_bottom).offset(35);
//        make.left.right.mas_equalTo(self);
//        make.bottom.mas_equalTo(self).offset(-64);
//    }];
}

#pragma mark - VLSRSwitcherViewDelegate

- (void)switcherView:(VLSRSwitcherView *)switcherView on:(BOOL)on {
    VLSRValueDidChangedType type;
    if (switcherView == self.soundSwitcher) {
        self.setting.soundOn = on;
        type = VLSRValueDidChangedTypeEar;
    } else {
        type = VLSRValueDidChangedTypeMV;
        self.setting.mvOn = on;
    }
    if ([self.delegate respondsToSelector:@selector(settingViewSettingChanged:valueDidChangedType:)]) {
        [self.delegate settingViewSettingChanged:self.setting valueDidChangedType:type];
    }
}

#pragma mark - VLSRSliderViewDelegate

- (void)sliderView:(VLSRSliderView *)sliderView valueChanged:(float)value {
    VLSRValueDidChangedType type;
    if (sliderView == self.soundSlider) {
        NSLog(@"value:%f", value);
        self.setting.soundValue = value;
        type = VLSRValueDidChangedTypeSound;
    } else {
        self.setting.accValue = value;
        type = VLSRValueDidChangedTypeAcc;
    }
    if ([self.delegate respondsToSelector:@selector(settingViewSettingChanged:valueDidChangedType:)]) {
        [self.delegate settingViewSettingChanged:self.setting valueDidChangedType:type];
    }
}

#pragma mark - VLSRKindsViewDelegate

- (void)kindsViewDidClickIndex:(NSInteger)index {
    self.setting.kindIndex = index;
    if ([self.delegate respondsToSelector:@selector(settingViewSettingChanged:valueDidChangedType:)]) {
        [self.delegate settingViewSettingChanged:self.setting valueDidChangedType:VLSRValueDidChangedTypeListItem];
    }
}

#pragma mark - VLSRTonesViewDelegate

- (void)tonesViewValueChanged:(NSInteger)value {
    self.setting.toneValue = value;
    if ([self.delegate respondsToSelector:@selector(settingViewSettingChanged:valueDidChangedType:)]) {
        [self.delegate settingViewSettingChanged:self.setting valueDidChangedType:VLSRValueDidChangedRiseFall];
    }
}

#pragma mark VLSRRemoteVolumeViewDelegate
- (void)view:(VLSRRemoteVolumeView *)view remoteVolumeValueChanged:(int)value {
    self.setting.remoteVolume = value;
    if ([self.delegate respondsToSelector:@selector(settingViewSettingChanged:valueDidChangedType:)]) {
        [self.delegate settingViewSettingChanged:self.setting valueDidChangedType:VLSRValueDidChangedTypeRemoteValue];
    }
}

#pragma mark - Lazy

- (UILabel *)titleLabel {
    if (!_titleLabel) {
        _titleLabel = [[UILabel alloc] init];
        _titleLabel.text = SRLocalizedString(@"ktv_music_menu_dialog_title");
        _titleLabel.font = VLUIFontMake(16);
        _titleLabel.textColor = UIColorMakeWithHex(@"#EFF4FF");
    }
    return _titleLabel;
}

- (VLSRSwitcherView *)soundSwitcher {
    if (!_soundSwitcher) {
        _soundSwitcher = [[VLSRSwitcherView alloc] init];
        _soundSwitcher.titleLabel.text = SRLocalizedString(@"ktv_music_menu_dialog_ear");
        _soundSwitcher.subText = SRLocalizedString(@"ktv_please_use_headset");
        _soundSwitcher.delegate = self;
    }
    return _soundSwitcher;
}

- (VLSRTonesView *)tonesView {
    if (!_tonesView) {
        _tonesView = [[VLSRTonesView alloc] initWithMaxLevel:12 currentLevel:6];
        _tonesView.titleLabel.text = SRLocalizedString(@"ktv_music_menu_dialog_tone");
        _tonesView.delegate = self;
    }
    return _tonesView;
}

- (VLSRSliderView *)soundSlider {
    if (!_soundSlider) {
        _soundSlider = [[VLSRSliderView alloc] initWithMax:1 min:0];
        _soundSlider.titleLabel.text = SRLocalizedString(@"ktv_music_menu_dialog_vol1");
        _soundSlider.delegate = self;
    }
    return _soundSlider;
}

- (VLSRSliderView *)accSlider {
    if (!_accSlider) {
        _accSlider = [[VLSRSliderView alloc] initWithMax:1 min:0];
        _accSlider.titleLabel.text = SRLocalizedString(@"ktv_music_menu_dialog_vol2");
        _accSlider.delegate = self;
    }
    return _accSlider;
}

- (VLSRRemoteVolumeView*)remoteVolumeView {
    if (!_remoteVolumeView) {
        _remoteVolumeView = [[VLSRRemoteVolumeView alloc] initWithMin:0 withMax:100 withCurrent:40];
        _remoteVolumeView.titleLabel.text = SRLocalizedString(@"ktv_music_menu_dialog_remote_volume");
        _remoteVolumeView.delegate = self;
        _setting.remoteVolume = 40;
    }
    return _remoteVolumeView;
}

- (VLSRKindsView *)kindsView {
    if (!_kindsView) {
        _kindsView = [[VLSRKindsView alloc] init];
        _kindsView.delegate = self;
        _kindsView.list = [VLSRKindsModel kinds];
    }
    return _kindsView;
}

- (void)setIsEarOn:(BOOL)isEarOn
{
    self.setting.soundOn = isEarOn;
    self.soundSwitcher.on = isEarOn;
}

- (void)setAccValue:(float)accValue {
    self.setting.accValue = accValue;
    self.accSlider.value = accValue;
}

-(void)setIspause:(BOOL)isPause{
    _remoteVolumeView.userInteractionEnabled = !isPause;
    [_remoteVolumeView setCurrent:isPause ? 100 : self.setting.remoteVolume];
}

@end

@implementation VLSRSettingModel

- (void)setDefaultProperties {
    self.soundOn = NO;
    self.mvOn = NO;
    self.toneValue = 0;
    self.soundValue = 0.0;
    self.accValue = 0.0;
    self.kindIndex = kKindUnSelectedIdentifier;
}

@end
