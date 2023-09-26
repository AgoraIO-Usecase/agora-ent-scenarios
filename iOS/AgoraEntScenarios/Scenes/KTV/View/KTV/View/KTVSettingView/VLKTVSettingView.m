//
//  VLKTVSettingView.m
//  VoiceOnLine
//

#import "VLKTVSettingView.h"
#import "VLKTVSwitcherView.h"
#import "VLKTVTonesView.h"
#import "VLKTVSliderView.h"
#import "VLKTVKindsView.h"
#import "VLKTVRemoteVolumeView.h"
#import "VLFontUtils.h"
#import "AESMacro.h"
@import Masonry;

@interface VLKTVSettingView() <
VLKTVSwitcherViewDelegate,
VLKTVSliderViewDelegate,
VLKTVKindsViewDelegate,
VLKTVTonesViewDelegate,
VLKTVRemoteVolumeViewDelegate
>

@property (nonatomic, strong) UILabel *titleLabel;
@property (nonatomic, strong) VLKTVSwitcherView *soundSwitcher;
@property (nonatomic, strong) VLKTVTonesView *tonesView;
@property (nonatomic, strong) VLKTVSliderView *soundSlider;
@property (nonatomic, strong) VLKTVSliderView *accSlider;
@property (nonatomic, strong) VLKTVKindsView *kindsView;
@property (nonatomic, strong) VLKTVRemoteVolumeView* remoteVolumeView;
@property (nonatomic, strong, readonly) VLKTVSettingModel *setting;

@end

@implementation VLKTVSettingView

- (instancetype)initWithSetting:(VLKTVSettingModel *)setting {
    if (self = [super init]) {
        [self configData:setting];
        [self initSubViews];
        [self addSubViewConstraints];
        self.soundSlider.value = 1.0;
        self.accSlider.value = 0.5;
    }
    return self;
}

- (void)configData:(VLKTVSettingModel *)setting {
    if (!setting) {
        _setting = [[VLKTVSettingModel alloc] init];
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

#pragma mark - VLKTVSwitcherViewDelegate

- (void)switcherView:(VLKTVSwitcherView *)switcherView on:(BOOL)on {
    VLKTVValueDidChangedType type;
    if (switcherView == self.soundSwitcher) {
        self.setting.soundOn = on;
        type = VLKTVValueDidChangedTypeEar;
    } else {
        type = VLKTVValueDidChangedTypeMV;
        self.setting.mvOn = on;
    }
    if ([self.delegate respondsToSelector:@selector(settingViewSettingChanged:valueDidChangedType:)]) {
        [self.delegate settingViewSettingChanged:self.setting valueDidChangedType:type];
    }
}

#pragma mark - VLKTVSliderViewDelegate

- (void)sliderView:(VLKTVSliderView *)sliderView valueChanged:(float)value {
    VLKTVValueDidChangedType type;
    if (sliderView == self.soundSlider) {
        NSLog(@"value:%f", value);
        self.setting.soundValue = value;
        type = VLKTVValueDidChangedTypeSound;
    } else {
        self.setting.accValue = value;
        type = VLKTVValueDidChangedTypeAcc;
    }
    if ([self.delegate respondsToSelector:@selector(settingViewSettingChanged:valueDidChangedType:)]) {
        [self.delegate settingViewSettingChanged:self.setting valueDidChangedType:type];
    }
}

#pragma mark - VLKTVKindsViewDelegate

- (void)kindsViewDidClickIndex:(NSInteger)index {
    self.setting.kindIndex = index;
    if ([self.delegate respondsToSelector:@selector(settingViewSettingChanged:valueDidChangedType:)]) {
        [self.delegate settingViewSettingChanged:self.setting valueDidChangedType:VLKTVValueDidChangedTypeListItem];
    }
}

#pragma mark - VLKTVTonesViewDelegate

- (void)tonesViewValueChanged:(NSInteger)value {
    self.setting.toneValue = value;
    if ([self.delegate respondsToSelector:@selector(settingViewSettingChanged:valueDidChangedType:)]) {
        [self.delegate settingViewSettingChanged:self.setting valueDidChangedType:VLKTVValueDidChangedRiseFall];
    }
}

#pragma mark VLKTVRemoteVolumeViewDelegate
- (void)view:(VLKTVRemoteVolumeView *)view remoteVolumeValueChanged:(int)value {
    self.setting.remoteVolume = value;
    if ([self.delegate respondsToSelector:@selector(settingViewSettingChanged:valueDidChangedType:)]) {
        [self.delegate settingViewSettingChanged:self.setting valueDidChangedType:VLKTVValueDidChangedTypeRemoteValue];
    }
}

#pragma mark - Lazy

- (UILabel *)titleLabel {
    if (!_titleLabel) {
        _titleLabel = [[UILabel alloc] init];
        _titleLabel.text = KTVLocalizedString(@"ktv_music_menu_dialog_title");
        _titleLabel.font = VLUIFontMake(16);
        _titleLabel.textColor = UIColorMakeWithHex(@"#EFF4FF");
    }
    return _titleLabel;
}

- (VLKTVSwitcherView *)soundSwitcher {
    if (!_soundSwitcher) {
        _soundSwitcher = [[VLKTVSwitcherView alloc] init];
        _soundSwitcher.titleLabel.text = KTVLocalizedString(@"ktv_music_menu_dialog_ear");
        _soundSwitcher.subText = KTVLocalizedString(@"ktv_please_use_headset");
        _soundSwitcher.delegate = self;
    }
    return _soundSwitcher;
}

- (VLKTVTonesView *)tonesView {
    if (!_tonesView) {
        _tonesView = [[VLKTVTonesView alloc] initWithMaxLevel:12 currentLevel:6];
        _tonesView.titleLabel.text = KTVLocalizedString(@"ktv_music_menu_dialog_tone");
        _tonesView.delegate = self;
    }
    return _tonesView;
}

- (VLKTVSliderView *)soundSlider {
    if (!_soundSlider) {
        _soundSlider = [[VLKTVSliderView alloc] initWithMax:1 min:0];
        _soundSlider.titleLabel.text = KTVLocalizedString(@"ktv_music_menu_dialog_vol1");
        _soundSlider.delegate = self;
    }
    return _soundSlider;
}

- (VLKTVSliderView *)accSlider {
    if (!_accSlider) {
        _accSlider = [[VLKTVSliderView alloc] initWithMax:1 min:0];
        _accSlider.titleLabel.text = KTVLocalizedString(@"ktv_music_menu_dialog_vol2");
        _accSlider.delegate = self;
    }
    return _accSlider;
}

- (VLKTVRemoteVolumeView*)remoteVolumeView {
    if (!_remoteVolumeView) {
        _remoteVolumeView = [[VLKTVRemoteVolumeView alloc] initWithMin:0 withMax:100 withCurrent:40];
        _remoteVolumeView.titleLabel.text = KTVLocalizedString(@"ktv_music_menu_dialog_remote_volume");
        _remoteVolumeView.delegate = self;
        _setting.remoteVolume = 40;
    }
    return _remoteVolumeView;
}

- (VLKTVKindsView *)kindsView {
    if (!_kindsView) {
        _kindsView = [[VLKTVKindsView alloc] init];
        _kindsView.delegate = self;
        _kindsView.list = [VLKTVKindsModel kinds];
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

@implementation VLKTVSettingModel

- (void)setDefaultProperties {
    self.soundOn = NO;
    self.mvOn = NO;
    self.toneValue = 0;
    self.soundValue = 0.0;
    self.accValue = 0.0;
    self.kindIndex = kKindUnSelectedIdentifier;
}

@end
