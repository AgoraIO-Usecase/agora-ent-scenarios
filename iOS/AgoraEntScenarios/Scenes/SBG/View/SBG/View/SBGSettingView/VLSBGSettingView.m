//
//  VLSBGSettingView.m
//  VoiceOnLine
//

#import "VLSBGSettingView.h"
#import "VLSBGSwitcherView.h"
#import "VLSBGTonesView.h"
#import "VLSBGSliderView.h"
#import "VLSBGKindsView.h"
#import "VLSBGRemoteVolumeView.h"
#import "SBGMacro.h"
@import Masonry;

@interface VLSBGSettingView() <
VLSBGSwitcherViewDelegate,
VLSBGSliderViewDelegate,
VLSBGKindsViewDelegate,
VLSBGTonesViewDelegate,
VLSBGRemoteVolumeViewDelegate
>

@property (nonatomic, strong) UILabel *titleLabel;
@property (nonatomic, strong) VLSBGSwitcherView *soundSwitcher;
@property (nonatomic, strong) VLSBGTonesView *tonesView;
@property (nonatomic, strong) VLSBGSliderView *soundSlider;
@property (nonatomic, strong) VLSBGSliderView *accSlider;
@property (nonatomic, strong) VLSBGKindsView *kindsView;
@property (nonatomic, strong, readonly) VLSBGSettingModel *setting;

@end

@implementation VLSBGSettingView

- (instancetype)initWithSetting:(VLSBGSettingModel *)setting {
    if (self = [super init]) {
        [self configData:setting];
        [self initSubViews];
        [self addSubViewConstraints];
        self.soundSlider.value = 1.0;
        self.accSlider.value = 0.5;
    }
    return self;
}

- (void)configData:(VLSBGSettingModel *)setting {
    if (!setting) {
        _setting = [[VLSBGSettingModel alloc] init];
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
    [self addSubview:self.soundSlider];
    [self addSubview:self.accSlider];
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
    
}

#pragma mark - VLSBGSwitcherViewDelegate

- (void)switcherView:(VLSBGSwitcherView *)switcherView on:(BOOL)on {
    VLSBGValueDidChangedType type;
    if (switcherView == self.soundSwitcher) {
        self.setting.soundOn = on;
        type = VLSBGValueDidChangedTypeEar;
    } else {
        type = VLSBGValueDidChangedTypeMV;
        self.setting.mvOn = on;
    }
    if ([self.delegate respondsToSelector:@selector(settingViewSettingChanged:valueDidChangedType:)]) {
        [self.delegate settingViewSettingChanged:self.setting valueDidChangedType:type];
    }
}

#pragma mark - VLSBGSliderViewDelegate

- (void)sliderView:(VLSBGSliderView *)sliderView valueChanged:(float)value {
    VLSBGValueDidChangedType type;
    if (sliderView == self.soundSlider) {
        NSLog(@"value:%f", value);
        self.setting.soundValue = value;
        type = VLSBGValueDidChangedTypeSound;
    } else {
        self.setting.accValue = value;
        type = VLSBGValueDidChangedTypeAcc;
    }
    if ([self.delegate respondsToSelector:@selector(settingViewSettingChanged:valueDidChangedType:)]) {
        [self.delegate settingViewSettingChanged:self.setting valueDidChangedType:type];
    }
}

#pragma mark - VLSBGKindsViewDelegate

- (void)kindsViewDidClickIndex:(NSInteger)index {
    self.setting.kindIndex = index;
    if ([self.delegate respondsToSelector:@selector(settingViewSettingChanged:valueDidChangedType:)]) {
        [self.delegate settingViewSettingChanged:self.setting valueDidChangedType:VLSBGValueDidChangedTypeListItem];
    }
}

#pragma mark - VLSBGTonesViewDelegate

- (void)tonesViewValueChanged:(NSInteger)value {
    self.setting.toneValue = value;
    if ([self.delegate respondsToSelector:@selector(settingViewSettingChanged:valueDidChangedType:)]) {
        [self.delegate settingViewSettingChanged:self.setting valueDidChangedType:VLSBGValueDidChangedRiseFall];
    }
}

#pragma mark - Lazy

- (UILabel *)titleLabel {
    if (!_titleLabel) {
        _titleLabel = [[UILabel alloc] init];
        _titleLabel.text = SBGLocalizedString(@"sbg_music_menu_dialog_title");
        _titleLabel.font = [UIFont systemFontOfSize:16];
        _titleLabel.textColor = UIColorMakeWithHex(@"#EFF4FF");
    }
    return _titleLabel;
}

- (VLSBGSwitcherView *)soundSwitcher {
    if (!_soundSwitcher) {
        _soundSwitcher = [[VLSBGSwitcherView alloc] init];
        _soundSwitcher.titleLabel.text = SBGLocalizedString(@"sbg_music_menu_dialog_ear");
        _soundSwitcher.subText = SBGLocalizedString(@"sbg_please_use_headset");
        _soundSwitcher.delegate = self;
    }
    return _soundSwitcher;
}

- (VLSBGTonesView *)tonesView {
    if (!_tonesView) {
        _tonesView = [[VLSBGTonesView alloc] initWithMaxLevel:12 currentLevel:6];
        _tonesView.titleLabel.text = SBGLocalizedString(@"sbg_music_menu_dialog_tone");
        _tonesView.delegate = self;
    }
    return _tonesView;
}

- (VLSBGSliderView *)soundSlider {
    if (!_soundSlider) {
        _soundSlider = [[VLSBGSliderView alloc] initWithMax:1 min:0];
        _soundSlider.titleLabel.text = SBGLocalizedString(@"sbg_music_menu_dialog_vol1");
        _soundSlider.delegate = self;
    }
    return _soundSlider;
}

- (VLSBGSliderView *)accSlider {
    if (!_accSlider) {
        _accSlider = [[VLSBGSliderView alloc] initWithMax:1 min:0];
        _accSlider.titleLabel.text = SBGLocalizedString(@"sbg_music_menu_dialog_vol2");
        _accSlider.delegate = self;
    }
    return _accSlider;
}

- (VLSBGKindsView *)kindsView {
    if (!_kindsView) {
        _kindsView = [[VLSBGKindsView alloc] init];
        _kindsView.delegate = self;
        _kindsView.list = [VLSBGKindsModel kinds];
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

@end

@implementation VLSBGSettingModel

- (void)setDefaultProperties {
    self.soundOn = NO;
    self.mvOn = NO;
    self.toneValue = 0;
    self.soundValue = 0.0;
    self.accValue = 0.0;
    self.kindIndex = kKindUnSelectedIdentifier;
}

@end
