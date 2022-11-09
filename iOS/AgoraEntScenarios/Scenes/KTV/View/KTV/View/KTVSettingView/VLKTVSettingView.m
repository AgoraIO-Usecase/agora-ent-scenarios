//
//  VLKTVSettingView.m
//  VoiceOnLine
//

#import "VLKTVSettingView.h"
#import "VLKTVSwitcherView.h"
#import "VLKTVTonesView.h"
#import "VLKTVSliderView.h"
#import "VLKTVKindsView.h"
#import "VLFontUtils.h"
#import "KTVMacro.h"
@import Masonry;
@import QMUIKit;

@interface VLKTVSettingView() <VLKTVSwitcherViewDelegate, VLKTVSliderViewDelegate, VLKTVKindsViewDelegate, VLKTVTonesViewDelegate>

@property (nonatomic, strong) UILabel *titleLabel;
@property (nonatomic, strong) VLKTVSwitcherView *soundSwitcher;
@property (nonatomic, strong) VLKTVTonesView *tonesView;
@property (nonatomic, strong) VLKTVSliderView *soundSlider;
@property (nonatomic, strong) VLKTVSliderView *accSlider;
@property (nonatomic, strong) VLKTVKindsView *kindsView;

@property (nonatomic, strong) VLKTVSettingModel *setting;

@end

@implementation VLKTVSettingView

- (instancetype)initWithSetting:(VLKTVSettingModel *)setting {
    if (self = [super init]) {
        [self configData:setting];
        [self initSubViews];
        [self addSubViewConstraints];
        self.soundSlider.value = 0.25;
        self.accSlider.value = 0.25;
    }
    return self;
}

- (void)configData:(VLKTVSettingModel *)setting {
    if (!setting) {
        self.setting = [[VLKTVSettingModel alloc] init];
        [self.setting setDefaultProperties];
    } else {
        self.setting = setting;
    }
    
    self.soundSwitcher.on = self.setting.soundOn;
    self.soundSlider.value = self.setting.soundValue;
    self.accSlider.value = self.setting.accValue;
}

- (void)initSubViews {
    [self addSubview:self.titleLabel];
    [self addSubview:self.soundSwitcher];
    [self addSubview:self.tonesView];
    [self addSubview:self.soundSlider];
    [self addSubview:self.accSlider];
    [self addSubview:self.kindsView];
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
    
    [self.tonesView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.right.mas_equalTo(self);
        make.top.mas_equalTo(self.soundSwitcher.mas_bottom).offset(25);
        make.height.mas_equalTo(26);
    }];
    
    [self.soundSlider mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.right.mas_equalTo(self);
        make.top.mas_equalTo(self.tonesView.mas_bottom).offset(25);
        make.height.mas_equalTo(22);
    }];
    
    [self.accSlider mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.right.mas_equalTo(self);
        make.top.mas_equalTo(self.soundSlider.mas_bottom).offset(25);
        make.height.mas_equalTo(22);
    }];
    
    [self.kindsView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.mas_equalTo(self.accSlider.mas_bottom).offset(35);
        make.left.right.mas_equalTo(self);
        make.bottom.mas_equalTo(self).offset(-64);
    }];
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

#pragma mark - Lazy

- (UILabel *)titleLabel {
    if (!_titleLabel) {
        _titleLabel = [[UILabel alloc] init];
        _titleLabel.text = KTVLocalizedString(@"控制台");
        _titleLabel.font = VLUIFontMake(16);
        _titleLabel.textColor = UIColorMakeWithHex(@"#EFF4FF");
    }
    return _titleLabel;
}

- (VLKTVSwitcherView *)soundSwitcher {
    if (!_soundSwitcher) {
        _soundSwitcher = [[VLKTVSwitcherView alloc] init];
        _soundSwitcher.titleLabel.text = KTVLocalizedString(@"耳返");
        _soundSwitcher.subText = KTVLocalizedString(@"请插入耳机使用耳返功能");
        _soundSwitcher.delegate = self;
    }
    return _soundSwitcher;
}

- (VLKTVTonesView *)tonesView {
    if (!_tonesView) {
        _tonesView = [[VLKTVTonesView alloc] initWithMaxLevel:12 currentLevel:6];
        _tonesView.titleLabel.text = KTVLocalizedString(@"升降调");
        _tonesView.delegate = self;
    }
    return _tonesView;
}

- (VLKTVSliderView *)soundSlider {
    if (!_soundSlider) {
        _soundSlider = [[VLKTVSliderView alloc] initWithMax:1 min:0];
        _soundSlider.titleLabel.text = KTVLocalizedString(@"音量");
        _soundSlider.delegate = self;
    }
    return _soundSlider;
}

- (VLKTVSliderView *)accSlider {
    if (!_accSlider) {
        _accSlider = [[VLKTVSliderView alloc] initWithMax:1 min:0];
        _accSlider.titleLabel.text = KTVLocalizedString(@"伴奏");
        _accSlider.delegate = self;
    }
    return _accSlider;
}

- (VLKTVKindsView *)kindsView {
    if (!_kindsView) {
        _kindsView = [[VLKTVKindsView alloc] init];
        _kindsView.delegate = self;
        _kindsView.list = [VLKTVKindsModel kinds];
    }
    return _kindsView;
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
