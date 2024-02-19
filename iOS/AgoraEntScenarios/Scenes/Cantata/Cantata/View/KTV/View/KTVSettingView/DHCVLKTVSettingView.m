//
//  VLKTVSettingView.m
//  VoiceOnLine
//

#import "DHCVLKTVSettingView.h"
#import "VLKTVSwitcherView.h"
#import "VLKTVTonesView.h"
#import "VLKTVSliderView.h"
#import "VLKTVKindsView.h"
#import "VLKTVRemoteVolumeView.h"
#import "EffectCollectionViewCell.h"
@import Masonry;

@interface DHCVLKTVSettingView() <
VLKTVSwitcherViewDelegate,
VLKTVSliderViewDelegate,
VLKTVKindsViewDelegate,
VLKTVTonesViewDelegate,
VLKTVRemoteVolumeViewDelegate,
UICollectionViewDelegate,
UICollectionViewDataSource
>

@property (nonatomic, strong) UILabel *titleLabel;
@property (nonatomic, strong) VLKTVSwitcherView *soundSwitcher;
@property (nonatomic, strong) VLKTVTonesView *tonesView;
@property (nonatomic, strong) VLKTVSliderView *soundSlider;
@property (nonatomic, strong) VLKTVSliderView *accSlider;
@property (nonatomic, strong) VLKTVSliderView *remoteSlider;
@property (nonatomic, strong) VLKTVKindsView *kindsView;
@property (nonatomic, strong) VLKTVRemoteVolumeView* remoteVolumeView;
@property (nonatomic, strong) VLKTVSwitcherView *imSwitcher;
@property (nonatomic, strong, readonly) VLKTVSettingModel *setting;
@property (nonatomic, strong) UICollectionView *collectionView;
@property (nonatomic, assign) CGFloat cellWidth;
@property (nonatomic, strong) NSArray *effectImgs;
@property (nonatomic, strong) NSArray *titles;
@end

@implementation DHCVLKTVSettingView

- (instancetype)initWithSetting:(VLKTVSettingModel *)setting {
    if (self = [super init]) {
        [self configData:setting];
        [self initSubViews];
        [self addSubViewConstraints];
        self.soundSlider.value = 1.0;
        self.accSlider.value = 0.5;
        self.remoteSlider.value = 0.3;
        self.setting.remoteVolume = 30;
        self.setting.imMode = 0;
        self.cellWidth = (CGRectGetWidth([UIScreen mainScreen].bounds) - 48) / 4.0;
        self.titles = @[@"ktv_cantata".toSceneLocalization,@"ktv_effect_off".toSceneLocalization, @"KTV".toSceneLocalization,@"ktv_effect_concert".toSceneLocalization, @"ktv_effect_studio".toSceneLocalization, @"ktv_effect_phonograph".toSceneLocalization, @"ktv_effect_spatial".toSceneLocalization, @"ktv_effect_ethereal".toSceneLocalization, @"ktv_effect_pop".toSceneLocalization,@"R&B"];
        self.effectImgs = @[@"ktv_console_setting2",@"ktv_console_setting1",@"ktv_console_setting3",@"ktv_console_setting4"];
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
    self.remoteSlider.value = self.setting.remoteValue;
}

- (void)initSubViews {
    [self addSubview:self.titleLabel];
    [self addSubview:self.soundSwitcher];
    [self addSubview:self.soundSlider];
    [self addSubview:self.accSlider];
    [self addSubview:self.remoteSlider];
    [self addSubview:self.imSwitcher];
    [self addSubview:self.collectionView];
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
    
    [self.remoteSlider mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.right.mas_equalTo(self);
        make.top.mas_equalTo(self.accSlider.mas_bottom).offset(25);
        make.height.mas_equalTo(22);
    }];
    
    [self.imSwitcher mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.right.mas_equalTo(self);
        make.top.mas_equalTo(self.remoteSlider.mas_bottom).offset(25);
        make.height.mas_equalTo(22);
    }];

    [self.collectionView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.right.mas_equalTo(self);
        make.top.mas_equalTo(self.imSwitcher.mas_bottom).offset(10);
        make.height.mas_equalTo(78);
    }];
    
}

#pragma mark - VLKTVSwitcherViewDelegate

- (void)switcherView:(VLKTVSwitcherView *)switcherView on:(BOOL)on {
    DHCVLKTVValueDidChangedType type;
    if (switcherView == self.soundSwitcher) {
        self.setting.soundOn = on;
        type = DHCVLKTVValueDidChangedTypeEar;
    } else if (switcherView == self.imSwitcher) {
        self.setting.imMode = self.imSwitcher.modeOn ? 1 : 0;
        self.setting.remoteVolume = self.imSwitcher.modeOn ? 0 : 30;
        self.remoteSlider.value = self.imSwitcher.modeOn ? 0 : 0.3;
        type = DHCVLKTVValueDidChangedTypeRemoteValue;
      //  type = DHCVLKTVValueDidChangedTypeIMMode;
    } else {
        type = DHCVLKTVValueDidChangedTypeMV;
        self.setting.mvOn = on;
    }
    if ([self.delegate respondsToSelector:@selector(settingViewSettingChanged:valueDidChangedType:)]) {
        [self.delegate settingViewSettingChanged:self.setting valueDidChangedType:type];
    }
}

#pragma mark - VLKTVSliderViewDelegate

- (void)sliderView:(VLKTVSliderView *)sliderView valueChanged:(float)value {
    DHCVLKTVValueDidChangedType type;
    if (sliderView == self.soundSlider) {
        NSLog(@"value:%f", value);
        self.setting.soundValue = value ;
        type = DHCVLKTVValueDidChangedTypeSound;
    } else if (sliderView == self.accSlider){
        self.setting.accValue = value ;
        type = DHCVLKTVValueDidChangedTypeAcc;
    } else {
        self.setting.remoteVolume = value ;
        type = DHCVLKTVValueDidChangedTypeRemoteValue;
    }
    if ([self.delegate respondsToSelector:@selector(settingViewSettingChanged:valueDidChangedType:)]) {
        [self.delegate settingViewSettingChanged:self.setting valueDidChangedType:type];
    }
}

#pragma mark - collectionViewDelegate

- (NSInteger)numberOfSectionsInCollectionView:(UICollectionView *)collectionView {
    return 1;
}

- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section {
    return self.titles.count; //设置cell数量
}

- (UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath {
    EffectCollectionViewCell *cell = [collectionView dequeueReusableCellWithReuseIdentifier:@"EffectCollectionViewCell" forIndexPath:indexPath];
    cell.bgImageView.image = [UIImage dhc_sceneImageWith:self.effectImgs[indexPath.item % 4]];
    cell.titleLabel.text = self.titles[indexPath.item];
    cell.layer.cornerRadius = 5;
    cell.layer.masksToBounds = true;
    if(self.setting.selectEffect == indexPath.item){
        cell.layer.borderColor = [UIColor blueColor].CGColor;
        cell.layer.borderWidth = 2;
    } else {
        cell.layer.borderWidth = 0;
    }
    return cell;
}

- (CGSize)collectionView:(UICollectionView *)collectionView layout:(UICollectionViewLayout*)collectionViewLayout sizeForItemAtIndexPath:(NSIndexPath *)indexPath {
    return CGSizeMake(self.cellWidth, 54); //设置cell大小
}

- (CGFloat)collectionView:(UICollectionView *)collectionView layout:(UICollectionViewLayout*)collectionViewLayout minimumLineSpacingForSectionAtIndex:(NSInteger)section {
    return 12; //设置上下间距
}

- (CGFloat)collectionView:(UICollectionView *)collectionView layout:(UICollectionViewLayout*)collectionViewLayout minimumInteritemSpacingForSectionAtIndex:(NSInteger)section {
    return 12; //设置左右间距
}

- (void)collectionView:(UICollectionView *)collectionView didSelectItemAtIndexPath:(NSIndexPath *)indexPath {
    self.setting.selectEffect = indexPath.item;
    [self.collectionView reloadData];
    if([self.delegate respondsToSelector:@selector(settingViewEffectChoosed:)]){
        [self.delegate settingViewEffectChoosed:indexPath.item];
    }
}

#pragma mark - VLKTVKindsViewDelegate

- (void)kindsViewDidClickIndex:(NSInteger)index {
    self.setting.kindIndex = index;
    if ([self.delegate respondsToSelector:@selector(settingViewSettingChanged:valueDidChangedType:)]) {
        [self.delegate settingViewSettingChanged:self.setting valueDidChangedType:DHCVLKTVValueDidChangedTypeListItem];
    }
}

#pragma mark - VLKTVTonesViewDelegate

- (void)tonesViewValueChanged:(NSInteger)value {
    self.setting.toneValue = value;
    if ([self.delegate respondsToSelector:@selector(settingViewSettingChanged:valueDidChangedType:)]) {
        [self.delegate settingViewSettingChanged:self.setting valueDidChangedType:DHCVLKTVValueDidChangedRiseFall];
    }
}

#pragma mark VLKTVRemoteVolumeViewDelegate
- (void)view:(VLKTVRemoteVolumeView *)view remoteVolumeValueChanged:(int)value {
    self.setting.remoteVolume = value;
    if ([self.delegate respondsToSelector:@selector(settingViewSettingChanged:valueDidChangedType:)]) {
        [self.delegate settingViewSettingChanged:self.setting valueDidChangedType:DHCVLKTVValueDidChangedTypeRemoteValue];
    }
}

#pragma mark - Lazy

- (UILabel *)titleLabel {
    if (!_titleLabel) {
        _titleLabel = [[UILabel alloc] init];
        _titleLabel.text = DHCLocalizedString(@"ktv_music_menu_dialog_title");
        _titleLabel.font = VLUIFontMake(16);
        _titleLabel.textColor = UIColorMakeWithHex(@"#EFF4FF");
    }
    return _titleLabel;
}

- (VLKTVSwitcherView *)soundSwitcher {
    if (!_soundSwitcher) {
        _soundSwitcher = [[VLKTVSwitcherView alloc] init];
        _soundSwitcher.titleLabel.text = DHCLocalizedString(@"ktv_ear_setting");
        [_soundSwitcher.swich setHidden: true];
        _soundSwitcher.subText = DHCLocalizedString(@"ktv_please_use_headset");
        _soundSwitcher.delegate = self;
    }
    return _soundSwitcher;
}

- (VLKTVSwitcherView *)imSwitcher {
    if (!_imSwitcher) {
        _imSwitcher = [[VLKTVSwitcherView alloc] init];
        _imSwitcher.titleLabel.text = DHCLocalizedString(@"ktv_sleep_type");
        [_imSwitcher.subLabel setHidden: true];
        _imSwitcher.delegate = self;
    }
    return _imSwitcher;
}


- (VLKTVTonesView *)tonesView {
    if (!_tonesView) {
        _tonesView = [[VLKTVTonesView alloc] initWithMaxLevel:12 currentLevel:6];
        _tonesView.titleLabel.text = DHCLocalizedString(@"升降调");
        _tonesView.delegate = self;
    }
    return _tonesView;
}

- (VLKTVSliderView *)soundSlider {
    if (!_soundSlider) {
        _soundSlider = [[VLKTVSliderView alloc] initWithMax:1 min:0];
        _soundSlider.titleLabel.text = DHCLocalizedString(@"ktv_music_menu_dialog_vol1");
        _soundSlider.delegate = self;
    }
    return _soundSlider;
}

- (VLKTVSliderView *)accSlider {
    if (!_accSlider) {
        _accSlider = [[VLKTVSliderView alloc] initWithMax:1 min:0];
        _accSlider.accessibilityIdentifier = @"ktv_room_setting_acc_slider_id";
        _accSlider.titleLabel.text = DHCLocalizedString(@"ktv_music_menu_dialog_vol2");
        _accSlider.delegate = self;
    }
    return _accSlider;
}

- (VLKTVSliderView *)remoteSlider {
    if (!_remoteSlider) {
        _remoteSlider = [[VLKTVSliderView alloc] initWithMax:1 min:0];
        _remoteSlider.titleLabel.text = DHCLocalizedString(@"ktv_music_menu_dialog_remote_volume");
        _remoteSlider.delegate = self;
    }
    return _remoteSlider;
}

-(UICollectionView *)collectionView {
    if(!_collectionView){
            UICollectionViewFlowLayout *layout = [[UICollectionViewFlowLayout alloc]init];
            layout.scrollDirection = UICollectionViewScrollDirectionHorizontal;
            self.collectionView = [[UICollectionView alloc] initWithFrame:CGRectZero collectionViewLayout:layout];
            self.collectionView.dataSource = self;
            self.collectionView.delegate = self;
            self.collectionView.backgroundColor = [UIColor clearColor];
            self.collectionView.showsHorizontalScrollIndicator = NO;
            self.collectionView.contentInset = UIEdgeInsetsMake(12, 12, 12, 12);
            [self.collectionView registerClass:[EffectCollectionViewCell class] forCellWithReuseIdentifier:@"EffectCollectionViewCell"];
    }
    return _collectionView;
}

//- (VLKTVRemoteVolumeView*)remoteVolumeView {
//    if (!_remoteVolumeView) {
//        _remoteVolumeView = [[VLKTVRemoteVolumeView alloc] initWithMin:0 withMax:100 withCurrent:40];
//        _remoteVolumeView.titleLabel.text = DHCLocalizedString(@"RemoteVolume");
//        _remoteVolumeView.delegate = self;
//        _setting.remoteVolume = 40;
//    }
//    return _remoteVolumeView;
//}

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
    _remoteSlider.userInteractionEnabled = !isPause;
    _remoteSlider.value = isPause ? 1 : self.setting.remoteVolume / 100.0;
}

- (void)setSelectEffect:(NSInteger)index{
    self.setting.selectEffect = index;
    
}

-(void)setIMMode:(BOOL)flag {
    
}

@end

@implementation VLKTVSettingModel

- (void)setDefaultProperties {
    self.soundOn = NO;
    self.mvOn = NO;
    self.toneValue = 0;
    self.soundValue = 100;
    self.accValue = 50;
    self.remoteValue = 30;
    self.kindIndex = kKindUnSelectedIdentifier;
    self.imMode = 0;
}

@end
